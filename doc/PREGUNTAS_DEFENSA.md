# Preguntas de Defensa — Recetas Asturianas

> Preparación para la presentación/defensa del proyecto de Informática Móvil.
> Answers with page/file references so you can quickly find the code.

---

## 🏛️ ARQUITECTURA

### P1: ¿Por qué arquitectura MVVM y no otra?

**Respuesta:**
MVVM (Model-View-ViewModel) es el patrón recomendado por Google para Android. Permite:
- **Separación clara**: la UI (Fragment) no conoce la lógica de negocio
- **Ciclo de vida seguro**: ViewModel sobrevive a cambios de configuración (rotación, llamada entrante)
- **Testabilidad**: el ViewModel no depende de Android, se puede testear facilmente
- **LiveData reactivo**: los cambios en datos propagan automaticamente a la UI

Alternativas que descarté:
- MVC: Demasiado acoplado, el Controller depende de la Vista
- MVP: Mucho boilerplate con interfaces
- Clean Architecture: Exagerado para este tamaño de proyecto

**Ver:** `PROYECTO_ESTADO.md:43-76`

---

### P2: ¿Qué es el Repository Pattern y por qué lo usas?

**Respuesta:**
El Repository es un **pattern que actua como fuente única de datos** (Single Source of Truth). En mi caso, RecipeRepository decide:
1. ¿Tengo datos en caché? → usarlos
2. ¿La caché es muy vieja? → ir a la API
3. ¿La API falla? → cargar desde assets (fallback local)

```
Fragment → ViewModel → Repository → [Room ó Retrofit ó Assets]
```

**Ventajas:**
- El Fragment no sabe de dónde vienen los datos (da igual)
- Si mañana cambia la API, solo toco el Repository
- Facilita el testing (puedes mockear el Repository)

**Ver:** `RecipeRepository.kt:30-168`

---

### P3: Explica el flujo de datos desde que abres la app hasta que ves una receta

**Respuesta:**
```
1. SplashActivity → MainActivity (Splash Screen API, Android 12+)
        ↓
2. MainActivity.onCreate() → setupNavigation()
        ↓
3. NavHostFragment carga RecipeListFragment (destino inicial)
        ↓
4. RecipeListFragment.onViewCreated() → observeViewModel()
        ↓
5. RecipeListViewModel.loadRecipes()
        ↓
6. RecipeRepository.refreshRecipesIfNeeded()
   - Comprueba cachedAt vs refresh_hours (SharedPreferences)
   - Si necesito refrescar → RetrofitClient.apiService.getRecipes()
   - Si falla → loadFromAssets("recetas.json")
        ↓
7. recipeDao.insertAll(recipes) → Room
        ↓
8. recipeDao.getAll() → LiveData<List<Recipe>>
        ↓
9. Fragment observa el LiveData → adapter.submitList(recipes)
        ↓
10. RecyclerView.render() → usuario ve la lista
```

**Ver:** `RecipeListFragment.kt:55-65` + `RecipeRepository.kt:87-118`

---

### P4: ¿Qué es LiveData y por qué lo usas?

**Respuesta:**
LiveData es un **observable lifecycle-aware**. Significa:
- Los datos se "observan" y cuando cambian, la UI se actualiza sola
- Es consciente del ciclo de vida: si el Fragment está pausado, no recibe actualizaciones
- No hay leaks de memoria (se destruye con el Fragment)

```kotlin
// El ViewModel expone LiveData
val recipes: LiveData<List<Recipe>> = recipeDao.getAll()

// El Fragment observa
viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
    adapter.submitList(recipes) // La UI se actualiza sola
}
```

**Ver:** `RecipeListViewModel.kt:40-60`

---

### P5: ¿Por qué分开 UI y datos? ¿No era más fácil poner todo en el Fragment?

**Respuesta:**
分开 (separar) es fundamental por:
1. **Rotación de pantalla**: si todo está en el Fragment, al rotar se pierden los datos. El ViewModel sobrevive.
2. **Testing**: puedo testear el ViewModel sin abrir la app
3. **Mantenibilidad**: si mañana cambia la API, solo toco Repository, no la UI
4. **Reutilización**: el mismo ViewModel puede servir para tablets y teléfonos

```kotlin
// MAL (acoplado)
class RecipeListFragment {
    fun loadRecipes() {
        // todo aqui: red, base de datos, UI... lío
    }
}

// BIEN (separado)
class RecipeListViewModel {
    // solo lógica de datos
}
class RecipeListFragment {
    // solo mostrar cosas
}
```

---

## 💾 BASE DE DATOS (ROOM)

### P6: ¿Por qué Room y no otra solución?

**Respuesta:**
Room es la **biblioteca oficial de Android para SQLite**. Ventajas:
- **SQL queries typed-safe** en tiempo de compilación
- **LiveData integration** para UI reactiva
- **Compile-time verification** — errores de SQL los pillas al compilar, no en producción
- **Documentation excelente** y mantenimiento de Google

Alternativas:
- SQLite raw: mucho código, sin tipado
- Realm: overkill, licencia complicated
- SharedPreferences: solo para ajustes simples, no para 78 recetas

**Ver:** `RecipeDatabase.kt` + `RecipeDao.kt`

---

### P7: ¿Por qué dos tablas (recipes y favorites) y no una?

**Respuesta:**
Separación de responsabilidades:
- **recipes**: caché de las recetas descargadas (puede crecer, actualizarse)
- **favorites**: preferencias del usuario (solo tuyos favoritos)

Ventajas:
- Puedo borrar la caché de recetas y recalcar desde la API sin perder favoritos
- Las consultas de favoritos son simples (buscar por recipeId)
- Si en el futuro quiero añadir评分 o notas personales, tengo tabla para hacerlo

```kotlin
@Entity(tableName = "recipes") // caché de recetas
data class Recipe(...)

@Entity(tableName = "favorites") // favoritos del usuario
data class Favorite(
    @PrimaryKey val recipeId: Int, // FK a recipes.id
    val savedAt: Long
)
```

**Ver:** `Recipe.kt:23-36` + `Favorite.kt`

---

### P8: ¿Qué pasa si el usuario gira el teléfono mientras carga datos?

**Respuesta:**
El ViewModel sobrevive a cambios de configuración gracias a ViewModelScope:
- Cuando gira, el Fragment se destruye y se recrea
- PERO el ViewModel se mantiene (es lifecycle-aware)
- Cuando el nuevo Fragment se crea, ya tiene los datos en el ViewModel
- No se pierde la carga, no se vuelve a empezar

```
Giro pantalla:
Fragment.onDestroy() → ViewModel sobrevive → Fragment.onCreate() → observa datos (ya disponibles)
```

Si estuviera todo en el Fragment, al girar se perdería todo.

**Ver:** `RecipeListViewModel.kt:29` — `viewModelScope.launch`

---

## 🌐 RED / API

### P9: ¿De dónde vienen los datos exactamente?

**Respuesta:**
3 fuentes, en orden de prioridad:

1. **API Universidad** (requiere VPN): `http://156.35.163.145/json/RecetasCocina.json`
2. **API Pública alternativa**: `https://www.turismoasturiasprofesional.es/open-data/turismoasturias`
3. **Fallback local**: `assets/recetas.json` (276KB, 78 recetas, siempre disponible)

El Repository prueba en orden:
```kotlin
val recipes = try {
    apiService.getRecipes() // intenta API
} catch (e) {
    emptyList() // falla → siguiente
}
if (recipes.isEmpty()) {
    loadFromAssets() // fallback
}
```

**Ver:** `RecipeRepository.kt:140-168` + `RetrofitClient.kt:18-21`

---

### P10: ¿Por qué Moshi y no Gson?

**Respuesta:**
Requisito explícito del profesor. Adicionalmente, Moshi tiene ventajas para Kotlin:
- Mejor null-safety (el profesor especificó Moshi)
- `@Json(name=...)` más explícito que `@SerializedName`
- `FlexibleAdapterFactory` para manejar JSON inconsistente de la API (a veces objeto, a veces array)
- Mejor rendimiento en Kotlin (uso de反射)

Gson también funciona, pero Moshi es el estándar actual en Android con Kotlin.

**Ver:** `RecipeResponse.kt:1-50` + `Mappers.kt` + `build.gradle.kts`

---

### P11: ¿Qué es FlexibleAdapterFactory y por qué lo necesitas?

**Respuesta:**
La API del Principado de Asturias devuelve JSON **inconsistente**:
- Algunos campos vienen como objeto: `"Imagen": { "content": "..." }`
- Otros vienen como array: `"Visualizador": [{ "Slide": {...} }]`

FlexibleAdapterFactory prueba ambos formatos:
```kotlin
// Intenta parsear como objeto primero
// Si es array, coge el primer elemento
class FlexibleAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>?
}
```

Sin esto, Moshi fallaría al parsear recipes con campos array y la app cascaría.

**Ver:** `RecipeResponse.kt` + `RetrofitClient.kt:30`

---

### P12: ¿Por qué el tiempo de preparación es nullable (Int?)?

**Respuesta:**
Porque no todas las recetas tienen ese dato:
- **78 recetas totales**
- **55 tienen tiempo** (campo "Tiempo")
- **23 NO tienen tiempo** (el campo no existe o está vacío)

Si guardo `null` en vez de 0, puedo distinguir:
- `null` = no se proporcionó tiempo
- `0` = se proporcionó pero era "0 minutos" (raro)

El adapter maneja esto:
```kotlin
recipe.timeMinutes?.let { "${it} min" } ?: "Tiempo no disponible"
```

**Ver:** `RecipeDao.kt:26-30` (solo recetas con tiempo)

---

## 🔍 BÚSQUEDA Y FILTRADO

### P13: Explica la diferencia entre búsqueda y filtrado

**Respuesta:**
Esta es LA pregunta clave del proyecto:

| | Búsqueda | Filtrado |
|--|---------|---------|
| **Capa** | Datos (Room SQL) | UI (Adapter) |
| **Qué modifica** | El dataset completo | Lo que se muestra |
| **Cómo funciona** | `WHERE name LIKE '%query%'` | `filter { it.timeMinutes <= 30 }` |
| **Resultado** | Menos datos del servidor | Mismos datos, menos visibles |

**Búsqueda** (capa de datos):
```
Usuario escribe "casa" → Room SQL → "SELECT * FROM recipes WHERE name LIKE '%casa%'"
→ El ViewModel recibe 3 recetas → el Adapter muestra esas 3
```

**Filtrado** (capa de UI):
```
Usuario pulsa "Rápida" → Adapter.applyFilter(QUICK)
→ El ViewModel tiene 78 recetas → el Adapter muestra solo las 12 que son ≤30min
→ Los otros 66 siguen ahí, pero ocultos
```

**Ver:** `RecipeDao.kt:32-39` (búsqueda) + `RecipeAdapter.kt:85-90` (filtrado)

---

### P14: ¿Por qué la búsqueda va a Room y no a la API?

**Respuesta:**
Porque los datos ya están en el móvil (Room). Ventajas:
- **Velocidad**: no hay llamada de red, respuesta instantánea
- **Offline**: funciona sin conexión
- **Menos carga servidor**: no generas tráfico innecesario
- **Resultados precisos**: la búsqueda de la API sería igual (mismo SQL LIKE en el servidor)

Además, si buscara en la API:
1. Cada tecla del usuario = 1 petición HTTP (壶)
2. Sin VPN no funcionaría
3. Más latencia

**Ver:** `RecipeListViewModel.kt:67-75` → `recipeDao.search(query)`

---

## 📱 NAVEGACIÓN

### P15: ¿Por qué BottomNavigation y no DrawerLayout?

**Respuesta:**
Material Design recomienda:
- **BottomNavigation**: 3-5 destinos de igual importancia
- **DrawerNavigation**: cuando hay más jerarquía o destinos secundarios

Mi app tiene 3 tabs principales (Recetas, Favoritos, Ajustes) — caso perfecto para BottomNavigation.

Drawer sería overkill y menos accesible (necesitas gestos para abrir).

**Ver:** `MainActivity.kt:54-60` — `AppBarConfiguration(setOf(...))`

---

### P16: ¿Por qué Navigation Component en vez de fragments Transactions manuales?

**Respuesta:**
Navigation Component ofrece:
1. **Type-safe arguments** con SafeArgs — evita ClassCastException
2. **Animaciones automáticas** — slide, fade configuradas en XML
3. **Gestión de back stack** — no tienes que gestionar manually qué fragment va encima
4. **Deep links** — fácil de configurar
5. **Inspector gráfico** — ves la navegación como grafo

Manualmente:
```kotlin
// Aburrido, propenso a errores
supportFragmentManager.beginTransaction()
    .replace(R.id.container, fragment)
    .addToBackStack(null)
    .commit()
```

Con NavComponent:
```kotlin
// Limpio, declarativo
findNavController().navigate(R.id.action_list_to_detail, bundle)
```

**Ver:** `nav_graph.xml` + `RecipeListFragment.kt:159-162`

---

### P17: ¿Por qué usar WebView para el restaurante y no un intent al navegador?

**Respuesta:**
Requisito del profesor por falta de datos GPS en el dataset:
- Los datos del Principado de Asturias no tienen coordenadas
- No se puede abrir Google Maps
- El profesor pidió WebView embebido para no salir de la app

Implementación:
```kotlin
webView.webViewClient = WebViewClient() // NO abre navegador externo
// shouldOverrideUrlLoading retorna false → todo dentro del WebView
```

Ventajas:
- UX fluida (no sales de la app)
- El usuario puede volver fácil
- ProgressBar muestra carga

**Ver:** `RestaurantWebViewFragment.kt` + `readme.md:84-85`

---

## 🧪 TESTING

### P18: ¿Por qué tests unitarios de los Mappers y no de otras cosas?

**Respuesta:**
Los Mappers son funciones puras (deterministas, sin efectos secundarios), ideales para testear:
- `extractRestaurantName("Por Casa Fermín")` → `"Casa Fermín"` siempre
- `parseTimeToMinutes("1 hora 30 min")` → `90` siempre

No testeo:
- Fragments (necesitaría Espresso, más lento)
- Repository (necesitaría mockear Room + Retrofit, complejo)

Los tests de Mappers cubren parsing edge cases:
- null, vacío, solo números, horas y minutos combinados, etc.

20 tests covering 4 functions × various inputs.

**Ver:** `MappersTest.kt:1-200`

---

### P19: ¿Por qué Barista y no solo Espresso puro?

**Respuesta:**
Barista es un **wrapper de Espresso** que hace los tests más legibles:
```kotlin
// Espresso puro (verboso)
onView(withId(R.id.search)).perform(click())
onView(withId(R.id.search_src_text)).perform(typeText("fabada"))
onView(withId(R.id.search_go_btn)).perform(click())

// Barista (limpio)
writeTo(R.id.search, "fabada")
clickOn(R.id.search)
```

Ventajas Barista:
- API fluida y descriptiva
- Scroll automático antes de interactuar
- Esperas condicionales integradas
- Más fácil de mantener y leer

Barista NO reemplaza Espresso — coexisten (Barista para acciones simples, Espresso para RecyclerViewActions complejos).

**Ver:** `RecipeListFragmentTest.kt` + `build.gradle.kts` (dependencia barista)

---

### P20: ¿Cuántos tests tienes y cómo los ejecutas?

**Respuesta:**
| Tipo | Cantidad | Herramienta | Comando |
|------|----------|------------|---------|
| Unitarios (JVM) | 20 | JUnit 4 | `./gradlew test` |
| UI (dispositivo) | 10 | Barista + Espresso | `./gradlew connectedAndroidTest` |
| **Total** | **30** | | |

Todos pasan en Samsung Galaxy S21 FE (SM-G990B).

**Ver:** `MappersTest.kt` + `NavigationTest.kt` + `RecipeListFragmentTest.kt` + `RecipeDetailFragmentTest.kt`

---

## 📦 DEPENDENCIAS

### P21: ¿Por qué Glide para imágenes y no otra librería?

**Respuesta:**
Glide es el estándar para Android:
- **Placeholder** mientras carga
- **Crossfade** suave al cargar
- **Cache** en memoria y disco (no recarga cada vez)
- **Error handling** con imagen por defecto
- **Transformaciones** (resize, centerCrop)

```kotlin
Glide.with(context)
    .load(recipe.imageUrl)
    .placeholder(R.drawable.placeholder_recipe)
    .error(R.drawable.placeholder_recipe)
    .crossfade()
    .into(imageView)
```

Alternativas:
- Picasso: menos features, no tiene crossfade
- Coil: más moderno pero menos estable en 2024
- Fresco: overkill, demasiadas dependencias

**Ver:** `RecipeAdapter.kt:60-75`

---

### P22: ¿Por qué minSdk 26 y no 21?

**Respuesta:**
Necesitaba APIs que requieren Android 8.0+:
- **Splash Screen API** (Android 12+, pero mínimo para core-splashscreen)
- **NotificationChannel** (Android 8.0+)
- **Bibliotecas modernas** que no soportan API 21

Justificación en `build.gradle.kts`:
```kotlin
minSdk = 26 // Splash Screen API, NotificationChannel, librerías modernas
```

API 21 sería posible pero:
- Splash Screen API no estaría disponible (necesitaría implementación manual)
- algunas bibliotecas (Room 2.6.1) tienen mejor rendimiento en API 26+

**Ver:** `build.gradle.kts:26`

---

## 🔧 PROBLEMAS Y SOLUCIONES

### P23: ¿Qué pasó con la migración de Gson a Moshi?

**Respuesta:**
El profesor pidió Moshi explícitamente. El cambio fue:
1. `RecipeResponse.kt`: `@SerializedName` → `@Json(name=...)`
2. `RetrofitClient.kt`: `GsonConverterFactory` → `MoshiConverterFactory`
3. `build.gradle.kts`: quitar `converter-gson`, añadir `moshi-kotlin` + `converter-moshi`
4. Añadir `FlexibleAdapterFactory` para JSON inconsistente

Resultado: mismo comportamiento, pero con la librería requerida.

**Ver:** `CHANGELOG_FALLBACK_API.md` + `RecipeResponse.kt`

---

### P24: ¿Cómo manejas el modo offline?

**Respuesta:**
Estrategia por capas:
1. **NetworkUtils** — detecta si hay conexión
2. **Banner en UI** — avisa al usuario que está offline
3. **Room cache** — los datos siguen disponibles
4. **Fallback assets** — si la API falla, carga de `recetas.json`

```kotlin
// NetworkUtils.kt
fun isNetworkAvailable(context: Context): Boolean {
    val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return connectivity.activeNetworkInfo?.isConnected == true
}
```

El usuario ve la lista igual, pero con banner "Modo offline".

**Ver:** `NetworkUtils.kt` + `RecipeListFragment.kt:154-156`

---

### P25: ¿Qué pasa cuando la API devuelve datos vacíos o falla?

**Respuesta:**
Dos escenarios:

**API falla (excepción)**:
```kotlin
try {
    apiService.getRecipes()
} catch (e) {
    Log.w("API falló: ${e.message}")
    emptyList() // → luego cargar assets
}
if (recipes.isEmpty()) {
    loadFromAssets() // fallback automático
}
```

**API devuelve lista vacía** (respuesta 200 pero sin articles):
```kotlin
val articles = response.articles.article
if (articles.isEmpty()) {
    loadFromAssets()
}
```

El usuario nunca se queda sin datos.

**Ver:** `RecipeRepository.kt:144-161`

---

## 💡 DECISIONES DE DISEÑO

### P26: ¿Por qué ViewBinding y no DataBinding?

**Respuesta:**
ViewBinding genera código en tiempo de compilación para acceso type-safe a vistas:
```kotlin
// ViewBinding
binding.recyclerView // tipo correcto, null safety

// findViewById
findViewById<RecyclerView>(R.id.recyclerView) // verbose, cast manual
```

DataBinding va más allá: permite expressions en XML (`@{user.name}`), binding adapters, etc. Eso añade overhead y complejidad innecesaria para este proyecto.

**Regla simple**: usa ViewBinding, usa DataBinding solo si necesitas绑 expressions en layouts.

**Ver:** `RecipeListFragment.kt:51` — `_binding`

---

### P27: ¿Por qué un archivo JSON local como fallback?

**Respuesta:**
La API principal (`156.35.163.145`) **requiere VPN de la universidad**. Si el alumno presenta desde casa:
- No tiene VPN → la API no responde
- Sin fallback → app vazía o error

Solución: copiar el JSON de la API a `assets/recetas.json`. Así:
- La app siempre tiene datos (78 recetas)
- Puedo desarollar y testear sin VPN
- El profesor puede probar sin estar en la universidad

El Repository lo detecta automáticamente (si API devuelve vacío o falla, carga assets).

**Ver:** `RecipeRepository.kt:170-189`

---

### P28: ¿Cómo funciona el Splash Screen?

**Respuesta:**
Android 12+ requiere Splash Screen API. Mi implementación:
```kotlin
// SplashActivity.kt
override fun onCreate(savedInstanceState: Bundle?) {
    // Instalar splash antes de mostrar ventana
    val splashScreen = installSplashScreen()
    
    // El theme Defined en styles.xml (Theme.RecetasAsturianas.Splash)
    // Se aplica automaticamente con el icono de ic_launcher
    
    // Redirigir a MainActivity
    startActivity(Intent(this, MainActivity::class.java))
    finish()
}
```

El splash muestra el icono de la app centrado, luego transiciona suavemente a MainActivity.

**Ver:** `SplashActivity.kt` + `styles.xml` + `build.gradle.kts` (dependencia core-splashscreen)

---

## 🤔 PREGUNTAS CAPCIOSAS

### P29: ¿Quépassa si el usuario marca favorito y luego se borra la caché?

**Respuesta:**
Las dos tablas son independientes:
- `recipes` → puede borrarse y recalcarse
- `favorites` → NO se borra jamás (a menos que el usuario quite favorito)

Cuando el usuario vuelve a abrir la app:
1. Repository detecta caché vieja → recalca recipes
2. favorites se mantienen intactas
3. El JOIN vuelve a unir favorites + recipes

```kotlin
favoriteDao.getAllFavorites() // SELECT * FROM favorites
// Luego se une con recipes para mostrar los datos completos
```

**Ver:** `FavoriteDao.kt` + `RecipeRepository.kt:193`

---

### P30: ¿Quépassa con el parsing del tiempo? ¿Y si alguien escribe "3 horas y media"?

**Respuesta:**
Los regex solo capturan números enteros:
```kotlin
val hours = Regex("""(\d+)\s*hora""").find(raw)?.groupValues?.get(1)?.toIntOrNull()
val minutes = Regex("""(\d+)\s*minuto""").find(raw)?.groupValues?.get(1)?.toIntOrNull()
```

"3 horas y media" → `hours = 3`, `minutes = null` → "3 horas" → 180 minutos

Perder la fracción "media hora" no es crítico — es información aproximada para clasificar (rápida/media/larga).

**Ver:** `Mappers.kt:14-26` + `MappersTest.kt:47-80`

---

### P31: Si tuvieras que añadir persistencia de la búsqueda del usuario, ¿cómo lo harías?

**Respuesta:**
Opciones:
1. **SharedPreferences** (quick & dirty): guardar string "ultima_busqueda"
2. **ViewModel con savedStateHandle** (más elegante, sobrevive a muerte del proceso): 
   ```kotlin
   class RecipeListViewModel(private val handle: SavedStateHandle) : ViewModel() {
       val lastQuery: String? = handle.get("last_query")
   }
   ```

Para este caso sencillo usaría SharedPreferences (como ya hago con los filtros de tiempo en `last_filter`).

**Ver:** `RecipeListViewModel.kt:35-40`

---

### P32: ¿Qué mejorarías si tuvieras más tiempo?

**Respuesta:**
1. **Paginación**: ahora cargo 78 recetas de golpe. Con Room + Paging 3 sería más eficiente.
2. **Cache HTTP con OkHttp**: no solo cache de imágenes, también respuestas JSON.
3. **Search debouncing**: ahora cada tecla dispara búsqueda, debería esperar 300ms.
4. **Dark mode más elaborado**: no solo colores, también icons y layouts.
5. **Tests de Repository**: con mockk para testear lógica de fallback.
6. **Screenshot tests**: con Paparazzi para validar UI sin dispositivo.

---

## 📋 RESUMEN RÁPIDO DE ARGUMENTOS

| Tema | Argumento clave |
|------|----------------|
| MVVM | Separación, testabilidad, lifecycle-safe |
| Room | SQL typed, LiveData, offline |
| Moshi | Requisito profesor + null safety |
| Fallback assets | Siempre hay datos (sin VPN) |
| Busqueda vs filtrado | Diferencia clave del enunciado |
| BottomNav | 3 tabs = caso típico BottomNav |
| WebView embebido | Requisito profesor (sin GPS) |
| Testing | 20 unit + 10 UI = 30 tests |
| minSdk 26 | Splash Screen API + librerías modernas |

---

*Última actualización: 19 de mayo de 2026*