package com.tianji.data

import com.tianji.domain.Codec
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class DeviceAuthorizationTest {
 private class Memory:PendingLoginStorage {
  var text=""
  override fun read()=text.takeIf{it.isNotBlank()}?.let{Codec.decodeFromString<DeviceLogin>(it)}
  override fun write(login:DeviceLogin){text=Codec.encodeToString(DeviceLogin.serializer(),login)}
  override fun clear(){text=""}
 }
 @Test fun transientPollingFailureKeepsSameLoginUntilSuccess()=runBlocking {
  var clock=0L;val login=DeviceLogin("synthetic-code","synthetic-device",60000,3);var attempts=0
  val status=mutableListOf<String>();val pauses=mutableListOf<Long>()
  val expected=ChatGptGrant("synthetic","refresh",999)
  val result=DeviceAuthorization.await(login,{
   attempts++
   when(attempts){1->throw LoginFailure("POLL",true,"temporary");2->null;else->expected}
  },{status+=it},{clock},{ms->pauses+=ms;clock+=ms})
  assertEquals(expected,result);assertEquals(3,attempts);assertEquals(listOf(3000L,3000L),pauses)
  assertTrue(status.first().contains("自動重試"));assertEquals("synthetic-device",login.deviceId)
 }
 @Test fun retriesAreBoundedByExpiryAndHonorSlowdown()=runBlocking {
  var clock=0L;var attempts=0;val login=DeviceLogin("code","device",13000,8);val waits=mutableListOf<Long>()
  val error=runCatching{DeviceAuthorization.await(login,{attempts++;throw LoginFailure("POLL",true,"temporary")},{},{clock},{ms->waits+=ms;clock+=ms})}.exceptionOrNull()
  assertEquals("EXPIRED",(error as LoginFailure).stage);assertEquals(2,attempts);assertEquals(listOf(8000L,5000L),waits)
 }
 @Test fun denialAndCoroutineCancellationAreNeverRetried()=runBlocking {
  var attempts=0;val login=DeviceLogin("code","device",Long.MAX_VALUE,3)
  val denial=runCatching{DeviceAuthorization.await(login,{attempts++;throw LoginFailure("DENIED",false,"denied")},{},pause={fail("Should not pause")})}.exceptionOrNull()
  assertEquals("DENIED",(denial as LoginFailure).stage);assertEquals(1,attempts)
  val cancelled=runCatching{DeviceAuthorization.await(login,{throw CancellationException("cancelled")},{},pause={fail("Should not pause")})}.exceptionOrNull()
  assertTrue(cancelled is CancellationException)
 }
 @Test fun recreationRestoresExactExchangeStageAndCancelInvalidatesOldWrites(){
  val memory=Memory();val first=DeviceLoginRecovery(memory){1000}
  val login=DeviceLogin("code","device",10000,3,"authorization","verifier")
  val old=first.start(login)
  val second=DeviceLoginRecovery(memory){2000};val resumed=second.resume()!!
  assertEquals("authorization",resumed.first.authorizationCode);assertEquals("verifier",resumed.first.codeVerifier)
  second.clear(resumed.second);assertNull(second.resume())
  first.clear(old);assertTrue(runCatching{first.remember(login,old)}.exceptionOrNull() is CancellationException)
 }
 @Test fun expiredRecoveryIsDiscardedAndStaleCancellationCannotDeleteNewAttempt(){
  val m=Memory();val recovery=DeviceLoginRecovery(m){2000}
  val old=recovery.start(DeviceLogin("one","first",10000,3))
  val current=recovery.start(DeviceLogin("two","second",10000,3))
  recovery.clear(old);recovery.remember(DeviceLogin("two","second",10000,8),current)
  assertEquals("second",recovery.resume()!!.first.deviceId)
  assertNull(DeviceLoginRecovery(m){10001}.resume());assertEquals("",m.text)
 }
}
