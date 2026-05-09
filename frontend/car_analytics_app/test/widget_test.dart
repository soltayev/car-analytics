import 'package:car_analytics_app/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('renders the Car Analytics shell', (tester) async {
    await tester.pumpWidget(const CarAnalyticsApp());
    await tester.pump();

    expect(find.text('Car Analytics'), findsOneWidget);
    expect(find.text('Vehicles'), findsWidgets);
    expect(find.text('Diagnostics'), findsWidgets);
  });
}
