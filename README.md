# AI Vehicle Diagnostics Dissertation

Spring Boot проект для диссертации по интеллектуальной диагностике и поддержке ремонта автомобилей на основе данных `OBD2`.

Сейчас в репозитории уже есть:

- REST API для регистрации автомобилей и загрузки диагностических сессий
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
- `ObdReading` - считанные параметры с ЭБУ
- `FaultCode` - коды неисправностей
- `Recommendation` - рекомендации системы
- `DiagnosticReport` - итоговый отчет по сессии

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
      { "code": "P0301", "description": "Cylinder 1 misfire detected", "severity": "HIGH" },
      { "code": "P0118", "description": "Engine coolant temperature circuit high", "severity": "CRITICAL" }
    ]
  }'
```

### Получить только диагностический отчет

```bash
curl http://localhost:8080/api/diagnostic-sessions/1/report
```

## Куда можно развивать дальше

1. Подключить реальный модуль связи с `OBD2` адаптером.
2. Перейти с `H2` на `PostgreSQL`.
3. Добавить JWT-аутентификацию и роли пользователя.
4. Подключить настоящий AI/ML модуль интерпретации неисправностей.
5. Добавить каталог СТО, запчасти и repair guides.
6. Сделать мобильный или web-интерфейс для водителя и диагноста.
