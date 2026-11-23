# A Day in Lines

The wallpaper follows the computer's local time:

- 05:00–07:00 — quiet morning at home
- 07:00–09:00 — walking to work
- 09:00–18:00 — working at the desk
- 18:00–20:00 — walking home at sunset
- 20:00–22:00 — evening at home
- 22:00–05:00 — sleeping

## Preview

Run `gradlew.bat :desktop:runDailyRhythm`. The preview does not attach to the desktop.

Press `1`–`6` to preview each scene, and `0` to return to live local time.

Run the actual wallpaper with `gradlew.bat --init-script work/run-wallpaper.init.gradle :desktop:runWallpaper`.
