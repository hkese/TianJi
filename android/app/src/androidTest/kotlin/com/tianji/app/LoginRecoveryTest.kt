package com.tianji.app

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tianji.data.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Synthetic HTTPS only. Exercises the actual AppModel, Keystore and recovery path. */
@RunWith(AndroidJUnit4::class)
class LoginRecoveryTest {
 private val instrumentation=InstrumentationRegistry.getInstrumentation()
 private val app get()=instrumentation.targetContext.applicationContext as Application
 private lateinit var server:MockWebServer
 private lateinit var http:OkHttpClient
 private lateinit var repo:Repository
 private val stores=mutableListOf<ViewModelStore>()
 private fun <T> main(block:()->T):T {var value:Result<T>?=null;instrumentation.runOnMainSync{value=runCatching(block)};return value!!.getOrThrow()}
 private fun await(message:String,condition:()->Boolean){val end=SystemClock.elapsedRealtime()+20000;while(!condition()){check(SystemClock.elapsedRealtime()<end){message};SystemClock.sleep(10)}}
 private fun model():Pair<AppModel,ViewModelStore> = main {
  val store=ViewModelStore();stores+=store
  val vm=AppModel(app,repo,Preferences(app),CredentialVault(app,"auth-test-provider")){ChatGptClient(http)}
  store.put("auth",vm);vm to store
 }
 @Before fun setup(){
  EncryptedGrantStorage(app).clear();EncryptedPendingLoginStorage(app).clear();app.getSharedPreferences("intention-ai",0).edit().clear().commit()
  repo=Repository(Room.inMemoryDatabaseBuilder(app,TianJiDatabase::class.java).build())
  val cert=HeldCertificate.Builder().addSubjectAlternativeName("localhost").build()
  server=MockWebServer();server.useHttps(HandshakeCertificates.Builder().heldCertificate(cert).build().sslSocketFactory(),false);server.start()
  val trust=HandshakeCertificates.Builder().addTrustedCertificate(cert.certificate).build()
  http=OkHttpClient.Builder().sslSocketFactory(trust.sslSocketFactory(),trust.trustManager).addInterceptor{chain->
   val req=chain.request();check(req.url.host in setOf("auth.openai.com","chatgpt.com"))
   chain.proceed(req.newBuilder().url(server.url(req.url.encodedPath+(req.url.encodedQuery?.let{"?$it"}?:""))).build())
  }.build()
 }
 @After fun cleanup(){
  main{stores.forEach{it.clear()}};SystemClock.sleep(100)
  EncryptedGrantStorage(app).clear();EncryptedPendingLoginStorage(app).clear();app.getSharedPreferences("intention-ai",0).edit().clear().commit()
  repo.db.close();server.shutdown();http.dispatcher.executorService.shutdown();http.connectionPool.evictAll()
 }
 private fun tokens()=MockResponse().setBody("""{"access_token":"synthetic-access","refresh_token":"synthetic-refresh","expires_in":3600}""")
 private fun catalog()=MockResponse().setBody("""{"models":[{"slug":"synthetic-model","display_name":"Synthetic model","visibility":"list"}]}""")
 @Test fun browserAuthorizationSurvivesTemporaryPollingFailure(){
  val(vm,_)=model();await("Initial recovery must settle"){!vm.busy.value && !vm.chat.value.working}
  server.enqueue(MockResponse().setBody("""{"user_code":"TEST-CODE","device_auth_id":"synthetic-device","interval":3}"""))
  server.enqueue(MockResponse().setResponseCode(503))
  server.enqueue(MockResponse().setBody("""{"authorization_code":"synthetic-auth","code_verifier":"synthetic-verifier"}"""))
  server.enqueue(tokens());server.enqueue(catalog())
  main{vm.loginChatGpt()}
  await("Temporary failure must retain the code and retry"){vm.chat.value.loginStatus.contains("自動重試")}
  assertEquals("TEST-CODE",vm.chat.value.userCode);assertTrue(vm.chat.value.working);assertNotNull(EncryptedPendingLoginStorage(app).read())
  await("Same login must finish after network recovery"){vm.chat.value.connected && !vm.chat.value.working}
  assertEquals("synthetic-model",vm.chat.value.model);assertEquals("CHATGPT",vm.chat.value.mode);assertEquals("",vm.chat.value.error)
  assertNull(EncryptedPendingLoginStorage(app).read())
  assertEquals("/api/accounts/deviceauth/usercode",server.takeRequest().path)
  val failed=server.takeRequest();val success=server.takeRequest();assertEquals(failed.body.readUtf8(),success.body.readUtf8())
  assertEquals("/oauth/token",server.takeRequest().path);assertTrue(server.takeRequest().path!!.startsWith("/backend-api/codex/models"))
 }
 @Test fun recreatedModelResumesExchangeAndKeepsLoginWhenCatalogFails(){
  EncryptedPendingLoginStorage(app).write(DeviceLogin("TEST-CODE","synthetic-device",System.currentTimeMillis()+60000,3,"synthetic-auth","synthetic-verifier"))
  server.enqueue(MockResponse().setResponseCode(503))
  val(first,store)=model()
  await("Exchange interruption must remain recoverable"){first.chat.value.loginStatus.contains("EXCHANGE")}
  assertEquals(0L,first.chat.value.browserRequest)
  main{store.clear()}
  assertNotNull(EncryptedPendingLoginStorage(app).read())
  server.enqueue(tokens());server.enqueue(MockResponse().setResponseCode(503))
  val(second,_)=model()
  await("Restored authorization must be saved independently of catalog"){second.chat.value.connected && !second.chat.value.working}
  assertTrue(second.chat.value.error.contains("MODELS"));assertEquals("CHATGPT",second.chat.value.mode)
  assertNotNull(EncryptedGrantStorage(app).read());assertNull(EncryptedPendingLoginStorage(app).read())
  assertEquals("/oauth/token",server.takeRequest().path);assertEquals("/oauth/token",server.takeRequest().path)
  assertTrue(server.takeRequest().path!!.startsWith("/backend-api/codex/models"))
  server.enqueue(catalog());main{second.refreshChatModels()}
  await("Catalog retry must not require login again"){!second.chat.value.working && second.chat.value.model=="synthetic-model"}
  assertTrue(second.chat.value.connected);assertEquals("",second.chat.value.error)
 }
}
