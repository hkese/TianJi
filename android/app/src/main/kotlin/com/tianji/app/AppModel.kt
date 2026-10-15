package com.tianji.app
import android.app.Application
import org.koin.android.ext.koin.androidContext
import android.net.Uri
import androidx.lifecycle.*
import com.tianji.data.*
import com.tianji.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import java.util.UUID
import java.time.Instant
data class ChatUiState(val connected:Boolean=false,val working:Boolean=false,val userCode:String="",val loginUrl:String="",val expiresAt:Long=0,val error:String="",val models:List<ChatGptModel> = emptyList(),val model:String="",val mode:String="OFFLINE",val loginStatus:String="",val browserRequest:Long=0)
class TianJiApplication:Application(){override fun onCreate(){super.onCreate();org.koin.core.context.startKoin{androidContext(this@TianJiApplication);modules(org.koin.dsl.module{single{Repository.open(get())};single{Preferences(get())};single{CredentialVault(get())}})}}}
data class UiState(val data:Backup=Backup(),val settings:Settings=Settings(),val loaded:Boolean=false)
class AppModel(val app:Application,val repo:Repository,val prefs:Preferences,val vault:CredentialVault,private val chatClientFactory:()->ChatGptClient={ChatGptClient()}):ViewModel(){
 val state=combine(repo.state,prefs.flow){data,s->UiState(data,s,true)}.stateIn(viewModelScope,SharingStarted.Eagerly,UiState())
 val messages=Channel<String>(Channel.BUFFERED);val busy=MutableStateFlow(false);val provider=MutableStateFlow(AiProviderConfig());private val ai=AiClient()
 private val chatPrefs=app.getSharedPreferences("intention-ai",0)
 private val session=ChatGptSession(EncryptedGrantStorage(app))
 private val pendingLogin=DeviceLoginRecovery(EncryptedPendingLoginStorage(app))
 val chat=MutableStateFlow(ChatUiState(model=chatPrefs.getString("model","")?:"",mode=chatPrefs.getString("mode","OFFLINE")?:"OFFLINE"))
 val quickBusy=MutableStateFlow(false);val quickMessage=MutableStateFlow("")
 private var accountJob:Job?=null;private var quickJob:Job?=null
 private var accountClient:ChatGptClient?=null;private var quickClient:ChatGptClient?=null;private var quickApi:AiClient?=null
 private var accountEpoch=0;private var quickEpoch=0
 init{perform{provider.value=repo.provider();val connected=session.connected();chat.value=chat.value.copy(connected=connected);if(connected)pendingLogin.clear() else withContext(Dispatchers.Main){resumeChatGpt()}}}
 fun aiMode(mode:String){require(mode in listOf("OFFLINE","CHATGPT","API"));cancelQuick();chat.value=chat.value.copy(mode=mode);chatPrefs.edit().putString("mode",mode).apply()}
 fun selectModel(id:String){require(chat.value.models.any{it.id==id});cancelQuick();chat.value=chat.value.copy(model=id);chatPrefs.edit().putString("model",id).apply()}
 private fun safeAiError(e:Exception):String=when(e){is AccountFailure->e.message?:"ChatGPT 未能回覆。";is java.io.IOException->"連線未完成，請檢查網絡或重新登入；本機指引已保留。";else->"AI 未提供符合本盤嘅完整解讀；本機指引已保留，可以重試。"}
 private fun loginError(e:Exception,stage:String):String {
  if(e is LoginFailure)return "${e.message}（${e.stage}）"
  if(stage=="MODELS")return "已登入；模型清單未能更新，可以按「更新模型清單」再試。（MODELS）"
  val detail=when(e){
   is java.net.SocketTimeoutException->"登入連線逾時"
   is java.net.UnknownHostException->"暫時未能連接登入網址"
   is javax.net.ssl.SSLException->"登入安全連線未能建立"
   is AccountFailure->e.message?:"登入服務拒絕請求"
   is java.io.IOException->"登入連線中斷"
   else->if(stage=="STORE")"登入憑證未能儲存" else "登入服務未提供完整回覆"
  }
  return "$detail，請重試。（$stage）"
 }
 fun cancelAccount(keepPending:Boolean=false){accountEpoch++;session.invalidatePending();accountClient?.cancel();accountJob?.cancel();accountClient=null;if(!keepPending)pendingLogin.clear();chat.value=chat.value.copy(working=false,userCode="",loginUrl="",loginStatus="")}
 fun loginChatGpt()=startAccount(false)
 fun resumeChatGpt(){if(!chat.value.working && !chat.value.connected)startAccount(true)}
 private fun startAccount(resume:Boolean){
  cancelAccount(keepPending=resume);val epoch=accountEpoch;val grantTicket=session.invalidatePending();val client=chatClientFactory();accountClient=client
  chat.value=chat.value.copy(working=true,error="",loginStatus=if(resume)"檢查緊未完成嘅授權…" else "連接緊 OpenAI 登入服務…")
  accountJob=viewModelScope.launch{
   var ticket:Long?=null;var stage="START"
   try{
    val recovered=if(resume)withContext(Dispatchers.IO){pendingLogin.resume()}else null
    ensureActive();if(epoch!=accountEpoch)return@launch
    if(resume && recovered==null)return@launch
    val device:DeviceLogin
    if(recovered!=null){device=recovered.first;ticket=recovered.second}else{
     device=withContext(Dispatchers.IO){client.begin()};ensureActive();if(epoch!=accountEpoch)return@launch
     ticket=withContext(Dispatchers.IO){pendingLogin.start(device)}
    }
    ensureActive();if(epoch!=accountEpoch)return@launch
    val activeTicket=requireNotNull(ticket)
    chat.value=chat.value.copy(userCode=device.userCode,loginUrl=device.verificationUrl,expiresAt=device.expiresAt,
     loginStatus="等緊 OpenAI 授權；網頁顯示已登入後，返天機就得。",browserRequest=if(resume)0 else System.currentTimeMillis())
    stage="POLL"
    val authorized=DeviceAuthorization.await(device,{
     withContext(Dispatchers.IO){val result=client.poll(device){pendingLogin.remember(it,activeTicket)};if(result==null)pendingLogin.remember(device,activeTicket);result}
    },{status->if(epoch==accountEpoch)chat.value=chat.value.copy(loginStatus=status)})
    ensureActive();if(epoch!=accountEpoch)return@launch
    stage="STORE";chat.value=chat.value.copy(loginStatus="授權已收到，儲存緊登入…")
    withContext(Dispatchers.IO){session.save(authorized,grantTicket);pendingLogin.clear(activeTicket)}
    ensureActive();if(epoch!=accountEpoch)return@launch
    chat.value=chat.value.copy(connected=true,userCode="",loginUrl="",mode="CHATGPT",loginStatus="已登入，更新緊模型清單…")
    chatPrefs.edit().putString("mode","CHATGPT").apply();stage="MODELS"
    loadModels(client,epoch)
   }catch(e:CancellationException){throw e}catch(e:Exception){
    if(epoch==accountEpoch){withContext(Dispatchers.IO){pendingLogin.clear(ticket)};chat.value=chat.value.copy(error=loginError(e,stage),loginStatus="")}
   }finally{client.close();if(epoch==accountEpoch){accountClient=null;chat.value=chat.value.copy(working=false,userCode="",loginUrl="",loginStatus="")}}
  }
 }
 private suspend fun loadModels(client:ChatGptClient,epoch:Int){
  val models=withContext(Dispatchers.IO){session.authenticated(client){client.models(it)}}
  currentCoroutineContext().ensureActive();if(epoch!=accountEpoch)return
  val selected=chat.value.model.takeIf{m->models.any{it.id==m}}?:models.first().id
  chat.value=chat.value.copy(models=models,model=selected,error="");chatPrefs.edit().putString("model",selected).apply()
 }
 fun refreshChatModels(){
  if(chat.value.working)return;cancelAccount();val epoch=accountEpoch;val client=chatClientFactory();accountClient=client;chat.value=chat.value.copy(working=true,error="")
  accountJob=viewModelScope.launch{try{loadModels(client,epoch)}catch(e:CancellationException){throw e}catch(e:Exception){if(epoch==accountEpoch)chat.value=chat.value.copy(error=safeAiError(e))}finally{client.close();if(epoch==accountEpoch){accountClient=null;chat.value=chat.value.copy(working=false)}}}
 }
 fun logoutChatGpt(){cancelAccount();cancelQuick();chat.value=ChatUiState();chatPrefs.edit().clear().apply();viewModelScope.launch(Dispatchers.IO){session.logout()}}
 fun cancelQuick(){quickEpoch++;quickClient?.cancel();quickApi?.cancel();quickJob?.cancel();quickBusy.value=false;quickMessage.value=""}
 fun quickIntention(text:String,hours:Int=0,purpose:String?=null){
  if(quickBusy.value || busy.value)return
  val epoch=++quickEpoch;quickBusy.value=true;quickMessage.value="正在起盤及整理方位…"
  quickJob=viewModelScope.launch{
   var saved=false
   try{
    val plan=withContext(Dispatchers.IO){repo.quickIntention(text,state.value.settings.timezone,hours,purpose)}
    saved=true
    ensureActive();if(epoch!=quickEpoch)return@launch
    explainQuick(plan.id,epoch)
   }catch(e:CancellationException){throw e}catch(e:Exception){if(epoch==quickEpoch)quickMessage.value=if(saved)safeAiError(e)else text("今次起盤未完成，請重試；仍未儲存新指引。","The chart could not be completed. Please retry; no new guide was saved.")}finally{if(epoch==quickEpoch)quickBusy.value=false}
  }
 }
 fun retryIntentionAi(planId:String){
  if(quickBusy.value)return
  val epoch=++quickEpoch;quickBusy.value=true
  quickJob=viewModelScope.launch{try{explainQuick(planId,epoch)}catch(e:CancellationException){throw e}catch(e:Exception){if(epoch==quickEpoch)quickMessage.value=safeAiError(e)}finally{if(epoch==quickEpoch)quickBusy.value=false}}
 }
 private suspend fun explainQuick(planId:String,epoch:Int){
  val mode=chat.value.mode
  if(mode=="OFFLINE"){quickMessage.value="本機指引已準備好。";return}
  if(mode=="CHATGPT" && !session.connected()){quickMessage.value="本機指引已準備好；登入 ChatGPT 後可直接解讀。";return}
  quickMessage.value="盤已起好，等緊 AI 解讀…"
  val data=withContext(Dispatchers.IO){repo.snapshot()};val plan=data.plans.single{it.id==planId}
  val actions=data.actions.filter{it.planVersionId==plan.id}
  val context=Intention.context(plan,actions,data.charts,Instant.now())
  val model:String;val raw:String
  if(mode=="CHATGPT"){
   val client=ChatGptClient();quickClient=client
   try{
    val models=withContext(Dispatchers.IO){session.authenticated(client){client.models(it)}}
    currentCoroutineContext().ensureActive();model=chat.value.model.takeIf{m->models.any{it.id==m}}?:models.first().id
    chat.value=chat.value.copy(models=models,model=model);chatPrefs.edit().putString("model",model).apply()
    raw=withContext(Dispatchers.IO){repo.consent("intention-chatgpt-once");session.authenticated(client){client.complete(it,model,Intention.instruction,context)}}
   }finally{client.close();quickClient=null}
  }else{
   val client=AiClient();quickApi=client;model=provider.value.modelName
   try{raw=withContext(Dispatchers.IO){repo.consent("intention-api-once");client.explain(provider.value,vault.read(),Intention.instruction,context)}}finally{quickApi=null}
  }
  currentCoroutineContext().ensureActive();if(epoch!=quickEpoch)return
  val result=Intention.parseReply(raw,plan,actions,model,Instant.now())
  withContext(Dispatchers.IO){repo.saveGuidance(planId,result)}
  quickMessage.value="AI 解讀已保存。"
 }
 override fun onCleared(){cancelAccount(keepPending=true);cancelQuick();super.onCleared()}
 fun perform(block:suspend ()->Unit){if(busy.value)return;busy.value=true;viewModelScope.launch{try{withContext(Dispatchers.IO){block()}}catch(e:Exception){messages.send(if(state.value.settings.english)"Could not complete this action. Check the input, deadline, file or connection." else "未能完成。請檢查輸入、限期、檔案或連線。")}finally{busy.value=false}}}
 fun settings(s:Settings)=perform{prefs.save(s);ReminderWorker.configure(app,s.reminders)}
 fun saveProvider(p:AiProviderConfig,key:String)=perform{if(p.baseUrl!=provider.value.baseUrl)vault.clear();if(key.isNotBlank())vault.save(key);repo.provider(p);provider.value=p}
 private fun text(zh:String,en:String)=if(state.value.settings.english)en else zh
 fun testProvider()=perform{ai.test(provider.value,vault.read());messages.send(text("連線成功","Connection OK"))}
 fun payload(g:Goal,actions:List<ActionCandidate>)=ai.payload(g,actions)
 fun compare(payload:String,actions:List<ActionCandidate>)=perform{repo.consent("ai-context-once");val result=ai.compare(provider.value,vault.read(),payload,actions);val top=actions.single{it.id==result.action_order.first()};messages.send(text("AI 建議："+top.title+"（實用規劃，非預測）","AI suggestion: "+top.kind+" (heuristic planning)"))}
 fun export(uri:Uri,research:Boolean=false)=perform{
  val text=if(research)researchExport() else repo.export()
  app.contentResolver.openOutputStream(uri)?.use{it.write(text.toByteArray())}?:error("Output unavailable")
  messages.send(text("備份已匯出","Export saved"))
 }
 fun restore(uri:Uri)=perform{
  val bytes=app.contentResolver.openInputStream(uri)?.use { input -> val buffer=java.io.ByteArrayOutputStream();val chunk=ByteArray(8192);while(true){val n=input.read(chunk);if(n<0)break;require(buffer.size()+n<=10*1024*1024);buffer.write(chunk,0,n)};buffer.toByteArray() }?:error("Input unavailable")
  repo.restore(bytes.toString(Charsets.UTF_8));messages.send(text("備份已還原","Backup restored"))
 }
 private suspend fun researchExport():String{
  repo.consent("research-export");val local=app.getSharedPreferences("research-pseudonym",0);val subject=local.getString("id",null)?:UUID.randomUUID().toString().also{check(local.edit().putString("id",it).commit())};val data=repo.snapshot()
  return data.journal.joinToString("\n"){j->val a=data.actions.single{it.id==j.actionId};buildJsonObject{
   put("event_id",j.id);put("subject_pseudonym",subject);put("occurred_at_utc",j.occurredAt);put("outcome_at_utc",j.occurredAt);put("timezone",state.value.settings.timezone);put("location_coarse",JsonNull);put("context",buildJsonObject{});put("action",buildJsonObject{put("kind",a.kind);put("cost",a.cost)});put("outcome",when(j.outcome){"UNKNOWN"->JsonNull;"SUCCESS"->JsonPrimitive(1);else->JsonPrimitive(0)});put("source","navigator-retrospective-v2");put("consent_scope","research-export");put("chart_ref",JsonNull);put("dataset_version","1")
  }.toString()}
 }
 fun deleteAll(){cancelAccount();cancelQuick();perform{ReminderWorker.configure(app,false);repo.deleteAll();vault.clear();session.logout();chatPrefs.edit().clear().commit();chat.value=ChatUiState();app.getSharedPreferences("research-pseudonym",0).edit().clear().commit();provider.value=AiProviderConfig();prefs.save(state.value.settings.copy(reminders=false));messages.send(text("本機資料已刪除","Local data deleted"))}}
}
