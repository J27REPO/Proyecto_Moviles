# Proyecto Recetas Asturianas - Estado Final

## Resumen de Implementación

La aplicación Android está **completa y mejorada** según los requisitos de la asignatura Informática Móvil.

---

## ✅ Requisitos Implementados

| Requisito | Estado | Detalle |
|-----------|--------|---------|
| Arquitectura MVVM | ✅ | ViewModels + LiveData + Repository |
| Room (2 BDs) | ✅ | RecipeDatabase + FavoritesDatabase |
| Retrofit + API | ✅ | Turismo Asturias Profesional |
| Navigation Component | ✅ | nav_graph.xml con animaciones |
| Búsqueda | ✅ | Por nombre, restaurante E ingredientes |
| Filtrado | ✅ | Por tiempo (rápido/medio/largo) |
| Favoritos | ✅ | Persistencia en Room + FAB toggle |
| Preferencias | ✅ | SettingsFragment con PreferenceFragmentCompat |
| WebView embebido | ✅ | RestaurantWebViewFragment |
| Bilingüe | ✅ | Español + Inglés |
| Tablet (sw600dp) | ✅ | Layout maestro-detalle |
| Espresso Tests | ✅ | Tests UI mejorados |
| Unit Tests | ✅ | MappersTest.kt |

---

## 🆕 Mejoras Adicionales

| Mejora | Descripción |
|--------|-------------|
| **Ordenación por preferencia** | name/restaurant/time |
| **Compartir receta** | Texto completo o solo enlace |
| **Animaciones** | Transiciones slide entre fragments |
| **Splash Screen** | Pantalla de bienvenida |
| **Indicador offline** | Banner cuando no hay conexión |
| **Búsqueda por ingredientes** | SQL LIKE en ingredientsHtml |
| **Empty states mejorados** | Iconos y mensajes guía |
| **Detección de red** | NetworkUtils.kt |

---

## 📁 Estructura de Archivos

```
app/src/main/java/es/uniovi/recetasasturianas/
├── data/
│   ├── local/
│   │   ├── RecipeDao.kt          ← Búsqueda por ingredientes
│   │   ├── RecipeDatabase.kt
│   │   ├── FavoriteDao.kt
│   │   └── FavoritesDatabase.kt
│   ├── remote/
│   │   ├── RecipeApiService.kt
│   │   ├── RetrofitClient.kt
│   │   └── dto/
│   │       ├── RecipeResponse.kt
│   │       └── Mappers.kt        ← Parseo tiempo, URLs
│   ├── model/
│   │   ├── Recipe.kt
│   │   └── Favorite.kt
│   └── repository/
│       └── RecipeRepository.kt   ← Lógica offline
├── ui/
│   ├── list/
│   │   ├── RecipeListFragment.kt ← Banner offline
│   │   ├── RecipeListViewModel.kt ← Ordenación
│   │   └── RecipeAdapter.kt      ← Selección tablet
│   ├── detail/
│   │   ├── RecipeDetailFragment.kt ← Compartir
│   │   └── RecipeDetailViewModel.kt
│   ├── favorites/
│   │   ├── FavoritesFragment.kt
│   │   └── FavoritesViewModel.kt
│   ├── settings/
│   │   └── SettingsFragment.kt
│   └── webview/
│       └── RestaurantWebViewFragment.kt
├── util/
│   └── NetworkUtils.kt           ← Detección conectividad
├── MainActivity.kt               ← Lógica tablet
├── SplashActivity.kt             ← SplashScreen
└── App.kt

app/src/test/java/
└── data/remote/dto/
    └── MappersTest.kt            ← Unit tests

app/src/androidTest/java/
├── RecipeListFragmentTest.kt
├── RecipeDetailFragmentTest.kt
├── NavigationTest.kt
└── util/
    └── DataLoadingIdlingResource.kt
```

---

## 🎨 Recursos

### Animaciones
- `slide_in_right.xml`, `slide_out_left.xml`
- `slide_in_left.xml`, `slide_out_right.xml`
- `fade_in.xml`, `fade_out.xml`

### Drawables
- `ic_share.xml` - Compartir
- `ic_offline.xml` - Modo sin conexión
- `ic_empty_favorites.xml` - Empty state
- `ic_splash.xml` - Splash screen

### Layouts
- `activity_main.xml` - Teléfono
- `layout-sw600dp/activity_main.xml` - Tablet
- `fragment_recipe_list.xml` - Con banner offline
- `fragment_favorites.xml` - Empty state mejorado
- `menu_detail.xml` - Opciones compartir

---

## 🔧 Preferencias de Usuario

| Clave | Tipo | Valores | Default |
|-------|------|---------|---------|
| `refresh_hours` | String | 1, 6, 12, 24, 48, 168 | 24 |
| `default_sort` | String | name, restaurant, time | name |
| `hide_no_time` | Boolean | true/false | false |

---

## 📱 Para Compilar

```bash
# Debug APK
./gradlew assembleDebug

# Tests unitarios
./gradlew test

# Tests UI (requiere dispositivo/emulador)
./gradlew connectedAndroidTest
```

---

## 🌐 API

- **Principal**: `https://www.turismoasturiasprofesional.es/open-data/turismoasturias`
- **Alternativa**: `http://156.35.163.145/json/RecetasCocina.json` (VPN)

---

## 📝 Notas Técnicas

1. **Offline**: Muestra caché si no hay red, banner informativo
2. **Parseo tiempo**: Soporta "X horas Y minutos", "X minutos", etc.
3. **HTML**: Renderizado con HtmlCompat
4. **Imágenes**: Glide con placeholder y crossfade
5. **WebView**: No abre navegador externo (requisito)
6. **Tablet**: Detalle en panel derecho, selección visual
7. **Compartir**: Texto formateado con emojis

---

## 🧪 Tests

### Unit Tests (MappersTest.kt)
- `extractRestaurantName()` - Con/sin prefijo "Por"
- `parseTimeToMinutes()` - Horas, minutos, combinados
- `extractRestaurantUrl()` - URLs relativas/absolutas
- `extractImageUrl()` - JSON parse, errores

### UI Tests (Espresso)
- Carga de lista
- Navegación al detalle
- Búsqueda
- Filtrado por tiempo
- Pull-to-refresh
- Navegación entre tabs
- Toggle favoritos

---

## ✅ Verificación de Requisitos del Profesor

| Requisito | ✅ |
|-----------|---|
| Mostrar información resumen | ✅ Cards con imagen, nombre, tiempo |
| Mostrar información detalle | ✅ Ingredientes, preparación, trucos |
| Integrar imágenes | ✅ Glide + URL construida |
| WebView embebido | ✅ RestaurantWebViewFragment |
| Búsquedas (receta, autor, ingredientes) | ✅ Room SQL LIKE |
| Filtrado por característica | ✅ Tiempo (chips) |
| Guardar favoritos | ✅ Room + FAB toggle |
| Recordar preferencias | ✅ SharedPreferences |
