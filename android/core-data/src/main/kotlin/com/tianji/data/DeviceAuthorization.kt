package com.tianji.data

import android.content.Context
import com.tianji.domain.Codec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.encodeToString

interface PendingLoginStorage {
    fun read():DeviceLogin?
    fun write(login:DeviceLogin)
    fun clear()
}

class EncryptedPendingLoginStorage(context:Context):PendingLoginStorage {
    private val vault=CredentialVault(context,"chatgpt-pending")
    override fun read()=vault.read().takeIf{it.isNotBlank()}?.let{Codec.decodeFromString<DeviceLogin>(it)}
    override fun write(login:DeviceLogin)=vault.save(Codec.encodeToString(login))
    override fun clear()=vault.clear()
}

/** Only the current login may update its encrypted recovery record. No network under this lock. */
class DeviceLoginRecovery(private val storage:PendingLoginStorage,private val now:()->Long={System.currentTimeMillis()}) {
    private var generation=0L
    @Synchronized fun start(login:DeviceLogin):Long {
        require(login.expiresAt>now());generation++;storage.write(login);return generation
    }
    @Synchronized fun resume():Pair<DeviceLogin,Long>? {
        val login=storage.read()?:return null
        if(login.expiresAt<=now()){generation++;storage.clear();return null}
        generation++;return login to generation
    }
    @Synchronized fun remember(login:DeviceLogin,ticket:Long) {
        if(ticket!=generation)throw CancellationException("登入已取消。")
        storage.write(login)
    }
    @Synchronized fun clear(ticket:Long?=null) {
        if(ticket!=null && ticket!=generation)return
        generation++;storage.clear()
    }
}

object DeviceAuthorization {
    /** Retry only declared temporary failures until this device code expires. */
    suspend fun await(login:DeviceLogin,poll:suspend ()->ChatGptGrant?,onStatus:(String)->Unit,
        now:()->Long={System.currentTimeMillis()},pause:suspend (Long)->Unit={delay(it)}):ChatGptGrant {
        var failures=0
        while(true){
            currentCoroutineContext().ensureActive()
            if(now()>=login.expiresAt)throw LoginFailure("EXPIRED",false,"登入代碼已過期，請重新登入。")
            var retry=false
            try{
                val grant=poll();currentCoroutineContext().ensureActive()
                if(grant!=null)return grant
                failures=0;onStatus("等緊 OpenAI 授權；網頁顯示已登入後，返天機就得。")
            }catch(e:LoginFailure){
                if(!e.recoverable)throw e
                currentCoroutineContext().ensureActive();failures=(failures+1).coerceAtMost(4);retry=true
                onStatus("${e.message}（${e.stage}，自動重試中）")
            }
            val backoff=if(retry)(3000L shl (failures-1)).coerceAtMost(30000L)else 0L
            val interval=maxOf(login.intervalSeconds*1000L,backoff)
            val remaining=login.expiresAt-now()
            if(remaining>0)pause(minOf(interval,remaining))
        }
    }
}
