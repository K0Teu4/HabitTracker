# Habit Tracker 0.4.0

Минималистичный Android-трекер привычек. Quiet Precision.

## 0.4.0

- Компактные заголовки (без лишнего отступа под статус-баром)
- Тёмная / светлая / системная тема (Настройки → Тема)
- Онбординг при первом запуске (3 экрана, можно пропустить)
- Один FAB на пустом «Сегодня» (без дубля)
- AutoMirrored иконки в навигации

## Сборка

```powershell
.\gradlew.bat --no-daemon assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n ru.habittracker/.MainActivity
```

Чтобы снова увидеть онбординг:

```powershell
adb shell pm clear ru.habittracker
```
