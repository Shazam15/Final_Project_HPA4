# Documentación de código de HomePet

## Índice

- [Arquitectura](#arquitectura)
- [Paquetes](#paquetes)
- [Configuración Gradle](#configuración-gradle)
- [AndroidManifest](#androidmanifest)
- [Activities](#activities)
- [ViewModels](#viewmodels)
- [Room y entidades](#room-y-entidades)
- [Repositories y preferencias](#repositories-y-preferencias)
- [Dominio de mascota](#dominio-de-mascota)
- [WorkManager](#workmanager)
- [Notificaciones](#notificaciones)
- [Google Maps](#google-maps)
- [Tema claro y oscuro](#tema-claro-y-oscuro)
- [Arte vectorial de mascotas](#arte-vectorial-de-mascotas)
- [Layouts y recursos XML](#layouts-y-recursos-xml)
- [Pruebas](#pruebas)
- [Flujos principales del sistema](#flujos-principales-del-sistema)

## Arquitectura

HomePet sigue este flujo:

```text
Activity / View Binding
        ↓ eventos y observación
ViewModel + StateFlow
        ↓ operaciones de dominio
HomePetRepository
        ↓
Room DAOs / HomePetPreferences / WorkManager
```

Las Activities conocen Android y las Views. Los ViewModels no guardan referencias a `Activity`, `View` ni `Context`. `HomePetRepository` recibe el contexto de aplicación y coordina Room y preferencias. Las operaciones de base de datos son `suspend` y se ejecutan en `Dispatchers.IO`.

## Paquetes

- `com.utp.finalproject`: Activities y `HomePetApplication`.
- `data.local.entity`: tablas Room.
- `data.local.dao`: consultas Room.
- `data.local.database`: creación y migración de la base.
- `data.preferences`: sesión, tema, onboarding y notificaciones.
- `data.repository`: fuente de datos unificada.
- `domain`: reglas puras de XP, degradación y alertas.
- `viewmodel`: estado de pantalla y acciones.
- `ui.adapters`: adaptadores de RecyclerView.
- `ui/PetArtwork.kt`: selección de mascota y recompensa vectorial.
- `worker`: trabajo periódico de bienestar.
- `notifications`: canal y construcción de notificaciones.
- `utils`: formato de fechas y apertura de mapas.

## Configuración Gradle

### `gradle/libs.versions.toml`

El bloque `[versions]` centraliza versiones de AndroidX, Room, WorkManager, KSP, coroutines y Maps. El bloque `[libraries]` relaciona alias como `androidx-room-runtime` con sus coordenadas Maven. `[plugins]` declara Android Application y KSP.

### `app/build.gradle.kts`

- `import java.util.Properties` permite leer `local.properties` sin exponer secretos.
- `plugins` activa Android Application y KSP, necesario para generar implementaciones Room.
- `localProperties` abre el archivo local solo si existe.
- `mapsApiKey` usa cadena vacía como fallback; por eso compilar no depende de tener una key.
- `ksp { arg("room.schemaLocation", ...) }` exporta los esquemas versionados de Room.
- `namespace`, `applicationId`, `minSdk` y `targetSdk` definen identidad y compatibilidad.
- `manifestPlaceholders["MAPS_API_KEY"]` entrega la key al Manifest durante build.
- `buildConfigField` genera `BuildConfig.MAPS_API_KEY_CONFIGURED`, usado para mostrar un error controlado.
- `viewBinding = true` genera bindings tipados para layouts.
- `buildConfig = true` habilita la constante de configuración.
- Las dependencias añaden Lifecycle, Coroutines, Room, WorkManager y Google Maps.

### `local.properties.example`

Contiene únicamente `MAPS_API_KEY=REEMPLAZAR_CON_API_KEY`. Sirve de plantilla y nunca contiene la key real.

## AndroidManifest

### `app/src/main/AndroidManifest.xml`

- `CALL_PHONE` permite que “Solicitar ayuda” llame al número configurado después de autorización.
- `POST_NOTIFICATIONS` habilita alertas en Android 13+.
- `android:name=".HomePetApplication"` inicia tema, canal y Worker desde la aplicación.
- El `meta-data` de Maps usa `${MAPS_API_KEY}`; Gradle sustituye el valor local.
- `LocationPickerActivity` no es exportada porque solo se abre dentro de HomePet.
- Settings, History, Rewards, TaskList, TaskForm, Main y Login tampoco son exportadas.
- `WelcomeActivity` es exportada porque posee el filtro `MAIN/LAUNCHER`.

## Activities

### `HomePetApplication.kt`

`HomePetApplication : Application` se crea antes de las Activities. `onCreate()` lee el tema persistido, crea el canal de bienestar y agenda trabajo periódico único. Usar Application evita repetir inicialización en cada pantalla.

### `WelcomeActivity.kt`

- Infla `ActivityWelcomeBinding`.
- Crea `OnboardingViewModel` con `RepositoryViewModelFactory`.
- El spinner permite elegir perro, gato o conejo.
- `savePet()` valida el nombre y delega a ViewModel.
- La colección de `uiState` navega a Home si hay sesión o a Login si falta sesión.
- `finish()` evita volver a onboarding con Atrás.

### `LoginActivity.kt`

- Conserva el botón de ayuda y la llamada a `+507 6253-8997`.
- `LoginViewModel` carga el nombre guardado sin tener Context.
- `login()` valida nombre y correo; nunca persiste la contraseña.
- `viewModel.login()` guarda solo sesión local, nombre y correo.
- El Intent explícito a `MainActivity` usa `putExtra` con claves declaradas en el `companion object` de destino.
- `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` elimina pantallas de autenticación del back stack.
- La llamada usa permiso runtime `CALL_PHONE` y muestra feedback si se rechaza.

### `MainActivity.kt`

- Verifica `repository.isLoggedIn()` antes de mostrar Home. Una notificación antigua no puede saltar Login.
- Infla `ActivityMainBinding` y obtiene `HomeViewModel`.
- `registerForActivityResult` abre TaskForm y recibe `EXTRA_TASK_CHANGED`.
- `setupTaskList()` conecta completar, editar y abrir ubicación.
- `setupNavigation()` usa Intents explícitos a tareas, recompensas, historial y ajustes.
- `render()` presenta nivel, monedas, estado, salud, hambre, energía, felicidad y XP.
- La mascota se resuelve por tipo+estado y se compone con capas de fondo, capa, color, collar y sombrero.
- `petMessage()` traduce el estado emocional en un mensaje accesible; el estado no depende solo del color.

### `TaskListActivity.kt`

- Observa `TasksViewModel.uiState` y actualiza un `ListAdapter`.
- Los spinners cambian filtro y orden.
- Editar usa Intent explícito y pasa el id con `TaskFormActivity.EXTRA_TASK_ID`.
- El launcher de Activity Result recibe el cierre del formulario.
- Eliminar requiere confirmación con `AlertDialog`.

### `TaskFormActivity.kt`

- Lee `EXTRA_TASK_ID`; cero significa creación y otro valor significa edición.
- Usa ViewModel para leer, guardar y eliminar en Room.
- `locationPickerLauncher` recibe nombre, latitud, longitud y placeId.
- `fillTask()` restaura todos los campos, incluida ubicación nullable.
- `saveTask()` valida título/fecha y conserva estado, creación, XP y ubicación.
- `openLocationPicker()` pasa la ubicación actual mediante extras definidos por la Activity destino.
- `clearLocation()` vuelve los cuatro campos de ubicación a `null`.
- Al guardar/eliminar devuelve `RESULT_OK` y `EXTRA_TASK_CHANGED`.

### `LocationPickerActivity.kt`

- Implementa `OnMapReadyCallback` para recibir `GoogleMap`.
- Si `MAPS_API_KEY_CONFIGURED` es falso, oculta el mapa, explica la configuración y no se cierra inesperadamente.
- `onMapReady()` centra el mapa en Panamá o en el punto previo.
- `setOnMapClickListener` permite mover el marcador manualmente sin pedir permiso de ubicación.
- `reverseGeocode()` ejecuta `Geocoder` en `Dispatchers.IO` y tolera falta de red/resultados.
- `confirmSelection()` devuelve coordenadas y nombre con Activity Result.
- Cancelar termina sin modificar la tarea.

### `SettingsActivity.kt`

- Carga nombre/tipo de mascota, notificaciones, hora y tema.
- El selector incluye Sistema, Claro y Oscuro.
- `ThemeManager.apply()` aplica el cambio inmediatamente con AppCompatDelegate.
- El permiso de notificaciones se solicita solo cuando el usuario habilita recordatorios.
- `confirmLogout()` elimina únicamente sesión y abre Login limpiando todo el back stack.
- Reiniciar progreso es una acción separada y destructiva con confirmación.

### `RewardsActivity.kt` y `HistoryActivity.kt`

Rewards observa recompensas, muestra el SVG correspondiente y delega compra/equipamiento al ViewModel. Una compra válida equipa la recompensa inmediatamente. History observa eventos y estadísticas acumuladas. Ambas usan Repository y StateFlow, manteniendo la UI reactiva.

## ViewModels

### `ViewModelFactories.kt`

`RepositoryViewModelFactory` recibe un Repository y construye el ViewModel solicitado mediante `isAssignableFrom`. El cast está suprimido porque el `when` garantiza el tipo correspondiente.

### `HomeViewModel.kt`

`HomeUiState` agrupa mascota, tareas urgentes, tareas del día, recompensas y máximo de XP. `dashboardFlow.map` transforma datos Room en estado de UI. `stateIn` convierte el Flow en StateFlow ligado a `viewModelScope`.

### `TasksViewModel.kt`

Combina `tasksFlow`, filtro y orden. `when` filtra por estado y ordena por fecha, prioridad o categoría. Completar/eliminar lanza coroutines y llama al Repository.

### `TaskFormViewModel.kt`

`MutableStateFlow` privado evita que la View modifique estado directamente. `loadTask`, `saveTask` y `deleteTask` delegan al Repository y publican banderas de resultado.

### `OnboardingViewModel.kt` y `LoginViewModel.kt`

Onboarding decide entre crear mascota, Login o Home según preferencias. Login expone el nombre recordado y guarda una sesión local sin Context ni Views.

### `SettingsViewModel.kt`

Expone `pet` como StateFlow y preferencias mediante propiedades. Cambia tema, notificaciones, hora, mascota y sesión delegando al Repository.

## Room y entidades

### Diagrama textual

```text
pet (id PK)
  ├─ estadísticas: health, hunger, energy, happiness, mood
  ├─ progreso: level, experience, coins
  └─ timestamps: lastInteractionAt, lastStatsUpdateAt,
                 lastAppOpenedAt, lastDecayNotificationAt

tasks (id PK autogenerado)
  ├─ title, description, category, priority, frequency
  ├─ status, createdAtMillis, dueAtMillis, completedAtMillis
  ├─ xpReward, completedOnTime
  └─ locationName?, latitude?, longitude?, placeId?

rewards (id PK)        activity_history (id PK)
```

No hay claves foráneas porque la versión actual mantiene una sola mascota local y tareas globales del dispositivo.

### `PetEntity.kt`

`@Entity(tableName = "pet")` declara la tabla. `id` es clave primaria fija. Las estadísticas usan enteros 0–100. `lastStatsUpdateAt` marca hasta dónde se aplicó degradación; `lastInteractionAt` registra cuidado real; `lastAppOpenedAt` registra apertura; los campos de notificación implementan cooldown.

### `TaskEntity.kt`

`@PrimaryKey(autoGenerate = true)` crea ids. Las constantes del companion object evitan estados/prioridades mágicos. Los cuatro campos de ubicación son nullable, permitiendo guardar tareas sin mapa.

### DAOs

- `PetDao`: observa, obtiene, inserta, actualiza y limpia mascota.
- `TaskDao`: observa, consulta por id/estado, detecta vencidas, cuenta, inserta, actualiza y elimina.
- `RewardDao`: observa catálogo, desbloquea/equipa y limpia.
- `HistoryDao`: observa eventos, inserta y limpia.

Las funciones de escritura son `suspend`. Los métodos `observe...()` devuelven Flow para refrescar automáticamente la UI.

### `HomePetDatabase.kt`

`@Database` registra cuatro entidades y versión 3. El singleton usa `@Volatile` y `synchronized` para crear una sola instancia. `addMigrations(MIGRATION_1_2, MIGRATION_2_3)` conserva datos.

La migración 1→2 ejecuta `ALTER TABLE` para cuatro columnas nullable de ubicación, `hunger`, timestamps y nivel de notificación. Después copia `lastUpdatedAt` a timestamps nuevos para no degradar retroactivamente desde 1970. No usa migración destructiva.

La migración 2→3 añade `equippedHat`, `equippedClothing` y `equippedBackground`. Junto con `equippedAccessory` y `equippedColor`, estos campos permiten conservar varias categorías visuales a la vez.

## Repositories y preferencias

### `HomePetRepository.kt`

- Construye DAOs y `HomePetPreferences` con contexto de aplicación.
- Expone Flows de mascota, tareas, recompensas e historial.
- `prepareInitialData()` siembra recompensas, aplica degradación, registra apertura y actualiza vencidas.
- `saveTask()` inserta/actualiza y registra interacción.
- `completeTask()` calcula XP, actualiza tarea/mascota, desbloquea recompensas y escribe historial.
- `applyPendingDecay()` ejecuta el calculador y persiste solo si cambió.
- `markNotificationSent()` guarda timestamp/nivel para cooldown.
- Métodos de sesión/tema son fachada sobre SharedPreferences.
- Todas las operaciones Room usan `withContext(Dispatchers.IO)`.

### `HomePetPreferences.kt`

Usa un archivo privado `homepet_preferences`. Persiste onboarding, notificaciones, hora, tema y sesión. `saveSession()` no recibe ni guarda contraseña. `clearSession()` elimina únicamente login/nombre/correo. `clear()` completo se reserva para “Reiniciar progreso”.

### `PreferencePolicies.kt`

`ThemePreferencePolicy.normalize()` acepta solo Claro, Oscuro o Sistema. Un valor inválido vuelve a Sistema. `SessionPreferencePolicy.cleared()` representa el estado esperado después de logout y permite una prueba JVM pura.

## Dominio de mascota

### `PetRules.kt`

Calcula XP por prioridad/frecuencia, premia puntualidad, sube nivel, suma monedas y recupera necesidades al completar una tarea. `clamp()` limita estadísticas. `calculateMood()` centraliza Feliz, Neutral, Triste, Enferma y En peligro.

### `PetDecayCalculator.kt`

`INTERVAL_MILLIS` equivale a 6 horas. Por cada intervalo completo:

- Hambre: −8.
- Felicidad: −4.
- Energía: −3.
- Cada cuatro intervalos (24 h): salud −5 si hambre o felicidad quedan bajo 30.

El calculador no modifica XP ni nivel. Todos los valores usan `coerceIn(0, 100)`. `lastStatsUpdateAt` avanza únicamente los intervalos completos; por eso repetir el cálculo para el mismo instante no degrada dos veces.

### `PetNotificationEvaluator.kt`

Los umbrales son constantes compartidas. Warning ocurre si alguna estadística ≤30. Crítico ocurre con salud ≤15, hambre ≤10 o promedio ≤20. Warning tiene cooldown de 12 h; crítico, 6 h. Empeorar de warning a crítico permite aviso inmediato.

## WorkManager

### `HomePetReminderScheduler.kt`

Crea `PeriodicWorkRequest` cada 12 horas, intervalo válido para WorkManager. `enqueueUniquePeriodicWork` usa nombre estable y política UPDATE para impedir duplicados.

### `HomePetReminderWorker.kt`

`CoroutineWorker.doWork()` obtiene Repository, aplica degradación, actualiza vencidas y evalúa alertas. Si notificaciones están deshabilitadas, conserva la degradación pero no notifica. Errores temporales retornan `retry()`; éxito retorna `success()`.

## Notificaciones

### `HomePetNotificationManager.kt`

El canal `pet_wellbeing_channel` se crea con importancia HIGH. La notificación usa `BigTextStyle`, prioridad alta y `PendingIntent` inmutable a MainActivity. IDs distintos separan warning y crítico. `runCatching` evita cierre si el permiso no está disponible.

## Google Maps

La key nunca aparece en Kotlin, XML versionado ni documentación. Gradle la inserta como placeholder. El selector manual no solicita ubicación del dispositivo. La tarea guarda nombre/latitud/longitud/placeId nullable. `MapIntentHelper` intenta Google Maps y luego un Intent geo genérico; comprueba `resolveActivity` antes de abrir.

## Tema claro y oscuro

### `ThemeManager.kt`

Convierte la preferencia en `MODE_NIGHT_NO`, `MODE_NIGHT_YES` o `MODE_NIGHT_FOLLOW_SYSTEM` y llama a `AppCompatDelegate.setDefaultNightMode`.

### `values/colors.xml` y `values-night/colors.xml`

Definen fondo, superficie, texto principal/secundario, borde, color sobre primario, progreso y error. Los mismos nombres cambian según modo, por lo que layouts y drawables no necesitan condiciones.

### `values/themes.xml` y `values-night/themes.xml`

Configuran `colorPrimary`, `colorOnPrimary`, `colorSurface`, `colorOnSurface`, `colorOnBackground`, status bar y navigation bar. Material hereda estos atributos para diálogos, spinners, menús y controles.

## Arte vectorial de mascotas

### SVG fuente

`app/src/main/assets/artwork/` contiene 20 SVG editables: 15 combinaciones de perro/gato/conejo con estados feliz, neutral, triste, enferma y peligro; y 5 recompensas: collar, sombrero, capa, color dorado y fondo jardín. El estilo usa siluetas planas, contorno negro grueso y expresiones legibles.

### VectorDrawable Android

`res/drawable/pet_*.xml` y `reward_*.xml` contienen los mismos paths en formato Android. Esto permite usar `ImageView.setImageResource()` sin una dependencia para interpretar SVG en ejecución.

### `PetArtwork.kt`

`pet(type, mood)` traduce constantes de `PetEntity` a uno de los 15 drawables. `reward(assetName)` traduce el nombre persistido de recompensa al drawable correspondiente. El mapper centralizado evita condicionales de recursos dispersos por Activities y Adapters.

### Composición de capas

`activity_main.xml` utiliza un `FrameLayout`: fondo jardín y capa se dibujan detrás de la mascota; halo dorado, collar y sombrero se dibujan delante. `MainActivity.renderRewardLayer()` controla visibilidad según los campos equipados de Room.

### Generador

`tools/generate_pet_artwork.py` contiene las siluetas, expresiones y accesorios. Al ejecutarlo regenera los SVG fuente y VectorDrawable sincronizados.

## Layouts y recursos XML

- `activity_welcome.xml`: onboarding, imagen, nombre/tipo y acción principal.
- `activity_login.xml`: nombre, correo, contraseña visual, login y ayuda.
- `activity_main.xml`: resumen de mascota, composición vectorial por capas, XP, estadísticas, navegación y tareas urgentes.
- `activity_tasks.xml`: filtros, orden y RecyclerView.
- `activity_task_form.xml`: datos de tarea, spinners, fecha y ubicación opcional.
- `activity_location_picker.xml`: mapa, coordenadas, nombre y confirmar/cancelar.
- `activity_settings.xml`: mascota, recordatorios, tema, compartir, reset y logout.
- `activity_rewards.xml` y `activity_history.xml`: listas y métricas.
- `item_task.xml`: estado, contenido, ubicación y acciones.
- `bg_login_input`, `bg_primary_button`, `bg_help_button`, `bg_task_item`: superficies con colores que cambian por tema.
- `strings.xml`: textos centralizados, accesibilidad y formatos parametrizados.

Los IDs generan propiedades de View Binding. `match_parent`, `wrap_content`, pesos, márgenes y padding mantienen jerarquía visual. Hints y textos usan colores semánticos para ambos temas.

## Pruebas

### `PetDecayCalculatorTest.kt`

Verifica degradación a 6 h, penalización a 24 h, rango 0–100, idempotencia y cambio emocional.

### `PetNotificationEvaluatorTest.kt`

Verifica warning, crítico, cooldown y escalamiento warning→crítico.

### `PreferencesAndLocationTest.kt`

Verifica normalización/guardado lógico de tema, estado limpio de sesión y tareas con/sin ubicación.

Los esquemas Room v2 y v3 se exportan en `app/schemas`. Las migraciones están declaradas explícitamente y conservan datos; una prueba instrumentada de migración requeriría dispositivo/emulador.

## Flujos principales del sistema

### 1. Crear una tarea

Main/TaskList abre TaskForm. El usuario guarda. `TaskFormViewModel.saveTask()` llama `HomePetRepository.saveTask()`, que inserta mediante `TaskDao` y registra interacción. Room emite el nuevo listado y Activity Result confirma el cambio.

### 2. Editar una tarea

El adaptador pasa el id a TaskForm. ViewModel consulta `TaskDao.getTask()`. El formulario rellena campos y `TaskDao.update()` conserva el mismo id.

### 3. Completar una tarea

El adaptador llama `HomeViewModel/TasksViewModel.completeTask()`. Repository calcula recompensa, actualiza `tasks`, `pet`, historial y recompensas. StateFlow refresca Home.

### 4. Aplicar experiencia a la mascota

`PetRules.calculateReward()` calcula XP/monedas. `updatePetAfterTask()` procesa subidas de nivel sin modificar fuera de Room. Repository persiste la mascota.

### 5. Reducir estadísticas por inactividad

Al abrir o ejecutar Worker, Repository lee mascota, llama `PetDecayCalculator.calculate()` y actualiza `pet`. El timestamp avanzado hace el proceso idempotente.

### 6. Enviar notificación crítica

Worker evalúa la mascota. Si el nivel es crítico y pasa cooldown/escalamiento, NotificationManager muestra la alerta y Repository guarda timestamp/nivel.

### 7. Cambiar modo claro/oscuro

Settings selecciona modo → ViewModel → Repository → SharedPreferences. `ThemeManager.apply()` cambia AppCompatDelegate y recrea las superficies necesarias.

### 8. Cerrar sesión

Settings muestra diálogo. Confirmar llama `clearSession()`, abre Login con NEW_TASK/CLEAR_TASK y conserva tablas Room.

### 9. Seleccionar ubicación

TaskForm lanza LocationPicker. El usuario toca mapa, Geocoder intenta nombrar el punto y confirmar devuelve datos al launcher.

### 10. Guardar tarea sin ubicación

Los campos permanecen `null`; Room acepta las columnas nullable y el adaptador oculta la acción de mapa.

### 11. Guardar tarea con ubicación

TaskForm incorpora resultado en `TaskEntity`; Room persiste los cuatro campos y la tarjeta muestra nombre/acción.

### 12. Ejecutar migración Room

Al abrir una base v1/v2, Room ejecuta en orden `MIGRATION_1_2` y `MIGRATION_2_3`, añade columnas y conserva filas. Después valida el esquema contra las entidades actuales.

### 13. Comprar y mostrar una recompensa

Rewards llama `buyOrEquipReward()`. El Repository valida nivel/monedas, marca la recompensa desbloqueada y equipada, actualiza el campo visual correspondiente de `pet` y descuenta monedas. Room emite la mascota actualizada; Home selecciona el VectorDrawable y lo muestra en su capa del FrameLayout.
