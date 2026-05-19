# Prompt de Referencia: App Android "Recetas de Cocina Asturiana"

## Contexto de la asignatura

Aplicación Android individual para la asignatura **Informática Móvil (Grado en Ingeniería Informática en Tecnologías de la Información)**. La app es la denominada "aplicación estándar": descarga datos de una API REST pública y los presenta de la mejor manera posible siguiendo el paradigma maestro/detalle.

---

## Fuente de datos

- **URL de producción:** `http://156.35.163.145/json/RecetasCocina.json` (servidor interno de la universidad, requiere VPN)
- **URL alternativa para desarrollo:** `https://www.turismoasturiasprofesional.es/open-data/turismoasturias` (Turismo Asturias Profesional, mismos datos)
- **Formato:** JSON
- **Total de recetas:** 78

### Estructura del JSON

```json
{
  "articles": {
    "article": [
      {
        "Nombre":      { "content": "Nombre de la receta" },
        "Resumen":     { "content": "Por Nombre del Restaurante" },
        "Imagen":      { "content": "{\"classPK\":94845,\"groupId\":\"39908\",\"title\":\"foto.jpg\",\"uuid\":\"...\"}" },
        "Visualizador": {
          "Slide": { "value": "{...mismo JSON que Imagen...}" }
        },
        "Informacion": {
          "Preparacion": { "content": "<p>Pasos en HTML...</p>" },
          "Donde":       { "content": "<p><a href=\"/ruta/restaurante\">Nombre</a></p>" },
          "JornadasGastronomicas": { "content": "..." }
        },
        "Contacto": {
          "Tiempo":       { "content": "20 minutos" },
          "Ingredientes": { "content": "<ul><li>...</li></ul>" }
        },
        "Observaciones": {
          "TrucosYConsejos": { "content": "..." },
          "Observacion":     { "content": "..." }
        }
      }
    ]
  }
}
```

### Disponibilidad de campos

| Campo                        | Disponibilidad | Notas                                      |
|------------------------------|----------------|--------------------------------------------|
| Nombre                       | 78/78 ✅        | Siempre presente                           |
| Resumen (restaurante autor)  | 78/78 ✅        | Formato "Por Nombre Restaurante"           |
| Informacion.Preparacion      | 78/78 ✅        | HTML con `<p>` tags                        |
| Contacto.Ingredientes        | 78/78 ✅        | HTML con `<ul><li>` tags                   |
| Imagen                       | 76/78 ✅        | JSON string con uuid e info de imagen      |
| Informacion.Donde            | 74/78 ✅        | HTML con enlace relativo al restaurante    |
| Contacto.Tiempo              | 55/78 ⚠️       | Texto libre ("20 minutos", "3 horas", etc.)|
| Observaciones.TrucosYConsejos| 17/78 ⚠️       | Solo mostrar si existe                     |
| Observaciones.Observacion    | 9/78  ⚠️       | Solo mostrar si existe                     |
| Informacion.JornadasGastronomicas | 1/78 ⚠️   | Prácticamente vacío, ignorar               |

### Construcción de la URL de imagen

La imagen se construye a partir del campo `Imagen` (JSON string). Hay que parsearlo y usar el `uuid` y `groupId`:

```
https://www.turismoasturias.es/documents/{groupId}/{uuid}/{title}
```

Ejemplo:
```
https://www.turismoasturias.es/documents/39908/a744d57d-73a3-09f5-bb64-837d690337a4/Yumay_Arroz_1.jpg
```

### Construcción de la URL del restaurante (campo Donde)

El campo `Donde` contiene HTML con una URL relativa. Hay que extraer el `href` y añadir el dominio base:

```
https://www.turismoasturias.es{href}
```

**IMPORTANTE (requisito del profesor):** El enlace al restaurante debe abrirse **embebido dentro de la app** usando un `WebView` en un Fragment, NO con un Intent al navegador externo. Esto es un requisito explícito añadido al aceptar el tema.

---

## Requisitos de la asignatura

### Requisitos de diseño obligatorios (en rojo en el enunciado)

1. Arquitectura **MVVM**
2. Código legible y comentado
3. Nomenclatura Kotlin (camelCase métodos/variables, PascalCase clases)
4. División clara en paquetes por finalidad (data, ui, domain...)
5. Acceso a datos con **Room**
6. **Pruebas de interfaz de usuario** (UI tests con Espresso)

### Requisitos funcionales obligatorios

1. **Desarrollo personal y original**
2. **Adaptación:** mínimo 2 idiomas (español + inglés), 2 dispositivos (teléfono + tablet Nexus 7), API 21+
3. **Esquema de navegación** apropiado y justificado (BottomNavigationView o Drawer)
4. **Navigation Component + Fragments** para toda la interfaz
5. **Robustez** ante interrupciones (rotación, llamadas entrantes) — ViewModel resuelve esto

### Diferencia entre Búsqueda y Filtrado (concepto clave)

- **Búsqueda** → se implementa en la **capa de datos** (repositorio + Room SQL). Cambia el conjunto de datos subyacente.
- **Filtrado** → se implementa en la **capa UI** (Adapter/ViewHolder). Muestra u oculta elementos del conjunto actual SIN modificarlo.

---

## Arquitectura MVVM detallada

```
┌─────────────────────────────────────────────────────┐
│                      UI Layer                        │
│  Fragments ──observan──► ViewModels                  │
│  (ListFragment, DetailFragment, FavFragment, etc.)   │
└──────────────────────┬──────────────────────────────┘
                       │ llaman a
┌──────────────────────▼──────────────────────────────┐
│                  Repository                          │
│  Fuente única de verdad. Decide si ir a Room o Red.  │
│  Lógica de timestamp para refrescar caché.           │
└─────────────┬───────────────────────┬───────────────┘
              │                       │
┌─────────────▼──────┐   ┌───────────▼────────────────┐
│   Room (BD local)   │   │   Retrofit (red)            │
│  RecipeDatabase     │   │   RecipeApiService          │
│  FavoritesDatabase  │   │   GET /json/RecetasCocina   │
└─────────────────────┘   └────────────────────────────┘
```

---

## Estructura de paquetes

```
es.uniovi.recetasasturianas/
├── data/
│   ├── local/
│   │   ├── RecipeDatabase.kt          ← Room BD 1 (caché de recetas)
│   │   ├── FavoritesDatabase.kt       ← Room BD 2 (favoritos del usuario)
│   │   ├── RecipeDao.kt               ← Consultas SQL para recetas
│   │   └── FavoriteDao.kt             ← Consultas SQL para favoritos
│   ├── remote/
│   │   ├── RecipeApiService.kt        ← Interface Retrofit
│   │   ├── RetrofitClient.kt          ← Singleton Retrofit + OkHttp
│   │   └── dto/
│   │       └── RecipeResponse.kt      ← Data classes para parsear el JSON crudo
│   ├── model/
│   │   ├── Recipe.kt                  ← @Entity Room (modelo limpio)
│   │   └── Favorite.kt               ← @Entity Room (id + timestamp guardado)
│   └── repository/
│       └── RecipeRepository.kt        ← Lógica de caché + búsqueda
├── ui/
│   ├── list/
│   │   ├── RecipeListFragment.kt      ← Pantalla principal (maestro)
│   │   ├── RecipeListViewModel.kt
│   │   └── RecipeAdapter.kt           ← RecyclerView adapter con filtrado
│   ├── detail/
│   │   ├── RecipeDetailFragment.kt    ← Pantalla detalle (detalle)
│   │   └── RecipeDetailViewModel.kt
│   ├── webview/
│   │   ├── RestaurantWebViewFragment.kt ← WebView embebido para el restaurante
│   │   └── RestaurantWebViewViewModel.kt
│   ├── favorites/
│   │   ├── FavoritesFragment.kt
│   │   └── FavoritesViewModel.kt
│   └── settings/
│       └── SettingsFragment.kt        ← PreferenceFragmentCompat
├── MainActivity.kt                    ← NavHost + BottomNavigation
└── App.kt                             ← Application class (init Room, etc.)
```

---

## Modelo de datos (Room Entity)

```kotlin
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: Int,
    val name: String,
    val restaurant: String,       // "Por Nombre Restaurante" → extraer nombre
    val preparationHtml: String,  // HTML crudo de Preparacion
    val ingredientsHtml: String,  // HTML crudo de Ingredientes
    val imageUrl: String?,        // URL construida a partir del JSON de Imagen
    val restaurantUrl: String?,   // URL completa del restaurante (de Donde)
    val timeMinutes: Int?,        // Parsear "20 minutos" → 20, null si no existe
    val tipsHtml: String?,        // TrucosYConsejos, null si no existe
    val cachedAt: Long            // timestamp epoch para caché
)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val recipeId: Int,
    val savedAt: Long = System.currentTimeMillis()
)
```

---

## Lógica de caché y refresco

El repositorio decide cuándo ir a la red usando el campo `cachedAt`:

```kotlin
// En RecipeRepository
suspend fun getRecipes(): List<Recipe> {
    val cached = recipeDao.getAll()
    val cacheAgeHours = (System.currentTimeMillis() - (cached.firstOrNull()?.cachedAt ?: 0)) / 3600000
    val refreshInterval = prefs.getInt("refresh_hours", 24) // configurable en Settings

    return if (cached.isEmpty() || cacheAgeHours > refreshInterval) {
        val remote = apiService.getRecipes()
        val mapped = remote.map { it.toEntity() }
        recipeDao.deleteAll()
        recipeDao.insertAll(mapped)
        mapped
    } else {
        cached
    }
}
```

---

## Búsqueda (capa de datos — Room DAO)

```kotlin
@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAll(): List<Recipe>

    // BÚSQUEDA por nombre o restaurante — modifica el dataset
    @Query("""
        SELECT * FROM recipes 
        WHERE name LIKE '%' || :query || '%' 
           OR restaurant LIKE '%' || :query || '%'
    """)
    fun search(query: String): LiveData<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<Recipe>)

    @Query("DELETE FROM recipes")
    suspend fun deleteAll()
}
```

---

## Filtrado (capa UI — Adapter)

```kotlin
// En RecipeAdapter — FILTRADO por tiempo (no modifica el dataset)
fun applyFilter(maxMinutes: Int?) {
    filtered = if (maxMinutes == null) {
        fullList
    } else {
        fullList.filter { it.timeMinutes != null && it.timeMinutes <= maxMinutes }
    }
    notifyDataSetChanged()
}
```

Opciones de filtro de tiempo:
- Sin filtro (todos)
- Rápida (≤ 30 min)
- Media (31–60 min)
- Larga (> 60 min)

---

## Parseo del tiempo

El campo `Tiempo` es texto libre ("20 minutos", "3 horas", "1 hora 30 minutos", etc.). Hay que normalizarlo a minutos enteros:

```kotlin
fun parseTimeToMinutes(raw: String?): Int? {
    if (raw.isNullOrBlank()) return null
    var total = 0
    val hours = Regex("""(\d+)\s*h""").find(raw)?.groupValues?.get(1)?.toIntOrNull()
    val minutes = Regex("""(\d+)\s*min""").find(raw)?.groupValues?.get(1)?.toIntOrNull()
    if (hours != null) total += hours * 60
    if (minutes != null) total += minutes
    return if (total == 0) null else total
}
```

---

## Parseo del HTML

Los campos `Preparacion`, `Ingredientes`, `Donde` contienen HTML. Para mostrarlos:

```kotlin
// En el Fragment/ViewHolder
textView.text = HtmlCompat.fromHtml(recipe.ingredientsHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
```

Para extraer la URL del restaurante del campo `Donde`:
```kotlin
fun extractRestaurantUrl(whereHtml: String?): String? {
    if (whereHtml.isNullOrBlank()) return null
    val match = Regex("""href="([^"]+)"""").find(whereHtml) ?: return null
    val href = match.groupValues[1]
    return if (href.startsWith("http")) href
    else "https://www.turismoasturias.es$href"
}
```

---

## WebView del restaurante (requisito del profesor)

El enlace al restaurante debe abrirse **dentro de la app**, no en el navegador externo.

```kotlin
// RestaurantWebViewFragment.kt
class RestaurantWebViewFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val url = arguments?.getString("url") ?: return
        binding.webView.apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient() // evita abrir el navegador externo
            loadUrl(url)
        }
    }
}
```

Navegar a él desde el detalle:
```kotlin
val action = RecipeDetailFragmentDirections
    .actionDetailToWebView(url = recipe.restaurantUrl)
findNavController().navigate(action)
```

---

## Navegación (Navigation Component)

```
MainActivity (NavHostFragment)
    │
    ├── RecipeListFragment (inicio)
    │       │
    │       └──(click item)──► RecipeDetailFragment
    │                               │
    │                               └──(click restaurante)──► RestaurantWebViewFragment
    │
    ├── FavoritesFragment
    │       │
    │       └──(click item)──► RecipeDetailFragment
    │
    └── SettingsFragment
```

`BottomNavigationView` con 3 tabs: Recetas / Favoritos / Ajustes.

---

## Preferencias del usuario (Settings)

Implementar con `PreferenceFragmentCompat` y `SharedPreferences`:

| Preferencia              | Tipo    | Default | Descripción                              |
|--------------------------|---------|---------|------------------------------------------|
| `refresh_hours`          | ListPref| 24      | Cada cuántas horas refrescar el JSON     |
| `default_sort`           | ListPref| nombre  | Orden por defecto (nombre / restaurante) |
| `show_only_with_time`    | Boolean | false   | Ocultar recetas sin tiempo de preparación|

---

## Adaptación tablet/teléfono

Para tablet (sw600dp) implementar layout de maestro/detalle en pantalla dividida:

```
res/
├── layout/                     ← teléfono: un solo fragment
│   └── activity_main.xml
└── layout-sw600dp/             ← tablet: dos paneles
    └── activity_main.xml
```

---

## Dependencias Gradle (build.gradle.kts)

```kotlin
// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Retrofit + Gson
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Glide (imágenes)
implementation("com.github.bumptech.glide:glide:4.16.0")

// Navigation Component
implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

// ViewModel + LiveData
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

// Preferences
implementation("androidx.preference:preference-ktx:1.2.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// UI Tests (Espresso)
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
```

---

## Notas importantes

1. **HTTP en Android:** el servidor usa HTTP (no HTTPS). Añadir en `AndroidManifest.xml`:
   ```xml
   <application android:usesCleartextTraffic="true" ...>
   ```
   O usar un `network_security_config.xml` que permita solo ese dominio.

2. **Imágenes con campos vacíos:** 2 recetas no tienen imagen. El adapter debe manejar `imageUrl == null` con un placeholder.

3. **El restaurant "Por X":** el campo `Resumen` tiene siempre el formato "Por Nombre". Para mostrar solo el nombre: `resumen.removePrefix("Por ").trim()`.

4. **HTML con estilos inline:** el HTML del servidor incluye estilos inline con fuentes específicas. `HtmlCompat.fromHtml` los ignorará automáticamente, lo cual es el comportamiento deseado.

5. **El campo `Donde` a veces es relativo y a veces absoluto:** verificar siempre si el href empieza por `http` antes de añadir el dominio base.
