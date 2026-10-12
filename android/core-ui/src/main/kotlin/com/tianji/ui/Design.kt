package com.tianji.ui
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tianji.data.Settings
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import java.util.Locale
import java.time.*
import java.time.format.DateTimeFormatter
val English=compositionLocalOf{false}
@Composable fun t(zh:String,en:String)=if(English.current)en else zh
@Composable fun Theme(s:Settings,content:@Composable () -> Unit){
 val dark=s.theme=="dark" || (s.theme=="system" && isSystemInDarkTheme())
 val colors=if(dark)darkColorScheme(primary=Color(0xFF9CD4BC),onPrimary=Color(0xFF103D30),primaryContainer=Color(0xFF285342),onPrimaryContainer=Color(0xFFBCEED5),secondary=Color(0xFFB5CCBC),onSecondary=Color(0xFF21372C),secondaryContainer=Color(0xFF354F40),onSecondaryContainer=Color(0xFFD4E8D9),onSurface=Color(0xFFE1E8DE),onSurfaceVariant=Color(0xFFBECABD),outline=Color(0xFF88958B),background=Color(0xFF10201A),surface=Color(0xFF15281F),surfaceContainerLow=Color(0xFF1B3026),surfaceContainer=Color(0xFF223A2E),surfaceContainerHigh=Color(0xFF2A4036),surfaceContainerHighest=Color(0xFF31493B)) else lightColorScheme(primary=Color(0xFF1A6356),background=Color(0xFFF8F7F2),surface=Color(0xFFF8F7F2),surfaceContainerLow=Color(0xFFF0F2EB),surfaceContainer=Color(0xFFE8EDE3),surfaceContainerHigh=Color(0xFFE2E8DC),surfaceContainerHighest=Color(0xFFDDE5D7),onSurfaceVariant=Color(0xFF4D5D52),onSecondaryContainer=Color(0xFF163F34),secondaryContainer=Color(0xFFDCEBDD))
 CompositionLocalProvider(English provides s.english){MaterialTheme(colorScheme=colors,typography=Typography(),content=content)}
}
@Composable fun Page(title:String,subtitle:String="",scrollState:ScrollState=rememberScrollState(),content:@Composable ColumnScope.()->Unit){
 Column(Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal=20.dp,vertical=24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
  Text(title,style=MaterialTheme.typography.headlineLarge)
  if(subtitle.isNotEmpty())Text(subtitle,color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodyMedium)
  content();Spacer(Modifier.height(32.dp))
 }
}
@Composable fun CardBlock(title:String,content:@Composable ColumnScope.()->Unit){Card(shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(title,style=MaterialTheme.typography.titleLarge);content()}}}
@Composable fun Evidence(){Text(t("實用規劃 · 非預測","HEURISTIC · Practical planning"),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary);Text(t("依據你輸入的目標與限制；不代表已驗證預測，請按實際情況判斷。","Based on your goals and constraints. This is not a validated prediction; review against your circumstances."),style=MaterialTheme.typography.bodySmall)}
@Composable fun ActionButton(label:String,enabled:Boolean=true,onClick:()->Unit){Button(onClick,enabled=enabled,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(label)}}
@Composable fun Field(value:String,label:String,onChange:(String)->Unit,modifier:Modifier=Modifier,single:Boolean=true){OutlinedTextField(value,onChange,label={Text(label)},modifier=modifier.fillMaxWidth(),singleLine=single)}
fun displayTime(value:String?,zone:String):String=value?.let{runCatching{Instant.parse(it).atZone(ZoneId.of(zone)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}.getOrDefault(it)}?:"—"
@Composable fun ErrorText(message:String){if(message.isNotBlank())Text(message,color=MaterialTheme.colorScheme.error)}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DateField(value:String?,zone:String,onChange:(String?)->Unit){
 var show by remember{mutableStateOf(false)}
 OutlinedButton(onClick={show=true},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("deadline-picker")){Text(t("限期：","Deadline: ")+(value?.let{displayTime(it,zone)}?:t("未設定（可選填）","Not set (optional)")))}
 if(value!=null)TextButton(onClick={onChange(null)}){Text(t("移除限期","Remove deadline"))}
 if(show){
  val context=LocalContext.current
  val configuration=LocalConfiguration.current
  val english=English.current
  val localized=remember(configuration,english){Configuration(configuration).apply{setLocale(if(english)Locale.ENGLISH else Locale.forLanguageTag("zh-HK"))}}
  val localizedContext=remember(context,localized){context.createConfigurationContext(localized)}
  CompositionLocalProvider(LocalContext provides localizedContext,LocalConfiguration provides localized){
   val initial=value?.let{Instant.parse(it).atZone(ZoneId.of(zone)).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()}
   val state=rememberDatePickerState(initialSelectedDateMillis=initial)
   DatePickerDialog(onDismissRequest={show=false},confirmButton={
    TextButton(enabled=state.selectedDateMillis!=null,onClick={
     state.selectedDateMillis?.let{millis->
      val date=Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
      onChange(date.plusDays(1).atStartOfDay(ZoneId.of(zone)).minusSeconds(1).toInstant().toString())
     };show=false
    }){Text(t("確定","Confirm"))}
   },dismissButton={TextButton(onClick={show=false}){Text(t("取消","Cancel"))}}){
    // Dialog creates a new Android composition root. Reapply the app locale
    // inside it so Material's month, weekdays and accessibility strings agree.
    CompositionLocalProvider(LocalContext provides localizedContext,LocalConfiguration provides localized){
     DatePicker(state,title={Text(t("選擇限期","Choose deadline"),Modifier.padding(start=24.dp,top=16.dp))})
    }
   }
  }
 }
}
