package com.tianji.data

import android.content.Context
import com.tianji.domain.Codec
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSource
import java.io.IOException
import java.util.Base64
import java.util.concurrent.TimeUnit

@Serializable data class ChatGptGrant(val accessToken:String,val refreshToken:String,val expiresAt:Long)
data class ChatGptModel(val id:String,val label:String)
@Serializable data class DeviceLogin(val userCode:String,val deviceId:String,val expiresAt:Long,var intervalSeconds:Int,
    var authorizationCode:String="",var codeVerifier:String="") {
    val verificationUrl get()="https://auth.openai.com/codex/device"
}
class AccountFailure(val status:Int,message:String):IOException(message)
class LoginFailure(val stage:String,val recoverable:Boolean,message:String):IOException(message)
interface GrantStorage {fun read():ChatGptGrant?;fun write(grant:ChatGptGrant);fun clear()}
class EncryptedGrantStorage(context:Context):GrantStorage {
    private val vault=CredentialVault(context,"chatgpt")
    override fun read()=vault.read().takeIf{it.isNotBlank()}?.let{Codec.decodeFromString<ChatGptGrant>(it)}
    override fun write(grant:ChatGptGrant)=vault.save(Codec.encodeToString(grant))
    override fun clear()=vault.clear()
}
/** One serialized grant, so refresh rotation and logout cannot resurrect an old session. */
class ChatGptSession(private val storage:GrantStorage) {
    private val generation=java.util.concurrent.atomic.AtomicLong()
    fun invalidatePending():Long=generation.incrementAndGet()
    @Synchronized fun connected()=runCatching{storage.read()!=null}.getOrDefault(false)
    @Synchronized fun save(grant:ChatGptGrant,expected:Long){check(expected==generation.get()){"登入已取消。"};storage.write(grant)}
    @Synchronized fun logout(){generation.incrementAndGet();storage.clear()}
    @Synchronized fun access(client:ChatGptClient,rejected:String?=null):String {
        var grant=storage.read()?:throw IOException("請先登入 ChatGPT。")
        if(grant.expiresAt<=System.currentTimeMillis()/1000+120 || grant.accessToken==rejected){
            if(grant.refreshToken.isBlank())throw IOException("登入已過期，請重新登入。")
            grant=client.refresh(grant);storage.write(grant)
        }
        return grant.accessToken
    }
    fun <T> authenticated(client:ChatGptClient,block:(String)->T):T {
        val token=access(client)
        return try{block(token)}catch(e:AccountFailure){if(e.status!=401)throw e;block(access(client,token))}
    }
}

/** Native device-code protocol used by the owner's keyboard, implemented independently.
 * Public client ID is not a secret. Never route these grants to an editable API URL.
 * This compatibility flow is distinct from the newer dynamic-agent SIWC flow. */
class ChatGptClient(client:OkHttpClient=OkHttpClient()) {
    private val http=client.newBuilder().dispatcher(Dispatcher()).connectionPool(ConnectionPool())
        .connectTimeout(15,TimeUnit.SECONDS).readTimeout(60,TimeUnit.SECONDS).callTimeout(120,TimeUnit.SECONDS)
        .followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false).build()
    @Volatile private var cancelled=false
    fun cancel(){cancelled=true;http.dispatcher.cancelAll()}
    fun close(){cancel();http.connectionPool.evictAll();http.dispatcher.executorService.shutdown()}
    private fun request(url:String)=Request.Builder().url(url).header("User-Agent","TianJi/0.6.1").header("originator","tianji-navigator")
    private fun jsonBody(data:JsonObject)=data.toString().toRequestBody("application/json".toMediaType())
    private fun form(vararg args:Pair<String,String>)=FormBody.Builder().apply{args.forEach{add(it.first,it.second)}}.build()
    private fun call(request:Request):Response {check(!cancelled){"已取消。"};return http.newCall(request).execute()}
    private fun json(request:Request,limit:Long=64000):Pair<Int,JsonObject> = call(request).use {r->
        val source=r.body?.source()?:throw IOException("服務沒有回應。")
        source.request(limit+1);require(source.buffer.size<=limit){"服務回應過大。"}
        val text=source.readUtf8()
        r.code to (runCatching{Codec.parseToJsonElement(text).jsonObject}.getOrNull()?:JsonObject(emptyMap()))
    }
    fun begin():DeviceLogin {
        val(code,data)=json(request("$AUTH/api/accounts/deviceauth/usercode").post(jsonBody(buildJsonObject{put("client_id",CLIENT)})).build())
        if(code !in 200..299)throw AccountFailure(code,"未能開始 ChatGPT 登入（HTTP $code）。請確認帳戶安全設定已開啟裝置碼登入。")
        val user=data.string("user_code");val id=data.string("device_auth_id")
        require(user.length in 1..128 && id.length in 1..4096){"登入服務未提供有效代碼。"}
        return DeviceLogin(user,id,System.currentTimeMillis()+data.long("expires_in",900).coerceIn(30,900)*1000,data.long("interval",5).toInt().coerceIn(3,60))
    }
    private fun loginJson(request:Request,stage:String):Pair<Int,JsonObject> = try{json(request)}catch(e:IOException){
        throw LoginFailure(stage,true,if(stage=="POLL")"接收授權暫時中斷，會用同一代碼繼續。" else "網頁授權已收到，正在重新連接登入服務。")
    }
    fun poll(login:DeviceLogin,onAuthorization:(DeviceLogin)->Unit={}):ChatGptGrant? {
        if(System.currentTimeMillis()>=login.expiresAt)throw LoginFailure("EXPIRED",false,"登入代碼已過期，請重新登入。")
        if(login.authorizationCode.isBlank()){
            val(code,data)=loginJson(request("$AUTH/api/accounts/deviceauth/token").post(jsonBody(buildJsonObject{put("device_auth_id",login.deviceId);put("user_code",login.userCode)})).build(),"POLL")
            val error=data.string("error")
            if(error in listOf("access_denied","expired_token"))throw LoginFailure("DENIED",false,"授權被取消或已過期，請重新登入。")
            if(code==429 || error=="slow_down"){login.intervalSeconds=(login.intervalSeconds+5).coerceAtMost(60);return null}
            if(code in listOf(403,404) || error=="authorization_pending")return null
            if(code in 500..599)throw LoginFailure("POLL",true,"登入服務暫時未能回應，會保留代碼再試。")
            if(code !in 200..299)throw LoginFailure("POLL_HTTP_$code",false,failure(code).message!!)
            val authCode=data.string("authorization_code");val verifier=data.string("code_verifier")
            if(authCode.isBlank() || verifier.isBlank())throw LoginFailure("POLL_FORMAT",false,"登入服務未提供完整授權資料，請重新登入。")
            login.authorizationCode=authCode;login.codeVerifier=verifier
            onAuthorization(login) // Persist the exact exchange stage before another network call.
        }
        val(status,tokens)=loginJson(request("$AUTH/oauth/token").post(form("grant_type" to "authorization_code","client_id" to CLIENT,"code" to login.authorizationCode,"code_verifier" to login.codeVerifier,"redirect_uri" to "$AUTH/deviceauth/callback")).build(),"EXCHANGE")
        if(status in 500..599 || status==429)throw LoginFailure("EXCHANGE",true,"授權已收到，登入服務暫時繁忙，會繼續嘗試。")
        if(status !in 200..299)throw LoginFailure("EXCHANGE_HTTP_$status",false,if(status==400)"網頁授權已收到，但回傳憑證已失效，請重新登入。" else failure(status).message!!)
        return grant(tokens)
    }
    fun refresh(previous:ChatGptGrant):ChatGptGrant {
        val(code,tokens)=json(request("$AUTH/oauth/token").post(form("grant_type" to "refresh_token","client_id" to CLIENT,"refresh_token" to previous.refreshToken)).build())
        if(code !in 200..299)throw failure(code)
        return grant(tokens,previous)
    }
    private fun grant(data:JsonObject,previous:ChatGptGrant?=null):ChatGptGrant {
        val access=data.string("access_token");require(access.isNotBlank() && access.length<=32000){"登入未提供有效憑證。"}
        require(data.string("token_type","Bearer").equals("Bearer",true)){"登入憑證種類不受支援。"}
        val now=System.currentTimeMillis()/1000
        val expiry=if(data.long("expires_in")>0)now+data.long("expires_in") else claims(access).long("exp",now+3600)
        return ChatGptGrant(access,data.string("refresh_token",previous?.refreshToken?:""),expiry)
    }
    private fun accountRequest(url:String,token:String):Request.Builder {
        val b=request(url).header("Authorization","Bearer $token")
        val metadata=claims(token)["https://api.openai.com/auth"] as? JsonObject
        metadata?.string("chatgpt_account_id")?.takeIf{safeHeader(it)}?.let{b.header("ChatGPT-Account-ID",it)}
        metadata?.let{it.string("chatgpt_data_residency",it.string("chatgpt_compute_residency"))}?.takeIf{safeHeader(it)}?.let{b.header("x-openai-internal-codex-residency",it)}
        return b
    }
    fun models(token:String):List<ChatGptModel> {
        val(code,data)=json(accountRequest("$CODEX/models?client_version=0.156.1",token).build(),2_000_000)
        if(code !in 200..299)throw failure(code)
        val result=(data["models"] as? JsonArray).orEmpty().mapNotNull{element->
            val m=element as? JsonObject?:return@mapNotNull null
            val id=m.string("slug",m.string("id"));val visibility=m.string("visibility","list")
            if(id.isBlank() || id.length>200 || visibility!="list")null else ChatGptModel(id,m.string("display_name",id).take(200))
        }.distinctBy{it.id}
        require(result.isNotEmpty() && result.size<=3000){"帳戶未回傳可用模型，請稍後重試或重新登入。"}
        return result
    }
    fun complete(token:String,model:String,instruction:String,context:String):String {
        require(model.isNotBlank() && model.length<=200 && context.length<=120000)
        val body=buildJsonObject {
            put("model",model);put("store",false);put("stream",true);put("instructions",instruction)
            put("input",buildJsonArray{add(buildJsonObject{put("role","user");put("content",buildJsonArray{add(buildJsonObject{put("type","input_text");put("text",context)})})})})
        }
        return call(accountRequest("$CODEX/responses",token).header("Accept","text/event-stream").post(jsonBody(body)).build()).use {r->
            if(!r.isSuccessful)throw failure(r.code)
            parseStream(r.body?.source()?:throw IOException("AI 沒有回應。"))
        }
    }
    companion object {
        const val AUTH="https://auth.openai.com"
        const val CODEX="https://chatgpt.com/backend-api/codex"
        const val CLIENT="app_EMoamEEZ73f0CkXaXp7hrann"
        private fun safeHeader(value:String)=value.isNotBlank() && value.length<=256 && value.all{it.code in 33..126}
        internal fun claims(token:String):JsonObject=runCatching{Codec.parseToJsonElement(String(Base64.getUrlDecoder().decode(token.split('.')[1]),Charsets.UTF_8)).jsonObject}.getOrDefault(JsonObject(emptyMap()))
        internal fun failure(status:Int)=AccountFailure(status,when(status){
            401->"登入已失效，請重新登入 ChatGPT。"
            403->"帳戶或服務拒絕此請求；請檢查帳戶權限。"
            429->"ChatGPT 用量或頻率達上限，請稍後再試。"
            in 300..399->"服務要求轉址，已停止傳送登入資料。"
            in 500..599->"ChatGPT 暫時未能回覆，請稍後再試。"
            else->"未能完成 ChatGPT 請求，請重試或更新模型清單。"
        }+"（HTTP $status）")
        private fun outputText(response:JsonObject):String=(response["output"] as? JsonArray).orEmpty().joinToString(""){item->
            val o=item.jsonObject
            if(o.string("type")!="message")"" else (o["content"] as? JsonArray).orEmpty().joinToString(""){part->
                val p=part.jsonObject;require(p.string("type")!="refusal"){"AI 未有提供解讀。"};if(p.string("type")=="output_text")p.string("text") else ""
            }
        }
        internal fun parseStream(source:BufferedSource):String {
            val finished=sortedMapOf<Pair<Int,Int>,String>(compareBy({it.first},{it.second}))
            var total=0L;var eventData=StringBuilder();var refused=false
            fun remember(output:Int,content:Int,text:String){require(output in 0..4095 && content in 0..4095 && text.length<=10000);finished[output to content]=text;require(finished.values.sumOf{it.length}<=10000)}
            fun event():String? {
                val raw=eventData.toString().trim();eventData=StringBuilder();if(raw.isBlank() || raw=="[DONE]")return null
                val e=Codec.parseToJsonElement(raw).jsonObject
                when(e.string("type")){
                    "response.refusal.delta","response.refusal.done"->refused=true
                    "response.output_text.done"->remember(e.long("output_index").toInt(),e.long("content_index").toInt(),e.string("text"))
                    "response.content_part.done"->{val p=e["part"]!!.jsonObject;if(p.string("type")=="refusal")refused=true;if(p.string("type")=="output_text")remember(e.long("output_index").toInt(),e.long("content_index").toInt(),p.string("text"))}
                    "response.output_item.done"->{val item=e["item"]!!.jsonObject;if(item.string("type")=="message")(item["content"] as? JsonArray).orEmpty().forEachIndexed{i,p->if(p.jsonObject.string("type")=="refusal")refused=true;if(p.jsonObject.string("type")=="output_text")remember(e.long("output_index").toInt(),i,p.jsonObject.string("text"))}}
                    "response.failed","response.incomplete","error"->throw IOException("AI 未完成解讀，保留本機指引，請稍後再試。")
                    "response.completed"->{val r=e["response"]!!.jsonObject;require(r.string("status")=="completed" && !refused){"AI 未完成解讀。"};return outputText(r).ifBlank{finished.values.joinToString("")}.trim().also{require(it.isNotBlank() && it.length<=10000){"AI 回覆空白或過長。"}}}
                }
                return null
            }
            while(!source.exhausted()){
                val line=source.readUtf8LineStrict(128000);total+=line.toByteArray().size+1;require(total<=2_000_000){"AI 回覆過大。"}
                if(line.isEmpty()){event()?.let{return it}} else if(line.startsWith("data:")){eventData.append(line.substring(5).trimStart()).append('\n');require(eventData.length<=128000)}
            }
            event()?.let{return it};throw IOException("連線中斷，未採用不完整解讀。")
        }
    }
}
private fun JsonObject.string(key:String,default:String="")=(this[key] as? JsonPrimitive)?.contentOrNull?:default
private fun JsonObject.long(key:String,default:Long=0)=(this[key] as? JsonPrimitive)?.content?.toLongOrNull()?:default
