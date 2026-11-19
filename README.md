# K2 ScreenOff — выкл/вкл экрана ТВ через app-links

**Пакет:** `com.al.k2screenoff` · **minSdk 21 / targetSdk 34** · **Собрано:** 2026-11-16

> **AI-generated / Сгенерировано ИИ** — приложение написано, собрано и проверено
> ИИ-агентом (Hermes Agent) на реальном телевизоре Xiaomi MiTV (Android TV 14).
> Ручных правок кода не было. / *The app was written, built and verified by an
> AI agent (Hermes Agent) on a real Xiaomi MiTV (Android TV 14); no manual code
> edits.*

## Русский

Помощник для Xiaomi MiTV: гасит и зажигает **экран (панель)** без выключения
самого ТВ — звук и стриминг продолжают работать. Удобно для «радио по TV» и
для автоматизаций Home Assistant.

### Как это работает
Приложение пишет ключ `Settings.Global "screen_off_control"`
(0 = экран гаснет, 1 = загорается) — тот же ключ, что системная настройка
«Питание → Откл. изображение». Цепочка (подтверждена logcat в обоих
направлениях):
`mitv.service (TvService) → ScreenSaverManagerImpl.setPictureOff → RenderService SetPanelPowerStatus → TCON Power Seq`.
Это гашение панели, а **не** сон PowerManager: TV формально остаётся
включённым, аудио и стриминг не прерываются.

### Что внутри
- `OffActivity` — гасит экран; app-link `android://k2screenoff/off`;
  плюс иконка в лаунчере «K2Screen» (тап = гасит);
- `OnActivity` — зажигает экран; app-link `android://k2screenoff/on`;
- обе Activity: `Theme.NoDisplay` (экран не мигает), `finish()` сразу.

### Интеграция с Home Assistant
Запуск по app-link через `media_player.play_media`
(интеграция androidtv / androidtv_remote, порт 6466, ADB не нужен):

```yaml
# Экран ВЫКЛ
service: media_player.play_media
target:
  entity_id: media_player.<ваш_tv>
data:
  media_content_id: "android://k2screenoff/off"
  media_content_type: "app"

# Экран ВКЛ
service: media_player.play_media
target:
  entity_id: media_player.<ваш_tv>
data:
  media_content_id: "android://k2screenoff/on"
  media_content_type: "app"
```

Задержка от команды до срабатывания ~0.3–0.6 с.

### Проверено на железе (важно знать)
- нужно право `WRITE_SECURE_SETTINGS` (запись в Global-таблицу на AOSP 14
  проверяет именно его):
  `adb shell pm grant com.al.k2screenoff android.permission.WRITE_SECURE_SETTINGS` —
  выдаётся один раз, переживает перезагрузку ТВ; после сброса к заводским —
  переустановить.
- vendor-обработчик значения **абсолютный** (0 → off, иначе → on), а
  `Settings.Global.putInt` с **неизменным** значением не будит ContentObserver.
  Поэтому «включить экран» надёжнее делать физической кнопкой питания
  (инжект KEYCODE_POWER через HA remote) — так и сделано в проверенных
  автоматизациях; app-link `.../on` работает как запасной путь.
- клавиши, инжектируемые через порт 6466 (HOME/OK/MENU), гашёный экран **не**
  пробуждают — wake-логика vendor привязана к физическому пульту (BLE/RF).
- несколько `<data>` в одном intent-filter = AND: у каждой Activity свой фильтр
  с точным `path`, иначе URI не матчится и вылезает системный выбор приложения.

### Установка / сборка
- готовый APK: `dist/k2screenoff-v1.apk`;
- установка: `adb push` + `pm install -r` + `pm grant` (см. выше);
- сборка: `aapt → javac → d8 (classes.dex в корень APK) → apksigner`;
  тулчейн build-tools r34 + `android.jar` API 34, JDK 17;
  кейстр `build/ks.jks` (password `android`, alias `k2`);
  `resources.arsc` — ZIP_STORED (требование API 30+).

---

## English

A helper app for Xiaomi MiTV: turns the **TV panel** off and on **without**
powering off the TV itself — audio and streaming keep playing. Great for
"radio over TV" setups and Home Assistant automations.

### How it works
The app writes `Settings.Global "screen_off_control"`
(0 = panel off, 1 = panel on) — the same key as the system setting
"Power → Turn off display". Chain (confirmed in logcat, both directions):
`mitv.service (TvService) → ScreenSaverManagerImpl.setPictureOff → RenderService SetPanelPowerStatus → TCON Power Seq`.
It is a panel-level switch, **not** PowerManager sleep: the TV stays
technically on, audio and streaming are not interrupted.

### Contents
- `OffActivity` — panel off; app-link `android://k2screenoff/off`;
  plus a launcher icon "K2Screen" (tap = off);
- `OnActivity` — panel on; app-link `android://k2screenoff/on`;
- both: `Theme.NoDisplay` (no screen flash), `finish()` immediately.

### Home Assistant integration
Launch via app-link using `media_player.play_media`
(androidtv / androidtv_remote integration, port 6466, no ADB needed):

```yaml
# Panel OFF
service: media_player.play_media
target:
  entity_id: media_player.<your_tv>
data:
  media_content_id: "android://k2screenoff/off"
  media_content_type: "app"

# Panel ON
service: media_player.play_media
target:
  entity_id: media_player.<your_tv>
data:
  media_content_id: "android://k2screenoff/on"
  media_content_type: "app"
```

Latency from command to effect: ~0.3–0.6 s.

### Verified on device (good to know)
- the `WRITE_SECURE_SETTINGS` permission is required (AOSP 14 Global-table
  writes check exactly this one):
  `adb shell pm grant com.al.k2screenoff android.permission.WRITE_SECURE_SETTINGS` —
  granted once, survives TV reboots; after a factory reset, reinstall.
- the vendor handler is **absolute** (0 → off, otherwise on), and
  `Settings.Global.putInt` with an **unchanged** value does not wake the
  ContentObserver. So turning the panel on is more reliable via the physical
  power button (KEYCODE_POWER injected through the HA remote) — that is how the
  tested automations do it; the `.../on` app-link remains a fallback path.
- keycodes injected over port 6466 (HOME/OK/MENU) do **not** wake a panel that
  is off — vendor wake logic is tied to the physical remote (BLE/RF).
- multiple `<data>` elements in one intent-filter = AND: each Activity has its
  own filter with an exact `path`, otherwise the URI won't match and the
  system app-picker pops up.

### Install / build
- prebuilt APK: `dist/k2screenoff-v1.apk`;
- install: `adb push` + `pm install -r` + `pm grant` (see above);
- build: `aapt → javac → d8 (classes.dex at APK root) → apksigner`;
  toolchain build-tools r34 + `android.jar` API 34, JDK 17;
  keystore `build/ks.jks` (password `android`, alias `k2`);
  `resources.arsc` — ZIP_STORED (required for API 30+).
