# flutter_set_defaults_app

Android-only Flutter plugin for receiving documents opened from other apps.

The plugin does not use a proxy activity. It listens to the host `MainActivity`
intent through `ActivityAware` and `onNewIntent`, then copies the incoming
`content://` or `file://` URI into app cache and returns a local file path.

## Dart API

```dart
final FlutterSetDefaultsAppFile? initialFile =
    await FlutterSetDefaultsApp.getInitialFile();

FlutterSetDefaultsApp.fileStream.listen((FlutterSetDefaultsAppFile file) {
  // file.path, file.name, file.mimeType, file.sourceUri
});
```

## Android setup

Add the document `intent-filter` to your app `MainActivity`.

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop"
    android:theme="@style/LaunchTheme"
    android:configChanges="orientation|keyboardHidden|keyboard|screenSize|smallestScreenSize|locale|layoutDirection|fontScale|screenLayout|density|uiMode"
    android:hardwareAccelerated="true"
    android:windowSoftInputMode="adjustResize">

    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>

    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="content" />
        <data android:scheme="file" />
        <data android:mimeType="application/pdf" />
        <data android:mimeType="application/msword" />
        <data android:mimeType="application/vnd.openxmlformats-officedocument.wordprocessingml.document" />
        <data android:mimeType="application/vnd.ms-excel" />
        <data android:mimeType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" />
        <data android:mimeType="text/csv" />
    </intent-filter>
</activity>
```

For better compatibility with Android's intent matching, you can split these
MIME types into separate `intent-filter` blocks.

## Returned file

`FlutterSetDefaultsAppFile.path` is a copied local cache path. Use this path in
Flutter code instead of reading `sourceUri` directly.
