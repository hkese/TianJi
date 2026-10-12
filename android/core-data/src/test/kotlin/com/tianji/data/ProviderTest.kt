package com.tianji.data
import com.tianji.domain.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import okhttp3.tls.*
import org.junit.Assert.*
import org.junit.Test
class ProviderTest {
 private fun runServer(block:(MockWebServer,AiClient)->Unit){
  val cert=HeldCertificate.Builder().addSubjectAlternativeName("localhost").build()
  val serverCert=HandshakeCertificates.Builder().heldCertificate(cert).build()
  val clientCert=HandshakeCertificates.Builder().addTrustedCertificate(cert.certificate).build()
  val server=MockWebServer();server.useHttps(serverCert.sslSocketFactory(),false);server.start()
  try{block(server,AiClient(OkHttpClient.Builder().sslSocketFactory(clientCert.sslSocketFactory(),clientCert.trustManager).followRedirects(false).build()))}finally{server.shutdown()}
 }
 @Test fun httpsConnectionTest(){runServer{s,client->s.enqueue(MockResponse().setBody("{\"data\":[]}"));client.test(AiProviderConfig(baseUrl=s.url("/v1").toString()),"test-only-key");val req=s.takeRequest();assertEquals("/v1/models",req.path);assertEquals("Bearer test-only-key",req.getHeader("Authorization"))}}
 @Test fun failureIsNotSuccessfulConnection(){runServer{s,client->s.enqueue(MockResponse().setResponseCode(401).setBody("private provider error"));try{client.test(AiProviderConfig(baseUrl=s.url("/v1").toString()),"");fail("Expected failure")}catch(e:IllegalStateException){assertFalse(e.message!!.contains("private provider error"))}}}
}
