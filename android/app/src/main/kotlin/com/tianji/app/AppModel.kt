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
data class ChatUiState(val connected:Boolean=false,val working:Boolean=false,val userCode:String="",val loginUrl:String="",val expiresAt:Long=0,val error:String="",val models:List<ChatGptModel> = emptyList(),val model:String="",val mode:String="OFFLINE")
class TianJiApplication:Application(){override fun onCreate(){super.onCreate();org.koin.core.context.startKoin{androidContext(this@TianJiApplication);modules(org.koin.dsl.module{single{Repository.open(get())};single{Preferences(get())};single{CredentialVault(get())}})}}}
data class UiState(val data:Backup=Backup(),val settings:Settings=Settings(),val loaded:Boolean=false)
class AppModel(val app:Application,val repo:Repository,val prefs:Preferences,val vault:CredentialVault):ViewModel(){
 val state=combine(repo.state,prefs.flow){data,s->UiState(data,s,true)}.stateIn(viewModelScope,SharingStarted.Eagerly,UiState())
 val messages=Channel<String>(Channel.BUFFERED);val busy=MutableStateFlow(false);val provider=MutableStateFlow(AiProviderConfig());private val ai=AiClient()
 private val chatPrefs=app.getSharedPreferences("intention-ai",0)
 private val session=ChatGptSession(EncryptedGrantStorage(app))
 val chat=MutableStateFlow(ChatUiState(model=chatPrefs.getString("model","")?:"",mode=chatPrefs.getString("mode","OFFLINE")?:"OFFLINE"))
 val quickBusy=MutableStateFlow(false);val quickMessage=MutableStateFlow("")
 private var accountJob:Job?=null;private var quickJob:Job?=null
 private var accountClient:ChatGptClient?=null;private var quickClient:ChatGptClient?=null;private var quickApi:AiClient?=null
 private var accountEpoch=0;private var quickEpoch=0
 init{perform{provider.value=repo.provider();chat.value=chat.value.copy(connected=session.connected())}}
 fun aiMode(mode:String){require(mode in listOf("OFFLINE","CHATGPT","API"));cancelQuick();chat.value=chat.value.copy(mode=mode);chatPrefs.edit().putString("mode",mode).apply()}
 fun selectModel(id:String){require(chat.value.models.any{it.id==id});cancelQuick();chat.value=chat.value.copy(model=id);chatPrefs.edit().putString("model",id).apply()}
 private fun safeAiError(e:Exception):String=when(e){is AccountFailure->e.message?:"ChatGPT 未能回覆。";is java.io.IOException->"連線未完成，請檢查網絡或重新登入；本機指引已保留。";else->"AI 未提供符合本盤嘅完整解讀；本機指引已保留，可以重試。"}
 fun cancelAccount(){accountEpoch++;session.invalidatePending();accountClient?.cancel();accountJob?.cancel();accountClient=null;chat.value=chat.value.copy(working=false,userCode="",loginUrl="")}
 fun loginChatGpt(){
  cancelAccount();val epoch=accountEpoch;val grantTicket=session.invalidatePending();val client=ChatGptClient();accountClient=client
  chat.value=chat.value.copy(working=true,error="")
  accountJob=viewModelScope.launch{
   try{
    val device=withContext(Dispatchers.IO){client.begin()};ensureActive();if(epoch!=accountEpoch)return@launch
    chat.value=chat.value.copy(userCode=device.userCode,loginUrl=device.verificationUrl,expiresAt=device.expiresAt)
    var grant:ChatGptGrant?=null
    while(grant==null){delay(device.intervalSeconds*1000L);grant=withContext(Dispatchers.IO){client.poll(device)}}
    ensureActive();if(epoch!=accountEpoch)return@launch
    val authorized=requireNotNull(grant)
    withContext(Dispatchers.IO){session.save(authorized,grantTicket)}
    chat.value=chat.value.copy(connected=true,userCode="",loginUrl="",mode="CHATGPT")
    chatPrefs.edit().putString("mode","CHATGPT").apply()
    loadModels(client,epoch)
   }catch(e:CancellationException){throw e}catch(e:Exception){if(epoch==accountEpoch)chat.value=chat.value.copy(error=safeAiError(e))}
   finally{client.close();if(epoch==accountEpoch){accountClient=null;chat.value=chat.value.copy(working=false,userCode="",loginUrl="")}}
  }
 }
 private suspend fun loadModels(client:ChatGptClient,epoch:Int){
  val models=withContext(Dispatchers.IO){session.authenticated(client){client.models(it)}}
  currentCoroutineContext().ensureActive();if(epoch!=accountEpoch)return
  val selected=chat.value.model.takeIf{m->models.any{it.id==m}}?:models.first().id
  chat.value=chat.value.copy(models=models,model=selected,error="");chatPrefs.edit().putString("model",selected).apply()
 }
 fun refreshChatModels(){
  if(chat.value.working)return;cancelAccount();val epoch=accountEpoch;val client=ChatGptClient();accountClient=client;chat.value=chat.value.copy(working=true,error="")
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
 override fun onCleared(){cancelAccount();cancelQuick();super.onCleared()}
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
