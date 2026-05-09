import 'package:flutter/material.dart';

import 'api.dart';

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
  late CarAnalyticsApi _api = CarAnalyticsApi(_baseUrlController.text);
  var _selectedIndex = 0;
  var _loading = true;
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
        onRefresh: _refresh,
      ),
      VehiclesPage(
        vehicles: _vehicles,
        onCreate: _createVehicle,
        onRefresh: _refresh,
      ),
      DiagnosticsPage(
        vehicles: _vehicles,
        sessions: _sessions,
        onCreateDemoSession: _createDemoSession,
        onWifiScan: _wifiScan,
        onRefresh: _refresh,
      ),
      CatalogPage(api: _api),
      SettingsPage(controller: _baseUrlController, onApply: _applyBaseUrl),
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Car Analytics'),
        actions: [
          IconButton(
            tooltip: 'Refresh',
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
                destinations: const [
                  NavigationRailDestination(
                    icon: Icon(Icons.space_dashboard_outlined),
                    selectedIcon: Icon(Icons.space_dashboard),
                    label: Text('Overview'),
                  ),
                  NavigationRailDestination(
                    icon: Icon(Icons.directions_car_outlined),
                    selectedIcon: Icon(Icons.directions_car),
                    label: Text('Vehicles'),
                  ),
                  NavigationRailDestination(
                    icon: Icon(Icons.monitor_heart_outlined),
                    selectedIcon: Icon(Icons.monitor_heart),
                    label: Text('Diagnostics'),
                  ),
                  NavigationRailDestination(
                    icon: Icon(Icons.inventory_2_outlined),
                    selectedIcon: Icon(Icons.inventory_2),
                    label: Text('Catalog'),
                  ),
                  NavigationRailDestination(
                    icon: Icon(Icons.tune),
                    selectedIcon: Icon(Icons.tune),
                    label: Text('Settings'),
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
            destinations: const [
              NavigationDestination(
                icon: Icon(Icons.space_dashboard_outlined),
                selectedIcon: Icon(Icons.space_dashboard),
                label: 'Overview',
              ),
              NavigationDestination(
                icon: Icon(Icons.directions_car_outlined),
                selectedIcon: Icon(Icons.directions_car),
                label: 'Vehicles',
              ),
              NavigationDestination(
                icon: Icon(Icons.monitor_heart_outlined),
                selectedIcon: Icon(Icons.monitor_heart),
                label: 'Diagnostics',
              ),
              NavigationDestination(
                icon: Icon(Icons.inventory_2_outlined),
                selectedIcon: Icon(Icons.inventory_2),
                label: 'Catalog',
              ),
              NavigationDestination(icon: Icon(Icons.tune), label: 'Settings'),
            ],
          );
        },
      ),
    );
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
    required this.onRefresh,
    super.key,
  });

  final List<Vehicle> vehicles;
  final List<DiagnosticSession> sessions;
  final bool loading;
  final String? error;
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
            title: 'Backend unavailable',
            message: error!,
            action: FilledButton.icon(
              onPressed: onRefresh,
              icon: const Icon(Icons.refresh),
              label: const Text('Retry'),
            ),
          ),
        if (loading) const LinearProgressIndicator(),
        Wrap(
          spacing: 12,
          runSpacing: 12,
          children: [
            MetricCard(
              label: 'Vehicles',
              value: vehicles.length.toString(),
              icon: Icons.directions_car,
            ),
            MetricCard(
              label: 'Sessions',
              value: sessions.length.toString(),
              icon: Icons.assignment,
            ),
            MetricCard(
              label: 'Risk faults',
              value: criticalFaults.toString(),
              icon: Icons.warning_amber,
            ),
            MetricCard(
              label: 'Last score',
              value: latest?.report?.healthScore.toString() ?? '-',
              icon: Icons.speed,
            ),
          ],
        ),
        if (latest != null) SessionDetails(session: latest),
      ],
    );
  }
}

class VehiclesPage extends StatelessWidget {
  const VehiclesPage({
    required this.vehicles,
    required this.onCreate,
    required this.onRefresh,
    super.key,
  });

  final List<Vehicle> vehicles;
  final Future<void> Function(Map<String, dynamic> data) onCreate;
  final Future<void> Function() onRefresh;

  @override
  Widget build(BuildContext context) {
    return AppScroll(
      children: [
        SectionHeader(
          title: 'Vehicles',
          trailing: FilledButton.icon(
            onPressed: () => showDialog<void>(
              context: context,
              builder: (_) => VehicleDialog(onCreate: onCreate),
            ),
            icon: const Icon(Icons.add),
            label: const Text('Add'),
          ),
        ),
        if (vehicles.isEmpty)
          const EmptyPanel(
            icon: Icons.directions_car_outlined,
            title: 'No vehicles',
            message: 'Create a vehicle to attach diagnostic sessions.',
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
    required this.onCreateDemoSession,
    required this.onWifiScan,
    required this.onRefresh,
    super.key,
  });

  final List<Vehicle> vehicles;
  final List<DiagnosticSession> sessions;
  final Future<void> Function(int vehicleId) onCreateDemoSession;
  final Future<void> Function(int vehicleId, String host, int port) onWifiScan;
  final Future<void> Function() onRefresh;

  @override
  State<DiagnosticsPage> createState() => _DiagnosticsPageState();
}

class _DiagnosticsPageState extends State<DiagnosticsPage> {
  final _hostController = TextEditingController(text: '192.168.0.10');
  final _portController = TextEditingController(text: '35000');
  int? _vehicleId;
  var _busy = false;

  @override
  void dispose() {
    _hostController.dispose();
    _portController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    _vehicleId ??= widget.vehicles.firstOrNull?.id;
    return AppScroll(
      children: [
        SectionHeader(
          title: 'Diagnostics',
          trailing: IconButton(
            tooltip: 'Refresh',
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
                    decoration: const InputDecoration(labelText: 'Vehicle'),
                  ),
                ),
                SizedBox(
                  width: 180,
                  child: TextField(
                    controller: _hostController,
                    decoration: const InputDecoration(labelText: 'ELM327 host'),
                  ),
                ),
                SizedBox(
                  width: 120,
                  child: TextField(
                    controller: _portController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(labelText: 'Port'),
                  ),
                ),
                FilledButton.icon(
                  onPressed: _vehicleId == null || _busy ? null : _runDemo,
                  icon: const Icon(Icons.play_arrow),
                  label: const Text('Demo ingest'),
                ),
                OutlinedButton.icon(
                  onPressed: _vehicleId == null || _busy ? null : _runWifi,
                  icon: const Icon(Icons.wifi),
                  label: const Text('Wi-Fi scan'),
                ),
              ],
            ),
          ),
        ),
        if (_busy) const LinearProgressIndicator(),
        if (widget.sessions.isEmpty)
          const EmptyPanel(
            icon: Icons.monitor_heart_outlined,
            title: 'No sessions',
            message: 'Run demo ingest or connect an ELM327 Wi-Fi adapter.',
          )
        else
          ...widget.sessions.map((session) => SessionDetails(session: session)),
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

  Future<void> _submit(Future<void> Function() action) async {
    setState(() => _busy = true);
    try {
      await action();
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Diagnostic session created')),
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
  const CatalogPage({required this.api, super.key});

  final CarAnalyticsApi api;

  @override
  State<CatalogPage> createState() => _CatalogPageState();
}

class _CatalogPageState extends State<CatalogPage> {
  final _queryController = TextEditingController(text: 'P0118');
  var _tab = 0;
  var _loading = false;
  List<CatalogItem> _items = [];

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _queryController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AppScroll(
      children: [
        SectionHeader(
          title: 'Catalog',
          trailing: SegmentedButton<int>(
            segments: const [
              ButtonSegment(
                value: 0,
                icon: Icon(Icons.code),
                label: Text('DTC'),
              ),
              ButtonSegment(
                value: 1,
                icon: Icon(Icons.build),
                label: Text('Parts'),
              ),
              ButtonSegment(
                value: 2,
                icon: Icon(Icons.local_hospital),
                label: Text('STO'),
              ),
            ],
            selected: {_tab},
            onSelectionChanged: (value) {
              setState(() => _tab = value.first);
              _load();
            },
          ),
        ),
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _queryController,
                decoration: InputDecoration(
                  labelText: _tab == 2 ? 'City' : 'Fault code',
                  prefixIcon: const Icon(Icons.search),
                ),
                onSubmitted: (_) => _load(),
              ),
            ),
            const SizedBox(width: 12),
            FilledButton.icon(
              onPressed: _load,
              icon: const Icon(Icons.search),
              label: const Text('Search'),
            ),
          ],
        ),
        if (_loading) const LinearProgressIndicator(),
        if (_items.isEmpty && !_loading)
          const EmptyPanel(
            icon: Icons.inventory_2_outlined,
            title: 'No catalog results',
            message: 'Try another code or city.',
          )
        else
          ..._items.map(
            (item) => Card(
              child: ListTile(
                title: Text(item.title),
                subtitle: Text(item.subtitle),
                trailing: item.badge.isEmpty
                    ? null
                    : Chip(label: Text(item.badge)),
              ),
            ),
          ),
      ],
    );
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    try {
      final query = _queryController.text.trim();
      final items = switch (_tab) {
        0 => await widget.api.faultCodes(code: query),
        1 => await widget.api.spareParts(faultCode: query),
        _ => await widget.api.serviceCenters(
          city: query.isEmpty ? 'Almaty' : query,
        ),
      };
      if (mounted) {
        setState(() => _items = items);
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
}

class SettingsPage extends StatelessWidget {
  const SettingsPage({
    required this.controller,
    required this.onApply,
    super.key,
  });

  final TextEditingController controller;
  final VoidCallback onApply;

  @override
  Widget build(BuildContext context) {
    return AppScroll(
      children: [
        const SectionHeader(title: 'Settings'),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: controller,
                    decoration: const InputDecoration(
                      labelText: 'Backend URL',
                      helperText:
                          'Android emulator usually uses http://10.0.2.2:8080',
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                FilledButton.icon(
                  onPressed: onApply,
                  icon: const Icon(Icons.check),
                  label: const Text('Apply'),
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
  const VehicleDialog({required this.onCreate, super.key});

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
      title: const Text('Add vehicle'),
      content: SizedBox(
        width: 420,
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              AppField(controller: _vin, label: 'VIN'),
              AppField(controller: _brand, label: 'Brand'),
              AppField(controller: _model, label: 'Model'),
              AppField(controller: _year, label: 'Year', number: true),
              AppField(controller: _engine, label: 'Engine'),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: _saving ? null : () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        FilledButton.icon(
          onPressed: _saving ? null : _save,
          icon: const Icon(Icons.save),
          label: const Text('Save'),
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
  const SessionDetails({required this.session, super.key});

  final DiagnosticSession session;

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
                Chip(label: Text(session.connectionType)),
                Chip(label: Text(session.protocol)),
                Chip(label: Text(session.overallStatus)),
              ],
            ),
            const SizedBox(height: 12),
            if (report != null) ReportPanel(report: report),
            const SizedBox(height: 12),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                InfoBlock(
                  title: 'Readings',
                  children: session.readings
                      .map(
                        (reading) =>
                            '${reading.parameterName}: ${reading.value} ${reading.unit}',
                      )
                      .toList(),
                ),
                InfoBlock(
                  title: 'Faults',
                  children: session.faultCodes
                      .map(
                        (fault) =>
                            '${fault.code} ${fault.severity} ${fault.description}',
                      )
                      .toList(),
                ),
                InfoBlock(
                  title: 'Recommendations',
                  children: session.recommendations
                      .map((item) => '${item.actionLabel}: ${item.message}')
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
  const ReportPanel({required this.report, super.key});

  final DiagnosticReport report;

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
              Chip(label: Text(report.urgency)),
              Chip(label: Text(report.drivable ? 'Drivable' : 'Do not drive')),
            ],
          ),
          const SizedBox(height: 8),
          Text(report.primaryIssue),
          if (report.summary.isNotEmpty) Text(report.summary),
          if (report.nextActions.isNotEmpty) ...[
            const SizedBox(height: 8),
            ...report.nextActions.map((action) => Text('- $action')),
          ],
        ],
      ),
    );
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
    this.number = false,
    super.key,
  });

  final TextEditingController controller;
  final String label;
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
            return 'Required';
          }
          if (number && int.tryParse(value) == null) {
            return 'Number expected';
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
