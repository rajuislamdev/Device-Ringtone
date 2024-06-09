package com.example.device_ringtones

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity(){
    private val channel = "com.example.device_ringtones/device_ringtones"
    private val eventChannel = "com.example.device_ringtones/ringtoneStatus"
    private var currentRingtone: Ringtone? = null
    private var currentRingtoneUri: Uri? = null
    private var isPlaying = false
    private var eventSink: EventChannel.EventSink? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger,channel).setMethodCallHandler{
            call, result ->
            when(call.method){
                "getRingtones" -> {
                    val ringtones = getRingtones()
                    result.success(ringtones)
                }
                "playRingtone" -> {
                    val uri: String? = call.argument("uri")
                    if(uri != null){
                       playRingtone(uri)
                        result.success(null)
                    }else{
                        result.error("UNAVAILABLE","Ringtone URI is not available",null)
                    }
                }
                "stopRingtone" -> {
                    stopCurrentRingtone()
                    result.success(null)
                }
                "isPlaying" -> {
                    result.success(isPlaying)
                }
                else -> result.notImplemented()
            }
        }

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, eventChannel).setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    eventSink = events
                }

                override fun onCancel(arguments: Any?) {
                    eventSink = null
                }
            }
        )
    }


    private fun getRingtones(): List<Map<String,String>>{
        val ringtoneList = mutableListOf<Map<String,String>>()
        val ringtoneManager = RingtoneManager(this)
        ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)
        val cursor = ringtoneManager.cursor
        while (cursor.moveToNext()){
            val ringtoneUri: Uri = ringtoneManager.getRingtoneUri(cursor.position)
            val ringtoneTitle: String = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            ringtoneList.add(mapOf("uri" to ringtoneUri.toString(), "title" to ringtoneTitle))
        }
        cursor.close()
        return  ringtoneList
    }

    private fun playRingtone(uriString: String){
        try {
            val uri = Uri.parse(uriString.split("\\?")[0])
            currentRingtone?.stop()
            currentRingtone = RingtoneManager.getRingtone(applicationContext, Uri.parse(uriString))
            currentRingtone?.play()
            isPlaying = true
            currentRingtoneUri = uri
            eventSink?.success(true)
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun stopCurrentRingtone(){
      try {
          currentRingtone?.stop()
          isPlaying = false
          eventSink?.success(false)
          currentRingtone = null
          currentRingtoneUri = null

      } catch (e: Exception){
          e.printStackTrace()
      }
    }
}
