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
import androidx.compose.runtime.saveable.rememberSaveable
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
      composable("home"){IntentionScreen(vm,{nav.navigate("plan/$it")},{nav.navigate("chart")},{nav.navigate("guide")},{nav.navigate("goals")},{nav.navigate("settings")})}
      composable("chart"){ChartScreen(s.timezone,data.charts,{vm.settings(s.copy(timezone=it))},{chart->vm.perform{vm.repo.saveChart(chart);vm.messages.send(if(s.english)"Chart saved offline" else "命盤已儲存喺手機")}}, {nav.navigate("goals")},{nav.navigate("guide")})}
      composable("goals"){GoalsScreen(data,s.timezone,{vm.perform{vm.repo.saveGoal(it)}},{vm.perform{vm.repo.deleteGoal(it)}},{vm.perform{vm.repo.saveMilestone(it)}},{nav.navigate("plan/$it")})}
      composable("plan/{goal}"){back->val goalId=back.arguments?.getString("goal")?:return@composable;PlannerScreen(goalId,data,s.timezone,{reason->vm.perform{vm.repo.newPlan(goalId,s.timezone,reason)}},{a->vm.perform{vm.repo.choose(a)}},{a->entry=a;nav.navigate("journal")},{g,a->aiReview=vm.payload(g,a) to a},{nav.popBackStack()},{request,reason->vm.perform{vm.repo.newDirectionPlan(goalId,s.timezone,reason,request)}},{id,point->vm.perform{vm.repo.advanceDirection(id,point)}})}
      composable("journal"){JournalScreen(data,s.timezone,entry,{j->vm.perform{vm.repo.journal(j)};entry=null},{entry=null})}
      composable("settings"){SettingsScreen(s,provider,{next->if(next.reminders&&!s.reminders&&Build.VERSION.SDK_INT>=33)notification.launch(Manifest.permission.POST_NOTIFICATIONS) else vm.settings(next)},vm::saveProvider,vm::testProvider,{export.launch("TianJi-backup.json")},{restore.launch(arrayOf("application/json","text/plain","application/octet-stream"))},{research=true},vm::deleteAll,{nav.navigate("guide")})}
      composable("guide"){GuideScreen({nav.navigate("home"){popUpTo("home");launchSingleTop=true}},{nav.navigate("settings"){popUpTo("home");launchSingleTop=true}},{nav.popBackStack()})}
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
@Composable fun Onboarding(settings:Settings,save:(Settings)->Unit){
 var zone by rememberSaveable{mutableStateOf(settings.timezone)}
 Surface(Modifier.fillMaxSize()){Box(Modifier.safeDrawingPadding()){Page(t("天機 · TianJi","TianJi Navigator"),t("由當下開始。","Start from here.")){
  Text(t("寫低心願、擇時擇方、跟住行程記錄。資料預設只留在手機，毋須帳戶、GPS 或 AI。","Set an intention, choose a time and direction, then follow your itinerary. Data stays on your phone by default. No account, GPS or AI required."))
  CardBlock(t("先講清楚","Before you start")){
   Text(t("基本奇門排盤、中文傳統簡讀及行動規劃均可離線使用。奇門解讀是傳統參考，預測效果未經科學證實，不會給出成功機率或保證結果。","Basic Qimen charts, traditional readings and practical planning work offline. Predictive effectiveness is not scientifically established; no success probabilities or guaranteed outcomes are provided."))
  }
  LanguageSelector(settings.english){save(settings.copy(english=it))}
  TimeZoneSelector(zone){zone=it}
  ActionButton(t("開始離線使用","Start offline")){save(settings.copy(onboarded=true,timezone=zone))}
  Text(t("入到首頁可隨時開啟「使用教學」。","The Getting started guide is always available on Home."),style=MaterialTheme.typography.bodySmall)
 }}}
}
