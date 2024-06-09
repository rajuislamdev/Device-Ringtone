import 'dart:async';

import 'package:device_rington/main.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class RingtoneScreen extends StatefulWidget {
  const RingtoneScreen({super.key});

  @override
  State<RingtoneScreen> createState() => _RingtoneScreenState();
}

class _RingtoneScreenState extends State<RingtoneScreen> {
  List<Ringtone> ringtones = [];
  bool isPaying = false;
  int? selectedIndex;
  StreamSubscription? subscription;

  // define the method channel name
  static const platform =
      MethodChannel('com.example.device_ringtones/device_ringtones');
  static const eventChannel =
      EventChannel('com.example.device_ringtones/ringtoneStatus');

  @override
  void initState() {
    getRingtons();
    startListening();
    super.initState();
  }

  Future<void> getRingtons() async {
    try {
      final List<dynamic> ringtones =
          await platform.invokeMethod('getRingtones');
      setState(() {
        this.ringtones = ringtones
            .map((ringtone) =>
                Ringtone(uri: ringtone['uri'], title: ringtone['title']))
            .toList();
      });
    } on PlatformException catch (e) {
      debugPrint('Failed to get ringtones: ${e.message}');
    }
  }

  Future<void> playRingtone(String uri) async {
    try {
      await platform.invokeMethod('playRingtone', {'uri': uri});
    } on PlatformException catch (e) {
      debugPrint('Failed to play ringtone: ${e.message}');
    }
  }

  Future<void> stopRingtone() async {
    try {
      await platform.invokeMethod('stopRingtone');
    } on PlatformException catch (e) {
      debugPrint('Failed to stop ringtone: ${e.message}');
    }
  }

  void startListening() {
    subscription = eventChannel.receiveBroadcastStream().listen((event) {
      setState(() {
        isPaying = event;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      floatingActionButton: FloatingActionButton(
        onPressed: () => stopRingtone().then(
          (value) => setState(() {
            selectedIndex = null;
          }),
        ),
        child: Icon(isPaying ? Icons.stop : Icons.play_arrow),
      ),
      appBar: AppBar(
        title: const Text(
          'Device Ringtones',
          style: TextStyle(color: Colors.white),
        ),
        backgroundColor: Colors.purple,
      ),
      body: ListView.builder(
        itemBuilder: (context, index) => ListTile(
          leading: Text((index + 1).toString()),
          trailing: Checkbox(
            value: selectedIndex == index,
            onChanged: (_) {},
          ),
          title: Text(ringtones[index].title),
          onTap: () {
            playRingtone(ringtones[index].uri);
            setState(() {
              selectedIndex = index;
            });
          },
        ),
        itemCount: ringtones.length,
      ),
    );
  }
}
