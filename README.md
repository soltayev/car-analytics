# AI Vehicle Diagnostics Dissertation

Spring Boot проект для диссертации по интеллектуальной диагностике и поддержке ремонта автомобилей на основе данных `OBD2`.

Сейчас в репозитории уже есть:

- REST API для регистрации автомобилей и загрузки диагностических сессий
- live scan через `ELM327 Wi-Fi` адаптер
- модель предметной области под `OBD2`, неисправности, сырые фреймы и рекомендации
- H2 база для локального прототипирования
- валидация входных данных
- базовый AI-like сервис рекомендаций по fault codes и телеметрии
- отдельный диагностический отчет с уровнем срочности и действиями

## Идея проекта

Система подключается к автомобилю через `OBD2` по `Wi-Fi` или `Bluetooth`, получает диагностические данные и помогает пользователю:

- распознавать неисправности
- интерпретировать DTC-коды
- получать рекомендации по ремонту
- понимать, когда можно устранить проблему самостоятельно
- получать подсказки по поиску запчастей
- понимать, когда нужно направить автомобиль в `СТО`

## Текущая архитектура

- `Vehicle` - автомобиль
- `DiagnosticSession` - диагностическая сессия
- `RawObdFrame` - сырой ответ адаптера по mode/PID
- `VehicleInfoItem` - данные mode `09`, например `VIN`
- `ObdReading` - считанные параметры с ЭБУ
- `FaultCode` - коды неисправностей
- `FaultCodeDictionary` - справочник DTC-кодов и их расшифровки
- `Recommendation` - рекомендации системы
- `DiagnosticReport` - итоговый отчет по сессии
- `ServiceCenter` - каталог СТО
- `SparePart` - каталог запчастей
- `RepairGuide` - ремонтные инструкции

На этом этапе ИИ реализован как rule-based слой-заглушка, чтобы backend уже был рабочим. Позже сюда можно подключить отдельную ML/LLM-модель.

## Запуск

Нужны `Java 17+` и `Maven 3.9+`.

```bash
mvn spring-boot:run
```

После запуска:

- API: `http://localhost:8080/api/vehicles`
- H2 console: `http://localhost:8080/h2-console`

Параметры H2:

- JDBC URL: `jdbc:h2:mem:carsdb`
- User: `sa`
- Password: `password`

## Flutter frontend

Кроссплатформенный клиент находится в `frontend/car_analytics_app` и поддерживает `web`, `iOS` и `Android`.

```bash
cd frontend/car_analytics_app
flutter pub get
flutter run -d chrome
```

Для web-сборки:

```bash
flutter build web
```

По умолчанию приложение ходит в backend `http://localhost:8080`. URL можно поменять во вкладке `Settings`; для Android emulator обычно нужен `http://10.0.2.2:8080`.

## Основные endpoint'ы

### Зарегистрировать автомобиль

```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Content-Type: application/json" \
  -d '{
    "vin": "WAUZZZ8V7JA123456",
    "brand": "Audi",
    "model": "A4",
    "productionYear": 2018,
    "engineType": "2.0 TFSI"
  }'
```

### Загрузить диагностическую сессию

```bash
curl -X POST http://localhost:8080/api/diagnostic-sessions/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "connectionType": "BLUETOOTH",
    "adapterName": "ELM327",
    "adapterIdentifier": "00:1D:A5:68:98:12",
    "protocol": "ISO_15765_4_CAN",
    "rawFrames": [
      {
        "mode": "01",
        "pid": "05",
        "rawResponse": "41 05 94",
        "decodedLabel": "Engine Coolant Temperature",
        "frameTimestamp": "2026-05-04T10:15:00"
      },
      {
        "mode": "03",
        "pid": null,
        "rawResponse": "43 01 18 03 01",
        "decodedLabel": "Stored DTCs",
        "frameTimestamp": "2026-05-04T10:15:01"
      }
    ],
    "readings": [
      { "parameterName": "engine_temperature", "pidCode": "05", "parameterValue": 108, "unit": "C" },
      { "parameterName": "rpm", "pidCode": "0C", "parameterValue": 850, "unit": "rpm" }
    ],
    "faultCodes": [
      { "code": "P0301" },
      { "code": "P0118" }
    ]
  }'
```

Если `description` и `severity` не переданы, backend попробует заполнить их из локального справочника `fault_code_dictionary`.

### Выполнить live scan через Wi-Fi ELM327

```bash
curl -X POST http://localhost:8080/api/live-scan/wifi \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "host": "192.168.0.10",
    "port": 35000,
    "adapterName": "ELM327 WiFi",
    "protocol": "ISO_15765_4_CAN",
    "socketTimeoutMs": 4000,
    "includeStoredFaultCodes": true,
    "includePendingFaultCodes": true,
    "includeFreezeFrame": true,
    "includeVehicleInfo": true,
    "currentDataPids": ["05", "0C", "0D", "11", "2F", "42"],
    "freezeFramePids": ["05", "0C", "0D", "11"]
  }'
```

### Получить только диагностический отчет

```bash
curl http://localhost:8080/api/diagnostic-sessions/1/report
```

### Получить каталог СТО

```bash
curl "http://localhost:8080/api/catalog/service-centers?emergencyOnly=true"
```

### Получить ближайшие СТО по координатам

```bash
curl "http://localhost:8080/api/catalog/service-centers?city=Almaty&latitude=43.2565&longitude=76.9284&maxDistanceKm=5"
```

### Получить каталог запчастей

```bash
curl "http://localhost:8080/api/catalog/spare-parts?faultCode=P0301"
```

### Получить ремонтные инструкции

```bash
curl "http://localhost:8080/api/catalog/repair-guides?faultCode=P0118"
```

### Получить справочник DTC-кодов

```bash
curl "http://localhost:8080/api/catalog/fault-codes?system=Powertrain&subsystem=Cooling"
```

### Получить карточку конкретного DTC-кода

```bash
curl "http://localhost:8080/api/catalog/fault-codes/P0118"
```

## Куда можно развивать дальше

1. Подключить реальный модуль связи с `OBD2` адаптером.
2. Добавить Bluetooth-подключение к адаптеру.
3. Перейти с `H2` на `PostgreSQL`.
4. Добавить JWT-аутентификацию и роли пользователя.
5. Подключить настоящий AI/ML модуль интерпретации неисправностей.
6. Сделать полноценный поиск запчастей по VIN и поставщикам.
7. Сделать мобильный или web-интерфейс для водителя и диагноста.
