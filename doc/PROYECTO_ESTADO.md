# Proyecto Recetas Asturianas - Estado Completo

## Informacion General

- **Nombre**: Recetas Asturianas
- **Package**: `es.uniovi.recetasasturianas`
- **Asignatura**: Informatica Movil (Universidad de Oviedo)
- **Arquitectura**: MVVM (Model-View-ViewModel) con Repository Pattern
- **SDK minimo**: 26 (Android 8.0 Oreo) — justificado por uso de Splash Screen API, NotificationChannel, y librerias modernas
- **SDK objetivo**: 35 (Android 15)
- **Lenguaje**: Kotlin 1.9.22
- **Deserializacion JSON**: Moshi 1.15.1 (requisito explicito del profesor)
- **Device testing**: Samsung Galaxy S21 FE (SM-G990B), Android 14
- **APK size**: ~8.4 MB

---

## Checklist del Contrato (Especificacion de la Aplicacion)

| # | Requisito Contractual | Estado | Implementacion |
|---|-----------------------|--------|----------------|
| 1 | **Fuente de datos**: catalogo abierto del Principado de Asturias (`https://www.turismoasturiasprofesional.es/open-data/turismoasturias`) | ✅ | `RetrofitClient.kt` URL publica + URL universidad + fallback `assets/recetas.json` |
| 2 | **Conjunto concreto**: "Recetas de cocina" via `156.35.163.145/json/RecetasCocina.json` | ✅ | `RetrofitClient.kt:18` + `RecipeApiService.kt:14` endpoint configurado |
| 3 | **Listado** con nombre, imagen y restaurante autor | ✅ | `RecipeAdapter.kt` bindea los 3 campos + Glide para imagenes |
| 4 | **Detalle completo**: ingredientes, preparacion, tiempo, trucos | ✅ | `RecipeDetailFragment.kt` renderiza HTML (ingredientsHtml, preparationHtml, timeMinutes, tipsHtml) |
| 5 | **Enlace al restaurante embebido** (NO navegador externo) — condicion extra por falta de GPS | ✅ | `RestaurantWebViewFragment.kt` con `WebViewClient` que NO sobrescribe URLs |
| 6 | **Buscar** por nombre y restaurante/autor | ✅ | `RecipeDao.kt:32-39` SQL LIKE sobre `name`, `restaurant`, `ingredientsHtml` |
| 7 | **Filtrar** por tiempo: rapida (<=30), media (31-60), larga (>60) | ✅ | `RecipeAdapter.kt:85-90` filtro UI con `TimeFilter` enum + Chips Material |
| 8 | **Guardar favoritos** | ✅ | `FavoriteDao.kt` CRUD + `RecipeDetailFragment.kt` FAB toggle + Snackbar feedback |
| 9 | **Recordar preferencias** (orden, filtros activos) | ✅ | `SettingsFragment.kt` + `preferences.xml` + `RecipeListViewModel.kt` persiste `last_filter` |
| 10 | **Condicion extra**: WebView embebido (sustituye a datos GPS inexistentes) | ✅ | `RestaurantWebViewFragment.kt` carga dentro de la app, comentario explicito L20 |

### Detalle de URLs configuradas

| Tipo | URL | Archivo |
|------|-----|---------|
| Principal (VPN universidad) | `http://156.35.163.145/json/RecetasCocina.json` | `RetrofitClient.kt:18` |
| Alternativa publica | `https://www.turismoasturiasprofesional.es/open-data/` | `RetrofitClient.kt:21` |
| Fallback local | `app/src/main/assets/recetas.json` (~276 KB, 78 recetas) | `RecipeRepository.kt:170` |

---

## Arquitectura MVVM

```
┌─────────────────────────────────────────────────────┐
│                    UI LAYER                          │
│  Fragments ──observan──► ViewModels (LiveData)       │
│  (RecipeListFragment, RecipeDetailFragment,          │
│   FavoritesFragment, SettingsFragment,                │
│   RestaurantWebViewFragment)                          │
└──────────────────────┬──────────────────────────────┘
                       │ llama a
┌──────────────────────▼──────────────────────────────┐
│               DOMAIN LAYER                           │
│           RecipeRepository (singleton)                │
│  Fuente unica de verdad. Decide: API remota,          │
│  Room cache, o assets fallback.                       │
│  Control de intervalo de refresco (SharedPreferences) │
└─────────────┬───────────────────────┬───────────────┘
              │                       │
┌─────────────▼──────┐   ┌───────────▼────────────────┐
│   DATA LAYER        │   │   DATA LAYER (REMOTE)      │
│  Room (SQLite)      │   │  Retrofit + Moshi          │
│  RecipeDatabase     │   │  RecipeApiService          │
│  RecipeDao           │   │  GET /json/RecetasCocina   │
│  FavoriteDao         │   │                            │
│  recipes table       │   │  OkHttp + logging          │
│  favorites table     │   │                            │
└─────────────────────┘   └────────────────────────────┘
                                   │ fallback
                          ┌────────▼───────────────────┐
                          │  assets/recetas.json        │
                          │  (Moshi parsing local)      │
                          └────────────────────────────┘
```

### Flujo de datos

1. **Inicio**: RecipeListFragment se crea y su ViewModel llama a `loadRecipes()`
2. **Repositorio**: `RecipeRepository.refreshRecipesIfNeeded()` decide si refrescar según `cachedAt` y `refresh_hours` (preferencia)
3. **Red**: Intenta `RetrofitClient.recipeApiService.getRecipes()` -> API Universidad (VPN)
4. **Fallback**: Si la API falla (timeout, sin VPN), carga desde `assets/recetas.json` via Moshi
5. **Cache**: Los datos se guardan en Room (tabla `recipes`) para acceso offline posterior
6. **UI**: ViewModel expone `LiveData<List<Recipe>>` que el Fragment observa y renderiza en RecyclerView
7. **Busqueda**: `RecipeDao.search(query)` con SQL `LIKE` sobre name, restaurant, ingredientsHtml
8. **Filtrado**: `RecipeAdapter.applyFilter()` filtra en la capa UI sin modificar el dataset subyacente

### Diferenciacion Busqueda vs Filtrado (concepto clave)

- **Busqueda**: Se implementa en la **capa de datos** (Repository + Room SQL). Cambia el conjunto de datos subyacente que recibe el ViewModel.
- **Filtrado**: Se implementa en la **capa UI** (Adapter + enum TimeFilter). Muestra u oculta elementos del dataset actual SIN modificarlo.

---

## Requisitos del Profesor (tenerEnCuenta.txt)

| Requisito | Estado | Implementacion |
|-----------|--------|----------------|
| minSdk 26 (justificar si API > 26) | ✅ CUMPLIDO | `build.gradle.kts`: `minSdk = 26`. Justificacion: Splash Screen API (Android 12+), NotificationChannel, librerias modernas que requieren API 26+. |
| Moshi en vez de Gson | ✅ CUMPLIDO | `RecipeResponse.kt` con `@Json(name=...)`, `FlexibleAdapterFactory`, `MoshiConverterFactory` en Retrofit |
| Espresso/Barista para UI tests | ✅ CUMPLIDO | 3 ficheros de test, 10 tests UI con Barista 4.3.0 + Espresso 3.5.1 |
| Documento Word explicativo | ✅ CUMPLIDO | `doc/Arquitectura_RecetasAsturianas.docx` — pendiente de generar |

---

## Tecnologias y Librerias (con justificacion)

### Core
| Libreria | Version | Proposito |
|----------|---------|-----------|
| Kotlin | 1.9.22 | Lenguaje oficial para Android, null-safety, corrutinas |
| Android SDK | 26-35 | Minimo Oreo, target Android 15 |
| ViewBinding | — | Acceso type-safe a vistas, elimina findViewById |

### Arquitectura
| Libreria | Version | Proposito | Justificacion |
|----------|---------|-----------|---------------|
| Room | 2.6.1 | Base de datos SQLite local | Cache offline, busquedas SQL eficientes, LiveData reactivo |
| Retrofit | 2.9.0 | Cliente HTTP type-safe | Estandar en Android para APIs REST |
| Moshi | 1.15.1 | Deserializacion JSON | Requisito profesor; mejor que Gson para Kotlin (null-safety, @JsonClass) |
| OkHttp | 4.12.0 | Cliente HTTP subyacente | Logging interceptor, timeouts configurables (30s) |
| LiveData | — | Datos observables reactivos | Ciclo-de-vida consciente, no pierde datos en rotacion |
| ViewModel | — | Estado UI sobrevive configuracion | Rotacion, llamadas, etc. |
| Coroutines | 1.7.3 | Operaciones async | Dispatchers.IO para Room/Retrofit, viewModelScope |

### UI
| Libreria | Version | Proposito |
|----------|---------|-----------|
| Material Design 3 | 1.11.0 | Tema Material You, BottomNavigation, Chips, FAB, Snackbar |
| Navigation Component | 2.7.7 | Navegacion entre fragments con SafeArgs, animaciones slide |
| Glide | 4.16.0 | Carga de imagenes con placeholder, crossfade, cache |
| SwipeRefreshLayout | 1.1.0 | Pull-to-refresh en la lista |
| Splash Screen API | 1.0.1 | Pantalla de bienvenida Android 12+ |
| Preference | 1.2.1 | Settings con PreferenceFragmentCompat |
| WebKit | 1.10.0 | WebView embebido para restaurantes |

### Testing
| Libreria | Version | Proposito |
|----------|---------|-----------|
| Espresso | 3.5.1 | Framework base de tests de UI |
| Barista | 4.3.0 | Wrapper de Espresso (API mas legible: clickOn, assertDisplayed, writeTo) |
| JUnit | 4.13.2 | Tests unitarios JVM |
| org.json | 20240303 | Test de parseo JSON sin dependencia de Android |

---

## Funcionalidades Implementadas (detalle)

### 1. Lista de Recetas (Maestro)
- **RecyclerView** con Cards (MaterialCardView)
- Cada card muestra: imagen (Glide), nombre, restaurante, tiempo de preparacion
- **Busqueda** en tiempo real: SearchView en ActionBar que busca por nombre, restaurante e ingredientes via Room SQL LIKE
- **Filtro** por tiempo: 3 Chips (Rapido <=30min, Medio 31-60min, Largo >60min) con persistencia de estado
- **Pull-to-refresh**: SwipeRefreshLayout para forzar recarga de datos
- **Banner offline**: Indicador visual cuando no hay conexion
- **Empty state**: Mensaje e icono cuando no hay resultados
- **Soporte tablet**: Layout alternativo en `res/layout-sw600dp/` con panel maestro-detalle

### 2. Detalle de Receta
- Imagen grande con crossfade (Glide)
- Nombre, restaurante, tiempo de preparacion
- Ingredientes renderizados desde HTML (`HtmlCompat.fromHtml`)
- Preparacion renderizada desde HTML
- Trucos y consejos (si existen en los datos, ~17/78 recetas)
- Notas adicionales (si existen, ~9/78 recetas)
- **FAB** para anadir/quitar favoritos con Snackbar de confirmacion
- **Boton "Ver restaurante"** que navega al WebView embebido
- **Menu "Compartir"** que lanza ACTION_SEND con el texto de la receta

### 3. Favoritos
- Lista independiente de recetas marcadas como favoritas
- Persistencia en Room (tabla `favorites`)
- Reutiliza el mismo RecipeAdapter que la lista principal
- Empty state con icono y mensaje cuando no hay favoritos
- Navegacion al detalle desde favoritos

### 4. Preferencias (SettingsFragment)
| Preferencia | Clave | Tipo | Valores | Default |
|-------------|-------|------|---------|---------|
| Intervalo de refresco | `refresh_hours` | ListPreference | 1, 6, 12, 24, 48, 168 horas | 24h |
| Ordenacion por defecto | `default_sort` | ListPreference | name, restaurant, time | name |
| Ocultar sin tiempo | `hide_no_time` | SwitchPreference | true/false | false |

### 5. WebView Embebido (condicion extra del profesor)
- Carga la URL del restaurante en un WebView dentro de la app
- **No abre navegador externo**: `WebViewClient.shouldOverrideUrlLoading()` retorna `false`
- ProgressBar durante la carga
- Manejo de errores con vista alternativa
- Zoom habilitado (settings.builtInZoomControls)
- JavaScript habilitado
- Toolbar propia con titulo "Ver restaurante"
- En modo telefono: oculta la toolbar principal de la actividad para evitar doble barra

### 6. Soporte Tablet (sw600dp)
- Layout alternativo en `res/layout-sw600dp/activity_main.xml`
- Panel izquierdo: RecyclerView de recetas (lista seleccionable)
- Panel derecho: detalle de la receta seleccionada (o WebView)
- Seleccion visual con color de fondo

### 7. Splash Screen
- Compatible con Android 12+ (core-splashscreen)
- Muestra icono de la app centrado
- Tema personalizado (Theme.RecetasAsturianas.Splash)
- Transicion suave a MainActivity

### 8. Modo Offline
- Deteccion de conectividad via `NetworkUtils.isNetworkAvailable()`
- Cache en Room para acceso sin conexion
- Fallback automatico a `assets/recetas.json` si la API no esta disponible
- Banner informativo en la UI cuando no hay conexion
- Intervalo de refresco configurable (SharedPreferences)

### 9. Multiidioma
- `res/values/strings.xml`: Espanol (idioma por defecto)
- `res/values-en/strings.xml`: Ingles
- Adaptacion automatica segun locale del dispositivo

### 10. Animaciones de navegacion
- `slide_in_right.xml` / `slide_out_left.xml`: Transicion hacia adelante
- `slide_in_left.xml` / `slide_out_right.xml`: Transicion hacia atras
- `fade_in.xml` / `fade_out.xml`: Transiciones sutiles

---

## API y Datos

### Fuente de Datos
- **Catalogo**: Datos abiertos del Principado de Asturias
- **Conjunto**: "Recetas de cocina" (78 recetas)
- **Formato**: JSON
- **URLs**: 3 fuentes (VPN universidad, publica, fallback local)

### Estructura JSON (simplificada)
```json
{
  "articles": {
    "article": [
      {
        "Nombre":       { "content": "Fabada Asturiana" },
        "Resumen":      { "content": "Por Casa Fermin" },
        "Imagen":       { "content": "{\"classPK\":94845,\"groupId\":\"39908\",\"title\":\"foto.jpg\",\"uuid\":\"...\"}" },
        "Visualizador": { "Slide": { "value": "...", "SlideUrl": { "content": "..." } } },
        "Informacion":  { "Preparacion": { "content": "<p>...</p>" },
                          "Donde":       { "content": "<a href='/ruta/...'>Nombre</a>" },
                          "JornadasGastronomicas": { "content": "..." } },
        "Contacto":     { "Tiempo":       { "content": "45 minutos" },
                          "Ingredientes": { "content": "<ul><li>...</li></ul>" } },
        "Observaciones": { "TrucosYConsejos": { "content": "..." },
                           "Observacion":     { "content": "..." } }
      }
    ]
  }
}
```

### Disponibilidad de campos
| Campo | Disponibilidad | Notas |
|-------|---------------|-------|
| Nombre | 78/78 ✅ | Siempre presente |
| Resumen (restaurante) | 78/78 ✅ | Formato "Por Nombre Restaurante" |
| Preparacion | 78/78 ✅ | HTML con `<p>` tags |
| Ingredientes | 78/78 ✅ | HTML con `<ul><li>` tags |
| Imagen | 76/78 ✅ | JSON string con uuid e info de imagen |
| Donde (URL restaurante) | 74/78 ✅ | HTML con enlace relativo |
| Tiempo | 55/78 ⚠️ | Texto libre ("20 minutos", "3 horas", etc.) |
| Trucos y consejos | 17/78 ⚠️ | Solo mostrar si existe |
| Notas | 9/78 ⚠️ | Solo mostrar si existe |

### Parseos Especiales (Mappers.kt)
- **Restaurante**: `resumen.removePrefix("Por ").trim()` -> "Casa Fermin"
- **Tiempo**: Regex `(\d+)\s*hora?s?` + `(\d+)\s*minuto?s?` -> minutos enteros. Ej: "1 hora 30 minutos" -> 90
- **URL imagen**: Construye desde JSON anidado: `https://www.turismoasturias.es/documents/{groupId}/0/{title}/{uuid}?version=1.0`
- **URL restaurante**: Extrae `href` del HTML de "Donde". Si es relativo, antepone `https://www.turismoasturias.es`

---

## Base de Datos Room

### Tabla `recipes`
| Campo | Tipo | Descripcion |
|-------|------|-------------|
| id | Int (PK) | ID unico de la receta |
| name | String | Nombre de la receta |
| restaurant | String | Nombre del restaurante (sin prefijo "Por") |
| timeMinutes | Int? | Tiempo en minutos (null si no disponible) |
| ingredientsHtml | String | Ingredientes en HTML |
| preparationHtml | String | Preparacion en HTML |
| tipsHtml | String? | Trucos y consejos (nullable) |
| notesHtml | String? | Notas adicionales (nullable) |
| imageUrl | String? | URL construida de la imagen (nullable) |
| restaurantUrl | String? | URL del restaurante (nullable) |
| cachedAt | Long | Timestamp de cache (epoch millis) para control de refresco |

### Tabla `favorites`
| Campo | Tipo | Descripcion |
|-------|------|-------------|
| recipeId | Int (PK, FK -> recipes.id) | ID de la receta favorita |
| savedAt | Long | Timestamp de cuando se guardo |

### Consultas SQL destacadas (RecipeDao)
| Metodo | Funcion |
|--------|---------|
| `getAll()` | `SELECT * FROM recipes ORDER BY name` |
| `search(query)` | `SELECT * FROM recipes WHERE name LIKE '%query%' OR restaurant LIKE '%query%' OR ingredientsHtml LIKE '%query%'` |
| `getByMaxTime(max)` | `SELECT * FROM recipes WHERE timeMinutes <= max` |
| `getWithTime()` | `SELECT * FROM recipes WHERE timeMinutes IS NOT NULL` |

---

## Tests

### Unit Tests (`MappersTest.kt`) — 20 tests, JVM
| Grupo | Tests | Descripcion |
|-------|-------|-------------|
| `extractRestaurantName()` | 4 | Con prefijo "Por", sin prefijo, null, vacio |
| `parseTimeToMinutes()` | 6 | Solo minutos, solo horas, combinados, null, vacio, solo numero |
| `extractRestaurantUrl()` | 4 | URL relativa, absoluta, null, sin href |
| `extractImageUrl()` | 6 | JSON valido, anidado, espacios, invalido, null, con slide |

### UI Tests (Barista + Espresso) — 10 tests en dispositivo real
| Archivo | Tests | Descripcion |
|---------|-------|-------------|
| `NavigationTest.kt` | 2 | Navegacion BottomNavigation entre tabs, titulo toolbar |
| `RecipeListFragmentTest.kt` | 5 | Display lista, click receta -> detalle, busqueda, filtro tiempo, pull-to-refresh |
| `RecipeDetailFragmentTest.kt` | 3 | Display datos detalle, toggle favoritos, boton restaurante |

### Libreria de testing: Barista 4.3.0
- API fluida: `clickOn(R.id.button)`, `assertDisplayed(R.id.view)`, `writeTo(R.id.edit, "text")`
- Scroll automatico antes de interactuar con vistas
- Soporte para NestedScrollView (Espresso no soporta nativamente)
- Esperas condicionales integradas en `assertDisplayed()`
- Coexiste con Espresso para operaciones avanzadas (RecyclerViewActions)

---

## Decisiones de Diseno (con justificacion)

### 1. Moshi vs Gson
Moshi se eligio por requisito del profesor. Ademas, Moshi ofrece:
- Mejor soporte Kotlin (null safety, `@JsonClass`, `@Json(name=...)`)
- Mejor integracion con Retrofit (mismo ecosistema Square)
- `FlexibleAdapterFactory` para manejar campos JSON inconsistentes (objeto vs array)

### 2. BottomNavigation vs DrawerLayout
Se eligio BottomNavigation con 3 tabs porque la app tiene pocas pantallas principales (Recetas, Favoritos, Ajustes). Es el patron recomendado por Material Design para 3-5 destinos de navegacion.

### 3. Room como cache local
Room proporciona:
- Cache offline para acceso sin conexion
- Consultas SQL eficientes para busqueda
- LiveData reactivo que actualiza la UI automaticamente
- Control de refresco por intervalo configurable

### 4. Fallback a assets
Ante la imposibilidad de acceder a la API (VPN requerida), la app carga automaticamente desde `assets/recetas.json`, garantizando que siempre haya datos disponibles para tests y demostraciones.

### 5. FlexibleAdapterFactory (Moshi)
La API devuelve campos JSON inconsistentes: a veces son objetos `{"content": "..."}` y a veces arrays `[{"content": "..."}]`. FlexibleAdapterFactory resuelve esto probando primero BEGIN_OBJECT y, si es BEGIN_ARRAY, tomando el primer elemento del array.

### 6. WebView embebido (condicion extra profesor)
El conjunto de datos "Recetas de cocina" del Principado de Asturias **no incluye coordenadas GPS**. El profesor impuso que los enlaces a restaurantes se abran DENTRO de la app. Implementado con `WebViewClient.shouldOverrideUrlLoading()` retornando `false`.

### 7. ViewBinding vs findViewById
ViewBinding ofrece acceso type-safe a las vistas, eliminando errores de tipo y null safety en tiempo de compilacion. Mas ligero que DataBinding (solo binding, sin expresiones).

---

## Problemas Conocidos

### 1. VPN Universidad
- La API principal requiere VPN GlobalProtect (portalgp.uniovi.es)
- **Solucion**: Fallback automatico a `assets/recetas.json` en caso de error de red
- El fallback se activa automaticamente sin intervencion del usuario

### 2. Android 15 (API 35)
- Splash Screen API obligatorio en Android 15+
- **Solucion**: Implementado con `androidx.core:core-splashscreen`

### 3. Toolbar / ActionBar
- El tema es `NoActionBar` (Material 3) pero Navigation Component lo requiere
- **Solucion**: Toolbar en layout + `setSupportActionBar()` en MainActivity

### 4. Campos JSON inconsistentes
- Algunos campos JSON son a veces objeto, a veces array
- **Solucion**: FlexibleAdapterFactory de Moshi que maneja ambas variantes

---

## Comandos de Compilacion

```bash
# Compilar APK debug
./gradlew assembleDebug

# Instalar en dispositivo
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Ejecutar tests unitarios (JVM, rapidos)
./gradlew test

# Ejecutar tests UI (requiere dispositivo/emulador conectado)
./gradlew connectedAndroidTest

# Compilar + tests unitarios + tests UI
./gradlew test connectedAndroidTest

# Limpiar y compilar
./gradlew clean assembleDebug

# Generar APK release (firmado)
./gradlew assembleRelease

# Ver logcat para errores de la app
adb logcat -d | grep -E "RecetasAsturianas|FATAL"

# Ver tamano del APK
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

---

## Archivos de Recursos

### Layouts (6)
- `activity_main.xml` — Layout principal con Toolbar, NavHostFragment, BottomNavigation
- `layout-sw600dp/activity_main.xml` — Layout tablet con panel maestro-detalle
- `fragment_recipe_list.xml` — Lista con chips de filtro, SearchView, banner offline
- `fragment_recipe_detail.xml` — Detalle con scroll, FAB, boton restaurante
- `fragment_favorites.xml` — Favoritos con empty state
- `fragment_web_view.xml` — WebView con toolbar propia y ProgressBar
- `item_recipe.xml` — Card individual para el RecyclerView

### Drawables (8)
- `ic_favorite_filled.xml` / `ic_favorite_outline.xml` — Iconos favorito
- `ic_share.xml` — Icono compartir
- `ic_offline.xml` — Icono modo offline
- `ic_empty_favorites.xml` / `ic_empty_recipes.xml` — Empty states
- `ic_launcher_foreground.xml` — Icono app
- `placeholder_recipe.xml` — Placeholder para recetas sin imagen

### Menus (3)
- `bottom_nav_menu.xml` — 3 items: Recetas, Favoritos, Ajustes
- `menu_search.xml` — SearchView en ActionBar
- `menu_detail.xml` — Compartir receta

---

## Estructura del Proyecto Completa

```
app/
├── build.gradle.kts                  # Dependencias y configuracion
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml       # Permisos, activities, network config
│   │   ├── assets/
│   │   │   └── recetas.json          # Datos de respaldo (276 KB, 78 recetas)
│   │   ├── java/es/uniovi/recetasasturianas/
│   │   │   ├── App.kt                # Application class
│   │   │   ├── MainActivity.kt       # Activity principal con Navigation
│   │   │   ├── SplashActivity.kt     # Splash Screen (Android 12+)
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── RecipeDao.kt        # DAO recetas (busquedas SQL)
│   │   │   │   │   ├── RecipeDatabase.kt   # Room DB (recetas + favoritos)
│   │   │   │   │   └── FavoriteDao.kt      # DAO favoritos
│   │   │   │   ├── remote/
│   │   │   │   │   ├── RecipeApiService.kt # Interface Retrofit
│   │   │   │   │   ├── RetrofitClient.kt   # Singleton Retrofit+OkHttp+Moshi
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── RecipeResponse.kt  # DTOs + FlexibleAdapterFactory
│   │   │   │   │       └── Mappers.kt         # Mapeo DTO -> Entity
│   │   │   │   ├── model/
│   │   │   │   │   ├── Recipe.kt        # Entity Room + Parcelable
│   │   │   │   │   └── Favorite.kt      # Entity Room
│   │   │   │   └── repository/
│   │   │   │       └── RecipeRepository.kt  # Fuente unica de datos
│   │   │   ├── ui/
│   │   │   │   ├── list/               # Lista de recetas (maestro)
│   │   │   │   │   ├── RecipeListFragment.kt
│   │   │   │   │   ├── RecipeListViewModel.kt
│   │   │   │   │   └── RecipeAdapter.kt
│   │   │   │   ├── detail/             # Detalle de receta
│   │   │   │   │   ├── RecipeDetailFragment.kt
│   │   │   │   │   └── RecipeDetailViewModel.kt
│   │   │   │   ├── favorites/          # Lista de favoritos
│   │   │   │   │   ├── FavoritesFragment.kt
│   │   │   │   │   └── FavoritesViewModel.kt
│   │   │   │   ├── settings/           # Preferencias
│   │   │   │   │   └── SettingsFragment.kt
│   │   │   │   └── webview/            # WebView embebido
│   │   │   │       └── RestaurantWebViewFragment.kt
│   │   │   └── util/
│   │   │       └── NetworkUtils.kt     # Deteccion de conectividad
│   │   └── res/                        # Recursos (layouts, drawables, values, etc.)
│   ├── test/
│   │   └── .../MappersTest.kt          # 20 tests unitarios
│   └── androidTest/
│       ├── NavigationTest.kt           # 2 tests UI
│       ├── RecipeListFragmentTest.kt   # 5 tests UI
│       ├── RecipeDetailFragmentTest.kt # 3 tests UI
│       └── util/
│           └── DataLoadingIdlingResource.kt  # Utilidad de sincronizacion
```

---

## Historial de Cambios

### Mayo 2026 — Implementacion Barista + mejora tests
- `build.gradle.kts`: Añadida dependencia `com.adevinta.android:barista:4.3.0`
- Tests reescritos con API Barista (`clickOn`, `assertDisplayed`, `writeTo`)
- Esperas condicionales (polling) en vez de `Thread.sleep()` para carga de datos
- Documentacion actualizada en todos los MD

### Mayo 2026 — Correcciones finales (v1.0)
- `activity_main.xml`: `fitsSystemWindows="true"` para evitar solapamiento barra de estado
- `RestaurantWebViewFragment.kt`: Ocultar toolbar principal en modo telefono
- `MainActivity.kt`: Metodos `hideToolbar()` / `showToolbar()` para fragments con toolbar propia
- `RetrofitClient.kt` + `RecipeRepository.kt`: Anadido `KotlinJsonAdapterFactory()` a Moshi
- `SettingsFragment.kt`: Fix crash ListPreference con `Handler.post()` para cambio de tema
- Tests Espresso: timeouts 30s, matchers `withEffectiveVisibility`, busqueda por `search_src_text`
- Resultado: **20/20 unit tests + 10/10 UI tests pasan**

### Mayo 2026 — Migracion Gson a Moshi
- `RecipeResponse.kt`: `@SerializedName` -> `@Json(name=...)`, `FlexibleAdapterFactory`
- `RetrofitClient.kt`: `GsonConverterFactory` -> `MoshiConverterFactory`
- `RecipeRepository.kt`: `Gson()` -> Moshi `adapter()` para assets
- `build.gradle.kts`: `minSdk 21->26`, eliminado `converter-gson`, añadido `moshi-kotlin` y `converter-moshi`

---

## Informacion del Alumno

- **Alumno**: (Pon aqui tu nombre)
- **Email**: (Pon aqui tu email)
- **Universidad**: Universidad de Oviedo
- **Asignatura**: Informatica Movil — Grado en Ingenieria Informatica en Tecnologias de la Informacion
- **Curso**: 2025/2026

---

*Ultima actualizacion: 15 de mayo de 2026*
