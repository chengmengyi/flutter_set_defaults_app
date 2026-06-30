import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_set_defaults_app_platform_interface.dart';

/// An implementation of [FlutterSetDefaultsAppPlatform] that uses method channels.
class MethodChannelFlutterSetDefaultsApp extends FlutterSetDefaultsAppPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_set_defaults_app');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
