package com.tianji.data
import android.Manifest
import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import kotlinx.coroutines.flow.first
import java.time.*
import java.util.concurrent.TimeUnit
fun quiet(hour:Int)=hour>=22 || hour<8
class ReminderWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params){
 override suspend fun doWork():Result{
  val s=Preferences(applicationContext).flow.first();if(!s.reminders || quiet(ZonedDateTime.now(ZoneId.of(s.timezone)).hour))return Result.success()
  if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return Result.success()
  val n=applicationContext.getSystemService(NotificationManager::class.java);n.createNotificationChannel(NotificationChannel("reflection",if(s.english)"TianJi reflections" else "天機行動回顧",NotificationManager.IMPORTANCE_DEFAULT))
  val launch=applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
  val pending=PendingIntent.getActivity(applicationContext,0,launch,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
  n.notify(1,NotificationCompat.Builder(applicationContext,"reflection").setSmallIcon(android.R.drawable.ic_menu_today).setContentTitle(if(s.english)"TianJi" else "天機").setContentText(if(s.english)"A moment to review your next step" else "花一刻回顧下一步").setContentIntent(pending).setAutoCancel(true).build());return Result.success()
 }
 companion object { fun configure(context:Context,enabled:Boolean){val w=WorkManager.getInstance(context);if(!enabled){w.cancelUniqueWork("reflection");return};w.enqueueUniquePeriodicWork("reflection",ExistingPeriodicWorkPolicy.UPDATE,PeriodicWorkRequestBuilder<ReminderWorker>(12,TimeUnit.HOURS).setInitialDelay(12,TimeUnit.HOURS).build())} }
}
