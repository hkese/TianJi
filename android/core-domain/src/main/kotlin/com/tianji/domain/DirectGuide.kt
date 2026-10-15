package com.tianji.domain

import com.tianji.qimen.Chart
import com.tianji.qimen.DirectionRules
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class AvoidDirection(val palace:Int,val direction:String,val reasons:List<String>)
data class DirectGuide(val state:String,val direction:String,val bearing:Int,val window:String,val avoid:List<AvoidDirection>) {
    companion object {
        const val DAILY_INTENTION="今日趨吉避凶"
        fun intention(text:String)=text.trim().ifBlank{DAILY_INTENTION}
        fun from(action:ActionCandidate,chart:Chart,now:Instant,english:Boolean=false):DirectGuide {
            require(action.chartId==chart.chart_id)
            val trip=requireNotNull(action.itinerary);trip.validate()
            val decisions=DirectionRules.decisions(chart,trip.purpose)
            val chosen=decisions.single{it.palace==trip.palace}
            require(chosen.matched==trip.ruleIds && chosen.excluded.isEmpty())
            val start=Instant.parse(action.plannedStart);val end=Instant.parse(action.plannedEnd);require(end>start)
            val state=when{trip.phase==4->"DONE";trip.phase>0->"ACTIVE";now>=end->"EXPIRED";now<start->"WAIT";else->"READY"}
            val avoid=if(now>=end || trip.phase==4)emptyList() else decisions.filter{it.excluded.isNotEmpty()}.map { d->
                AvoidDirection(d.palace,chart.palaces.single{it.index==d.palace}.direction,d.excluded)
            }
            return DirectGuide(state,chart.palaces.single{it.index==trip.palace}.direction,Navigator.bearing(trip.palace),window(start,end,now,chart.input.timezone,english),avoid)
        }
        internal fun window(start:Instant,end:Instant,now:Instant,timezone:String,english:Boolean):String {
            val zone=ZoneId.of(timezone);val today=now.atZone(zone).toLocalDate()
            val offsetChanged=start.atZone(zone).offset!=end.atZone(zone).offset
            fun format(at:Instant):String {
                val t=at.atZone(zone)
                val day=when(t.toLocalDate()){
                    today->if(english)"Today"else"今日"
                    today.plusDays(1)->if(english)"Tomorrow"else"聽日"
                    else->t.format(DateTimeFormatter.ofPattern(if(english)"M/d"else"M月d日"))
                }
                return "$day ${t.format(DateTimeFormatter.ofPattern("HH:mm"))}"+if(offsetChanged)" (${t.offset})"else""
            }
            return format(start)+(if(english)" to "else" 至 ")+format(end)
        }
    }
}
