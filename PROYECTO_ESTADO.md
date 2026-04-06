# Proyecto Recetas Asturianas - Estado Completo

## Información General

- **Nombre**: Recetas Asturianas
- **Package**: `es.uniovi.recetasasturianas`
- **Asignatura**: Informática Móvil (Universidad de Oviedo)
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **SDK mínimo**: 21 (Android 5.0 Lollipop)
- **SDK objetivo**: 35 (Android 15)
- **Lenguaje**: Kotlin

---

## Requisitos del Profesor

| Requisito | Estado | Implementación |
|-----------|--------|----------------|
| Mostrar información resumen | ✅ | RecyclerView con Cards (imagen, nombre, restaurante, tiempo) |
| Mostrar información detalle | ✅ | Fragment con ingredientes, preparación, trucos, notas |
| Integrar imágenes | ✅ | Glide + URLs construidas dinámicamente |
| WebView embebido (no navegador externo) | ✅ | RestaurantWebViewFragment con WebViewClient personalizado |
| Búsquedas (receta, autor, ingredientes) | ✅ | Room SQL LIKE en nombre, restaurante e ingredientes |
| Filtrado por característica | ✅ | Chips para filtrar por tiempo (rápido/medio/largo) |
| Guardar favoritos | ✅ | Room Database + FAB toggle |
| Recordar preferencias | ✅ | SharedPreferences + PreferenceFragmentCompat |

---

## Estructura del Proyecto

```
app/src/main/java/es/uniovi/recetasasturianas/
├── App.kt                          # Application class
├── SplashActivity.kt               # Splash Screen (Android 12+)
├── MainActivity.kt                 # Activity principal con Navigation
│
├── data/
│   ├── local/
│   │   ├── RecipeDao.kt           # DAO para recetas (búsquedas SQL)
│   │   ├── RecipeDatabase.kt      # Room DB unificada (recetas + favoritos)
│   │   └── FavoriteDao.kt         # DAO para favoritos
│   │
│   ├── remote/
│   │   ├── RecipeApiService.kt    # Retrofit interface
│   │   ├── RetrofitClient.kt      # Configuración Retrofit + OkHttp
│   │   └── dto/
│   │       ├── RecipeResponse.kt  # DTOs para parsear JSON
│   │       └── Mappers.kt         # Conversión DTO → Entity
│   │
│   ├── model/
│   │   ├── Recipe.kt              # Entity Room para recetas
│   │   └── Favorite.kt            # Entity Room para favoritos
│   │
│   └── repository/
│       └── RecipeRepository.kt    # Fuente única de datos (API → Room)
│
├── ui/
│   ├── list/
│   │   ├── RecipeListFragment.kt  # Lista de recetas + búsqueda
│   │   ├── RecipeListViewModel.kt # ViewModel con LiveData
│   │   └── RecipeAdapter.kt       # RecyclerView Adapter + filtros
│   │
│   ├── detail/
│   │   ├── RecipeDetailFragment.kt # Detalle + compartir
│   │   └── RecipeDetailViewModel.kt
│   │
│   ├── favorites/
│   │   ├── FavoritesFragment.kt   # Lista de favoritos
│   │   └── FavoritesViewModel.kt
│   │
│   ├── settings/
│   │   └── SettingsFragment.kt    # Preferencias (PreferenceFragment)
│   │
│   └── webview/
│       └── RestaurantWebViewFragment.kt # WebView embebido
│
└── util/
    └── NetworkUtils.kt            # Detección de conectividad
```

---

## Tecnologías y Librerías

### Core
- **Kotlin** 1.9.22
- **Android SDK** 21-35
- **ViewBinding** para acceso a vistas

### Arquitectura
- **MVVM** con ViewModel + LiveData
- **Repository Pattern** para abstraer fuentes de datos
- **Room** 2.6.1 (base de datos local)
- **Retrofit** 2.9.0 + Gson (API REST)
- **OkHttp** 4.12.0 (logging interceptor)

### UI
- **Material Design 3**
- **Navigation Component** 2.7.7
- **Glide** 4.16.0 (carga de imágenes)
- **SwipeRefreshLayout** 1.1.0
- **Splash Screen API** 1.0.1

### Testing
- **Espresso** 3.5.1 (UI tests)
- **JUnit** 4.13.2 (unit tests)

---

## Funcionalidades Implementadas

### 1. Lista de Recetas (Maestro)
- RecyclerView con Cards
- Imagen, nombre, restaurante, tiempo
- Pull-to-refresh
- Chips de filtrado por tiempo:
  - Rápido: ≤30 min
  - Medio: 31-60 min
  - Largo: >60 min
- Búsqueda en tiempo real (nombre/restaurante/ingredientes)
- Banner de modo offline

### 2. Detalle de Receta
- Imagen grande
- Nombre y restaurante
- Tiempo de preparación
- Ingredientes (HTML renderizado)
- Preparación (HTML renderizado)
- Trucos y notas (si existen)
- FAB para añadir/quitar favoritos
- Botón para ver restaurante (WebView)
- Menú para compartir receta

### 3. Favoritos
- Lista de recetas marcadas como favoritas
- Persistencia en Room
- Mismo adaptador que lista principal
- Filtrado por preferencia `hide_no_time`

### 4. Preferencias
- **Intervalo de refresco**: 1h, 6h, 12h, 24h, 48h, 1 semana
- **Ordenación por defecto**: nombre, restaurante, tiempo
- **Ocultar sin tiempo**: filtra recetas sin tiempo de preparación

### 5. WebView Embebido
- Carga sitio web del restaurante
- No abre navegador externo (requisito profesor)
- ProgressBar de carga
- Manejo de errores

### 6. Soporte Tablet (sw600dp)
- Layout maestro-detalle
- Panel izquierdo: lista
- Panel derecho: detalle
- Selección visual en lista

### 7. Splash Screen
- Compatible Android 12+
- Muestra icono de la app
- Transición suave a MainActivity

### 8. Modo Offline
- Detección de conectividad
- Caché en Room
- Fallback a `assets/recetas.json`
- Banner informativo

---

## API y Datos

### Fuente de Datos
- **Principal**: `https://www.turismoasturiasprofesional.es/open-data/turismoasturias`
- **Alternativa (VPN)**: `http://156.35.163.145/json/RecetasCocina.json`
- **Fallback**: `assets/recetas.json` (276 KB)

### Estructura JSON
```json
{
  "articles": {
    "article": [
      {
        "title": "Nombre de la receta",
        "restaurant": "Por Casa Fermín",
        "time": "45 minutos",
        "ingredientsHtml": "<p>...</p>",
        "preparationHtml": "<p>...</p>",
        "tipsHtml": "<p>...</p>",
        "notesHtml": "<p>...</p>",
        "image": "{...json...}",
        "restaurantLink": "<a href=\"...\">...</a>"
      }
    ]
  }
}
```

### Parseos Especiales
- **Restaurante**: Quita prefijo "Por "
- **Tiempo**: Parsea "X horas Y minutos" → minutos
- **URL imagen**: Construye URL desde JSON
- **URL restaurante**: Extrae href de HTML

---

## Base de Datos Room

### Tabla `recipes`
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Int | ID único (autogenerado) |
| name | String | Nombre de la receta |
| restaurant | String | Nombre del restaurante |
| timeMinutes | Int? | Tiempo en minutos (nullable) |
| ingredientsHtml | String | Ingredientes en HTML |
| preparationHtml | String | Preparación en HTML |
| tipsHtml | String? | Trucos en HTML |
| notesHtml | String? | Notas en HTML |
| imageUrl | String? | URL de la imagen |
| restaurantUrl | String? | URL del restaurante |
| cachedAt | Long | Timestamp de caché |

### Tabla `favorites`
| Campo | Tipo | Descripción |
|-------|------|-------------|
| recipeId | Int | FK a recipes.id |
| savedAt | Long | Timestamp |

---

## Archivos de Recursos

### Layouts
- `activity_main.xml` - Layout principal (teléfono)
- `layout-sw600dp/activity_main.xml` - Layout tablet
- `fragment_recipe_list.xml` - Lista con chips y banner offline
- `fragment_recipe_detail.xml` - Detalle con FAB
- `fragment_favorites.xml` - Favoritos con empty state
- `fragment_web_view.xml` - WebView con toolbar
- `item_recipe.xml` - Card de receta

### Animaciones
- `slide_in_right.xml`, `slide_out_left.xml`
- `slide_in_left.xml`, `slide_out_right.xml`
- `fade_in.xml`, `fade_out.xml`

### Drawables
- `ic_favorite_filled.xml`, `ic_favorite_outline.xml`
- `ic_share.xml`, `ic_offline.xml`
- `ic_empty_favorites.xml`, `ic_empty_recipes.xml`
- `ic_launcher_foreground.xml`
- `placeholder_recipe.xml`

### Menús
- `bottom_nav_menu.xml` - BottomNavigationView
- `menu_search.xml` - SearchView
- `menu_detail.xml` - Compartir

### Valores
- `strings.xml` - Español
- `strings.xml` (values-en) - Inglés
- `colors.xml` - Paleta de colores
- `themes.xml` - Material 3 + Splash
- `arrays.xml` - Opciones de preferencias

---

## Tests

### Unit Tests (`MappersTest.kt`)
- `extractRestaurantName()` - Con/sin prefijo "Por"
- `parseTimeToMinutes()` - Horas, minutos, combinados
- `extractRestaurantUrl()` - URLs relativas/absolutas
- `extractImageUrl()` - Parseo de JSON

### UI Tests (Espresso)
- `RecipeListFragmentTest.kt` - Carga, búsqueda, filtrado
- `RecipeDetailFragmentTest.kt` - Navegación, favoritos
- `NavigationTest.kt` - Navegación entre tabs

### Utilidades de Test
- `DataLoadingIdlingResource.kt` - Sincronización para Espresso

---

## Comandos de Compilación

```bash
# Compilar APK debug
./gradlew assembleDebug

# Instalar en dispositivo
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests UI (requiere dispositivo/emulador)
./gradlew connectedAndroidTest

# Ver logcat para errores
adb logcat -d | grep -E "RecetasAsturianas|FATAL"
```

---

## Problemas Conocidos

### 1. VPN Universidad
- La API requiere VPN (GlobalProtect con portalgp.uniovi.es)
- **Solución**: Fallback a `assets/recetas.json`

### 2. Android 16 (API 35)
- Splash Screen API obligatorio
- **Solución**: Implementado con `androidx.core:core-splashscreen`

### 3. ActionBar
- El tema es `NoActionBar` pero Navigation lo requiere
- **Solución**: Toolbar en layout + `setSupportActionBar()`

---

## Próximos Pasos (Opcionales)

1. [ ] Implementar fallback a assets en RecipeRepository
2. [ ] Añadir widget de receta del día
3. [ ] Notificaciones para recordar recetas
4. [ ] Modo oscuro automático
5. [ ] Shared Element Transitions
6. [ ] Tests de integración

---

## Autor

- **J27REPO (Jose)**
- **Email**: josesf2004@gmail.com
- **Universidad**: Universidad de Oviedo
- **Asignatura**: Informática Móvil

---

*Última actualización: 20 de marzo de 2026*
