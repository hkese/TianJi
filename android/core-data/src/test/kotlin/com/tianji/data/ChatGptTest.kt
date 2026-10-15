package com.tianji.data

import com.tianji.domain.Codec
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.mockwebserver.*
import okhttp3.tls.*
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test

class ChatGptTest {
 private fun server(block:(MockWebServer,ChatGptClient)->Unit){
  val cert=HeldCertificate.Builder().addSubjectAlternativeName("localhost").build()
  val s=MockWebServer();s.useHttps(HandshakeCertificates.Builder().heldCertificate(cert).build().sslSocketFactory(),false);s.start()
  val trust=HandshakeCertificates.Builder().addTrustedCertificate(cert.certificate).build()
  val http=OkHttpClient.Builder().sslSocketFactory(trust.sslSocketFactory(),trust.trustManager).addInterceptor{chain->
   val req=chain.request();assertTrue(req.url.host in setOf("auth.openai.com","chatgpt.com"));assertEquals("https",req.url.scheme)
   chain.proceed(req.newBuilder().url(s.url(req.url.encodedPath+(req.url.encodedQuery?.let{"?$it"}?:""))).build())
  }.build();val client=ChatGptClient(http)
  try{block(s,client)}finally{client.close();s.shutdown()}
 }
 private class Memory:GrantStorage {var value:ChatGptGrant?=null;override fun read()=value;override fun write(grant:ChatGptGrant){value=grant};override fun clear(){value=null}}
 private fun tokens(access:String="test-access",refresh:String="test-refresh")="""{"access_token":"$access","refresh_token":"$refresh","expires_in":3600,"token_type":"Bearer"}"""
 private fun catalog()="""{"models":[{"slug":"test-model","display_name":"Test model","visibility":"list"},{"slug":"hidden","visibility":"hide"}]}"""
 @Test fun deviceCodePendingExchangeAndFixedOrigins(){server{s,c->
  s.enqueue(MockResponse().setBody("""{"user_code":"ABCD-EFGH","device_auth_id":"synthetic-device","interval":"3"}"""))
  val d=c.begin();assertEquals("https://auth.openai.com/codex/device",d.verificationUrl);assertEquals(3,d.intervalSeconds)
  assertEquals("/api/accounts/deviceauth/usercode",s.takeRequest().path)
  s.enqueue(MockResponse().setResponseCode(403));assertNull(c.poll(d));s.takeRequest()
  s.enqueue(MockResponse().setBody("""{"authorization_code":"test-code","code_verifier":"test-verifier"}"""));s.enqueue(MockResponse().setBody(tokens()))
  assertEquals("test-access",c.poll(d)!!.accessToken)
  assertEquals("/api/accounts/deviceauth/token",s.takeRequest().path)
  val exchange=s.takeRequest();assertEquals("/oauth/token",exchange.path);assertTrue(exchange.body.readUtf8().contains("code_verifier=test-verifier"))
 }}
 @Test fun slowdownDenialExpiryAndCancellation(){server{s,c->
  val d=DeviceLogin("test","device",System.currentTimeMillis()+60000,3)
  s.enqueue(MockResponse().setResponseCode(429));assertNull(c.poll(d));assertEquals(8,d.intervalSeconds)
  s.enqueue(MockResponse().setResponseCode(403).setBody("""{"error":"access_denied"}"""));assertTrue(runCatching{c.poll(d)}.isFailure)
  assertTrue(runCatching{c.poll(d.copy(expiresAt=0))}.isFailure)
  c.cancel();assertTrue(runCatching{c.begin()}.isFailure);assertEquals(2,s.requestCount)
 }}
 @Test fun pollServerFailureIsRecoverableWithoutReplacingCode(){server{s,c->
  val d=DeviceLogin("test-code","test-device",System.currentTimeMillis()+60000,3)
  s.enqueue(MockResponse().setResponseCode(503).setBody("private body"))
  val error=runCatching{c.poll(d)}.exceptionOrNull() as LoginFailure
  assertTrue(error.recoverable);assertEquals("POLL",error.stage);assertFalse(error.message!!.contains("private body"))
  s.enqueue(MockResponse().setResponseCode(403));assertNull(c.poll(d));assertEquals("",d.authorizationCode)
  assertEquals(s.takeRequest().body.readUtf8(),s.takeRequest().body.readUtf8())
 }}
 @Test fun droppedPollingConnectionCanBeRetriedWithSameDevice(){server{s,c->
  val d=DeviceLogin("test-code","test-device",System.currentTimeMillis()+60000,3)
  s.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
  val error=runCatching{c.poll(d)}.exceptionOrNull() as LoginFailure
  assertTrue(error.recoverable);assertEquals("POLL",error.stage)
  s.enqueue(MockResponse().setResponseCode(403));assertNull(c.poll(d))
  assertEquals(s.takeRequest().body.readUtf8(),s.takeRequest().body.readUtf8())
 }}
 @Test fun exchangeFailureResumesFromSavedAuthorizationInsteadOfPollingAgain(){server{s,c->
  val d=DeviceLogin("test","device",System.currentTimeMillis()+60000,3)
  s.enqueue(MockResponse().setBody("""{"authorization_code":"saved-code","code_verifier":"saved-verifier"}"""))
  s.enqueue(MockResponse().setResponseCode(503))
  var saved:DeviceLogin?=null
  val error=runCatching{c.poll(d){saved=it.copy()}}.exceptionOrNull() as LoginFailure
  assertEquals("EXCHANGE",error.stage);assertTrue(error.recoverable);assertEquals("saved-code",saved!!.authorizationCode)
  s.enqueue(MockResponse().setBody(tokens()))
  assertEquals("test-access",c.poll(saved!!)!!.accessToken)
  assertEquals("/api/accounts/deviceauth/token",s.takeRequest().path)
  assertEquals("/oauth/token",s.takeRequest().path);assertEquals("/oauth/token",s.takeRequest().path)
 }}
 @Test fun redirectsAndPrivateErrorBodiesAreNotUsed(){server{s,c->
  s.enqueue(MockResponse().setResponseCode(302).setHeader("Location","https://unrelated.invalid/steal").setBody("secret-example"))
  val e=runCatching{c.models("test-token")}.exceptionOrNull()!!;assertFalse(e.message.orEmpty().contains("secret-example"));assertEquals(1,s.requestCount)
 }}
 @Test fun account401RefreshesOnceAndRotatesToken(){server{s,c->
  val memory=Memory();val session=ChatGptSession(memory);session.save(ChatGptGrant("old","refresh-old",Long.MAX_VALUE),session.invalidatePending())
  s.enqueue(MockResponse().setResponseCode(401));s.enqueue(MockResponse().setBody(tokens("new","refresh-new")));s.enqueue(MockResponse().setBody(catalog()))
  val models=session.authenticated(c){c.models(it)};assertEquals(listOf("test-model"),models.map{it.id});assertEquals("refresh-new",memory.value!!.refreshToken)
  assertEquals("Bearer old",s.takeRequest().getHeader("Authorization"));assertEquals("/oauth/token",s.takeRequest().path);assertEquals("Bearer new",s.takeRequest().getHeader("Authorization"))
 }}
 @Test fun second401AndQuotaDoNotLoop(){server{s,c->
  val memory=Memory();val session=ChatGptSession(memory);session.save(ChatGptGrant("old","refresh",Long.MAX_VALUE),session.invalidatePending())
  s.enqueue(MockResponse().setResponseCode(401));s.enqueue(MockResponse().setBody(tokens()));s.enqueue(MockResponse().setResponseCode(401))
  assertTrue(runCatching{session.authenticated(c){c.models(it)}}.exceptionOrNull() is AccountFailure);assertEquals(3,s.requestCount)
  s.enqueue(MockResponse().setResponseCode(429));assertTrue(runCatching{session.authenticated(c){c.models(it)}}.isFailure);assertEquals(4,s.requestCount)
 }}
 @Test fun logoutInvalidatesPendingGrant(){val m=Memory();val session=ChatGptSession(m);val ticket=session.invalidatePending();session.logout();assertTrue(runCatching{session.save(ChatGptGrant("x","y",999),ticket)}.isFailure);assertNull(m.value)}
 private fun event(type:String,vararg fields:Pair<String,JsonElement>)="data: "+buildJsonObject{put("type",type);fields.forEach{put(it.first,it.second)}}+"\n\n"
 private fun complete(output:JsonArray=JsonArray(emptyList()))=event("response.completed","response" to buildJsonObject{put("status","completed");put("output",output)})
 @Test fun sparseCompletionAcceptsFinishedPartsOnly(){
  val done=event("response.output_text.done","output_index" to JsonPrimitive(0),"content_index" to JsonPrimitive(0),"text" to JsonPrimitive("完整解讀"))
  assertEquals("完整解讀",ChatGptClient.parseStream(Buffer().writeUtf8(done+done+complete())))
  assertTrue(runCatching{ChatGptClient.parseStream(Buffer().writeUtf8(done))}.isFailure)
 }
 @Test fun deltasFailureAndRefusalNeverBecomeACompletedAnswer(){
  val partial=event("response.output_text.delta","delta" to JsonPrimitive("partial"))
  assertTrue(runCatching{ChatGptClient.parseStream(Buffer().writeUtf8(partial+complete()))}.isFailure)
  assertTrue(runCatching{ChatGptClient.parseStream(Buffer().writeUtf8(event("response.failed")))}.isFailure)
  assertTrue(runCatching{ChatGptClient.parseStream(Buffer().writeUtf8(event("response.refusal.done")+complete()))}.isFailure)
 }
 @Test fun completedSnapshotIsAuthoritative(){
  val output=buildJsonArray{add(buildJsonObject{put("type","message");put("content",buildJsonArray{add(buildJsonObject{put("type","output_text");put("text","final")})})})}
  assertEquals("final",ChatGptClient.parseStream(Buffer().writeUtf8(complete(output))))
 }
 @Test fun requestUsesSelfIdentityAndDoesNotStoreContext(){server{s,c->
  val output=buildJsonArray{add(buildJsonObject{put("type","message");put("content",buildJsonArray{add(buildJsonObject{put("type","output_text");put("text","done")})})})}
  s.enqueue(MockResponse().setHeader("Content-Type","text/event-stream").setBody(complete(output)))
  assertEquals("done",c.complete("synthetic-token","test-model","instruction","synthetic context"))
  val req=s.takeRequest();assertEquals("/backend-api/codex/responses",req.path);assertEquals("tianji-navigator",req.getHeader("originator"));assertEquals("TianJi/0.6.1",req.getHeader("User-Agent"))
  val body=Codec.parseToJsonElement(req.body.readUtf8()).jsonObject;assertEquals(JsonPrimitive(false),body["store"]);assertEquals(JsonPrimitive(true),body["stream"]);assertFalse(body.containsKey("tools"))
 }}
}
