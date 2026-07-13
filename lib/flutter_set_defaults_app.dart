import 'flutter_set_defaults_app_platform_interface.dart';
import 'src/flutter_set_defaults_app_file.dart';
export 'src/flutter_set_defaults_app_file.dart';

class FlutterSetDefaultsApp {
  static Future<FlutterSetDefaultsAppFile?> getInitialFile() {
    return FlutterSetDefaultsAppPlatform.instance.getInitialFile();
  }

  static Stream<FlutterSetDefaultsAppFile> get fileStream {
    return FlutterSetDefaultsAppPlatform.instance.fileStream;
  }

  static Future<bool> openFile({
    required String path,
    String? mimeType,
    String? name,
  }) {
    return FlutterSetDefaultsAppPlatform.instance.openFile(
      path: path,
      mimeType: mimeType,
      name: name,
    );
  }

  static Future<bool> openFileForDefault({
    required String path,
    String? mimeType,
    String? name,
  }) {
    return FlutterSetDefaultsAppPlatform.instance.openFileForDefault(
      path: path,
      mimeType: mimeType,
      name: name,
    );
  }

  static Future<bool> isCurrentAppDefault({
    String mimeType = 'application/pdf',
  }) {
    return FlutterSetDefaultsAppPlatform.instance.isCurrentAppDefault(
      mimeType: mimeType,
    );
  }

  Future<String?> getPlatformVersion() {
    return FlutterSetDefaultsAppPlatform.instance.getPlatformVersion();
  }
}
