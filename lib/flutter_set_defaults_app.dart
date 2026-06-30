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

  Future<String?> getPlatformVersion() {
    return FlutterSetDefaultsAppPlatform.instance.getPlatformVersion();
  }
}
