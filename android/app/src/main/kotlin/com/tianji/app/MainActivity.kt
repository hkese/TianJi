package com.tianji.app
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.luminance
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.tianji.data.*
import com.tianji.domain.*
import com.tianji.ui.*
import com.tianji.home.*
import com.tianji.chart.*
import com.tianji.goals.*
import com.tianji.planner.*
import com.tianji.journal.*
import com.tianji.settings.*
import org.koin.core.context.GlobalContext
import java.time.ZoneId
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);enableEdgeToEdge();val model=ViewModelProvider(this,object:ViewModelProvider.Factory{override fun <T:ViewModel> create(modelClass:Class<T>):T{@Suppress("UNCHECKED_CAST")return AppModel(application,GlobalContext.get().get(),GlobalContext.get().get(),GlobalContext.get().get()) as T}})[AppModel::class.java];setContent{TianJiApp(model)}}}
@Composable fun TianJiApp(vm:AppModel){
 val state by vm.state.collectAsStateWithLifecycle();val provider by vm.provider.collectAsStateWithLifecycle();val busy by vm.busy.collectAsStateWithLifecycle();val s=state.settings;val data=state.data
 Theme(s){
  val view=LocalView.current
  val dark=MaterialTheme.colorScheme.background.luminance()<0.5f
  SideEffect{(view.context as? ComponentActivity)?.enableEdgeToEdge(
   statusBarStyle=SystemBarStyle.auto(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT){dark},
   navigationBarStyle=SystemBarStyle.auto(android.graphics.Color.rgb(248,247,242),android.graphics.Color.rgb(16,32,26)){dark}
  )}
  if(!state.loaded){Surface(Modifier.fillMaxSize()){Box(contentAlignment=Alignment.Center){CircularProgressIndicator()}};return@Theme}
  if(!s.onboarded){Onboarding(s,vm::settings);return@Theme}
  val nav=rememberNavController();val stack by nav.currentBackStackEntryAsState();val route=stack?.destination?.route?:"home";val snack=remember{SnackbarHostState()};var entry by remember{mutableStateOf<ActionCandidate?>(null)};var aiReview by remember{mutableStateOf<Pair<String,List<ActionCandidate>>?>(null)};var research by remember{mutableStateOf(false)};var importConfirm by remember{mutableStateOf<android.net.Uri?>(null)}
  val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){it?.let{vm.export(it)}}
  val researchExport=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/x-ndjson")){it?.let{vm.export(it,true)}}
  val restore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){importConfirm=it}
  val notification=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->vm.settings(s.copy(reminders=granted))}
  LaunchedEffect(Unit){for(message in vm.messages)snack.showSnackbar(message)}
  Scaffold(modifier=Modifier.imePadding(),contentWindowInsets=WindowInsets.safeDrawing,snackbarHost={SnackbarHost(snack)},bottomBar={NavigationBar{val tabs=listOf(Triple("home",t("首頁","Home"),Icons.Default.Home),Triple("chart",t("奇門","Chart"),Icons.Default.DateRange),Triple("goals",t("目標","Goals"),Icons.Default.List),Triple("journal",t("日誌","Journal"),Icons.Default.Edit),Triple("settings",t("設定","Settings"),Icons.Default.Settings));tabs.forEach{(id,label,icon)->NavigationBarItem(modifier=Modifier.testTag("tab-$id"),selected=route==id || (id=="goals" && route=="plan/{goal}"),onClick={nav.navigate(id){popUpTo("home");launchSingleTop=true}},icon={Icon(icon,contentDescription=label)},label={Text(label)})}}}){padding->
   Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),contentAlignment=Alignment.TopCenter){
    Box(Modifier.widthIn(max=760.dp).fillMaxSize()){
     NavHost(nav,"home"){
      composable("home"){HomeScreen(data,s.timezone,{nav.navigate("goals")},{nav.navigate("plan/$it")},{nav.navigate("chart")})}
      composable("chart"){ChartScreen(s.timezone)}
      composable("goals"){GoalsScreen(data,s.timezone,{vm.perform{vm.repo.saveGoal(it)}},{vm.perform{vm.repo.deleteGoal(it)}},{vm.perform{vm.repo.saveMilestone(it)}},{nav.navigate("plan/$it")})}
      composable("plan/{goal}"){back->val goalId=back.arguments?.getString("goal")?:return@composable;PlannerScreen(goalId,data,s.timezone,{reason->vm.perform{vm.repo.newPlan(goalId,s.timezone,reason)}},{a->vm.perform{vm.repo.choose(a)}},{a->entry=a;nav.navigate("journal")},{g,a->aiReview=vm.payload(g,a) to a},{nav.popBackStack()})}
      composable("journal"){JournalScreen(data,s.timezone,entry,{j->vm.perform{vm.repo.journal(j)};entry=null},{entry=null})}
      composable("settings"){SettingsScreen(s,provider,{next->if(next.reminders&&!s.reminders&&Build.VERSION.SDK_INT>=33)notification.launch(Manifest.permission.POST_NOTIFICATIONS) else vm.settings(next)},vm::saveProvider,vm::testProvider,{export.launch("TianJi-backup.json")},{restore.launch(arrayOf("application/json","text/plain","application/octet-stream"))},{research=true},vm::deleteAll)}
     }
     if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
    }
   }
  }
  aiReview?.let{(payload,actions)->AlertDialog(onDismissRequest={aiReview=null},title={Text(t("確認分享內容","Review shared context"))},text={Column{Text(provider.baseUrl.ifBlank{t("請先設定 HTTPS 供應商","Configure an HTTPS provider first")});Text(payload,style=MaterialTheme.typography.bodySmall);Text(t("只發送以上目標及方案。你可取消並繼續離線使用。","Only the displayed goal and options are shared. You can cancel and continue offline."))}},confirmButton={TextButton(enabled=provider.baseUrl.isNotBlank(),onClick={vm.compare(payload,actions);aiReview=null}){Text(t("同意這次傳送","Consent to this request"))}},dismissButton={TextButton(onClick={aiReview=null}){Text(t("取消","Cancel"))}})}
  if(research)AlertDialog(onDismissRequest={research=false},title={Text(t("研究匯出同意","Research export consent"))},text={Text(t("匯出假名、時間、時區、行動類型、支出及結果。不包括名稱、目標文字、筆記或金鑰。不會自動上傳。假名資料仍可能被重新識別；這是回顧記錄，並非盲測預測。","Exports a pseudonym, time, timezone, action type, cost and outcome. Excludes names, goal text, notes and credentials. No automatic upload. Pseudonymized records may remain identifiable; these are retrospective records, not blind predictions."))},confirmButton={TextButton(onClick={research=false;researchExport.launch("TianJi-consented-events.jsonl")}){Text(t("同意並選擇儲存位置","Consent and choose file"))}},dismissButton={TextButton(onClick={research=false}){Text(t("取消","Cancel"))}})
  importConfirm?.let{uri->AlertDialog(onDismissRequest={importConfirm=null},title={Text(t("取代目前資料？","Replace current records?"))},text={Text(t("只有完整驗證成功後才會取代資料。建議先匯出目前備份。","Records are replaced only after complete validation. Export a backup first if needed."))},confirmButton={TextButton(onClick={vm.restore(uri);importConfirm=null}){Text(t("匯入","Import"))}},dismissButton={TextButton(onClick={importConfirm=null}){Text(t("取消","Cancel"))}})}
 }
}
@Composable fun Onboarding(settings:Settings,save:(Settings)->Unit){var zone by remember{mutableStateOf(settings.timezone)};Surface(Modifier.fillMaxSize()){Box(Modifier.safeDrawingPadding()){Page(t("天機 · TianJi","TianJi Navigator"),t("由當下開始。","Start from here.")){
 Text(t("你的目標、方案及日誌預設只保存在手機，毋須帳戶、GPS 或 AI。","Your goals, plans and journal stay on your phone by default. No account, GPS or AI required."));CardBlock(t("先理解證據","Understand the evidence")){Text(t("奇門及時空導航假說未經科學證實。現時排盤待核實，實用規劃建議不是已驗證預測。","Qimen and the navigation hypothesis are not scientifically established. Chart calculation awaits verification; planning advice is not a validated prediction."))};Row{Text("English",Modifier.weight(1f));Switch(settings.english,{save(settings.copy(english=it))})};Field(zone,t("IANA 時區","IANA timezone"),{zone=it});ActionButton(t("開始離線使用","Start offline"),enabled=runCatching{ZoneId.of(zone)}.isSuccess){save(settings.copy(onboarded=true,timezone=zone))}
}}}}
