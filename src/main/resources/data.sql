INSERT INTO vehicles (vin, brand, model, production_year, engine_type)
VALUES ('JTNB11HK302123456', 'Toyota', 'Camry', 2020, '2.5 Petrol');

INSERT INTO vehicles (vin, brand, model, production_year, engine_type)
VALUES ('KMHJC81CBNU123456', 'Hyundai', 'Tucson', 2022, '2.0 Petrol');

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
