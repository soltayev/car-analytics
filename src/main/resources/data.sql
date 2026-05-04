INSERT INTO vehicles (vin, brand, model, production_year, engine_type)
VALUES ('JTNB11HK302123456', 'Toyota', 'Camry', 2020, '2.5 Petrol');

INSERT INTO vehicles (vin, brand, model, production_year, engine_type)
VALUES ('KMHJC81CBNU123456', 'Hyundai', 'Tucson', 2022, '2.0 Petrol');

INSERT INTO fault_code_dictionary (code, title, description, system_name, subsystem, manufacturer_specific, default_severity, possible_causes, recommended_actions, drivable_allowed)
VALUES ('P0118', 'Engine coolant temperature circuit high input', 'The engine control module detects an implausibly high voltage from the coolant temperature sensor circuit. This usually means the sensor signal is open, shorted to voltage, or disconnected.', 'Powertrain', 'Cooling', false, 'CRITICAL', 'Failed coolant temperature sensor, corroded connector, damaged harness, low coolant causing unstable readings, ECU input fault.', 'Inspect coolant level, connector locking, and wiring continuity. Compare live temperature against cold-engine ambient temperature and replace the sensor or repair the harness if the signal is unstable.', false);

INSERT INTO fault_code_dictionary (code, title, description, system_name, subsystem, manufacturer_specific, default_severity, possible_causes, recommended_actions, drivable_allowed)
VALUES ('P0128', 'Coolant thermostat below regulating temperature', 'The engine does not reach the expected operating temperature within the calibrated time window. The cooling system is likely staying open or measuring temperature incorrectly.', 'Powertrain', 'Cooling', false, 'MEDIUM', 'Thermostat stuck open, biased coolant temperature sensor, low coolant level, radiator fan staying on too long.', 'Verify warm-up curve with live data, inspect coolant level, check thermostat operation, and replace the thermostat if the engine warms up too slowly.', true);

INSERT INTO fault_code_dictionary (code, title, description, system_name, subsystem, manufacturer_specific, default_severity, possible_causes, recommended_actions, drivable_allowed)
VALUES ('P0171', 'System too lean bank 1', 'The air-fuel mixture is running lean on bank 1 based on fuel trim correction limits. The engine is compensating for unmetered air or insufficient fuel delivery.', 'Powertrain', 'Fuel', false, 'HIGH', 'Vacuum leak, intake boot leak, weak fuel pump, clogged injector, dirty mass air flow sensor, exhaust leak before oxygen sensor.', 'Inspect intake tract for leaks, review short-term and long-term fuel trims, test fuel pressure, and clean or replace airflow and fuel delivery components as needed.', true);

INSERT INTO fault_code_dictionary (code, title, description, system_name, subsystem, manufacturer_specific, default_severity, possible_causes, recommended_actions, drivable_allowed)
VALUES ('P0300', 'Random or multiple cylinder misfire detected', 'The control module detected misfire activity across multiple cylinders or an unstable combustion pattern.', 'Powertrain', 'Ignition', false, 'HIGH', 'Ignition coil failure, worn spark plugs, injector imbalance, fuel pressure drop, vacuum leak, low compression.', 'Review live misfire counters, inspect ignition components, verify fuel pressure, and run compression or leak-down testing if the misfire persists.', true);

INSERT INTO fault_code_dictionary (code, title, description, system_name, subsystem, manufacturer_specific, default_severity, possible_causes, recommended_actions, drivable_allowed)
VALUES ('P0301', 'Cylinder 1 misfire detected', 'Cylinder 1 is misfiring often enough to trigger a fault. Combustion in that cylinder is unstable or incomplete.', 'Powertrain', 'Ignition', false, 'HIGH', 'Failed ignition coil, worn spark plug, injector fault, compression loss, wiring or connector issue on cylinder 1.', 'Swap the ignition coil with another cylinder, inspect the spark plug, verify injector pulse and wiring, and confirm cylinder compression before replacing parts.', true);

INSERT INTO fault_code_dictionary (code, title, description, system_name, subsystem, manufacturer_specific, default_severity, possible_causes, recommended_actions, drivable_allowed)
VALUES ('P0302', 'Cylinder 2 misfire detected', 'Cylinder 2 is misfiring often enough to trigger a fault. Combustion in that cylinder is unstable or incomplete.', 'Powertrain', 'Ignition', false, 'HIGH', 'Failed ignition coil, worn spark plug, injector fault, compression loss, wiring or connector issue on cylinder 2.', 'Swap the ignition coil with another cylinder, inspect the spark plug, verify injector pulse and wiring, and confirm cylinder compression before replacing parts.', true);

INSERT INTO fault_code_dictionary (code, title, description, system_name, subsystem, manufacturer_specific, default_severity, possible_causes, recommended_actions, drivable_allowed)
VALUES ('P0420', 'Catalyst system efficiency below threshold bank 1', 'The catalytic converter efficiency on bank 1 is below the expected threshold based on oxygen sensor behavior.', 'Powertrain', 'Exhaust', false, 'MEDIUM', 'Aging catalytic converter, exhaust leak, rich or lean mixture, rear oxygen sensor drift.', 'Check for exhaust leaks, verify front and rear oxygen sensor switching, and repair mixture-control faults before condemning the catalytic converter.', true);

INSERT INTO fault_code_dictionary (code, title, description, system_name, subsystem, manufacturer_specific, default_severity, possible_causes, recommended_actions, drivable_allowed)
VALUES ('P0562', 'System voltage low', 'The control module measured battery or charging voltage below the expected minimum for stable operation.', 'Powertrain', 'Electrical', false, 'HIGH', 'Weak battery, charging system failure, belt slip, poor grounds, corroded battery terminals.', 'Measure battery voltage at rest and under load, test alternator output, inspect grounds and power connections, and repair the charging circuit before continued use.', true);

INSERT INTO fault_code_dictionary (code, title, description, system_name, subsystem, manufacturer_specific, default_severity, possible_causes, recommended_actions, drivable_allowed)
VALUES ('P0700', 'Transmission control system malfunction request', 'The engine control module received a request from the transmission control module to illuminate the malfunction indicator lamp. A related transmission code is usually stored in the transmission controller.', 'Powertrain', 'Transmission', false, 'HIGH', 'Stored fault inside the transmission control module, sensor failure, solenoid issue, internal transmission problem.', 'Read the transmission control module for companion codes, inspect transmission fluid condition, and avoid harsh driving until the root cause is confirmed.', true);

INSERT INTO fault_code_dictionary (code, title, description, system_name, subsystem, manufacturer_specific, default_severity, possible_causes, recommended_actions, drivable_allowed)
VALUES ('C0035', 'Left front wheel speed sensor circuit', 'The ABS module detected an electrical or signal problem in the left front wheel speed sensor circuit.', 'Chassis', 'ABS', false, 'HIGH', 'Wheel speed sensor failure, damaged tone ring, connector corrosion, wiring damage near the wheel hub.', 'Inspect the sensor harness, clean the connector, verify sensor signal while rotating the wheel, and check the tone ring for damage or contamination.', true);

INSERT INTO fault_code_dictionary (code, title, description, system_name, subsystem, manufacturer_specific, default_severity, possible_causes, recommended_actions, drivable_allowed)
VALUES ('U0100', 'Lost communication with ECM/PCM', 'A control module on the vehicle network lost communication with the engine or powertrain control module.', 'Network', 'CAN Bus', false, 'CRITICAL', 'CAN wiring issue, control module power loss, low system voltage, connector corrosion, failed module.', 'Check battery voltage first, inspect CAN bus wiring and connector fitment, verify module power and grounds, and stop driving if multiple modules are dropping offline.', false);

INSERT INTO service_centers (name, city, address, phone, specialization, emergency_support, rating, latitude, longitude)
VALUES ('Astana Auto Diagnostics', 'Astana', 'Kabanbay Batyr 15', '+7-701-111-2233', 'General Diagnostics', true, 4.70, 51.128220, 71.430420);

INSERT INTO service_centers (name, city, address, phone, specialization, emergency_support, rating, latitude, longitude)
VALUES ('Cooling System Lab', 'Almaty', 'Tole Bi 88', '+7-707-333-4455', 'Cooling', true, 4.90, 43.256540, 76.928480);

INSERT INTO service_centers (name, city, address, phone, specialization, emergency_support, rating, latitude, longitude)
VALUES ('Ignition Service Pro', 'Almaty', 'Rayimbek 210', '+7-705-555-6677', 'Ignition', false, 4.30, 43.264900, 76.910330);

INSERT INTO spare_parts (part_number, part_name, manufacturer, subsystem, compatible_brand, compatible_model, fault_code, price, currency, stock_status)
VALUES ('06H905110G', 'Ignition Coil', 'Bosch', 'Ignition', null, null, 'P0301', 18500.00, 'KZT', 'IN_STOCK');

INSERT INTO spare_parts (part_number, part_name, manufacturer, subsystem, compatible_brand, compatible_model, fault_code, price, currency, stock_status)
VALUES ('101905601F', 'Spark Plug', 'NGK', 'Ignition', null, null, 'P0301', 6200.00, 'KZT', 'IN_STOCK');

INSERT INTO spare_parts (part_number, part_name, manufacturer, subsystem, compatible_brand, compatible_model, fault_code, price, currency, stock_status)
VALUES ('06A919501A', 'Coolant Temperature Sensor', 'VAG', 'Cooling', null, null, 'P0118', 12400.00, 'KZT', 'ORDER_ONLY');

INSERT INTO spare_parts (part_number, part_name, manufacturer, subsystem, compatible_brand, compatible_model, fault_code, price, currency, stock_status)
VALUES ('9091603093', 'Thermostat', 'Toyota', 'Cooling', 'Toyota', 'Camry', null, 21500.00, 'KZT', 'IN_STOCK');

INSERT INTO repair_guides (title, subsystem, fault_code, description, difficulty, estimated_minutes, safety_notes)
VALUES ('Cylinder Misfire Initial Repair Flow', 'Ignition', 'P0301', 'Inspect ignition coil, spark plug, injector connector, and compression of cylinder 1. Start with swap-test of the ignition coil and inspect live misfire counters.', 'MEDIUM', 60, 'Disconnect battery before removing ignition components and avoid hot engine surfaces.');

INSERT INTO repair_guides (title, subsystem, fault_code, description, difficulty, estimated_minutes, safety_notes)
VALUES ('Coolant Temperature Sensor Verification', 'Cooling', 'P0118', 'Check sensor connector, wiring continuity, ECU reference voltage, and compare live temperature data with real engine temperature before replacing the sensor.', 'MEDIUM', 45, 'Open coolant system only on a cold engine to avoid burns.');

INSERT INTO repair_guides (title, subsystem, fault_code, description, difficulty, estimated_minutes, safety_notes)
VALUES ('Engine Overheating Emergency Checklist', 'Cooling', null, 'Stop engine load, inspect coolant level, radiator fan operation, leaks, and thermostat behavior before continuing driving.', 'EASY', 20, 'Do not open the expansion tank cap on a hot engine.');
