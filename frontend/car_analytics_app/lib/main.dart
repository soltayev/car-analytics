import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:geolocator/geolocator.dart';
import 'package:latlong2/latlong.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';

import 'api.dart';

const mapTileUrl = String.fromEnvironment(
  'MAP_TILE_URL',
  defaultValue: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
);
const mapTileAttribution = String.fromEnvironment(
  'MAP_TILE_ATTRIBUTION',
  defaultValue: '© OpenStreetMap contributors',
);

String tr(AppLanguage language, String ru, String en) =>
    language == AppLanguage.ru ? ru : en;

String severityLabel(String value, AppLanguage language) {
  final normalized = value.toUpperCase();
  final ru = switch (normalized) {
    'LOW' => 'Низкая',
    'MEDIUM' => 'Средняя',
    'HIGH' => 'Высокая',
    'CRITICAL' => 'Критическая',
    _ => value,
  };
  return language == AppLanguage.ru ? ru : value;
}

String urgencyLabel(String value, AppLanguage language) {
  final normalized = value.toUpperCase();
  final ru = switch (normalized) {
    'MONITOR' => 'Наблюдать',
    'SOON_SERVICE' => 'Сервис в ближайшее время',
    'URGENT_SERVICE' => 'Срочно в сервис',
    'IMMEDIATE_STOP' => 'Остановить эксплуатацию',
    'TOW_REQUIRED' => 'Нужен эвакуатор',
    _ => value,
  };
  return language == AppLanguage.ru ? ru : value;
}

int riskRank(String value) {
  final normalized = value.toUpperCase();
  return switch (normalized) {
    'LOW' => 1,
    'MEDIUM' => 2,
    'HIGH' => 3,
    'CRITICAL' => 4,
    _ => 0,
  };
}

String statusLabel(String value, AppLanguage language) {
  final normalized = value.toUpperCase();
  final ru = switch (normalized) {
    'OK' => 'Норма',
    'WARNING' => 'Внимание',
    'CRITICAL' => 'Критично',
    'DEGRADED' => 'Есть отклонения',
    'FAILED' => 'Неисправность',
    _ => value,
  };
  return language == AppLanguage.ru ? ru : value;
}

String connectionTypeLabel(String value, AppLanguage language) {
  final normalized = value.toUpperCase();
  final ru = switch (normalized) {
    'BLUETOOTH' => 'Bluetooth',
    'WIFI' => 'Wi-Fi',
    'USB' => 'USB',
    _ => value,
  };
  return language == AppLanguage.ru ? ru : value;
}

String serviceSpecializationLabel(String value, AppLanguage language) {
  if (language != AppLanguage.ru) {
    return value;
  }
  final normalized = value.toLowerCase();
  return switch (normalized) {
    'cooling' => 'Система охлаждения',
    'ignition' => 'Зажигание',
    'engine' => 'Двигатель',
    'transmission' => 'Трансмиссия',
    'electrical' => 'Электрика',
    'brake' || 'brakes' => 'Тормозная система',
    _ => value,
  };
}

void main() {
  runApp(const CarAnalyticsApp());
}

class CarAnalyticsApp extends StatelessWidget {
  const CarAnalyticsApp({super.key});

  @override
  Widget build(BuildContext context) {
    final scheme = ColorScheme.fromSeed(
      seedColor: const Color(0xff1f6f68),
      brightness: Brightness.light,
    );
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Car Analytics',
      theme: ThemeData(
        colorScheme: scheme,
        useMaterial3: true,
        cardTheme: const CardThemeData(
          elevation: 0,
          margin: EdgeInsets.zero,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(8)),
            side: BorderSide(color: Color(0xffd8dee4)),
          ),
        ),
        inputDecorationTheme: const InputDecorationTheme(
          border: OutlineInputBorder(
            borderRadius: BorderRadius.all(Radius.circular(8)),
          ),
        ),
      ),
      home: const HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _baseUrlController = TextEditingController(
    text: 'http://localhost:8080',
  );
  final _profileNameController = TextEditingController(text: 'Сабыржан');
  final _profilePhoneController = TextEditingController(
    text: '+7 707 000 00 00',
  );
  final _profileEmailController = TextEditingController(
    text: 'driver@example.com',
  );
  late CarAnalyticsApi _api = CarAnalyticsApi(_baseUrlController.text);
  var _selectedIndex = 0;
  var _loading = true;
  var _language = AppLanguage.ru;
  String? _error;
  List<Vehicle> _vehicles = [];
  List<DiagnosticSession> _sessions = [];

  @override
  void initState() {
    super.initState();
    _refresh();
  }

  @override
  void dispose() {
    _baseUrlController.dispose();
    _profileNameController.dispose();
    _profilePhoneController.dispose();
    _profileEmailController.dispose();
    super.dispose();
  }

  Future<void> _refresh() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final vehicles = await _api.vehicles();
      final sessions = await _api.sessions();
      if (!mounted) {
        return;
      }
      setState(() {
        _vehicles = vehicles;
        _sessions = sessions;
      });
    } catch (error) {
      if (!mounted) {
        return;
      }
      setState(() => _error = error.toString());
    } finally {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  void _applyBaseUrl() {
    _api = CarAnalyticsApi(_baseUrlController.text.trim());
    _refresh();
  }

  @override
  Widget build(BuildContext context) {
    final pages = [
      DashboardPage(
        vehicles: _vehicles,
        sessions: _sessions,
        loading: _loading,
        error: _error,
        language: _language,
        onFindServiceCenters: _openServiceCenters,
        onRefresh: _refresh,
      ),
      VehiclesPage(
        vehicles: _vehicles,
        language: _language,
        onCreate: _createVehicle,
        onRefresh: _refresh,
      ),
      DiagnosticsPage(
        vehicles: _vehicles,
        sessions: _sessions,
        language: _language,
        onFindServiceCenters: _openServiceCenters,
        onCreateDemoSession: _createDemoSession,
        onWifiScan: _wifiScan,
        onRefresh: _refresh,
      ),
      CatalogPage(
        api: _api,
        language: _language,
        serviceCenterOpenRequest: _serviceCenterOpenRequest,
      ),
      SettingsPage(
        controller: _baseUrlController,
        profileNameController: _profileNameController,
        profilePhoneController: _profilePhoneController,
        profileEmailController: _profileEmailController,
        vehiclesCount: _vehicles.length,
        sessionsCount: _sessions.length,
        language: _language,
        onLanguageChanged: (value) => setState(() => _language = value),
        onApply: _applyBaseUrl,
        onProfileChanged: () => setState(() {}),
      ),
    ];

    return Scaffold(
      appBar: AppBar(
        title: Text(
          _language == AppLanguage.ru ? 'Диагностика авто' : 'Car Analytics',
        ),
        actions: [
          IconButton(
            tooltip: tr(_language, 'Обновить', 'Refresh'),
            onPressed: _refresh,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: LayoutBuilder(
        builder: (context, constraints) {
          final wide = constraints.maxWidth >= 860;
          final content = IndexedStack(index: _selectedIndex, children: pages);
          if (!wide) {
            return content;
          }
          return Row(
            children: [
              NavigationRail(
                selectedIndex: _selectedIndex,
                onDestinationSelected: (value) =>
                    setState(() => _selectedIndex = value),
                labelType: NavigationRailLabelType.all,
                destinations: [
                  NavigationRailDestination(
                    icon: Icon(Icons.space_dashboard_outlined),
                    selectedIcon: Icon(Icons.space_dashboard),
                    label: Text(
                      _language == AppLanguage.ru ? 'Обзор' : 'Overview',
                    ),
                  ),
                  NavigationRailDestination(
                    icon: Icon(Icons.directions_car_outlined),
                    selectedIcon: Icon(Icons.directions_car),
                    label: Text(
                      _language == AppLanguage.ru ? 'Авто' : 'Vehicles',
                    ),
                  ),
                  NavigationRailDestination(
                    icon: Icon(Icons.monitor_heart_outlined),
                    selectedIcon: Icon(Icons.monitor_heart),
                    label: Text(
                      _language == AppLanguage.ru
                          ? 'Диагностика'
                          : 'Diagnostics',
                    ),
                  ),
                  NavigationRailDestination(
                    icon: Icon(Icons.inventory_2_outlined),
                    selectedIcon: Icon(Icons.inventory_2),
                    label: Text(
                      _language == AppLanguage.ru ? 'Справочник' : 'Catalog',
                    ),
                  ),
                  NavigationRailDestination(
                    icon: Icon(Icons.tune),
                    selectedIcon: Icon(Icons.tune),
                    label: Text(
                      _language == AppLanguage.ru ? 'Настройки' : 'Settings',
                    ),
                  ),
                ],
              ),
              const VerticalDivider(width: 1),
              Expanded(child: content),
            ],
          );
        },
      ),
      bottomNavigationBar: LayoutBuilder(
        builder: (context, constraints) {
          if (constraints.maxWidth >= 860) {
            return const SizedBox.shrink();
          }
          return NavigationBar(
            selectedIndex: _selectedIndex,
            onDestinationSelected: (value) =>
                setState(() => _selectedIndex = value),
            destinations: [
              NavigationDestination(
                icon: Icon(Icons.space_dashboard_outlined),
                selectedIcon: Icon(Icons.space_dashboard),
                label: _language == AppLanguage.ru ? 'Обзор' : 'Overview',
              ),
              NavigationDestination(
                icon: Icon(Icons.directions_car_outlined),
                selectedIcon: Icon(Icons.directions_car),
                label: _language == AppLanguage.ru ? 'Авто' : 'Vehicles',
              ),
              NavigationDestination(
                icon: Icon(Icons.monitor_heart_outlined),
                selectedIcon: Icon(Icons.monitor_heart),
                label: _language == AppLanguage.ru
                    ? 'Диагностика'
                    : 'Diagnostics',
              ),
              NavigationDestination(
                icon: Icon(Icons.inventory_2_outlined),
                selectedIcon: Icon(Icons.inventory_2),
                label: _language == AppLanguage.ru ? 'Справочник' : 'Catalog',
              ),
              NavigationDestination(
                icon: Icon(Icons.tune),
                label: _language == AppLanguage.ru ? 'Настройки' : 'Settings',
              ),
            ],
          );
        },
      ),
    );
  }

  var _serviceCenterOpenRequest = 0;

  void _openServiceCenters() {
    setState(() {
      _selectedIndex = 3;
      _serviceCenterOpenRequest++;
    });
  }

  Future<void> _createVehicle(Map<String, dynamic> data) async {
    await _api.createVehicle(data);
    await _refresh();
  }

  Future<void> _createDemoSession(int vehicleId) async {
    final session = await _api.createDemoSession(vehicleId);
    _upsertSession(session);
  }

  Future<void> _wifiScan(int vehicleId, String host, int port) async {
    final session = await _api.wifiScan(
      vehicleId: vehicleId,
      host: host,
      port: port,
    );
    _upsertSession(session);
  }

  void _upsertSession(DiagnosticSession session) {
    setState(() {
      _error = null;
      _sessions = [
        session,
        ..._sessions.where((item) => item.id != session.id),
      ];
    });
  }
}

class DashboardPage extends StatelessWidget {
  const DashboardPage({
    required this.vehicles,
    required this.sessions,
    required this.loading,
    required this.error,
    required this.language,
    required this.onFindServiceCenters,
    required this.onRefresh,
    super.key,
  });

  final List<Vehicle> vehicles;
  final List<DiagnosticSession> sessions;
  final bool loading;
  final String? error;
  final AppLanguage language;
  final VoidCallback onFindServiceCenters;
  final Future<void> Function() onRefresh;

  @override
  Widget build(BuildContext context) {
    final latest = sessions.isEmpty ? null : sessions.first;
    final criticalFaults = sessions
        .expand((session) => session.faultCodes)
        .where(
          (fault) => fault.severity == 'CRITICAL' || fault.severity == 'HIGH',
        )
        .length;
    return AppScroll(
      children: [
        if (error != null)
          BannerPanel(
            icon: Icons.cloud_off,
            title: language == AppLanguage.ru
                ? 'Backend недоступен'
                : 'Backend unavailable',
            message: error!,
            action: FilledButton.icon(
              onPressed: onRefresh,
              icon: const Icon(Icons.refresh),
              label: Text(tr(language, 'Повторить', 'Retry')),
            ),
          ),
        if (loading) const LinearProgressIndicator(),
        Wrap(
          spacing: 12,
          runSpacing: 12,
          children: [
            MetricCard(
              label: language == AppLanguage.ru ? 'Авто' : 'Vehicles',
              value: vehicles.length.toString(),
              icon: Icons.directions_car,
            ),
            MetricCard(
              label: language == AppLanguage.ru ? 'Сессии' : 'Sessions',
              value: sessions.length.toString(),
              icon: Icons.assignment,
            ),
            MetricCard(
              label: language == AppLanguage.ru ? 'Риски' : 'Risk faults',
              value: criticalFaults.toString(),
              icon: Icons.warning_amber,
            ),
            MetricCard(
              label: language == AppLanguage.ru ? 'Оценка' : 'Last score',
              value: latest?.report?.healthScore.toString() ?? '-',
              icon: Icons.speed,
            ),
          ],
        ),
        if (latest != null)
          SessionDetails(
            session: latest,
            language: language,
            onFindServiceCenters: onFindServiceCenters,
          ),
      ],
    );
  }
}

class VehiclesPage extends StatelessWidget {
  const VehiclesPage({
    required this.vehicles,
    required this.language,
    required this.onCreate,
    required this.onRefresh,
    super.key,
  });

  final List<Vehicle> vehicles;
  final AppLanguage language;
  final Future<void> Function(Map<String, dynamic> data) onCreate;
  final Future<void> Function() onRefresh;

  @override
  Widget build(BuildContext context) {
    return AppScroll(
      children: [
        SectionHeader(
          title: language == AppLanguage.ru ? 'Автомобили' : 'Vehicles',
          trailing: FilledButton.icon(
            onPressed: () => showDialog<void>(
              context: context,
              builder: (_) =>
                  VehicleDialog(language: language, onCreate: onCreate),
            ),
            icon: const Icon(Icons.add),
            label: Text(tr(language, 'Добавить', 'Add')),
          ),
        ),
        if (vehicles.isEmpty)
          EmptyPanel(
            icon: Icons.directions_car_outlined,
            title: language == AppLanguage.ru
                ? 'Нет автомобилей'
                : 'No vehicles',
            message: language == AppLanguage.ru
                ? 'Добавьте автомобиль, чтобы привязать диагностические сессии.'
                : 'Create a vehicle to attach diagnostic sessions.',
          )
        else
          ...vehicles.map(
            (vehicle) => Card(
              child: ListTile(
                leading: const Icon(Icons.directions_car),
                title: Text('${vehicle.brand} ${vehicle.model}'),
                subtitle: Text(
                  '${vehicle.vin} | ${vehicle.productionYear} | ${vehicle.engineType}',
                ),
                trailing: Text('#${vehicle.id}'),
              ),
            ),
          ),
      ],
    );
  }
}

class DiagnosticsPage extends StatefulWidget {
  const DiagnosticsPage({
    required this.vehicles,
    required this.sessions,
    required this.language,
    required this.onFindServiceCenters,
    required this.onCreateDemoSession,
    required this.onWifiScan,
    required this.onRefresh,
    super.key,
  });

  final List<Vehicle> vehicles;
  final List<DiagnosticSession> sessions;
  final AppLanguage language;
  final VoidCallback onFindServiceCenters;
  final Future<void> Function(int vehicleId) onCreateDemoSession;
  final Future<void> Function(int vehicleId, String host, int port) onWifiScan;
  final Future<void> Function() onRefresh;

  @override
  State<DiagnosticsPage> createState() => _DiagnosticsPageState();
}

class _DiagnosticsPageState extends State<DiagnosticsPage> {
  final _hostController = TextEditingController(text: '192.168.0.10');
  final _portController = TextEditingController(text: '35000');
  final _dtcFilterController = TextEditingController();
  final _fromDateController = TextEditingController();
  final _toDateController = TextEditingController();
  int? _vehicleId;
  int? _filterVehicleId;
  String? _filterRisk;
  var _busy = false;

  @override
  void dispose() {
    _hostController.dispose();
    _portController.dispose();
    _dtcFilterController.dispose();
    _fromDateController.dispose();
    _toDateController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    _vehicleId ??= widget.vehicles.firstOrNull?.id;
    final filteredSessions = _filteredSessions();
    return AppScroll(
      children: [
        SectionHeader(
          title: widget.language == AppLanguage.ru
              ? 'Диагностика'
              : 'Diagnostics',
          trailing: IconButton(
            tooltip: tr(widget.language, 'Обновить', 'Refresh'),
            onPressed: widget.onRefresh,
            icon: const Icon(Icons.refresh),
          ),
        ),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Wrap(
              spacing: 12,
              runSpacing: 12,
              crossAxisAlignment: WrapCrossAlignment.center,
              children: [
                SizedBox(
                  width: 280,
                  child: DropdownButtonFormField<int>(
                    initialValue: _vehicleId,
                    items: widget.vehicles
                        .map(
                          (vehicle) => DropdownMenuItem(
                            value: vehicle.id,
                            child: Text('${vehicle.brand} ${vehicle.model}'),
                          ),
                        )
                        .toList(),
                    onChanged: (value) => setState(() => _vehicleId = value),
                    decoration: InputDecoration(
                      labelText: widget.language == AppLanguage.ru
                          ? 'Авто'
                          : 'Vehicle',
                    ),
                  ),
                ),
                SizedBox(
                  width: 180,
                  child: TextField(
                    controller: _hostController,
                    decoration: InputDecoration(
                      labelText: widget.language == AppLanguage.ru
                          ? 'ELM327 адрес'
                          : 'ELM327 host',
                    ),
                  ),
                ),
                SizedBox(
                  width: 120,
                  child: TextField(
                    controller: _portController,
                    keyboardType: TextInputType.number,
                    decoration: InputDecoration(
                      labelText: tr(widget.language, 'Порт', 'Port'),
                    ),
                  ),
                ),
                FilledButton.icon(
                  onPressed: _vehicleId == null || _busy ? null : _runDemo,
                  icon: const Icon(Icons.play_arrow),
                  label: Text(
                    widget.language == AppLanguage.ru
                        ? 'Демо-скан'
                        : 'Demo ingest',
                  ),
                ),
                OutlinedButton.icon(
                  onPressed: _vehicleId == null || _busy ? null : _runWifi,
                  icon: const Icon(Icons.wifi),
                  label: Text(
                    widget.language == AppLanguage.ru
                        ? 'Wi-Fi скан'
                        : 'Wi-Fi scan',
                  ),
                ),
              ],
            ),
          ),
        ),
        if (_busy) const LinearProgressIndicator(),
        _buildHistoryFilters(),
        if (filteredSessions.isEmpty)
          EmptyPanel(
            icon: Icons.monitor_heart_outlined,
            title: widget.language == AppLanguage.ru
                ? 'Нет сессий'
                : 'No sessions',
            message: widget.language == AppLanguage.ru
                ? 'Запустите сканирование или измените фильтры.'
                : 'Run a scan or adjust filters.',
          )
        else
          ...filteredSessions.map(
            (session) => SessionDetails(
              session: session,
              language: widget.language,
              onFindServiceCenters: widget.onFindServiceCenters,
            ),
          ),
      ],
    );
  }

  Future<void> _runDemo() async {
    await _submit(() => widget.onCreateDemoSession(_vehicleId!));
  }

  Future<void> _runWifi() async {
    final port = int.tryParse(_portController.text) ?? 35000;
    await _submit(
      () => widget.onWifiScan(_vehicleId!, _hostController.text, port),
    );
  }

  Widget _buildHistoryFilters() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Wrap(
          spacing: 12,
          runSpacing: 12,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            SizedBox(
              width: 240,
              child: DropdownButtonFormField<int?>(
                initialValue: _filterVehicleId,
                items: [
                  DropdownMenuItem<int?>(
                    value: null,
                    child: Text(
                      tr(widget.language, 'Все авто', 'All vehicles'),
                    ),
                  ),
                  ...widget.vehicles.map(
                    (vehicle) => DropdownMenuItem<int?>(
                      value: vehicle.id,
                      child: Text('${vehicle.brand} ${vehicle.model}'),
                    ),
                  ),
                ],
                onChanged: (value) => setState(() => _filterVehicleId = value),
                decoration: InputDecoration(
                  labelText: tr(widget.language, 'Автомобиль', 'Vehicle'),
                ),
              ),
            ),
            SizedBox(
              width: 190,
              child: DropdownButtonFormField<String?>(
                initialValue: _filterRisk,
                items: [
                  DropdownMenuItem<String?>(
                    value: null,
                    child: Text(tr(widget.language, 'Любой риск', 'Any risk')),
                  ),
                  ...['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map(
                    (risk) => DropdownMenuItem<String?>(
                      value: risk,
                      child: Text(severityLabel(risk, widget.language)),
                    ),
                  ),
                ],
                onChanged: (value) => setState(() => _filterRisk = value),
                decoration: InputDecoration(
                  labelText: tr(widget.language, 'Уровень риска', 'Risk level'),
                ),
              ),
            ),
            SizedBox(
              width: 150,
              child: TextField(
                controller: _dtcFilterController,
                decoration: InputDecoration(
                  labelText: tr(widget.language, 'DTC-код', 'DTC code'),
                  prefixIcon: const Icon(Icons.code),
                ),
                onChanged: (_) => setState(() {}),
              ),
            ),
            SizedBox(
              width: 150,
              child: TextField(
                controller: _fromDateController,
                decoration: InputDecoration(
                  labelText: tr(widget.language, 'Дата с', 'From date'),
                  hintText: '2026-05-01',
                ),
                onChanged: (_) => setState(() {}),
              ),
            ),
            SizedBox(
              width: 150,
              child: TextField(
                controller: _toDateController,
                decoration: InputDecoration(
                  labelText: tr(widget.language, 'Дата по', 'To date'),
                  hintText: '2026-05-11',
                ),
                onChanged: (_) => setState(() {}),
              ),
            ),
            TextButton.icon(
              onPressed: _clearFilters,
              icon: const Icon(Icons.filter_alt_off),
              label: Text(tr(widget.language, 'Сбросить', 'Reset')),
            ),
          ],
        ),
      ),
    );
  }

  List<DiagnosticSession> _filteredSessions() {
    final dtc = _dtcFilterController.text.trim().toUpperCase();
    final fromDate = DateTime.tryParse(_fromDateController.text.trim());
    final rawToDate = DateTime.tryParse(_toDateController.text.trim());
    final toDate = rawToDate == null
        ? null
        : DateTime(rawToDate.year, rawToDate.month, rawToDate.day, 23, 59, 59);
    return widget.sessions.where((session) {
      if (_filterVehicleId != null && session.vehicle.id != _filterVehicleId) {
        return false;
      }
      if (dtc.isNotEmpty &&
          !session.faultCodes.any(
            (fault) => fault.code.toUpperCase().contains(dtc),
          )) {
        return false;
      }
      if (_filterRisk != null &&
          !session.faultCodes.any(
            (fault) => riskRank(fault.severity) >= riskRank(_filterRisk!),
          )) {
        return false;
      }
      final startedAt = DateTime.tryParse(session.startedAt);
      if (fromDate != null &&
          startedAt != null &&
          startedAt.isBefore(fromDate)) {
        return false;
      }
      if (toDate != null && startedAt != null && startedAt.isAfter(toDate)) {
        return false;
      }
      return true;
    }).toList();
  }

  void _clearFilters() {
    setState(() {
      _filterVehicleId = null;
      _filterRisk = null;
      _dtcFilterController.clear();
      _fromDateController.clear();
      _toDateController.clear();
    });
  }

  Future<void> _submit(Future<void> Function() action) async {
    setState(() => _busy = true);
    try {
      await action();
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            tr(
              widget.language,
              'Диагностическая сессия создана',
              'Diagnostic session created',
            ),
          ),
        ),
      );
    } catch (error) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text(error.toString())));
    } finally {
      if (mounted) {
        setState(() => _busy = false);
      }
    }
  }
}

class CatalogPage extends StatefulWidget {
  const CatalogPage({
    required this.api,
    required this.language,
    required this.serviceCenterOpenRequest,
    super.key,
  });

  final CarAnalyticsApi api;
  final AppLanguage language;
  final int serviceCenterOpenRequest;

  @override
  State<CatalogPage> createState() => _CatalogPageState();
}

class _CatalogPageState extends State<CatalogPage> {
  final _queryController = TextEditingController(text: 'P0118');
  final _latitudeController = TextEditingController(text: '43.2565');
  final _longitudeController = TextEditingController(text: '76.9284');
  final _radiusController = TextEditingController(text: '5');
  var _tab = 0;
  var _loading = false;
  var _locating = false;
  List<CatalogItem> _items = [];
  List<ServiceCenter> _serviceCenters = [];

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void didUpdateWidget(covariant CatalogPage oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.serviceCenterOpenRequest != widget.serviceCenterOpenRequest) {
      _queryController.text = 'Almaty';
      _latitudeController.text = '43.2565';
      _longitudeController.text = '76.9284';
      _radiusController.text = '5';
      setState(() => _tab = 2);
      _load();
    }
  }

  @override
  void dispose() {
    _queryController.dispose();
    _latitudeController.dispose();
    _longitudeController.dispose();
    _radiusController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AppScroll(
      children: [
        SectionHeader(
          title: widget.language == AppLanguage.ru ? 'Справочник' : 'Catalog',
          trailing: SegmentedButton<int>(
            segments: [
              ButtonSegment(
                value: 0,
                icon: const Icon(Icons.code),
                label: const Text('DTC'),
              ),
              ButtonSegment(
                value: 1,
                icon: const Icon(Icons.build),
                label: Text(tr(widget.language, 'Запчасти', 'Parts')),
              ),
              ButtonSegment(
                value: 2,
                icon: const Icon(Icons.local_hospital),
                label: Text(tr(widget.language, 'СТО', 'Service')),
              ),
            ],
            selected: {_tab},
            onSelectionChanged: (value) {
              final nextTab = value.first;
              setState(() {
                _tab = nextTab;
                if (_tab == 2 && _queryController.text.startsWith('P')) {
                  _queryController.text = 'Almaty';
                }
                if (_tab != 2 && _queryController.text == 'Almaty') {
                  _queryController.text = 'P0118';
                }
              });
              _load();
            },
          ),
        ),
        _tab == 2 ? _buildServiceCenterSearch() : _buildCatalogSearch(),
        if (_loading) const LinearProgressIndicator(),
        if (_tab == 2)
          ..._buildServiceCenterResults()
        else
          ..._buildCatalogResults(),
      ],
    );
  }

  Widget _buildCatalogSearch() {
    return Row(
      children: [
        Expanded(
          child: TextField(
            controller: _queryController,
            decoration: InputDecoration(
              labelText: tr(widget.language, 'Код ошибки', 'Fault code'),
              prefixIcon: const Icon(Icons.search),
            ),
            onSubmitted: (_) => _load(),
          ),
        ),
        const SizedBox(width: 12),
        FilledButton.icon(
          onPressed: _load,
          icon: const Icon(Icons.search),
          label: Text(widget.language == AppLanguage.ru ? 'Найти' : 'Search'),
        ),
      ],
    );
  }

  Widget _buildServiceCenterSearch() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Wrap(
          spacing: 12,
          runSpacing: 12,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            SizedBox(
              width: 180,
              child: TextField(
                controller: _queryController,
                decoration: InputDecoration(
                  labelText: widget.language == AppLanguage.ru
                      ? 'Город'
                      : 'City',
                  prefixIcon: const Icon(Icons.location_city),
                ),
                onSubmitted: (_) => _load(),
              ),
            ),
            SizedBox(
              width: 150,
              child: TextField(
                controller: _latitudeController,
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                  signed: true,
                ),
                decoration: InputDecoration(
                  labelText: tr(widget.language, 'Широта', 'Latitude'),
                ),
              ),
            ),
            SizedBox(
              width: 150,
              child: TextField(
                controller: _longitudeController,
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                  signed: true,
                ),
                decoration: InputDecoration(
                  labelText: tr(widget.language, 'Долгота', 'Longitude'),
                ),
              ),
            ),
            SizedBox(
              width: 120,
              child: TextField(
                controller: _radiusController,
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
                decoration: InputDecoration(
                  labelText: widget.language == AppLanguage.ru
                      ? 'Радиус, км'
                      : 'Radius km',
                ),
              ),
            ),
            FilledButton.icon(
              onPressed: _load,
              icon: const Icon(Icons.map),
              label: Text(widget.language == AppLanguage.ru ? 'Найти' : 'Find'),
            ),
            OutlinedButton.icon(
              onPressed: _locating ? null : _useCurrentLocation,
              icon: _locating
                  ? const SizedBox.square(
                      dimension: 18,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Icon(Icons.near_me),
              label: Text(
                tr(
                  widget.language,
                  'Использовать моё местоположение',
                  'Use my location',
                ),
              ),
            ),
            OutlinedButton.icon(
              onPressed: () {
                _queryController.text = 'Almaty';
                _latitudeController.text = '43.2565';
                _longitudeController.text = '76.9284';
                _radiusController.text = '5';
                _load();
              },
              icon: const Icon(Icons.my_location),
              label: const Text('Almaty'),
            ),
          ],
        ),
      ),
    );
  }

  List<Widget> _buildCatalogResults() {
    if (_items.isEmpty && !_loading) {
      return [
        EmptyPanel(
          icon: Icons.inventory_2_outlined,
          title: widget.language == AppLanguage.ru
              ? 'Ничего не найдено'
              : 'No catalog results',
          message: widget.language == AppLanguage.ru
              ? 'Попробуйте другой код.'
              : 'Try another code.',
        ),
      ];
    }
    return _items
        .map(
          (item) => Card(
            child: ListTile(
              title: Text(item.localizedTitle(widget.language)),
              subtitle: Text(item.localizedSubtitle(widget.language)),
              trailing: item.badge.isEmpty
                  ? null
                  : Chip(label: Text(_catalogBadgeLabel(item.badge))),
            ),
          ),
        )
        .toList();
  }

  String _catalogBadgeLabel(String badge) {
    final normalized = badge.toUpperCase();
    if (normalized == badge && RegExp(r'^[A-Z_]+$').hasMatch(badge)) {
      return severityLabel(badge, widget.language);
    }
    return badge;
  }

  List<Widget> _buildServiceCenterResults() {
    if (_serviceCenters.isEmpty && !_loading) {
      return [
        EmptyPanel(
          icon: Icons.local_hospital_outlined,
          title: widget.language == AppLanguage.ru
              ? 'СТО не найдены'
              : 'No service centers',
          message: widget.language == AppLanguage.ru
              ? 'Попробуйте другой город, координаты или радиус.'
              : 'Try another city, coordinates, or radius.',
        ),
      ];
    }
    return [
      if (_serviceCenters.isNotEmpty)
        ServiceCenterMap(
          centers: _serviceCenters,
          originLatitude: double.tryParse(_latitudeController.text),
          originLongitude: double.tryParse(_longitudeController.text),
        ),
      ..._serviceCenters.map(
        (center) => Card(
          child: ListTile(
            leading: Icon(
              center.emergencySupport ? Icons.local_hospital : Icons.car_repair,
            ),
            title: Text(center.name),
            subtitle: Text(
              '${center.address}\n${center.phone} | ${serviceSpecializationLabel(center.specialization, widget.language)}',
            ),
            trailing: Wrap(
              spacing: 6,
              children: [
                Chip(label: Text(center.distanceLabel(widget.language))),
                Chip(label: Text(center.badge)),
              ],
            ),
          ),
        ),
      ),
    ];
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    try {
      final query = _queryController.text.trim();
      if (_tab == 2) {
        final latitude = double.tryParse(_latitudeController.text);
        final longitude = double.tryParse(_longitudeController.text);
        final hasCoordinates = latitude != null && longitude != null;
        final centers = await widget.api.serviceCenters(
          city: query.isEmpty && !hasCoordinates ? 'Almaty' : query,
          latitude: latitude,
          longitude: longitude,
          maxDistanceKm: double.tryParse(_radiusController.text),
        );
        if (mounted) {
          setState(() {
            _serviceCenters = centers;
            _items = [];
          });
        }
        return;
      }

      final items = switch (_tab) {
        0 => await widget.api.faultCodes(code: query),
        _ => await widget.api.spareParts(faultCode: query),
      };
      if (mounted) {
        setState(() {
          _items = items;
          _serviceCenters = [];
        });
      }
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
      }
    } finally {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  Future<void> _useCurrentLocation() async {
    setState(() => _locating = true);
    try {
      final serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        throw Exception(
          tr(
            widget.language,
            'Геолокация выключена на устройстве.',
            'Location services are disabled on this device.',
          ),
        );
      }

      var permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }
      if (permission == LocationPermission.denied) {
        throw Exception(
          tr(
            widget.language,
            'Браузер не получил доступ к местоположению.',
            'The browser did not get location permission.',
          ),
        );
      }
      if (permission == LocationPermission.deniedForever) {
        throw Exception(
          tr(
            widget.language,
            'Доступ к местоположению запрещен в настройках.',
            'Location permission is blocked in settings.',
          ),
        );
      }

      final settings = kIsWeb
          ? WebSettings(
              accuracy: LocationAccuracy.high,
              maximumAge: const Duration(minutes: 5),
            )
          : const LocationSettings(
              accuracy: LocationAccuracy.high,
              timeLimit: Duration(seconds: 12),
            );
      final position = await Geolocator.getCurrentPosition(
        locationSettings: settings,
      );
      _queryController.text = '';
      _latitudeController.text = position.latitude.toStringAsFixed(6);
      _longitudeController.text = position.longitude.toStringAsFixed(6);
      await _load();
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
      }
    } finally {
      if (mounted) {
        setState(() => _locating = false);
      }
    }
  }
}

class ServiceCenterMap extends StatelessWidget {
  const ServiceCenterMap({
    required this.centers,
    required this.originLatitude,
    required this.originLongitude,
    super.key,
  });

  final List<ServiceCenter> centers;
  final double? originLatitude;
  final double? originLongitude;

  @override
  Widget build(BuildContext context) {
    final origin = originLatitude == null || originLongitude == null
        ? null
        : LatLng(originLatitude!, originLongitude!);
    final mapCenter =
        origin ?? LatLng(centers.first.latitude, centers.first.longitude);
    final markers = [
      if (origin != null)
        Marker(
          point: origin,
          width: 48,
          height: 48,
          child: const Icon(
            Icons.my_location,
            color: Color(0xff006d67),
            size: 34,
          ),
        ),
      ...centers.map(
        (center) => Marker(
          point: LatLng(center.latitude, center.longitude),
          width: 48,
          height: 48,
          child: Icon(
            center.emergencySupport ? Icons.local_hospital : Icons.car_repair,
            color: center.emergencySupport
                ? Theme.of(context).colorScheme.error
                : Theme.of(context).colorScheme.tertiary,
            size: 34,
          ),
        ),
      ),
    ];

    return Card(
      clipBehavior: Clip.antiAlias,
      child: SizedBox(
        height: 320,
        child: Stack(
          children: [
            FlutterMap(
              options: MapOptions(
                initialCenter: mapCenter,
                initialZoom: 13,
                minZoom: 3,
                maxZoom: 18,
              ),
              children: [
                TileLayer(
                  urlTemplate: mapTileUrl,
                  userAgentPackageName: 'com.example.car_analytics_app',
                ),
                MarkerLayer(markers: markers),
              ],
            ),
            Positioned(
              left: 8,
              bottom: 8,
              child: DecoratedBox(
                decoration: BoxDecoration(
                  color: Colors.white.withValues(alpha: 0.86),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Padding(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 6,
                    vertical: 3,
                  ),
                  child: Text(
                    mapTileAttribution,
                    style: const TextStyle(fontSize: 11),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class SettingsPage extends StatelessWidget {
  const SettingsPage({
    required this.controller,
    required this.profileNameController,
    required this.profilePhoneController,
    required this.profileEmailController,
    required this.vehiclesCount,
    required this.sessionsCount,
    required this.language,
    required this.onLanguageChanged,
    required this.onApply,
    required this.onProfileChanged,
    super.key,
  });

  final TextEditingController controller;
  final TextEditingController profileNameController;
  final TextEditingController profilePhoneController;
  final TextEditingController profileEmailController;
  final int vehiclesCount;
  final int sessionsCount;
  final AppLanguage language;
  final ValueChanged<AppLanguage> onLanguageChanged;
  final VoidCallback onApply;
  final VoidCallback onProfileChanged;

  @override
  Widget build(BuildContext context) {
    return AppScroll(
      children: [
        SectionHeader(
          title: language == AppLanguage.ru ? 'Настройки' : 'Settings',
        ),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  tr(language, 'Профиль водителя', 'Driver profile'),
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 12,
                  runSpacing: 12,
                  children: [
                    SizedBox(
                      width: 260,
                      child: TextField(
                        controller: profileNameController,
                        decoration: InputDecoration(
                          labelText: tr(language, 'Имя', 'Name'),
                          prefixIcon: const Icon(Icons.person),
                        ),
                        onChanged: (_) => onProfileChanged(),
                      ),
                    ),
                    SizedBox(
                      width: 220,
                      child: TextField(
                        controller: profilePhoneController,
                        decoration: InputDecoration(
                          labelText: tr(language, 'Телефон', 'Phone'),
                          prefixIcon: const Icon(Icons.phone),
                        ),
                        onChanged: (_) => onProfileChanged(),
                      ),
                    ),
                    SizedBox(
                      width: 260,
                      child: TextField(
                        controller: profileEmailController,
                        decoration: InputDecoration(
                          labelText: tr(language, 'Email', 'Email'),
                          prefixIcon: const Icon(Icons.mail),
                        ),
                        onChanged: (_) => onProfileChanged(),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [
                    Chip(
                      avatar: const Icon(Icons.directions_car, size: 18),
                      label: Text(
                        '${tr(language, 'Авто', 'Vehicles')}: $vehiclesCount',
                      ),
                    ),
                    Chip(
                      avatar: const Icon(Icons.assignment, size: 18),
                      label: Text(
                        '${tr(language, 'История', 'History')}: $sessionsCount',
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                TextField(
                  controller: controller,
                  decoration: InputDecoration(
                    labelText: tr(language, 'Адрес backend', 'Backend URL'),
                    helperText: language == AppLanguage.ru
                        ? 'Для Android emulator обычно нужен http://10.0.2.2:8080'
                        : 'Android emulator usually uses http://10.0.2.2:8080',
                  ),
                ),
                const SizedBox(height: 12),
                SegmentedButton<AppLanguage>(
                  segments: const [
                    ButtonSegment(
                      value: AppLanguage.ru,
                      label: Text('Русский'),
                    ),
                    ButtonSegment(
                      value: AppLanguage.en,
                      label: Text('English'),
                    ),
                  ],
                  selected: {language},
                  onSelectionChanged: (value) => onLanguageChanged(value.first),
                ),
                const SizedBox(height: 12),
                FilledButton.icon(
                  onPressed: onApply,
                  icon: const Icon(Icons.check),
                  label: Text(
                    language == AppLanguage.ru ? 'Применить' : 'Apply',
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class VehicleDialog extends StatefulWidget {
  const VehicleDialog({
    required this.language,
    required this.onCreate,
    super.key,
  });

  final AppLanguage language;
  final Future<void> Function(Map<String, dynamic> data) onCreate;

  @override
  State<VehicleDialog> createState() => _VehicleDialogState();
}

class _VehicleDialogState extends State<VehicleDialog> {
  final _formKey = GlobalKey<FormState>();
  final _vin = TextEditingController(text: 'WAUZZZ8V7JA123456');
  final _brand = TextEditingController(text: 'Audi');
  final _model = TextEditingController(text: 'A4');
  final _year = TextEditingController(text: '2018');
  final _engine = TextEditingController(text: '2.0 TFSI');
  var _saving = false;

  @override
  void dispose() {
    _vin.dispose();
    _brand.dispose();
    _model.dispose();
    _year.dispose();
    _engine.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(tr(widget.language, 'Добавить автомобиль', 'Add vehicle')),
      content: SizedBox(
        width: 420,
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              AppField(
                controller: _vin,
                label: 'VIN',
                language: widget.language,
              ),
              AppField(
                controller: _brand,
                label: tr(widget.language, 'Марка', 'Brand'),
                language: widget.language,
              ),
              AppField(
                controller: _model,
                label: tr(widget.language, 'Модель', 'Model'),
                language: widget.language,
              ),
              AppField(
                controller: _year,
                label: tr(widget.language, 'Год', 'Year'),
                language: widget.language,
                number: true,
              ),
              AppField(
                controller: _engine,
                label: tr(widget.language, 'Двигатель', 'Engine'),
                language: widget.language,
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: _saving ? null : () => Navigator.of(context).pop(),
          child: Text(tr(widget.language, 'Отмена', 'Cancel')),
        ),
        FilledButton.icon(
          onPressed: _saving ? null : _save,
          icon: const Icon(Icons.save),
          label: Text(tr(widget.language, 'Сохранить', 'Save')),
        ),
      ],
    );
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }
    setState(() => _saving = true);
    try {
      await widget.onCreate({
        'vin': _vin.text.trim(),
        'brand': _brand.text.trim(),
        'model': _model.text.trim(),
        'productionYear': int.parse(_year.text),
        'engineType': _engine.text.trim(),
      });
      if (mounted) {
        Navigator.of(context).pop();
      }
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
      }
    } finally {
      if (mounted) {
        setState(() => _saving = false);
      }
    }
  }
}

class SessionDetails extends StatelessWidget {
  const SessionDetails({
    required this.session,
    required this.language,
    required this.onFindServiceCenters,
    super.key,
  });

  final DiagnosticSession session;
  final AppLanguage language;
  final VoidCallback onFindServiceCenters;

  @override
  Widget build(BuildContext context) {
    final report = session.report;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Wrap(
              spacing: 8,
              runSpacing: 8,
              crossAxisAlignment: WrapCrossAlignment.center,
              children: [
                Text(
                  '${session.vehicle.brand} ${session.vehicle.model}',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                Chip(
                  label: Text(
                    connectionTypeLabel(session.connectionType, language),
                  ),
                ),
                Chip(label: Text(session.protocol)),
                Chip(label: Text(statusLabel(session.overallStatus, language))),
                OutlinedButton.icon(
                  onPressed: () =>
                      _exportDiagnosticReport(context, session, language),
                  icon: const Icon(Icons.picture_as_pdf),
                  label: Text(
                    tr(language, 'Скачать PDF-отчет', 'Download PDF report'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            if (report != null)
              ReportPanel(
                report: report,
                language: language,
                onFindServiceCenters: onFindServiceCenters,
              ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                InfoBlock(
                  title: language == AppLanguage.ru ? 'Параметры' : 'Readings',
                  children: session.readings
                      .map(
                        (reading) =>
                            '${reading.parameterName}: ${reading.value} ${reading.unit}',
                      )
                      .toList(),
                ),
                InfoBlock(
                  title: language == AppLanguage.ru ? 'Ошибки' : 'Faults',
                  children: session.faultCodes
                      .map(
                        (fault) =>
                            '${fault.code} ${severityLabel(fault.severity, language)} ${fault.localizedDescription(language)}',
                      )
                      .toList(),
                ),
                InfoBlock(
                  title: language == AppLanguage.ru
                      ? 'Рекомендации'
                      : 'Recommendations',
                  children: session.recommendations
                      .map(
                        (item) =>
                            '${item.localizedActionLabel(language)}: ${item.localizedMessage(language)}',
                      )
                      .toList(),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class ReportPanel extends StatelessWidget {
  const ReportPanel({
    required this.report,
    required this.language,
    required this.onFindServiceCenters,
    super.key,
  });

  final DiagnosticReport report;
  final AppLanguage language;
  final VoidCallback onFindServiceCenters;

  @override
  Widget build(BuildContext context) {
    final color = report.towRecommended
        ? Colors.red
        : report.drivable
        ? Colors.green
        : Colors.orange;
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(8),
        color: color.withValues(alpha: 0.08),
        border: Border.all(color: color.withValues(alpha: 0.35)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Wrap(
            spacing: 10,
            runSpacing: 8,
            crossAxisAlignment: WrapCrossAlignment.center,
            children: [
              Text(
                'Health ${report.healthScore}/100',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              Chip(label: Text(urgencyLabel(report.urgency, language))),
              Chip(
                label: Text(
                  report.drivable
                      ? (language == AppLanguage.ru
                            ? 'Можно ехать'
                            : 'Drivable')
                      : (language == AppLanguage.ru
                            ? 'Не ехать'
                            : 'Do not drive'),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(report.primaryIssue),
          if (report.localizedSummary(language).isNotEmpty)
            Text(report.localizedSummary(language)),
          if (report.localizedRiskForecast(language).isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(
              language == AppLanguage.ru ? 'Прогноз риска' : 'Risk forecast',
              style: Theme.of(context).textTheme.labelLarge,
            ),
            Text(report.localizedRiskForecast(language)),
          ],
          if (report.localizedNextActions(language).isNotEmpty) ...[
            const SizedBox(height: 8),
            ...report
                .localizedNextActions(language)
                .map((action) => Text('- $action')),
          ],
          if (report.urgency != 'MONITOR') ...[
            const SizedBox(height: 12),
            FilledButton.icon(
              onPressed: onFindServiceCenters,
              icon: const Icon(Icons.local_hospital),
              label: Text(
                language == AppLanguage.ru
                    ? 'Найти ближайшее СТО'
                    : 'Find nearby service centers',
              ),
            ),
          ],
        ],
      ),
    );
  }
}

Future<void> _exportDiagnosticReport(
  BuildContext context,
  DiagnosticSession session,
  AppLanguage language,
) async {
  try {
    final regularFont = await PdfGoogleFonts.notoSansRegular();
    final boldFont = await PdfGoogleFonts.notoSansBold();
    final theme = pw.ThemeData.withFont(base: regularFont, bold: boldFont);
    final report = session.report;
    final document = pw.Document(theme: theme);

    pw.Widget section(String title, List<String> lines) {
      final visibleLines = lines
          .where((line) => line.trim().isNotEmpty)
          .toList();
      if (visibleLines.isEmpty) {
        return pw.SizedBox.shrink();
      }
      return pw.Container(
        margin: const pw.EdgeInsets.only(top: 14),
        child: pw.Column(
          crossAxisAlignment: pw.CrossAxisAlignment.start,
          children: [
            pw.Text(
              title,
              style: pw.TextStyle(fontSize: 14, fontWeight: pw.FontWeight.bold),
            ),
            pw.SizedBox(height: 6),
            ...visibleLines.map(
              (line) => pw.Padding(
                padding: const pw.EdgeInsets.only(bottom: 3),
                child: pw.Text(line, style: const pw.TextStyle(fontSize: 11)),
              ),
            ),
          ],
        ),
      );
    }

    document.addPage(
      pw.MultiPage(
        pageFormat: PdfPageFormat.a4,
        margin: const pw.EdgeInsets.all(32),
        build: (pdfContext) => [
          pw.Text(
            tr(language, 'Диагностический отчет', 'Diagnostic report'),
            style: pw.TextStyle(fontSize: 22, fontWeight: pw.FontWeight.bold),
          ),
          pw.SizedBox(height: 8),
          pw.Text(
            '${session.vehicle.brand} ${session.vehicle.model} | VIN: ${session.vehicle.vin}',
          ),
          pw.Text(
            '${tr(language, 'Сессия', 'Session')}: #${session.id} | ${session.startedAt}',
          ),
          pw.Text(
            '${tr(language, 'Статус', 'Status')}: ${statusLabel(session.overallStatus, language)}',
          ),
          if (report != null)
            section(tr(language, 'Итог', 'Summary'), [
              '${tr(language, 'Оценка здоровья', 'Health score')}: ${report.healthScore}/100',
              '${tr(language, 'Срочность', 'Urgency')}: ${urgencyLabel(report.urgency, language)}',
              report.drivable
                  ? tr(
                      language,
                      'Можно продолжать движение',
                      'Vehicle is drivable',
                    )
                  : tr(language, 'Не продолжайте движение', 'Do not drive'),
              report.primaryIssue,
              report.localizedSummary(language),
            ]),
          if (report != null)
            section(tr(language, 'Прогноз риска', 'Risk forecast'), [
              report.localizedRiskForecast(language),
            ]),
          section(
            tr(language, 'Коды ошибок', 'Fault codes'),
            session.faultCodes
                .map(
                  (fault) =>
                      '${fault.code} | ${severityLabel(fault.severity, language)} | ${fault.localizedDescription(language)}',
                )
                .toList(),
          ),
          section(
            tr(language, 'Показатели', 'Readings'),
            session.readings
                .map(
                  (reading) =>
                      '${reading.parameterName}: ${reading.value} ${reading.unit}',
                )
                .toList(),
          ),
          if (report != null)
            section(
              tr(language, 'Следующие действия', 'Next actions'),
              report.localizedNextActions(language),
            ),
          section(
            tr(language, 'Рекомендации', 'Recommendations'),
            session.recommendations
                .map(
                  (item) =>
                      '${item.localizedActionLabel(language)}: ${item.localizedMessage(language)}',
                )
                .toList(),
          ),
        ],
      ),
    );

    await Printing.sharePdf(
      bytes: await document.save(),
      filename: 'diagnostic-report-${session.id}.pdf',
    );
  } catch (error) {
    if (context.mounted) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text(error.toString())));
    }
  }
}

class MetricCard extends StatelessWidget {
  const MetricCard({
    required this.label,
    required this.value,
    required this.icon,
    super.key,
  });

  final String label;
  final String value;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 210,
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Icon(icon, size: 32),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(label, style: Theme.of(context).textTheme.labelLarge),
                    Text(
                      value,
                      style: Theme.of(context).textTheme.headlineMedium,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class InfoBlock extends StatelessWidget {
  const InfoBlock({required this.title, required this.children, super.key});

  final String title;
  final List<String> children;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 300,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 6),
          if (children.isEmpty)
            const Text('-')
          else
            ...children.map(
              (item) => Padding(
                padding: const EdgeInsets.only(bottom: 4),
                child: Text(item),
              ),
            ),
        ],
      ),
    );
  }
}

class AppField extends StatelessWidget {
  const AppField({
    required this.controller,
    required this.label,
    required this.language,
    this.number = false,
    super.key,
  });

  final TextEditingController controller;
  final String label;
  final AppLanguage language;
  final bool number;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: TextFormField(
        controller: controller,
        keyboardType: number ? TextInputType.number : TextInputType.text,
        decoration: InputDecoration(labelText: label),
        validator: (value) {
          if (value == null || value.trim().isEmpty) {
            return tr(language, 'Обязательное поле', 'Required');
          }
          if (number && int.tryParse(value) == null) {
            return tr(language, 'Введите число', 'Number expected');
          }
          return null;
        },
      ),
    );
  }
}

class SectionHeader extends StatelessWidget {
  const SectionHeader({required this.title, this.trailing, super.key});

  final String title;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Text(title, style: Theme.of(context).textTheme.headlineSmall),
        ),
        ?trailing,
      ],
    );
  }
}

class BannerPanel extends StatelessWidget {
  const BannerPanel({
    required this.icon,
    required this.title,
    required this.message,
    required this.action,
    super.key,
  });

  final IconData icon;
  final String title;
  final String message;
  final Widget action;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Icon(icon),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: Theme.of(context).textTheme.titleMedium),
                  Text(message, maxLines: 3, overflow: TextOverflow.ellipsis),
                ],
              ),
            ),
            const SizedBox(width: 12),
            action,
          ],
        ),
      ),
    );
  }
}

class EmptyPanel extends StatelessWidget {
  const EmptyPanel({
    required this.icon,
    required this.title,
    required this.message,
    super.key,
  });

  final IconData icon;
  final String title;
  final String message;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Row(
          children: [
            Icon(icon, size: 36),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: Theme.of(context).textTheme.titleMedium),
                  Text(message),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class AppScroll extends StatelessWidget {
  const AppScroll({required this.children, super.key});

  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: children.length,
      separatorBuilder: (_, _) => const SizedBox(height: 12),
      itemBuilder: (context, index) => children[index],
    );
  }
}
