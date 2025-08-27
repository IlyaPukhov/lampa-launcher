# Lampa + TorrServer Launcher

This launcher manages the startup and shutdown of **TorrServer** and **Lampa**.  
It ensures TorrServer is running before launching Lampa, waits until Lampa exits,
and then shuts down both processes gracefully. All activity is logged to `launcher.log`.


---

## Build and Run

1. Build JAR:

```bash
gradlew clean shadowJar
```

2. Run using `javaw` (no console window):

```bash
javaw -jar build/libs/lampa-launcher-{version}-with-dependencies.jar
```

All logs are written to `launcher.log` in UTF-8.

---

## Configuration

The launcher is configured via `launcher.properties`.

Supported properties:

- **torrserver.path** — path to `torrserver.exe` (must exist).
- **torrserver.port** — port TorrServer will listen on (validated: must be >1024 and <65535).
- **torrserver.startupTimeout** — timeout in seconds to wait for TorrServer to be ready (validated: positive number).
- **lampa.path** — path to `lampa.exe` (must exist).

Example:

```properties
torrserver.path=torrserver.exe
torrserver.port=8090
torrserver.startup-timeout=30
lampa.path=lampa.exe
```

### Important: Paths for executables are validated to ensure the files exist and port are available!

---

## Notes

- `ProcessManager` coordinates TorrServer and Lampa.
- `TorrServerDaemon` is started first and waited on until it binds to the configured port.
- `LampaProcess` starts only after TorrServer is ready.
- Both processes are stopped gracefully during shutdown.
