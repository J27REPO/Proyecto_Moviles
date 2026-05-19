# AGENTS.md — Memoria persistente para sesiones con opencode

## Perfil del usuario
- Estudiante de Informatica Movil, Universidad de Oviedo
- Proyecto: Recetas Asturianas (Android/Kotlin)
- Dispositivo test: Samsung Galaxy S21 FE (SM-G990B), Android 14, conectado por USB
- Prefiere espanol para comunicacion
- Quiere respuestas concisas y directar, sin rodeos
- Le gusta que sea autonomo y pruebe cosas en su dispositivo sin pedir permiso cada vez
- Valida siempre con tests reales en dispositivo (no solo compilacion)
- Necesita documentacion detallada para el Word de entrega
- Fecha defensa: aproximadamente finales de mayo 2026

## Estado del proyecto (19 Mayo 2026) — COMPLETO para entrega
- MVVM + Repository + Room 2.6.1 + Retrofit 2.9.0 + Moshi 1.15.1
- minSdk 26, targetSdk 35, Kotlin 1.9.22, ViewBinding, Material 3
- Glide 4.16.0, Navigation Component 2.7.7, Splash Screen API
- Barista 4.3.0 + Espresso 3.5.1 para UI tests
- 20 tests unitarios (MappersTest.kt) + 10 tests UI -> TODOS PASAN (30 total)
- APK ~8.4 MB, probado en SM-G990B real

## Documentacion del proyecto
Los archivos de documentacion mas importantes son:
- `doc/PROYECTO_ESTADO.md` — Estado completo del proyecto (544 lineas)
- `doc/MEMORY.md` — Memoria tecnica detallada (159 lineas)
- `doc/readme.md` — Especificacion/prompt original del proyecto (445 lineas)
- `doc/GUIA_SIMPLE.md` — Guia "para tontos" explicando arquitectura y componentes (NUEVA)
- `doc/PREGUNTAS_DEFENSA.md` — 32 preguntas de defensa con respuestas (NUEVA)
- `doc/AGENTS.md` — Este archivo (memoria persistente)
- `doc/MACRO_WORD.md` — Guia para el Word de entrega
- `doc/plan.md` — Plan de correcciones
- `doc/CHANGELOG_FALLBACK_API.md` — Registro de cambios

## Requisitos obligatorios del profesor
- Moshi (NO Gson) — CUMPLIDO
- minSdk 26 — CUMPLIDO
- Espresso/Barista para UI tests — CUMPLIDO
- Documento Word explicativo — lo hace el alumno a mano guiado por doc/MACRO_WORD.md
- WebView embebido para restaurantes (NO navegador externo) — CUMPLIDO (requisito por falta de datos GPS)

## Arquitectura del proyecto
```
UI Layer: Fragments (RecipeList, RecipeDetail, Favorites, Settings, WebView) + ViewModels + LiveData
     ↓ llama a
Domain Layer: RecipeRepository (singleton, Single Source of Truth)
     ↓ decide
Data Layer:
  ├── Room (SQLite local): RecipeDatabase, RecipeDao, FavoriteDao
  ├── Retrofit (API): RecipeApiService, RetrofitClient, Moshi
  └── Assets: recetas.json (fallback, 78 recetas siempre disponible)
```

### Fuentes de datos (en orden de prioridad)
1. API Universidad (requiere VPN): `http://156.35.163.145/json/RecetasCocina.json`
2. API Publica: `https://www.turismoasturiasprofesional.es/open-data/turismoasturias`
3. Fallback local: `assets/recetas.json` (~276KB, 78 recetas)

### Flujo de datos clave
1. RecipeListFragment.onViewCreated() → RecipeListViewModel.loadRecipes()
2. → RecipeRepository.refreshRecipesIfNeeded() (mira cachedAt vs refresh_hours)
3. → Retrofit.getRecipes() o loadFromAssets() como fallback
4. → recipeDao.insertAll() (Room cache)
5. → recipeDao.getAll() → LiveData<List<Recipe>>
6. → Fragment observa y renderiza RecyclerView

## Archivos clave y líneas importantes
| Archivo | Línea/Clase clave | Propósito |
|---------|-------------------|-----------|
| `RecipeRepository.kt` | línea 87 `refreshRecipesIfNeeded()` | Jefe de datos, fallback automático |
| `RecipeRepository.kt` | línea 140 `refreshRecipes()` | Intenta API, si falla carga assets |
| `RecipeRepository.kt` | línea 173 `loadFromAssets()` | Carga recetas.json local |
| `RecipeListViewModel.kt` | línea 40 `loadRecipes()` | Carga recetas desde repository |
| `RecipeListViewModel.kt` | línea 67 `search()` | Búsqueda por nombre/restaurante |
| `RecipeListFragment.kt` | línea 68 `navigateToDetail()` | Navegación al detalle |
| `RecipeAdapter.kt` | línea 85 `applyFilter()` | Filtrado por tiempo (capa UI) |
| `RecipeDao.kt` | línea 32 `search()` | SQL LIKE búsqueda (capa datos) |
| `Mappers.kt` | línea 14 `extractRestaurantName()` | Quita "Por " del restaurante |
| `Mappers.kt` | línea 30 `parseTimeToMinutes()` | Convierte "1h 30min" → 90 |
| `Mappers.kt` | línea 45 `extractImageUrl()` | JSON → URL imagen real |
| `Mappers.kt` | línea 70 `extractRestaurantUrl()` | HTML href → URL limpia |
| `RetrofitClient.kt` | línea 30 `Moshi.Builder()` | Configura Moshi con FlexibleAdapterFactory |
| `MainActivity.kt` | línea 46 `setupNavigation()` | Configura NavController + BottomNav |
| `nav_graph.xml` | `<fragment>` tags | Define destinos y acciones de navegación |

## Diferencia BUSQUEDA vs FILTRADO (concepto clave del enunciado)
- **BUSQUEDA** (capa DATOS): `RecipeDao.search(query)` con SQL LIKE. Cambia el dataset subyacente (menos datos del servidor/BD).
- **FILTRADO** (capa UI): `RecipeAdapter.applyFilter(TimeFilter)`. Muestra/oculta elementos del dataset actual SIN modificarlo. Opera exclusivamente en el Adapter.

## Modelos de datos
```kotlin
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: Int,
    val name: String,
    val restaurant: String,        // Sin "Por "
    val preparationHtml: String,   // HTML crudo
    val ingredientsHtml: String,  // HTML crudo
    val imageUrl: String?,         // Construida de JSON
    val restaurantUrl: String?,   // Extraida del HTML de "Donde"
    val timeMinutes: Int?,        // null si no disponible (55/78 recetas tienen tiempo)
    val tipsHtml: String?,        // null si no existe (17/78)
    val notesHtml: String?,       // null si no existe (9/78)
    val cachedAt: Long             // Timestamp para control de refresco
)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val recipeId: Int,
    val savedAt: Long = System.currentTimeMillis()
)
```

## Funcionalidades implementadas
- Lista recetas (RecyclerView + Cards + Glide)
- Busqueda (Room SQL LIKE: nombre, restaurante, ingredientes)
- Filtro tiempo (chips: rapido <=30, medio 31-60, largo >60)
- Detalle receta (HTML renderizado, imagenes, ingredientes, preparacion, trucos, notas)
- Favoritos (Room + FAB toggle + Snackbar)
- Preferencias (SettingsFragment: orden, intervalo refresco, ocultar sin tiempo)
- WebView embebido (NO navegador externo — requisito por falta de datos GPS)
- Soporte tablet (layout-sw600dp maestro-detalle)
- Multiidioma (espanol + ingles)
- Splash Screen (Android 12+)
- Modo offline (Room cache + banner + fallback assets)
- Animaciones de navegacion (slide)
- Compartir receta (ACTION_SEND)

## Testing
- Unitarios (20): `./gradlew test` — MappersTest.kt (funciones puras parsing)
- UI (10): `./gradlew connectedAndroidTest` — Barista + Espresso
- Todos: `./gradlew test connectedAndroidTest`
- Compilar APK: `./gradlew assembleDebug`
- Instalar: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

## Parseos especiales (Mappers.kt)
- `extractRestaurantName("Por Casa Fermín")` → `"Casa Fermín"` (quitar prefijo)
- `parseTimeToMinutes("1 hora 30 minutos")` → `90` (regex horas + minutos)
- `extractImageUrl(JSON string)` → URL real de turismoasturias.es
- `extractRestaurantUrl(HTML string)` → URL limpia sin HTML

## Decisiones de diseño importantes
1. BottomNavigation (no Drawer): 3 destinos de igual importancia
2. Room 2 tablas separadas: recipes (cache) + favorites (usuario)
3. FlexibleAdapterFactory: maneja JSON inconsistente (objeto vs array)
4. WebView embebido: requisito profesor por falta de datos GPS
5. ViewBinding (no DataBinding): mas ligero, solo binding type-safe
6. Fallback assets: garantiza datos siempre disponibles (sin VPN)
7. SharedPreferences para ajustes (refresh_hours, default_sort, hide_no_time, app_theme)

## Preguntas de defensa preparadas
El archivo `doc/PREGUNTAS_DEFENSA.md` tiene 32 preguntas con respuestas.
Las mas importantes para repasar:
- P1: ¿Por qué MVVM?
- P3: Flujo de datos desde que abres la app
- P9: De dónde vienen los datos (3 fuentes)
- P10: Por qué Moshi y no Gson
- P13: Diferencia búsqueda vs filtrado (LA PREGUNTA CLAVE)
- P17: Por qué WebView embebido
- P21: Por qué Glide para imágenes

## Comandos útiles
```bash
./gradlew assembleDebug              # Compilar APK
./gradlew test                        # Tests unitarios (20)
./gradlew connectedAndroidTest       # Tests UI (10, requiere dispositivo)
./gradlew test connectedAndroidTest   # Todos los tests
adb install -r app/build/outputs/apk/debug/app-debug.apk  # Instalar
adb logcat -d | grep -E "RecetasAsturianas|FATAL"         # Ver errores
./gradlew clean assembleDebug        # Limpiar y compilar
```

## Package principal
`es.uniovi.recetasasturianas`

## Estructura de paquetes
```
es.uniovi.recetasasturianas/
├── data/
│   ├── local/          RecipeDatabase, RecipeDao, FavoriteDao
│   ├── remote/         RetrofitClient, RecipeApiService, dto/
│   ├── model/          Recipe, Favorite
│   └── repository/     RecipeRepository
├── ui/
│   ├── list/           RecipeListFragment, RecipeListViewModel, RecipeAdapter
│   ├── detail/         RecipeDetailFragment, RecipeDetailViewModel
│   ├── favorites/      FavoritesFragment, FavoritesViewModel
│   ├── settings/       SettingsFragment
│   ├── webview/        RestaurantWebViewFragment
│   └── theme/          ThemeManager
├── MainActivity.kt
├── SplashActivity.kt
├── App.kt
└── util/               NetworkUtils
```