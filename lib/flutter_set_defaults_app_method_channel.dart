import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_set_defaults_app_platform_interface.dart';
import 'src/flutter_set_defaults_app_file.dart';

/// An implementation of [FlutterSetDefaultsAppPlatform] that uses method channels.
class MethodChannelFlutterSetDefaultsApp extends FlutterSetDefaultsAppPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_set_defaults_app');

  @visibleForTesting
  final eventChannel = const EventChannel('flutter_set_defaults_app/events');

  Stream<FlutterSetDefaultsAppFile>? _fileStream;

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }

  @override
  Future<FlutterSetDefaultsAppFile?> getInitialFile() async {
    final Map<Object?, Object?>? fileMap = await methodChannel
        .invokeMapMethod<Object?, Object?>('getInitialFile');
    if (fileMap == null) {
      return null;
    }
    return FlutterSetDefaultsAppFile.fromMap(fileMap);
  }

  @override
  Stream<FlutterSetDefaultsAppFile> get fileStream {
    _fileStream ??= eventChannel.receiveBroadcastStream().map((dynamic event) {
      return FlutterSetDefaultsAppFile.fromMap(event as Map<Object?, Object?>);
    });
    return _fileStream!;
  }

  @override
  Future<bool> openFile({
    required String path,
    String? mimeType,
    String? name,
  }) async {
    final bool? result = await methodChannel.invokeMethod<bool>('openFile', {
      'path': path,
      'mimeType': mimeType,
      'name': name,
    });
    return result ?? false;
  }
}
