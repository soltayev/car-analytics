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
    required this.summaryRu,
    required this.summaryEn,
    required this.riskForecast,
    required this.riskForecastRu,
    required this.riskForecastEn,
    required this.nextActions,
    required this.nextActionsRu,
    required this.nextActionsEn,
  });

  final int healthScore;
  final String urgency;
  final bool drivable;
  final bool towRecommended;
  final String primaryIssue;
  final String summary;
  final String summaryRu;
  final String summaryEn;
  final String riskForecast;
  final String riskForecastRu;
  final String riskForecastEn;
  final List<String> nextActions;
  final List<String> nextActionsRu;
  final List<String> nextActionsEn;

  String localizedSummary(AppLanguage language) =>
      language == AppLanguage.ru ? summaryRu : summaryEn;

  String localizedRiskForecast(AppLanguage language) =>
      language == AppLanguage.ru ? riskForecastRu : riskForecastEn;

  List<String> localizedNextActions(AppLanguage language) =>
      language == AppLanguage.ru ? nextActionsRu : nextActionsEn;

  factory DiagnosticReport.fromJson(Map<String, dynamic> json) =>
      DiagnosticReport(
        healthScore: json['healthScore'] as int? ?? 0,
        urgency: json['urgency'] as String? ?? '',
        drivable: json['drivable'] as bool? ?? false,
        towRecommended: json['towRecommended'] as bool? ?? false,
        primaryIssue: json['primaryIssue'] as String? ?? '',
        summary: json['summary'] as String? ?? '',
        summaryRu: _string(json['summaryRu'], _string(json['summary'])),
        summaryEn: _string(json['summaryEn'], _string(json['summary'])),
        riskForecast: json['riskForecast'] as String? ?? '',
        riskForecastRu: _string(
          json['riskForecastRu'],
          _string(json['riskForecast']),
        ),
        riskForecastEn: _string(
          json['riskForecastEn'],
          _string(json['riskForecast']),
        ),
        nextActions: _stringList(json['nextActions']),
        nextActionsRu: _stringList(json['nextActionsRu'], json['nextActions']),
        nextActionsEn: _stringList(json['nextActionsEn'], json['nextActions']),
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
    required this.descriptionRu,
    required this.descriptionEn,
    required this.severity,
  });

  final String code;
  final String description;
  final String descriptionRu;
  final String descriptionEn;
  final String severity;

  String localizedDescription(AppLanguage language) =>
      language == AppLanguage.ru ? descriptionRu : descriptionEn;

  factory FaultCode.fromJson(Map<String, dynamic> json) => FaultCode(
    code: json['code'] as String? ?? '',
    description: json['description'] as String? ?? '',
    descriptionRu: _string(
      (json['dictionaryEntry'] as Map<String, dynamic>?)?['titleRu'],
      _string(json['description']),
    ),
    descriptionEn: _string(
      (json['dictionaryEntry'] as Map<String, dynamic>?)?['titleEn'],
      _string(json['description']),
    ),
    severity: json['severity'] as String? ?? '',
  );
}

class Recommendation {
  Recommendation({
    required this.type,
    required this.message,
    required this.messageRu,
    required this.messageEn,
    required this.actionLabel,
    required this.actionLabelRu,
    required this.actionLabelEn,
  });

  final String type;
  final String message;
  final String messageRu;
  final String messageEn;
  final String actionLabel;
  final String actionLabelRu;
  final String actionLabelEn;

  String localizedMessage(AppLanguage language) =>
      language == AppLanguage.ru ? messageRu : messageEn;

  String localizedActionLabel(AppLanguage language) =>
      language == AppLanguage.ru ? actionLabelRu : actionLabelEn;

  factory Recommendation.fromJson(Map<String, dynamic> json) => Recommendation(
    type: json['type'] as String? ?? '',
    message: json['message'] as String? ?? '',
    messageRu: _string(json['messageRu'], _string(json['message'])),
    messageEn: _string(json['messageEn'], _string(json['message'])),
    actionLabel: json['actionLabel'] as String? ?? '',
    actionLabelRu: _string(json['actionLabelRu'], _string(json['actionLabel'])),
    actionLabelEn: _string(json['actionLabelEn'], _string(json['actionLabel'])),
  );
}

enum AppLanguage { ru, en }

class CatalogItem {
  CatalogItem({
    required this.title,
    required this.titleRu,
    required this.titleEn,
    required this.subtitle,
    required this.subtitleRu,
    required this.subtitleEn,
    required this.badge,
  });

  final String title;
  final String titleRu;
  final String titleEn;
  final String subtitle;
  final String subtitleRu;
  final String subtitleEn;
  final String badge;

  String localizedTitle(AppLanguage language) =>
      language == AppLanguage.ru ? titleRu : titleEn;

  String localizedSubtitle(AppLanguage language) =>
      language == AppLanguage.ru ? subtitleRu : subtitleEn;
}

class ServiceCenter {
  ServiceCenter({
    required this.name,
    required this.city,
    required this.address,
    required this.phone,
    required this.specialization,
    required this.emergencySupport,
    required this.rating,
    required this.latitude,
    required this.longitude,
    this.distanceKm,
  });

  final String name;
  final String city;
  final String address;
  final String phone;
  final String specialization;
  final bool emergencySupport;
  final num rating;
  final double latitude;
  final double longitude;
  final double? distanceKm;

  factory ServiceCenter.fromJson(Map<String, dynamic> json) => ServiceCenter(
    name: json['name'] as String? ?? '',
    city: json['city'] as String? ?? '',
    address: json['address'] as String? ?? '',
    phone: json['phone'] as String? ?? '',
    specialization: json['specialization'] as String? ?? '',
    emergencySupport: json['emergencySupport'] as bool? ?? false,
    rating: json['rating'] as num? ?? 0,
    latitude: (json['latitude'] as num? ?? 0).toDouble(),
    longitude: (json['longitude'] as num? ?? 0).toDouble(),
    distanceKm: (json['distanceKm'] as num?)?.toDouble(),
  );

  String get badge => emergencySupport ? '24/7' : rating.toString();

  String distanceLabel(AppLanguage language) {
    if (distanceKm == null) {
      return city;
    }
    final value = distanceKm!.toStringAsFixed(2);
    return language == AppLanguage.ru ? '$value км' : '$value km';
  }
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

  Future<List<ServiceCenter>> serviceCenters({
    String city = 'Almaty',
    double? latitude,
    double? longitude,
    double? maxDistanceKm,
  }) async {
    final body = await _get('/api/catalog/service-centers', {
      'city': city,
      'latitude': latitude?.toString(),
      'longitude': longitude?.toString(),
      'maxDistanceKm': maxDistanceKm?.toString(),
    });
    return _list(body, ServiceCenter.fromJson);
  }

  Future<List<CatalogItem>> spareParts({String faultCode = ''}) async {
    final body = await _get('/api/catalog/spare-parts', {
      'faultCode': faultCode,
    });
    return (body as List<dynamic>).map((item) {
      final json = item as Map<String, dynamic>;
      return CatalogItem(
        title: json['partName'] as String? ?? '',
        titleRu: json['partName'] as String? ?? '',
        titleEn: json['partName'] as String? ?? '',
        subtitle: '${json['manufacturer'] ?? ''} ${json['partNumber'] ?? ''}',
        subtitleRu: '${json['manufacturer'] ?? ''} ${json['partNumber'] ?? ''}',
        subtitleEn: '${json['manufacturer'] ?? ''} ${json['partNumber'] ?? ''}',
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
        titleRu: json['code'] as String? ?? '',
        titleEn: json['code'] as String? ?? '',
        subtitle: json['description'] as String? ?? '',
        subtitleRu: _string(json['titleRu'], _string(json['description'])),
        subtitleEn: _string(json['titleEn'], _string(json['description'])),
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

String _string(dynamic value, [String fallback = '']) {
  return value == null ? fallback : value.toString();
}

List<String> _stringList(dynamic value, [dynamic fallback]) {
  final source = value is List<dynamic> ? value : fallback;
  return (source as List<dynamic>? ?? [])
      .map((item) => item.toString())
      .toList();
}
