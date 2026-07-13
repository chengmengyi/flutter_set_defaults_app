package com.set.defaults.app.flutter_set_defaults_app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.content.pm.PackageManager
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.NewIntentListener
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/** FlutterSetDefaultsAppPlugin */
class FlutterSetDefaultsAppPlugin :
    FlutterPlugin,
    MethodCallHandler,
    ActivityAware,
    NewIntentListener,
    EventChannel.StreamHandler {
    // The MethodChannel that will the communication between Flutter and native Android
    //
    // This local reference serves to register the plugin with the Flutter Engine and unregister it
    // when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private var applicationContext: Context? = null
    private var activity: Activity? = null
    private var activityBinding: ActivityPluginBinding? = null
    private var eventSink: EventChannel.EventSink? = null
    private var initialFile: Map<String, Any?>? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        applicationContext = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_set_defaults_app")
        channel.setMethodCallHandler(this)
        eventChannel = EventChannel(
            flutterPluginBinding.binaryMessenger,
            "flutter_set_defaults_app/events"
        )
        eventChannel.setStreamHandler(this)
    }

    override fun onMethodCall(
        call: MethodCall,
        result: Result
    ) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${Build.VERSION.RELEASE}")
            "getInitialFile" -> {
                result.success(initialFile)
                initialFile = null
            }
            "openFile" -> {
                val path = call.argument<String>("path")
                val mimeType = call.argument<String>("mimeType")
                val name = call.argument<String>("name")
                result.success(openFile(path, mimeType, name, true))
            }
            "openFileForDefault" -> {
                val path = call.argument<String>("path")
                val mimeType = call.argument<String>("mimeType")
                val name = call.argument<String>("name")
                result.success(openFile(path, mimeType, name, false))
            }
            "isCurrentAppDefault" -> {
                val mimeType = call.argument<String>("mimeType")
                result.success(isCurrentAppDefault(mimeType))
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
        applicationContext = null
        eventSink = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
        activity = binding.activity
        binding.addOnNewIntentListener(this)
        handleIntent(binding.activity.intent, true)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        detachActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        detachActivity()
    }

    override fun onNewIntent(intent: Intent): Boolean {
        return handleIntent(intent, false)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

    private fun detachActivity() {
        activityBinding?.removeOnNewIntentListener(this)
        activityBinding = null
        activity = null
    }

    private fun handleIntent(intent: Intent?, isInitial: Boolean): Boolean {
        if (intent == null || intent.action != Intent.ACTION_VIEW) {
            return false
        }
        val uri = intent.data ?: return false
        val context = applicationContext ?: activity ?: return false
        val fileMap = copyUriToCache(
            context,
            uri,
            intent.type
        ) ?: return false
        if (isInitial || eventSink == null) {
            initialFile = fileMap
        } else {
            eventSink?.success(fileMap)
        }
        return true
    }

    private fun copyUriToCache(
        context: Context,
        uri: Uri,
        intentMimeType: String?
    ): Map<String, Any?>? {
        return try {
            val sourceName = queryDisplayName(context, uri)
                ?: uri.lastPathSegment
                ?: "external_file"
            val mimeType = intentMimeType
                ?: context.contentResolver.getType(uri)
                ?: guessMimeType(sourceName)
            val extension = guessExtension(sourceName, mimeType)
            val safeName = sanitizeFileName(sourceName, extension)
            val targetDir = File(context.cacheDir, "flutter_set_defaults_app")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val targetFile = File(
                targetDir,
                "${System.currentTimeMillis()}_$safeName"
            )
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            mapOf(
                "path" to targetFile.absolutePath,
                "name" to safeName,
                "mimeType" to mimeType,
                "sourceUri" to uri.toString()
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") {
            return uri.lastPathSegment
        }
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }

    private fun sanitizeFileName(name: String, extension: String?): String {
        val cleanName = name
            .substringAfterLast('/')
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .ifBlank { "external_file" }
        if (extension.isNullOrBlank() || cleanName.contains(".")) {
            return cleanName
        }
        return "$cleanName.$extension"
    }

    private fun guessExtension(name: String, mimeType: String?): String? {
        val fileExtension = name.substringAfterLast('.', "")
        if (fileExtension.isNotBlank()) {
            return fileExtension.lowercase(Locale.US)
        }
        return mimeType?.let {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
        }
    }

    private fun guessMimeType(name: String): String? {
        val extension = name.substringAfterLast('.', "").lowercase(Locale.US)
        if (extension.isBlank()) {
            return null
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    private fun openFile(
        path: String?,
        mimeType: String?,
        name: String?,
        useChooser: Boolean
    ): Boolean {
        val currentActivity = activity ?: return false
        if (path.isNullOrBlank()) {
            return false
        }
        val file = File(path)
        if (!file.exists()) {
            return false
        }
        return try {
            val uri = FileProvider.getUriForFile(
                currentActivity,
                "${currentActivity.packageName}.flutter_set_defaults_app.fileprovider",
                file
            )
            val resolvedMimeType = mimeType
                ?: guessMimeType(name ?: file.name)
                ?: "*/*"
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, resolvedMimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (useChooser) {
                val chooser = Intent.createChooser(viewIntent, null).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                currentActivity.startActivity(chooser)
            } else {
                currentActivity.startActivity(viewIntent)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun isCurrentAppDefault(mimeType: String?): Boolean {
        val context = applicationContext ?: activity ?: return false
        val resolvedMimeType = mimeType?.takeIf { it.isNotBlank() }
            ?: "application/pdf"
        val uri = Uri.parse("content://${context.packageName}.flutter_set_defaults_app.default_check/file")
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            setDataAndType(uri, resolvedMimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                viewIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(
                viewIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        }
        val packageName = resolveInfo?.activityInfo?.packageName ?: return false
        return packageName == context.packageName
    }
}
