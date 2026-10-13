package com.tianji.planner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.location.*
import android.os.*
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tianji.domain.*
import com.tianji.ui.*
import java.util.Locale

@Composable internal fun DirectionCompass(palace:Int,point:GeoPoint?) {
    val context=LocalContext.current;val lifecycle=LocalLifecycleOwner.current.lifecycle
    var heading by remember{mutableStateOf<Float?>(null)}
    var unreliable by remember{mutableStateOf(false)}
    DisposableEffect(context,lifecycle,point){
        val manager=context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor=manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener=object:SensorEventListener {
            override fun onAccuracyChanged(s:Sensor?,accuracy:Int){unreliable=accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE}
            override fun onSensorChanged(event:SensorEvent){
                val matrix=FloatArray(9);SensorManager.getRotationMatrixFromVector(matrix,event.values)
                val remapped=FloatArray(9)
                val rotation=(context as? android.app.Activity)?.windowManager?.defaultDisplay?.rotation?:0
                val axes=when(rotation){1->SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X;2->SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y;3->SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X;else->SensorManager.AXIS_X to SensorManager.AXIS_Y}
                SensorManager.remapCoordinateSystem(matrix,axes.first,axes.second,remapped)
                val orientation=FloatArray(3);SensorManager.getOrientation(remapped,orientation)
                val correction=point?.let{GeomagneticField(it.latitude.toFloat(),it.longitude.toFloat(),0f,System.currentTimeMillis()).declination}?:0f
                heading=((Math.toDegrees(orientation[0].toDouble()).toFloat()+correction+360f)%360f)
            }
        }
        fun start(){if(sensor!=null)manager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_UI)}
        val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_RESUME)start() else if(event==Lifecycle.Event.ON_PAUSE)manager.unregisterListener(listener)}
        lifecycle.addObserver(observer);if(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))start()
        onDispose{manager.unregisterListener(listener);lifecycle.removeObserver(observer)}
    }
    val color=MaterialTheme.colorScheme.primary;val outline=MaterialTheme.colorScheme.outline
    val target=Navigator.bearing(palace)
    Row(horizontalArrangement=Arrangement.spacedBy(16.dp),modifier=Modifier.fillMaxWidth()){
        Canvas(Modifier.size(88.dp).graphicsLayer{rotationZ=target-(heading?:0f)}){
            drawCircle(outline,style=androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
            val p=Path().apply{moveTo(size.width/2,12.dp.toPx());lineTo(size.width/2+16.dp.toPx(),size.height-22.dp.toPx());lineTo(size.width/2,size.height-32.dp.toPx());lineTo(size.width/2-16.dp.toPx(),size.height-22.dp.toPx());close()};drawPath(p,color)
        }
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)){
            Text(t("目標方位 $target°","Target bearing $target°"),style=MaterialTheme.typography.titleMedium)
            Text(heading?.let{t("機頂朝向 ${it.toInt()}°","Phone heading ${it.toInt()}°")}?:t("未收到指南針訊號","No compass reading"))
            Text(if(point!=null)t("已用位置估算真北修正","True north correction from location") else t("目前顯示磁北；定位後可修正真北","Magnetic north; locate to correct to true north"),style=MaterialTheme.typography.bodySmall)
        }
    }
    Text(if(unreliable)t("指南針未準：遠離金屬，輕輕以 8 字校準。","Compass unreliable: move away from metal and calibrate.") else t("將手機平放，箭嘴指向目標方位。無感應器時請用其他指南針；未修正磁偏角時角度只供粗略參考。","Hold the phone flat. The arrow points to the target. Without a sensor use another compass; uncorrected magnetic readings are approximate."),style=MaterialTheme.typography.bodySmall)
}

@Composable internal fun LocationButton(label:String,onPoint:(GeoPoint)->Unit) {
    val context=LocalContext.current
    val manager=remember{context.getSystemService(Context.LOCATION_SERVICE) as LocationManager}
    var pending by remember{mutableStateOf(false)};var message by remember{mutableStateOf("")}
    val handler=remember{Handler(Looper.getMainLooper())}
    var listener by remember{mutableStateOf<LocationListener?>(null)}
    val english=English.current
    fun cancel(){listener?.let{manager.removeUpdates(it)};listener=null;handler.removeCallbacksAndMessages(null);pending=false}
    fun request(){
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)return
        cancel();message="";pending=true
        val provider=when{manager.isProviderEnabled(LocationManager.GPS_PROVIDER)->LocationManager.GPS_PROVIDER;manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)->LocationManager.NETWORK_PROVIDER;else->null}
        if(provider==null){pending=false;message=if(english)"Location is off. You can continue without it." else "手機未開定位，仍可不定位繼續。";return}
        val l=object:LocationListener{
            override fun onLocationChanged(location:Location){
                if(!location.hasAccuracy() || location.accuracy>100 || SystemClock.elapsedRealtimeNanos()-location.elapsedRealtimeNanos>120_000_000_000L)return
                onPoint(GeoPoint(location.latitude,location.longitude,location.accuracy));message=if(english)"Saved on this phone (±${location.accuracy.toInt()} m)" else "已記喺手機（精度 ±${location.accuracy.toInt()} 米）";cancel()
            }
        };listener=l
        try { @Suppress("DEPRECATION") manager.requestLocationUpdates(provider,1000L,0f,l,Looper.getMainLooper()) }
        catch(_:SecurityException){cancel();message=if(english)"Location permission unavailable." else "未獲定位權限。"}
        handler.postDelayed({if(pending){cancel();message=if(english)"No sufficiently accurate fix. Continue without location or retry outdoors." else "未取得足夠準確定位，可不定位繼續，或到戶外再試。"}},20000)
    }
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){p->
        if(p.values.any{it})request() else message=if(english)"Permission declined. Compass and plans still work." else "未授權定位，仍可睇盤及方位。"
    }
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle){
        val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_PAUSE)cancel()}
        lifecycle.addObserver(observer)
        onDispose{cancel();lifecycle.removeObserver(observer)}
    }
    OutlinedButton(enabled=!pending,onClick={
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)request()
        else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
    },modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(if(pending)t("定位中…最多等 20 秒","Locating… up to 20 seconds") else label)}
    if(message.isNotEmpty())Text(message,style=MaterialTheme.typography.bodySmall)
}

@Composable internal fun SpeakStep(text:String) {
    val context=LocalContext.current;val english=English.current
    var engine by remember{mutableStateOf<TextToSpeech?>(null)};var ready by remember{mutableStateOf(false)}
    DisposableEffect(context,english){
        var local:TextToSpeech?=null
        local=TextToSpeech(context){status->
            if(status==TextToSpeech.SUCCESS){
                val voices=local?.voices.orEmpty().filter{!it.isNetworkConnectionRequired}
                val voice=if(english)voices.firstOrNull{it.locale.language=="en"} else voices.firstOrNull{it.locale.toLanguageTag().startsWith("zh-HK") || it.locale.language=="yue"}
                if(voice!=null){local?.voice=voice;ready=true}
            }
        };engine=local
        onDispose{ready=false;local?.stop();local?.shutdown()}
    }
    OutlinedButton(enabled=ready,onClick={engine?.speak(text,TextToSpeech.QUEUE_FLUSH,null,"tianji-step")},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(t("讀出呢一步","Read this step"))}
    if(!ready)Text(t("未有可用嘅離線廣東話語音。可在 Android「文字轉語音」設定下載；文字指引照常用。","No offline English voice available. Install one in Android text-to-speech settings; written steps remain available."),style=MaterialTheme.typography.bodySmall)
}
