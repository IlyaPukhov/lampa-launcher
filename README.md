# Lampa + TorrServer Launcher

This project demonstrates a Java launcher that starts TorrServer as a daemon and then starts Lampa, waiting for the user
to close it. It logs all activity to a file and handles proper shutdown.

---

## Build and Run

1. Build JAR:

```bash
gradlew clean build
```

2. Run using `javaw` (no console window):

```bash
javaw -jar build/libs/lampa-launcher-1.0.jar
```

All logs will be written to `launcher.log` in UTF-8.

---

## Notes

- `AppLifecycleManager` handles starting and stopping both TorrServer and Lampa.
- `TorrServerDaemon` runs in the background and redirects output to avoid console spam.
- Logs are written to `launcher.log` instead of console.

