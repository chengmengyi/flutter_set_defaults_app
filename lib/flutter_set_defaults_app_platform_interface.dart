import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_set_defaults_app_method_channel.dart';
import 'src/flutter_set_defaults_app_file.dart';

abstract class FlutterSetDefaultsAppPlatform extends PlatformInterface {
  /// Constructs a FlutterSetDefaultsAppPlatform.
  FlutterSetDefaultsAppPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterSetDefaultsAppPlatform _instance =
      MethodChannelFlutterSetDefaultsApp();

  /// The default instance of [FlutterSetDefaultsAppPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterSetDefaultsApp].
  static FlutterSetDefaultsAppPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterSetDefaultsAppPlatform] when
  /// they register themselves.
  static set instance(FlutterSetDefaultsAppPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<FlutterSetDefaultsAppFile?> getInitialFile() {
    throw UnimplementedError('getInitialFile() has not been implemented.');
  }

  Stream<FlutterSetDefaultsAppFile> get fileStream {
    throw UnimplementedError('fileStream has not been implemented.');
  }

  Future<bool> openFile({
    required String path,
    String? mimeType,
    String? name,
  }) {
    throw UnimplementedError('openFile() has not been implemented.');
  }
}
