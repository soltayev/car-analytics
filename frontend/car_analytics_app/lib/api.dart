import 'dart:convert';

import 'package:http/http.dart' as http;

class Vehicle {
  Vehicle({
    required this.id,
    required this.vin,
    required this.brand,
    required this.model,
    required this.productionYear,
    required this.engineType,
  });

  final int id;
  final String vin;
  final String brand;
  final String model;
  final int productionYear;
  final String engineType;

  factory Vehicle.fromJson(Map<String, dynamic> json) => Vehicle(
    id: json['id'] as int,
    vin: json['vin'] as String? ?? '',
    brand: json['brand'] as String? ?? '',
    model: json['model'] as String? ?? '',
    productionYear: json['productionYear'] as int? ?? 0,
    engineType: json['engineType'] as String? ?? '',
  );
}

class DiagnosticSession {
  DiagnosticSession({
    required this.id,
    required this.vehicle,
    required this.connectionType,
    required this.adapterName,
    required this.protocol,
    required this.startedAt,
    required this.overallStatus,
    required this.readings,
    required this.faultCodes,
    required this.recommendations,
    this.report,
  });

  final int id;
  final Vehicle vehicle;
  final String connectionType;
  final String adapterName;
  final String protocol;
  final String startedAt;
  final String overallStatus;
  final List<ObdReading> readings;
  final List<FaultCode> faultCodes;
  final List<Recommendation> recommendations;
  final DiagnosticReport? report;

  factory DiagnosticSession.fromJson(Map<String, dynamic> json) =>
      DiagnosticSession(
        id: json['id'] as int,
        vehicle: Vehicle.fromJson(json['vehicle'] as Map<String, dynamic>),
        connectionType: json['connectionType'] as String? ?? '',
        adapterName: json['adapterName'] as String? ?? '',
        protocol: json['protocol'] as String? ?? '',
        startedAt: json['startedAt'] as String? ?? '',
        overallStatus: json['overallStatus'] as String? ?? '',
        readings: _list(json['readings'], ObdReading.fromJson),
        faultCodes: _list(json['faultCodes'], FaultCode.fromJson),
        recommendations: _list(
          json['recommendations'],
          Recommendation.fromJson,
        ),
        report: json['report'] == null
            ? null
            : DiagnosticReport.fromJson(json['report'] as Map<String, dynamic>),
      );
}

class DiagnosticReport {
  DiagnosticReport({
    required this.healthScore,
    required this.urgency,
    required this.drivable,
    required this.towRecommended,
    required this.primaryIssue,
    required this.summary,
    required this.nextActions,
  });

  final int healthScore;
  final String urgency;
  final bool drivable;
  final bool towRecommended;
  final String primaryIssue;
  final String summary;
  final List<String> nextActions;

  factory DiagnosticReport.fromJson(Map<String, dynamic> json) =>
      DiagnosticReport(
        healthScore: json['healthScore'] as int? ?? 0,
        urgency: json['urgency'] as String? ?? '',
        drivable: json['drivable'] as bool? ?? false,
        towRecommended: json['towRecommended'] as bool? ?? false,
        primaryIssue: json['primaryIssue'] as String? ?? '',
        summary: json['summary'] as String? ?? '',
        nextActions: (json['nextActions'] as List<dynamic>? ?? [])
            .map((item) => item.toString())
            .toList(),
      );
}

class ObdReading {
  ObdReading({
    required this.parameterName,
    required this.pidCode,
    required this.value,
    required this.unit,
  });

  final String parameterName;
  final String pidCode;
  final num value;
  final String unit;

  factory ObdReading.fromJson(Map<String, dynamic> json) => ObdReading(
    parameterName: json['parameterName'] as String? ?? '',
    pidCode: json['pidCode'] as String? ?? '',
    value: json['parameterValue'] as num? ?? 0,
    unit: json['unit'] as String? ?? '',
  );
}

class FaultCode {
  FaultCode({
    required this.code,
    required this.description,
    required this.severity,
  });

  final String code;
  final String description;
  final String severity;

  factory FaultCode.fromJson(Map<String, dynamic> json) => FaultCode(
    code: json['code'] as String? ?? '',
    description: json['description'] as String? ?? '',
    severity: json['severity'] as String? ?? '',
  );
}

class Recommendation {
  Recommendation({
    required this.type,
    required this.message,
    required this.actionLabel,
  });

  final String type;
  final String message;
  final String actionLabel;

  factory Recommendation.fromJson(Map<String, dynamic> json) => Recommendation(
    type: json['type'] as String? ?? '',
    message: json['message'] as String? ?? '',
    actionLabel: json['actionLabel'] as String? ?? '',
  );
}

class CatalogItem {
  CatalogItem({
    required this.title,
    required this.subtitle,
    required this.badge,
  });

  final String title;
  final String subtitle;
  final String badge;
}

class CarAnalyticsApi {
  CarAnalyticsApi(this.baseUrl);

  final String baseUrl;

  Uri _uri(String path, [Map<String, String?> query = const {}]) {
    final cleanBase = baseUrl.endsWith('/')
        ? baseUrl.substring(0, baseUrl.length - 1)
        : baseUrl;
    final params = Map.fromEntries(
      query.entries.where(
        (entry) => entry.value != null && entry.value!.isNotEmpty,
      ),
    );
    return Uri.parse('$cleanBase$path').replace(queryParameters: params);
  }

  Future<List<Vehicle>> vehicles() async {
    final body = await _get('/api/vehicles');
    return _list(body, Vehicle.fromJson);
  }

  Future<Vehicle> createVehicle(Map<String, dynamic> data) async {
    final body = await _post('/api/vehicles', data);
    return Vehicle.fromJson(body as Map<String, dynamic>);
  }

  Future<List<DiagnosticSession>> sessions() async {
    final body = await _get('/api/diagnostic-sessions');
    return _list(body, DiagnosticSession.fromJson);
  }

  Future<DiagnosticSession> createDemoSession(int vehicleId) async {
    final now = DateTime.now().toIso8601String().split('.').first;
    final body = await _post('/api/diagnostic-sessions/ingest', {
      'vehicleId': vehicleId,
      'connectionType': 'BLUETOOTH',
      'adapterName': 'ELM327 Demo',
      'adapterIdentifier': 'DEMO-00-01',
      'protocol': 'ISO_15765_4_CAN',
      'rawFrames': [
        {
          'mode': '01',
          'pid': '05',
          'rawResponse': '41 05 94',
          'decodedLabel': 'Engine Coolant Temperature',
          'frameTimestamp': now,
        },
        {
          'mode': '03',
          'rawResponse': '43 01 18 03 01',
          'decodedLabel': 'Stored DTCs',
          'frameTimestamp': now,
        },
      ],
      'readings': [
        {
          'parameterName': 'engine_temperature',
          'pidCode': '05',
          'sourceMode': '01',
          'parameterValue': 108,
          'unit': 'C',
        },
        {
          'parameterName': 'rpm',
          'pidCode': '0C',
          'sourceMode': '01',
          'parameterValue': 850,
          'unit': 'rpm',
        },
      ],
      'faultCodes': [
        {'code': 'P0301'},
        {'code': 'P0118'},
      ],
    });
    return DiagnosticSession.fromJson(body as Map<String, dynamic>);
  }

  Future<DiagnosticSession> wifiScan({
    required int vehicleId,
    required String host,
    required int port,
  }) async {
    final body = await _post('/api/live-scan/wifi', {
      'vehicleId': vehicleId,
      'host': host,
      'port': port,
      'adapterName': 'ELM327 WiFi',
      'protocol': 'ISO_15765_4_CAN',
      'socketTimeoutMs': 4000,
      'includeStoredFaultCodes': true,
      'includePendingFaultCodes': true,
      'includeFreezeFrame': true,
      'includeVehicleInfo': true,
      'currentDataPids': ['05', '0C', '0D', '11', '2F', '42'],
      'freezeFramePids': ['05', '0C', '0D', '11'],
    });
    return DiagnosticSession.fromJson(body as Map<String, dynamic>);
  }

  Future<List<CatalogItem>> serviceCenters({String city = 'Almaty'}) async {
    final body = await _get('/api/catalog/service-centers', {'city': city});
    return (body as List<dynamic>).map((item) {
      final json = item as Map<String, dynamic>;
      return CatalogItem(
        title: json['name'] as String? ?? '',
        subtitle: '${json['address'] ?? ''}\n${json['phone'] ?? ''}',
        badge: json['emergencySupport'] == true
            ? '24/7'
            : '${json['rating'] ?? ''}',
      );
    }).toList();
  }

  Future<List<CatalogItem>> spareParts({String faultCode = ''}) async {
    final body = await _get('/api/catalog/spare-parts', {
      'faultCode': faultCode,
    });
    return (body as List<dynamic>).map((item) {
      final json = item as Map<String, dynamic>;
      return CatalogItem(
        title: json['partName'] as String? ?? '',
        subtitle: '${json['manufacturer'] ?? ''} ${json['partNumber'] ?? ''}',
        badge: '${json['price'] ?? ''} ${json['currency'] ?? ''}',
      );
    }).toList();
  }

  Future<List<CatalogItem>> faultCodes({String code = ''}) async {
    final body = await _get('/api/catalog/fault-codes', {'code': code});
    return (body as List<dynamic>).map((item) {
      final json = item as Map<String, dynamic>;
      return CatalogItem(
        title: json['code'] as String? ?? '',
        subtitle: json['description'] as String? ?? '',
        badge: json['severity'] as String? ?? '',
      );
    }).toList();
  }

  Future<dynamic> _get(
    String path, [
    Map<String, String?> query = const {},
  ]) async {
    final response = await http.get(_uri(path, query));
    return _decode(response);
  }

  Future<dynamic> _post(String path, Map<String, dynamic> data) async {
    final response = await http.post(
      _uri(path),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(data),
    );
    return _decode(response);
  }

  dynamic _decode(http.Response response) {
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw ApiException(
        'HTTP ${response.statusCode}: ${response.body.isEmpty ? response.reasonPhrase : response.body}',
      );
    }
    if (response.body.isEmpty) {
      return null;
    }
    return jsonDecode(utf8.decode(response.bodyBytes));
  }
}

class ApiException implements Exception {
  ApiException(this.message);
  final String message;

  @override
  String toString() => message;
}

List<T> _list<T>(dynamic json, T Function(Map<String, dynamic>) mapper) {
  return (json as List<dynamic>? ?? [])
      .map((item) => mapper(item as Map<String, dynamic>))
      .toList();
}
