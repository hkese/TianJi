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
class TianJiApplication:Application(){override fun onCreate(){super.onCreate();org.koin.core.context.startKoin{androidContext(this@TianJiApplication);modules(org.koin.dsl.module{single{Repository.open(get())};single{Preferences(get())};single{CredentialVault(get())}})}}}
data class UiState(val data:Backup=Backup(),val settings:Settings=Settings(),val loaded:Boolean=false)
class AppModel(val app:Application,val repo:Repository,val prefs:Preferences,val vault:CredentialVault):ViewModel(){
 val state=combine(repo.state,prefs.flow){data,s->UiState(data,s,true)}.stateIn(viewModelScope,SharingStarted.Eagerly,UiState())
 val messages=Channel<String>(Channel.BUFFERED);val busy=MutableStateFlow(false);val provider=MutableStateFlow(AiProviderConfig());private val ai=AiClient()
 init{perform{provider.value=repo.provider()}}
 fun perform(block:suspend ()->Unit){viewModelScope.launch{busy.value=true;try{withContext(Dispatchers.IO){block()}}catch(e:Exception){messages.send(if(state.value.settings.english)"Could not complete this action. Check the input, deadline, file or connection." else "未能完成。請檢查輸入、限期、檔案或連線。")}finally{busy.value=false}}}
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
 fun deleteAll()=perform{ReminderWorker.configure(app,false);repo.deleteAll();vault.clear();app.getSharedPreferences("research-pseudonym",0).edit().clear().commit();provider.value=AiProviderConfig();prefs.save(state.value.settings.copy(reminders=false));messages.send(text("本機資料已刪除","Local data deleted"))}
}
