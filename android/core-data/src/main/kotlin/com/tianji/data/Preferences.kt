package com.tianji.data
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.time.ZoneId
private val Context.preferences by preferencesDataStore("preferences")
data class Settings(val onboarded:Boolean=false,val english:Boolean=false,val theme:String="system",val timezone:String="Asia/Hong_Kong",val reminders:Boolean=false)
class Preferences(private val context:Context){
 private val done=booleanPreferencesKey("onboarded");private val en=booleanPreferencesKey("english");private val theme=stringPreferencesKey("theme");private val zone=stringPreferencesKey("timezone");private val reminders=booleanPreferencesKey("reminders")
 val flow=context.preferences.data.map{Settings(it[done]?:false,it[en]?:false,it[theme]?:"system",it[zone]?:"Asia/Hong_Kong",it[reminders]?:false)}
 suspend fun save(s:Settings){ZoneId.of(s.timezone);require(s.theme in listOf("system","light","dark"));context.preferences.edit{it[done]=s.onboarded;it[en]=s.english;it[theme]=s.theme;it[zone]=s.timezone;it[reminders]=s.reminders}}
}
