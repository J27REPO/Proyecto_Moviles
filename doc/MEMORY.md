# Proyecto Recetas Asturianas — Memoria Tecnica

## Resumen

Aplicacion Android para explorar, buscar y guardar recetas tipicas de la gastronomia asturiana. Los datos se obtienen del catalogo de datos abiertos del Principado de Asturias (conjunto "Recetas de cocina"), con sistema de fallback local y cache en Room. Arquitectura MVVM con Repository Pattern.

---

## Arquitectura: MVVM + Repository

### Capas

1. **UI Layer** — Fragments + ViewModels + LiveData
   - Los Fragments observan `LiveData` expuestos por los ViewModels
   - Los ViewModels sobreviven a cambios de configuracion (rotacion)
   - ViewBinding para acceso type-safe a vistas

2. **Domain Layer** — RecipeRepository (singleton)
   - Fuente unica de datos (Single Source of Truth)
   - Decide si ir a red, cache o assets
   - Control de intervalo de refresco via SharedPreferences

3. **Data Layer** — Room (local) + Retrofit (remoto) + Assets (fallback)
   - Room 2.6.1 con 2 tablas: `recipes` y `favorites`
   - Retrofit 2.9.0 + Moshi 1.15.1 para API REST
   - Moshi + FlexibleAdapterFactory para JSON inconsistente
   - OkHttp 4.12.0 con logging interceptor y timeouts 30s

### Flujo de datos tipico

```
Fragment -> ViewModel.loadRecipes()
  -> Repository.refreshRecipesIfNeeded()
    -> [API disponible] Retrofit.getRecipes() -> Moshi parse -> Room save
    -> [API falla]     assets/recetas.json -> Moshi parse -> Room save
  -> Repository.getAllRecipes() -> LiveData<List<Recipe>>
  -> Fragment observa y renderiza RecyclerView
```

### Diferenciacion Busqueda vs Filtrado (concepto clave del enunciado)

- **Busqueda** (capa de datos): `RecipeDao.search(query)` con SQL `LIKE` sobre `name`, `restaurant`, `ingredientsHtml`. Cambia el dataset subyacente que recibe el ViewModel.
- **Filtrado** (capa UI): `RecipeAdapter.applyFilter()` + `TimeFilter` enum (ALL/QUICK/MEDIUM/LONG). Muestra u oculta elementos del dataset actual SIN modificarlo. Opera exclusivamente en el Adapter.

---

## Librerias y justificacion

| Libreria | Version | Proposito | Por que esta (no otra) |
|----------|---------|-----------|------------------------|
| Kotlin | 1.9.22 | Lenguaje | Oficial Android, null safety, corrutinas |
| Room | 2.6.1 | BD local | Cache offline, consultas SQL, LiveData reactivo |
| Retrofit | 2.9.0 | Cliente HTTP | Estandar Android, type-safe, integra Moshi |
| Moshi | 1.15.1 | JSON parsing | Requisito profesor, mejor que Gson para Kotlin |
| OkHttp | 4.12.0 | HTTP client | Logging, timeouts, manejo errores red |
| Glide | 4.16.0 | Imagenes | Placeholder, crossfade, cache disco/memoria |
| Nav. Comp. | 2.7.7 | Navegacion | SafeArgs, animaciones, BottomNavigation integrado |
| Material 3 | 1.11.0 | Diseno | Material You, Chips, FAB, BottomNavigation |
| Barista | 4.3.0 | UI tests | API legible sobre Espresso, scroll automatico |
| Espresso | 3.5.1 | UI tests | Framework base de testing UI Android |

---

## Modelo de Datos

### Recipe (Entity Room + Parcelable)
```kotlin
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: Int,
    val name: String,
    val restaurant: String,
    val preparationHtml: String,
    val ingredientsHtml: String,
    val imageUrl: String?,
    val restaurantUrl: String?,
    val timeMinutes: Int?,
    val tipsHtml: String?,
    val notesHtml: String?,
    val cachedAt: Long
)
```

### Favorite (Entity Room)
```kotlin
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val recipeId: Int,
    val savedAt: Long = System.currentTimeMillis()
)
```

---

## Parseos destacados

### Tiempo de preparacion
Campo texto libre: "20 minutos", "3 horas", "1 hora 30 minutos", "45"
```kotlin
val hours = Regex("""(\d+)\s*hora?s?""").find(raw)
val minutes = Regex("""(\d+)\s*minuto?s?""").find(raw)
// Si no hay match, intenta extraer el primer numero
```

### URL de imagen
JSON anidado en campo `Imagen.content`:
```json
{"groupId":"39908","title":"foto.jpg","uuid":"a744d57d-..."}
```
Resultado: `https://www.turismoasturias.es/documents/39908/0/foto.jpg/uuid?version=1.0`

### URL de restaurante
HTML en campo `Informacion.Donde.content`:
```html
<a href="/ruta/restaurante/casa-fermin">Casa Fermin</a>
```
Resultado: `https://www.turismoasturias.es/ruta/restaurante/casa-fermin`

---

## Tests

### Unit Tests (MappersTest.kt) — 20 tests, todos pasan
- `extractRestaurantName()`: Con/sin prefijo "Por", null, vacio
- `parseTimeToMinutes()`: Solo minutos, solo horas, combinados, null, vacio, numero solo
- `extractRestaurantUrl()`: URL relativa, absoluta, null, sin href
- `extractImageUrl()`: JSON valido, anidado (image field), espacios en title, invalido, null, con slide

### UI Tests (Barista + Espresso) — 10 tests, todos pasan en SM-G990B
- `NavigationTest.kt` (2): navegacion BottomNavigation, titulo toolbar
- `RecipeListFragmentTest.kt` (5): carga lista, click receta, busqueda, filtro tiempo, pull-to-refresh
- `RecipeDetailFragmentTest.kt` (3): datos detalle, toggle favorito, boton restaurante

---

## Decisiones de diseno

1. **BottomNavigation** (no Drawer): 3-5 destinos, patron recomendado Material Design
2. **Room 2 tablas separadas**: recipes + favorites con FK explicita
3. **Moshi FlexibleAdapterFactory**: maneja JSON inconsistente (objeto vs array)
4. **WebViewClient personalizado**: requisito profesor por falta de datos GPS
5. **Handler.post() en Settings**: evita crash por recreacion de Activity dentro de callback
6. **ViewBinding** (no DataBinding): mas ligero, solo binding type-safe

---

## Comandos

```bash
./gradlew assembleDebug              # Compilar APK
./gradlew test                        # Tests unitarios (20)
./gradlew connectedAndroidTest        # Tests UI (10, requiere dispositivo)
./gradlew test connectedAndroidTest   # Todos los tests
adb install -r app/build/outputs/apk/debug/app-debug.apk  # Instalar
```

---

*Ultima actualizacion: 15 de mayo de 2026*
