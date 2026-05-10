import 'package:car_analytics_app/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('renders the localized Car Analytics shell', (tester) async {
    await tester.pumpWidget(const CarAnalyticsApp());
    await tester.pump();

    expect(find.text('Диагностика авто'), findsOneWidget);
    expect(find.text('Авто'), findsWidgets);
    expect(find.text('Диагностика'), findsWidgets);
    expect(find.text('Справочник'), findsWidgets);
  });

  testWidgets('opens settings with driver profile', (tester) async {
    await tester.pumpWidget(const CarAnalyticsApp());
    await tester.pump();

    await tester.tap(find.text('Настройки'));
    await tester.pump();

    expect(find.text('Профиль водителя'), findsOneWidget);
    expect(find.text('Адрес backend'), findsOneWidget);
  });
}
