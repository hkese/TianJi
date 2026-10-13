package com.tianji.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.tianji.domain.*
import com.tianji.qimen.*
import com.tianji.ui.*
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.DateTimeFormatter

private fun departureTime(value:String,zone:String)=Instant.parse(value).atZone(ZoneId.of(zone)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm XXX"))

@OptIn(ExperimentalLayoutApi::class)
@Composable fun PlannerScreen(goalId:String,data:Backup,zone:String,onNew:(String)->Unit,onChoose:(ActionCandidate)->Unit,onJournal:(ActionCandidate)->Unit,onAi:(Goal,List<ActionCandidate>)->Unit,onBack:()->Unit,
    onDirectionPlan:(DirectionRequest,String)->Unit,onAdvance:(String,GeoPoint?)->Unit) {
    val goal=data.goals.firstOrNull{it.id==goalId}?:return
    var legacy by rememberSaveable{mutableStateOf(false)}
    if(legacy){LegacyPlannerScreen(goalId,data,zone,onNew,onChoose,onJournal,onAi,{legacy=false});return}
    val plans=data.plans.filter{it.goalId==goalId && it.directionRequest!=null}.sortedByDescending{it.createdAt}
    var shown by rememberSaveable(goalId){mutableStateOf<String?>(null)}
    val plan=plans.firstOrNull{it.id==shown}?:plans.firstOrNull()
    val actions=data.actions.filter{it.planVersionId==plan?.id}
    val selected=actions.firstOrNull{it.selected}
    var purpose by rememberSaveable(goalId){mutableStateOf(plan?.directionRequest?.purpose?:if(goal.purpose=="TALK")"HARMONY" else "OUTING")}
    var horizon by rememberSaveable(goalId){mutableIntStateOf(plan?.directionRequest?.horizonHours?:24)}
    var travel by rememberSaveable(goalId){mutableStateOf(plan?.directionRequest?.travelMode?:"WALK")}
    var stay by rememberSaveable(goalId){mutableIntStateOf(plan?.directionRequest?.stayMinutes?:if(goal.constraints.minutes>5)5 else 0)}
    var settings by rememberSaveable(goalId){mutableStateOf(plan==null)}
    var sources by rememberSaveable{mutableStateOf(false)}
    var history by rememberSaveable{mutableStateOf(false)}
    var reason by rememberSaveable{mutableStateOf("")}
    var handoff by remember{mutableStateOf<String?>(null)}
    var clock by remember{mutableStateOf(Instant.now())}
    LaunchedEffect(Unit){while(true){clock=Instant.now();delay(1000)}}
    val scroll=rememberScrollState();val focus=LocalFocusManager.current;val keyboard=LocalSoftwareKeyboardController.current
    LaunchedEffect(selected?.id){if(selected!=null)scroll.animateScrollTo(0)}
    val planZone=data.charts.firstOrNull{it.id==plan?.questionChartId}?.timezone?:zone
    Page(t("擇時・擇方","Time & direction"),goal.title,scrollState=scroll){
        TextButton(onClick=onBack){Text(t("返回目標","Back to goals"))}
        selected?.let{action->
            val chart=remember(action.chartId){data.charts.firstOrNull{it.id==action.chartId}?.let{Codec.decodeFromString<Chart>(it.chartJson)}}
            if(chart!=null)JourneyCard(action,chart,clock,onAdvance,onJournal)
        }
        if(plan==null || settings){
            CardBlock(t("為今次心願擇時擇方","Choose a traditional method")){
                Text(t("你想達成：","Your intention: ")+goal.title)
                Text(t("揀用途","Purpose"),style=MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    FilterChip(selected=purpose=="OUTING",onClick={purpose="OUTING"},label={Text(t("一般出行","General outing"))},modifier=Modifier.testTag("purpose-outing"))
                    FilterChip(selected=purpose=="HARMONY",onClick={purpose="HARMONY"},label={Text(t("和合相處","Harmony"))},modifier=Modifier.testTag("purpose-harmony"))
                }
                Text(if(purpose=="HARMONY")t("按人遁／玉女守門條件搜尋，唔會用其他門象假冒和合格。","Searches the Ren Dun / Jade Maiden rules; unrelated door symbolism is not substituted.") else t("按三奇配開、休、生門搜尋，再套用本簡式排除條件。","Matches the three wonders with Open, Rest or Life Door, with the documented exclusions."),style=MaterialTheme.typography.bodySmall)
                Text(t("搜尋範圍","Look ahead"))
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(24,72,168).forEach{h->FilterChip(horizon==h,{horizon=h},label={Text(if(h==168)t("7 日","7 days") else t("$h 小時","$h hours"))},modifier=Modifier.testTag("horizon-$h"))}}
                Text(t("點樣使用","How to use it"))
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    FilterChip(travel=="WALK",{travel="WALK"},label={Text(t("步行出發","Walk"))})
                    FilterChip(travel=="VIEW",{travel="VIEW"},label={Text(t("只看方位","View direction"))})
                }
                Text(t("到場停留（你自己安排，唔係古法吉數）","Stay on arrival (your schedule, not a ritual number)"))
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(0,5,10,15).forEach{m->FilterChip(stay==m,{stay=m},enabled=m<goal.constraints.minutes,label={Text(if(m==0)t("不另停留","No extra stay") else t("$m 分鐘","$m min"))})}}
                Text(t("時區：","Time zone: ")+zoneCity(zone,English.current))
                Text(t("目的地由你按實際道路選擇。指南針會帶你辨識方位；本版未有地圖路線或自動城市選址。","Choose an accessible destination yourself. The compass identifies the direction; this version has no street routing or automatic city selection."),style=MaterialTheme.typography.bodySmall)
                Text(t("總行程預算 ${goal.constraints.minutes} 分鐘；包括來回及停留，實際路程由你核實。","Trip budget: ${goal.constraints.minutes} minutes including travel and stay. Check the actual route yourself."),style=MaterialTheme.typography.bodySmall)
                val can=(goal.deadline?.let{Instant.parse(it)>clock.plusSeconds(goal.constraints.minutes*60L+300)}?:true) && stay<goal.constraints.minutes
                if(!can)ErrorText(t("限期未預留總行程預算及五分鐘起行窗口，或停留超出預算；請先調整。","Deadline is past or under five minutes away. Edit it first."))
                ActionButton(t("搜尋合適時段及方位","Find time and direction"),enabled=can){
                    focus.clearFocus(true);keyboard?.hide();shown=null;settings=false
                    onDirectionPlan(DirectionRequest(purpose,horizon,travel,stay),reason.ifBlank{"按心願擇時擇方"})
                }
                Text(t("依時間先後列出最多三個候選；不是成功率排名。","Up to three chronological matches, not a ranking of success rates."),style=MaterialTheme.typography.bodySmall)
            }
        } else OutlinedButton(onClick={settings=true},modifier=Modifier.fillMaxWidth()){Text(t("改用途／範圍・重新擇時","Change purpose / range · search again"))}
        if(plan!=null){
            Text(t("本次 ${actions.size} 個候選","${actions.size} candidates"),style=MaterialTheme.typography.headlineSmall)
            Text(t("起意時間：","Question time: ")+displayTime(plan.createdAt,planZone)+" · "+zoneCity(planZone,English.current),style=MaterialTheme.typography.bodySmall)
            if(actions.isEmpty())CardBlock(t("範圍內未有符合條件嘅時段","No matching window in this range")){
                Text(t("今次唔會硬配一個方向。呢個結果只代表未符合所選簡式，唔代表你嘅心願做唔到。可擴大至 7 日，或之後再查。","No direction is forced. This only means the chosen profile found no match, not that your intention is impossible. Try seven days or check again later."))
                ActionButton(t("改搜尋範圍","Change search range")){settings=true}
            }
            actions.forEachIndexed{index,a->
                val chart=data.charts.firstOrNull{it.id==a.chartId}?.let{Codec.decodeFromString<Chart>(it.chartJson)}?:return@forEachIndexed
                val itinerary=a.itinerary?:return@forEachIndexed
                val p=chart.palaces.single{it.index==itinerary.palace}
                val expired=clock>=Instant.parse(a.plannedEnd)
                CardBlock("${index+1} · ${p.direction} ${Navigator.bearing(p.index)}°"){
                    Text(itinerary.ruleIds.joinToString("／",transform=DirectionRules::name),style=MaterialTheme.typography.titleMedium)
                    Text(t("出發時段","Departure window"),style=MaterialTheme.typography.labelLarge)
                    Text(departureTime(a.plannedStart,chart.input.timezone)+" → "+departureTime(a.plannedEnd,chart.input.timezone))
                    Text(t("${chart.input.timezone}；終點不包括。時段作用於出發，唔係保證到達或返程吉時。","${chart.input.timezone}; end exclusive. This is a departure window, not an auspicious arrival or return time."),style=MaterialTheme.typography.bodySmall)
                    Text(t("${p.index} 宮｜${p.door}・${p.star}・${p.deity}","Palace ${p.index} | ${p.door} · ${p.star} · ${p.deity}"))
                    Text(t("天盤 ${p.heaven_stem} · 地盤 ${p.earth_stem}","Heaven ${p.heaven_stem} · Earth ${p.earth_stem}"))
                    Text(a.rationale,style=MaterialTheme.typography.bodySmall)
                    Text(t("從固定起點向${p.direction}方選可到達地點；停留 ${itinerary.stayMinutes} 分鐘由你安排。","Choose an accessible destination ${p.direction} of your fixed starting point; your chosen stay is ${itinerary.stayMinutes} minutes."))
                    if(expired)Text(t("時段已過，請重新擇時。","Window ended. Search again."),color=MaterialTheme.colorScheme.error)
                    ActionButton(if(a.selected)t("已選呢個行程","Selected") else t("選擇呢個行程","Choose itinerary"),enabled=!expired && !a.selected){onChoose(a)}
                }
            }
            if(actions.isNotEmpty())ActionButton(t("交畀 ChatGPT／Codex 解說行程","Explain this itinerary with ChatGPT / Codex")){
                handoff=DirectionNavigator.prompt(goal,plan,actions,data.charts,Instant.now())
            }
            Field(reason,t("新觀察／重新擇時原因","Observation / reason to search again"),{reason=it},single=false)
            OutlinedButton(onClick={settings=true},modifier=Modifier.fillMaxWidth()){Text(t("按新情況重新擇時","Search from the new situation"))}
        }
        TextButton(onClick={sources=!sources}){Text(t("方法、依據及限制","Method, sources and limits"))}
        if(sources)CardBlock(t("古籍簡式・TRADITIONAL","Classical profile · TRADITIONAL")){
            Text(t("排盤：拆補轉盤、民用時區、子初換日、中五寄坤二。取用：一般出行用奇門相合；和合用人遁／玉女守門。","Chart: Chai Bu rotating plate, civil time, 23:00 day change, center hosted in Kun 2. General outings use Qi-Men combination; harmony uses Ren Dun / Jade Maiden."))
            Text(t("來源：《煙波釣叟歌》及《遁甲演義》卷二。明示排除：五不遇時、全盤星門伏反吟、門迫、三奇入墓、六儀擊刑、蓬芮，以及交節前後兩分鐘。","Sources: Yanbo Diaosou Ge and Dunjia Yanyi, volume 2. Explicit exclusions include conflicting hours, repeated/reversed plates, door pressure, wonder tombs, instrument punishment, Peng/Rui and solar-term margins."))
            Text(t("只採本干，寄干不自動當主干。未包含空亡、馬星、旺衰等全盤判斷；結果係本簡式候選，唔係全面最佳吉時。","Main stems only. Void, horse and seasonal-strength synthesis are not included; these are matches to a limited profile, not a comprehensive best time."))
            Text(t("沒有手勢、步數或返程吉時規則就不生成。出生資料未用於本方法。停留計時屬你安排，不是傳統效力。","No invented gestures, step counts or return times. This method does not use birth data. Stay timers are your own scheduling choice."))
            Text(t("不能保證心願或他人行為改變。定位只存手機；指南針唔係道路導航。","No guarantee of an outcome or another person's behavior. Location stays on the phone; a compass is not road navigation."))
            Text(DirectionRules.VERSION,style=MaterialTheme.typography.labelSmall)
        }
        if(plans.size>1){TextButton(onClick={history=!history}){Text(t("歷史行程","Itinerary history"))};if(history)plans.forEach{p->TextButton(onClick={shown=p.id;settings=false}){Text("v${p.version} · ${displayTime(p.createdAt,zone)} · ${p.reason}")}}}
        if(data.plans.any{it.goalId==goalId&&it.directionRequest==null})TextButton(onClick={legacy=true}){Text(t("查看舊版實用方案記錄","View legacy practical plans"))}
    }
    handoff?.let{AnalysisHandoff(it){handoff=null}}
}

@Composable private fun JourneyCard(a:ActionCandidate,chart:Chart,now:Instant,onAdvance:(String,GeoPoint?)->Unit,onJournal:(ActionCandidate)->Unit){
    val i=a.itinerary?:return;val p=chart.palaces.single{it.index==i.palace}
    var point by remember(a.id,i.phase){mutableStateOf<GeoPoint?>(null)}
    var capturedAt by remember(a.id,i.phase){mutableStateOf<Instant?>(null)}
    val target="${p.direction} ${Navigator.bearing(p.index)}°"
    val english=English.current
    val step=when(i.phase){
        0->if(english)"Wait for the departure window. Choose an accessible destination in direction $target from your fixed starting point. Identify the direction before leaving." else "等到出發時段，以你嘅出發位置為固定起點，揀一個喺${target}方向而且實際行到嘅地方。先辨認方位。"
        1->if(english)"Walk toward your chosen destination in direction $target along accessible roads. The arrow identifies a compass direction, not a safe road route." else "沿可通行道路前往你選定嘅${target}方位目的地。箭嘴只指方位，唔會識別道路、水面或障礙。"
        2->if(english)"You marked arrival. Stay for your chosen ${i.stayMinutes} minutes if convenient. No sourced gesture or step count is specified. You may return early." else "你已標記到達。按自己安排停留 ${i.stayMinutes} 分鐘；本方法冇另外指定手勢或步數。需要時可以提早返程。"
        3->if(english)"Return by a familiar route. No auspicious return time or post-return sitting direction has been calculated." else "沿熟悉路線返回起點。本方法未計算返程吉時，亦冇指定返到屋企要坐邊一方。"
        else->if(english)"Itinerary complete. Record what happened to your intention separately; finishing a walk does not mean the intention was fulfilled." else "行程已完成。另行記錄心願有咩變化；完成出行唔等於心願已達成。"
    }
    val phaseName=listOf(t("準備出發","Prepare"),t("前往目的地","On the way"),t("到場停留","Stay"),t("返回起點","Return"),t("行程完成","Complete"))[i.phase]
    CardBlock(t("已選行程 · ","Selected itinerary · ")+phaseName){
        Text(target,style=MaterialTheme.typography.headlineMedium)
        Text(departureTime(a.plannedStart,chart.input.timezone)+" → "+departureTime(a.plannedEnd,chart.input.timezone))
        LinearProgressIndicator(progress={i.phase/4f},modifier=Modifier.fillMaxWidth())
        if(i.travelMode=="VIEW")Text(t("只看方位模式：不需出行，亦不代表已完成古籍出行法。","View-only mode: no travel required; viewing is not performance of the traditional outing."))
        Text(step)
        if(i.phase<4)SpeakStep(step)
        if(i.phase<=1){
            DirectionCompass(i.palace,point?:i.origin)
            LocationButton(if(i.phase==0)t("定位起點（選用）","Locate starting point (optional)") else t("定位到場位置（選用）","Locate arrival (optional)")){point=it;capturedAt=Instant.now()}
            val origin=i.origin;val arrival=point
            if(origin!=null && arrival!=null && i.phase==1){
                val distance=DirectionNavigator.distance(origin,arrival);val bearing=DirectionNavigator.bearing(origin,arrival)
                Text(t("相對起點：約 ${distance.toInt()} 米・${bearing.toInt()}°","From start: about ${distance.toInt()} m · ${bearing.toInt()}°"))
                val uncertain=distance<=maxOf(30.0,(origin.accuracyMetres+arrival.accuracyMetres)*2.0)
                Text(if(uncertain)t("距離接近定位誤差，未能可靠確認方位。","Too close to the location error to confirm direction reliably.") else if(DirectionNavigator.withinSector(bearing,i.palace))t("量度位於所選八方區間；不代表已驗證道路或效果。","Measured inside the chosen sector; this does not verify access or an outcome.") else t("目前位置未落喺所選方位區間。","The current position is outside the selected sector."))
            }
        }
        if(i.phase in 1..3 && i.departedAt!=null){
            val budgetLeft=Duration.between(now,Instant.parse(i.departedAt).plusSeconds(i.timeBudgetMinutes*60L)).seconds
            Text(if(budgetLeft>0)t("行程預算尚餘 ${budgetLeft/60} 分鐘；請預留返程時間。","Trip budget: ${budgetLeft/60} minutes left; allow time to return.") else t("已超過你設定嘅行程預算，請按實際情況安全返回。","Your trip budget has elapsed. Return safely as circumstances allow."),style=MaterialTheme.typography.bodySmall)
        }
        if(i.phase==2 && i.arrivedAt!=null){
            val remaining=Duration.between(now,Instant.parse(i.arrivedAt).plusSeconds(i.stayMinutes*60L)).seconds.coerceAtLeast(0)
            Text(if(remaining>0)t("你安排嘅停留尚餘 ${remaining/60} 分 ${remaining%60} 秒","Your stay timer: ${remaining/60}m ${remaining%60}s") else t("你安排嘅停留時間已到，可準備返回。","Your stay timer has ended. You can return."),style=MaterialTheme.typography.titleMedium)
        }
        if(i.phase==0){
            when{now<Instant.parse(a.plannedStart)->Text(t("未到時段；到時再按出發。","Not yet in the window. Start when it opens."))
                now>=Instant.parse(a.plannedEnd)->ErrorText(t("出發時段已過，請重新擇時。","Departure window ended. Search again."))}
        } else if(now>=Instant.parse(a.plannedEnd) && i.phase<4)Text(t("原出發時段已完；呢度繼續記錄已開始嘅行程，唔會當舊盤係現在盤。","The departure window ended. This continues an already started journey; the old chart is not a current chart."),style=MaterialTheme.typography.bodySmall)
        if(i.phase<4 && i.travelMode=="WALK")ActionButton(listOf(t("開始出發","Start walking"),t("我已到達","I have arrived"),t("開始返程","Start return"),t("已返回・完成行程","Returned · finish"))[i.phase],enabled=i.phase>0 || (now>=Instant.parse(a.plannedStart)&&now<Instant.parse(a.plannedEnd))){
            val fresh=point.takeIf{capturedAt?.let{Duration.between(it,Instant.now()).seconds<120}==true}
            onAdvance(a.id,fresh)
        }
        OutlinedButton(onClick={onJournal(a)},modifier=Modifier.fillMaxWidth()){Text(t("記錄心願結果／觀察","Record intention outcome / observation"))}
        Text(t("盤：${chart.chart_id}","Chart: ${chart.chart_id}"),style=MaterialTheme.typography.labelSmall)
    }
}
