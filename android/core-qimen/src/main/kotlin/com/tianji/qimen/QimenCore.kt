package com.tianji.qimen
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
@Serializable data class Input(val instant_utc: String, val timezone: String, val latitude: Double? = null, val longitude: Double? = null)
@Serializable data class Ruleset(val id: String, val version: String, val source: String)
@Serializable data class Palace(val index: Int, val direction: String, val door: String?, val star: String?, val deity: String?, val heaven_stem: String?, val earth_stem: String?)
@Serializable data class Chart(val schema_version: Int = 1, val chart_id: String, val status: String = "BLOCKED", val input: Input, val ruleset: Ruleset, val calendar: Map<String,String>, val dun_type: String? = null, val ju_number: Int? = null, val palaces: List<Palace> = emptyList(), val derivation_steps: List<String>, val warnings: List<String>, val input_hash: String)
interface QimenRuleset { val metadata: Ruleset; fun calculate(input: Input): Chart }
object UnverifiedShiJia : QimenRuleset {
 override val metadata = Ruleset("shijia-zhuanpan-chaibu", "0.0.1-blocked", "Authoritative convention and independent fixtures required")
 override fun calculate(input: Input): Chart {
  val i=Instant.parse(input.instant_utc); val z=ZoneId.of(input.timezone)
  require((input.latitude==null)==(input.longitude==null)); input.latitude?.let{require(it.isFinite() && it in -90.0..90.0)}; input.longitude?.let{require(it.isFinite() && it in -180.0..180.0)}
  val normalized=input.copy(instant_utc=i.toString()); val hash=sha(Canonical.encodeToString(normalized)+"|"+metadata.id+"|"+metadata.version)
  return Chart(chart_id=UUID.nameUUIDFromBytes(hash.toByteArray()).toString(),input=normalized,ruleset=metadata,calendar=mapOf("civil_local_iso" to i.atZone(z).toString()),derivation_steps=listOf("UTC instant normalized; IANA civil timezone applied.","Traditional solar terms, stems, Dun, Ju and palace symbols NOT computed: ruleset validation gate closed."),warnings=listOf("NO_SCIENTIFIC_VALIDATION","GOLDEN_FIXTURES_0_OF_100","LOCATION_NOT_USED"),input_hash=hash)
 }
}
val Canonical = Json { encodeDefaults=true; explicitNulls=true }
fun sha(s:String)=MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it)}
class QimenCore(private val rulesets: List<QimenRuleset> = listOf(ShiJiaChaiBu, UnverifiedShiJia)) {
 private val cache=object: LinkedHashMap<String,Chart>(128,0.75f,true){ override fun removeEldestEntry(e:MutableMap.MutableEntry<String,Chart>?)=size>128 }
 @Synchronized fun calculateChart(instant:Instant, timezone:String, rulesetId:String=rulesets.first().metadata.id, latitude:Double?=null,longitude:Double?=null):Chart {
  val input=Input(instant.toString(),timezone,latitude,longitude); val key=Canonical.encodeToString(input)+rulesetId
  return cache.getOrPut(key){ rulesets.single{it.metadata.id==rulesetId}.calculate(input) }
 }
 fun canonical(chart:Chart)=Canonical.encodeToString(chart)
 fun batch(instants:List<Instant>,timezone:String)=instants.map{calculateChart(it,timezone)}
 fun compare(instant:Instant,timezone:String)=rulesets.map{calculateChart(instant,timezone,it.metadata.id)}
}
