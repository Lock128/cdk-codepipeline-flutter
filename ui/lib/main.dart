import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

//https://5j552oravrqyecwbvrwt5izpy40remkd.lambda-url.eu-central-1.on.aws/ - checkage

Future<String> getMinimalDrivingAge(String country) async {
  http.Response response5;
  try {
    Map<String, String> queryParameters = new Map<String, String>();
    queryParameters.putIfAbsent("Country", () => country);
    Uri endpoint = Uri.https(
        "5j552oravrqyecwbvrwt5izpy40remkd.lambda-url.eu-central-1.on.aws",
        "",
        queryParameters);
    response5 = await http.get(endpoint);
    print("Return value 1: ${response5.body}");
    return response5.body;
  } catch (e) {
    print(e.toString());
  }
  return "NOT FOUND";
}

//https://tcmkvmtjvhr6iiseids252lbd40jyslp.lambda-url.eu-central-1.on.aws/
//https://tcmkvmtjvhr6iiseids252lbd40jyslp.lambda-url.eu-central-1.on.aws/ - paperSize
Future<String> getPaperSize(String country) async {
  http.Response response5;
  try {
    Map<String, String> queryParameters = new Map<String, String>();
    queryParameters.putIfAbsent("Country", () => country);
    Uri endpoint = Uri.https(
        "tcmkvmtjvhr6iiseids252lbd40jyslp.lambda-url.eu-central-1.on.aws",
        "",
        queryParameters);
    response5 = await http.get(endpoint);
    print("Return value 2: ${response5.body}");
    return response5.body;
  } catch (e) {
    print(e.toString());
  }
  return "NOT FOUND";
}

//doCalculation is not yet working in this implementation
Future<String> doCalculation(String calculationText) async {
  http.Response response5;
  try {
    var details = {'action': 'plus', 'x': 0, 'y': 0};
    details['x'] = 5;
    details['y'] = 4;
    Uri endpoint = Uri.https(
        "4fx43iriy6x5omci5pwl4o2gyi0byalb.lambda-url.eu-central-1.on.aws", "");
    response5 = await http.post(
      endpoint,
      body: details,
    );

    print("Return value 2: ${response5.body}");
    return response5.body;
  } catch (e) {
    print(e.toString());
  }
  return "NOT FOUND";
}

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'lockhead.info Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'lockhead.info Flutter Demo Homepage'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key, required this.title}) : super(key: key);

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;
  String _drivingAge = "-99";
  String _paperSize = "XX";
  int _calculationResult = 0;

  void _incrementCounter() {
    setState(() {
      // This call to setState tells the Flutter framework that something has
      // changed in this State, which causes it to rerun the build method below
      // so that the display can reflect the updated values. If we changed
      // _counter without calling setState(), then the build method would not be
      // called again, and so nothing would appear to happen.
      _counter++;
    });
  }

  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called, for instance as done
    // by the _incrementCounter method above.
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'You have clicked the button this many times:',
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headline4,
            ),
            SizedBox(height: 50),
            const Image(image: AssetImage('images/header.gif')),
            SizedBox(height: 50),
            const Text(
              'Driving Age Country:',
            ),
            TextField(onChanged: (text) async {
              print('First text field: $text');
              await getMinimalDrivingAge(text).then((value) async {
                print(value);
                setState(() {
                  _drivingAge = value;
                });
              });
            }),
            Text(
              'Driving Age: ' + _drivingAge,
            ),
            SizedBox(height: 50),
            const Text(
              'Paper Size Country:',
            ),
            TextField(onChanged: (text) async {
              print('Second text field: $text');
              await getPaperSize(text).then((value) async {
                print(value);
                setState(() {
                  _paperSize = value;
                });
              });
            }),
            Text(
              'Paper Size: ' + _paperSize,
            ),
            SizedBox(height: 50),
            const Text(
              'Calculation:',
            ),
            TextField(onChanged: (text) async {
              print('calculation input: $text');
              await doCalculation(text).then((value) async {
                print(value);
                setState(() {
                  try {
                    _calculationResult = int.parse(value);
                  } on FormatException catch (e) {
                    print(e);
                  }
                });
              });
            }),
            Text(
              'Calculation result: ' + _calculationResult.toString(),
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
