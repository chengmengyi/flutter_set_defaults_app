import 'flutter_set_defaults_app_platform_interface.dart';

class FlutterSetDefaultsAppFile {
  const FlutterSetDefaultsAppFile({
    required this.path,
    this.name,
    this.mimeType,
    this.sourceUri,
  });

  factory FlutterSetDefaultsAppFile.fromMap(Map<Object?, Object?> map) {
    return FlutterSetDefaultsAppFile(
      path: map['path'] as String? ?? '',
      name: map['name'] as String?,
      mimeType: map['mimeType'] as String?,
      sourceUri: map['sourceUri'] as String?,
    );
  }

  final String path;
  final String? name;
  final String? mimeType;
  final String? sourceUri;

  Map<String, Object?> toMap() {
    return <String, Object?>{
      'path': path,
      'name': name,
      'mimeType': mimeType,
      'sourceUri': sourceUri,
    };
  }
}

class FlutterSetDefaultsApp {
  static Future<FlutterSetDefaultsAppFile?> getInitialFile() {
    return FlutterSetDefaultsAppPlatform.instance.getInitialFile();
  }

  static Stream<FlutterSetDefaultsAppFile> get fileStream {
    return FlutterSetDefaultsAppPlatform.instance.fileStream;
  }

  Future<String?> getPlatformVersion() {
    return FlutterSetDefaultsAppPlatform.instance.getPlatformVersion();
  }
}
