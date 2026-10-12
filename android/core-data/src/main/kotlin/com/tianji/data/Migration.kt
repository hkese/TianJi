package com.tianji.data
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tianji.domain.*
import com.tianji.qimen.*
import kotlinx.serialization.json.*
import java.time.Instant
fun verifySnapshot(s:ChartSnapshot) {
 if(s.rulesetVersion=="0.0.0-blocked") {
  val chart=Json.parseToJsonElement(s.chartJson).jsonObject
  require(chart["schema_version"]?.jsonPrimitive?.content=="1.0")
  require(chart["status"]?.jsonPrimitive?.content=="BLOCKED" && chart["palaces"]?.jsonArray?.isEmpty()==true)
  require(chart["ju_number"]==JsonNull && chart["dun_type"]==JsonNull)
  val input=chart.getValue("input").jsonObject
  require(input.getValue("instant_utc").jsonPrimitive.content==s.instantUtc && input.getValue("timezone").jsonPrimitive.content==s.timezone)
  val canonicalInput=buildJsonObject { put("instant_utc",s.instantUtc);put("timezone",s.timezone);put("latitude",input["latitude"]?:JsonNull);put("longitude",input["longitude"]?:JsonNull) }.toString()
  require(s.rulesetId=="shijia-zhuanpan-chaibu" && s.inputHash==sha(canonicalInput+"|"+s.rulesetId+"|"+s.rulesetVersion) && s.id==s.inputHash)
  require(chart.getValue("chart_id").jsonPrimitive.content==s.id)
  val rules=chart.getValue("ruleset").jsonObject;require(rules.getValue("id").jsonPrimitive.content==s.rulesetId && rules.getValue("version").jsonPrimitive.content==s.rulesetVersion)
 } else {
  val core=QimenCore();val c=core.calculateChart(Instant.parse(s.instantUtc),s.timezone,s.rulesetId)
  require(c.chart_id==s.id && c.ruleset.version==s.rulesetVersion && c.input_hash==s.inputHash && core.canonical(c)==s.chartJson){"Unsupported or tampered chart"}
 }
}
val MIGRATION_1_2=object:Migration(1,2){override fun migrate(db:SupportSQLiteDatabase){
 db.execSQL("CREATE TABLE IF NOT EXISTS `goals` (`id` TEXT NOT NULL, `payload` TEXT NOT NULL, PRIMARY KEY(`id`))")
 db.execSQL("CREATE TABLE IF NOT EXISTS `milestones` (`id` TEXT NOT NULL, `goalId` TEXT NOT NULL, `payload` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`goalId`) REFERENCES `goals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
 db.execSQL("CREATE INDEX IF NOT EXISTS `index_milestones_goalId` ON `milestones` (`goalId`)")
 db.execSQL("CREATE TABLE IF NOT EXISTS `charts` (`id` TEXT NOT NULL, `payload` TEXT NOT NULL, PRIMARY KEY(`id`))")
 db.execSQL("CREATE TABLE IF NOT EXISTS `plans` (`id` TEXT NOT NULL, `goalId` TEXT NOT NULL, `payload` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`goalId`) REFERENCES `goals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
 db.execSQL("CREATE INDEX IF NOT EXISTS `index_plans_goalId` ON `plans` (`goalId`)")
 db.execSQL("CREATE TABLE IF NOT EXISTS `actions` (`id` TEXT NOT NULL, `planVersionId` TEXT NOT NULL, `chartId` TEXT NOT NULL, `payload` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`planVersionId`) REFERENCES `plans`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`chartId`) REFERENCES `charts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
 db.execSQL("CREATE INDEX IF NOT EXISTS `index_actions_planVersionId` ON `actions` (`planVersionId`)")
 db.execSQL("CREATE INDEX IF NOT EXISTS `index_actions_chartId` ON `actions` (`chartId`)")
 db.execSQL("CREATE TABLE IF NOT EXISTS `journal` (`id` TEXT NOT NULL, `actionId` TEXT NOT NULL, `payload` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`actionId`) REFERENCES `actions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
 db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_actionId` ON `journal` (`actionId`)")
 db.execSQL("CREATE TABLE IF NOT EXISTS `consents` (`id` TEXT NOT NULL, `scope` TEXT NOT NULL, `grantedAt` TEXT NOT NULL, `revokedAt` TEXT, PRIMARY KEY(`id`))")
 db.execSQL("CREATE TABLE IF NOT EXISTS `providers` (`id` TEXT NOT NULL, `baseUrl` TEXT NOT NULL, `modelName` TEXT NOT NULL, `credentialAlias` TEXT NOT NULL, PRIMARY KEY(`id`))")
 fun records(table:String):List<JsonObject> { val rows=mutableListOf<JsonObject>();db.query("SELECT payload FROM `$table`").use{c->while(c.moveToNext())rows+=Json.parseToJsonElement(c.getString(0)).jsonObject};return rows }
 fun value(o:JsonObject,k:String)=o.getValue(k).jsonPrimitive.content
 fun nullable(o:JsonObject,k:String)=o[k]?.jsonPrimitive?.contentOrNull
 records("Goal").forEach{o->val old=o.getValue("constraints").jsonObject;val updated=JsonObject(o.toMutableMap().apply{put("constraints",buildJsonObject{put("minutes",old["maxMinutes"]?:JsonPrimitive(20));put("maxCost",old["budget"]?:JsonPrimitive(0.0));put("notes",old["notes"]?:JsonPrimitive(""))})});db.execSQL("INSERT INTO goals(id,payload) VALUES(?,?)",arrayOf(value(o,"id"),updated.toString()))}
 records("Milestone").forEach{o->db.execSQL("INSERT INTO milestones(id,goalId,payload) VALUES(?,?,?)",arrayOf(value(o,"id"),value(o,"goalId"),o.toString()))}
 records("ChartSnapshot").forEach{o->db.execSQL("INSERT INTO charts(id,payload) VALUES(?,?)",arrayOf(value(o,"id"),o.toString()))}
 records("PlanVersion").forEach{o->db.execSQL("INSERT INTO plans(id,goalId,payload) VALUES(?,?,?)",arrayOf(value(o,"id"),value(o,"goalId"),o.toString()))}
 records("ActionCandidate").forEach{o->val updated=JsonObject(o.toMutableMap().apply{put("kind",JsonPrimitive("LEGACY_PRACTICAL_STEP"));put("selected",JsonPrimitive(nullable(o,"status")=="SELECTED"))});db.execSQL("INSERT INTO actions(id,planVersionId,chartId,payload) VALUES(?,?,?,?)",arrayOf(value(o,"id"),value(o,"planVersionId"),value(o,"chartId"),updated.toString()))}
 records("JournalEntry").forEach{o->db.execSQL("INSERT INTO journal(id,actionId,payload) VALUES(?,?,?)",arrayOf(value(o,"id"),value(o,"actionId"),o.toString()))}
 records("Consent").forEach{o->db.execSQL("INSERT INTO consents(id,scope,grantedAt,revokedAt) VALUES(?,?,?,?)",arrayOf(value(o,"id"),value(o,"scope"),value(o,"grantedAt"),nullable(o,"revokedAt")))}
 records("AiProviderConfig").forEach{o->db.execSQL("INSERT INTO providers(id,baseUrl,modelName,credentialAlias) VALUES(?,?,?,?)",arrayOf(value(o,"id"),value(o,"baseUrl"),value(o,"modelName"),value(o,"credentialAlias")))}
 listOf("JournalEntry","ActionCandidate","PlanVersion","Milestone","ChartSnapshot","Goal","Consent","AiProviderConfig").forEach{db.execSQL("DROP TABLE `$it`")}
}}
