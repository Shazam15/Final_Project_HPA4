# HomePet

HomePet es una aplicación Android en Kotlin que convierte tareas del hogar en progreso para una mascota virtual. Usa Activities, View Binding, MVVM, StateFlow, Room, WorkManager, notificaciones locales, SharedPreferences y Google Maps SDK.

La mascota se representa con arte vectorial propio: perro, gato y conejo poseen cinco estados visuales, y las recompensas se dibujan como capas equipables. Los SVG editables están en `app/src/main/assets/artwork/`; Android utiliza sus equivalentes VectorDrawable en `res/drawable/`.

## Configuración de Google Maps

1. Crea un proyecto en Google Cloud Console.
2. Habilita **Maps SDK for Android**.
3. Crea una API key.
4. Restringe la key por aplicación Android usando el paquete `com.utp.finalproject` y el SHA-1 de firma.
5. En el archivo local `local.properties`, agrega:

   ```properties
   MAPS_API_KEY=TU_API_KEY
   ```

6. No subas `local.properties` al repositorio. El archivo ya está ignorado por Git.

También se incluye `local.properties.example` como plantilla sin secretos. Si la key no está configurada, la app compila y el selector muestra un mensaje explicativo.

## Compilar y probar

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

## Documentación técnica

La explicación de arquitectura, archivos, bloques de código, base de datos, migración y flujos está en [DOCUMENTACION_CODIGO_LINEA_POR_LINEA.md](DOCUMENTACION_CODIGO_LINEA_POR_LINEA.md).
