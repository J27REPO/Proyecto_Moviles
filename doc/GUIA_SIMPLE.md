# Recetas Asturianas — Guía Para Tontos 🤓

> **¿Qué es esto?** Una app Android que muestra recetas de cocina asturiana. Los datos vienen de internet, se guardan en el móvil, y puedes verlas incluso sin conexión.

---

## 🏠 La Estructura del Proyecto (Carpeteras)

```
app/
├── src/main/
│   ├── java/es/uniovi/recetasasturianas/
│   │   ├── MainActivity.kt          ← La "puerta" de la app
│   │   ├── App.kt                   ← Inicio de la app (inicializa cosas)
│   │   ├── SplashActivity.kt        ← Pantalla de carga inicial
│   │   ├── data/                    ← 🗄️ TODO LO RELACIONADO CON DATOS
│   │   │   ├── model/               ← Recipe.kt (qué es una receta), Favorite.kt
│   │   │   ├── local/               ← Room (base de datos del móvil)
│   │   │   │   ├── RecipeDatabase.kt
│   │   │   │   ├── RecipeDao.kt      ← Consultas SQL para recetas
│   │   │   │   └── FavoriteDao.kt
│   │   │   ├── remote/               ← Retrofit (pedir datos a internet)
│   │   │   │   ├── RetrofitClient.kt ← Configuración de la conexión HTTP
│   │   │   │   ├── RecipeApiService.kt
│   │   │   │   └── dto/
│   │   │   │       ├── RecipeResponse.kt  ← Cómo parsear el JSON de la API
│   │   │   │       └── Mappers.kt         ← Convertir datosAPI → datosApp
│   │   │   └── repository/
│   │   │       └── RecipeRepository.kt ← "Jefe" de los datos
│   │   └── ui/                       ← 🖥️ TODO LO RELACIONADO CON PANTALLAS
│   │       ├── list/                ← Lista de recetas (pantalla principal)
│   │       │   ├── RecipeListFragment.kt
│   │       │   ├── RecipeListViewModel.kt
│   │       │   └── RecipeAdapter.kt
│   │       ├── detail/              ← Detalle de una receta
│   │       │   ├── RecipeDetailFragment.kt
│   │       │   └── RecipeDetailViewModel.kt
│   │       ├── favorites/           ← Favoritos guardados
│   │       │   ├── FavoritesFragment.kt
│   │       │   └── FavoritesViewModel.kt
│   │       ├── settings/            ← Ajustes del usuario
│   │       │   └── SettingsFragment.kt
│   │       └── webview/             ← Página web del restaurante (dentro de la app)
│   │           └── RestaurantWebViewFragment.kt
│   └── assets/
│       └── recetas.json             ← Copia de seguridad con 78 recetas
```

---

## 🎯 La Arquitectura Explicada Sencilla

### ¿Qué es MVVM?

Piensa en un **restaurante**:

```
🍽️ CLIENTE (FRAGMENT/PANTALLA)
     ↓ pide comida
👨‍🍳 COCINERO (VIEWMODEL) 
     ↓ pide ingredientes
📦 NEVERA (REPOSITORY)
     ↓ decide de dónde sacar
🧊 FRIDGE(Room) ←→ 🌐 INTERNET(Retrofit)
```

- **Fragment**: Es la "pantalla" — lo que el usuario ve
- **ViewModel**: Es el "cocinero" — prepara los datos para la pantalla
- **Repository**: Es la "nevera" — decide de dónde vienen los datos
- **Room**: Base de datos local (tu móvil)
- **Retrofit**: Conexión a internet (la API)

### El Flujo de Datos Paso a Paso

```
1. USUARIO abre la app
       ↓
2. RecipeListFragment se crea
       ↓
3. ViewModel.loadRecipes() 
       ↓
4. Repository (jefe): "¿Cuánto tiempo ha pasado desde la última descarga?"
       ↓
5. Si pasaron más de X horas → ir a internet (Retrofit)
   Si no → usar datos locales (Room)
       ↓
6. Los datos llegan al ViewModel como LiveData
       ↓
7. El Fragment observa esos datos y los muestra en pantalla
```

### ¿Por qué todo este lío?

**Separación de responsabilidades** — cada pieza hace una cosa:

| Pieza | Qué hace |
|-------|----------|
| Fragment | Solo muestra cosas en pantalla |
| ViewModel | Procesa los datos, decide qué mostrar |
| Repository | Decide de dónde vienen los datos (internet, local, etc) |
| Room | Guarda datos en el móvil |
| Retrofit | Pide datos a internet |

---

## 🗄️ Room — La Base de Datos Local

Room es como un **Excel privado** dentro del móvil.

### Tabla "recipes" (recetas)
```
┌────┬─────────────┬───────────────┬────────┬────────────┐
│ ID │    name     │   restaurant  │  time  │  imageUrl  │
├────┼─────────────┼───────────────┼────────┼────────────┤
│ 1  │ Fabada      │ Casa Fermín   │   90   │ url...     │
│ 2  │ Arroz con.. │ El Cruce      │   45   │ url...     │
└────┴─────────────┴───────────────┴────────┴────────────┘
```

### Tabla "favorites" (favoritos)
```
┌──────────┬──────────┐
│ recipeId │  savedAt  │
├──────────┼──────────┤
│    1     │  fecha... │
│    5     │  fecha... │
└──────────┴──────────┘
```

### Consultas SQL (RecipeDao)
```kotlin
// Buscar recetas por nombre
@Query("SELECT * FROM recipes WHERE name LIKE '%' || :query || '%'")

// Buscar recetas rápidas (≤30 min)
@Query("SELECT * FROM recipes WHERE timeMinutes <= 30")

// Obtener favoritos del usuario
@Query("SELECT * FROM recipes INNER JOIN favorites ON recipes.id = favorites.recipeId")
```

---

## 🌐 Retrofit — Conexión a Internet

La app se conecta a una API REST para descargar recetas.

### La API
- **URL principal**: `http://156.35.163.145/json/RecetasCocina.json` (requiere VPN universidad)
- **URL alternativa**: `https://www.turismoasturiasprofesional.es/open-data/turismoasturias`
- **Fallback local**: `assets/recetas.json` (siempre funciona, 78 recetas)

### Cómo funciona
```
1. RetrofitClient.kt → Crea la conexión HTTP
2. RecipeApiService.kt → Define qué endpoint llamar (GET /json/RecetasCocina.json)
3. Moshi → Convierte el JSON en objetos Kotlin
4. Mappers.kt → Convierte los datos de la API al formato de nuestra app
```

### El JSON que viene de la API (simplificado)
```json
{
  "articles": {
    "article": [
      {
        "Nombre": { "content": "Fabada Asturiana" },
        "Resumen": { "content": "Por Casa Fermín" },
        "Imagen": { "content": "{\"groupId\":\"39908\",\"title\":\"foto.jpg\",\"uuid\":\"...\"}" },
        "Contacto": {
          "Tiempo": { "content": "45 minutos" },
          "Ingredientes": { "content": "<ul><li>Fabes</li><li>Chozos</li></ul>" }
        }
      }
    ]
  }
}
```

---

## 🔄 Búsqueda vs Filtrado — ¡LA CLAVE!

Esta es la parte más confusa del proyecto. Son cosas DIFERENTES:

### Búsqueda (capa de DATOS)
```
USUARIO escribe "casa" → Repository → Room (SQL) → Devuelve solo recetas con "casa"
```
- Se modifica la consulta a la base de datos
- El ViewModel recibe MENOS datos

### Filtrado (capa de UI)
```
USUARIO pulsa "Rápida" → Adapter filtra la lista actual → Muestra solo ≤30 min
```
- NO modifica los datos, solo oculta los que no cumplen
- El ViewModel tiene TODOS los datos, pero el Adapter solo muestra algunos

**Analogía del armario:**
- Búsqueda = Buscar en el almacén qué camisas hay (cambia qué hay)
- Filtrado = De las camisas que hay, mostrar solo las azules (no cambia qué hay)

---

## 📱 Las Pantallas (Fragments)

### 1. RecipeListFragment (Lista de Recetas)
```
┌─────────────────────────────┐
│ 🔍 Buscar...                │ ← SearchView
├─────────────────────────────┤
│ [Todo] [Rápida] [Media]     │ ← Chips de filtrado por tiempo
├─────────────────────────────┤
│ ┌─────────────────────────┐ │
│ │ 🖼️ Fabada Asturiana     │ │ ← Card de receta
│ │    Por Casa Fermín      │ │
│ │    ⏱️ 90 min            │ │
│ └─────────────────────────┘ │
│ ┌─────────────────────────┐ │
│ │ 🖼️ Arroz con leche      │ │
│ │    Por El Cruce         │ │
│ │    ⏱️ 45 min            │ │
│ └─────────────────────────┘ │
│         ...                 │
├─────────────────────────────┤
│ 📴 Modo offline             │ ← Banner (solo si no hay conexión)
└─────────────────────────────┘
```

### 2. RecipeDetailFragment (Detalle de Receta)
```
┌─────────────────────────────┐
│ 🖼️ Imagen grande           │
│ ┌─────────────────────────┐ │
│ │ 📷 Fabada Asturiana      │ │
│ │    Por Casa Fermín       │ │
│ │    ⏱️ 90 min             │ │
│ └─────────────────────────┘ │
├─────────────────────────────┤
│ 📝 INGREDIENTES             │
│ • 1kg de fabes             │
│ • 200g de chocos           │
│ • 1 chorizo                │
├─────────────────────────────┤
│ 👨‍🍳 PREPARACIÓN            │
│ 1. Poner las fabes en...   │
│ 2. Cocer a fuego lento...  │
├─────────────────────────────┤
│ 💡 TRUCOS                  │
│ Para que queden más...     │
├─────────────────────────────┤
│ [❤️ Favorito] [🌐 Ver restaurante] │
│ [📤 Compartir]             │
└─────────────────────────────┘
```

### 3. FavoritesFragment (Favoritos)
- Igual que la lista principal, pero solo muestra recetas marcadas como favoritas
- Empty state: "No tienes favoritos guardados"

### 4. SettingsFragment (Ajustes)
```
┌─────────────────────────────┐
│ Ajustes                     │
├─────────────────────────────┤
│ Refrescar datos            │
│ Cada [24 horas ▼]          │ ← ListPreference
├─────────────────────────────┤
│ Orden por defecto          │
│ [Nombre ▼]                 │ ← ListPreference
├─────────────────────────────┤
│ ☑️ Ocultar recetas sin     │
│   tiempo de preparación    │ ← SwitchPreference
├─────────────────────────────┤
│ Tema                       │
│ [Claro ▼]                  │ ← ListPreference
└─────────────────────────────┘
```

### 5. RestaurantWebViewFragment (Web del Restaurante)
```
┌─────────────────────────────┐
│ ← Ver restaurante           │
├─────────────────────────────┤
│ ┌─────────────────────────┐ │
│ │ 🌐 Página web del       │ │
│ │    restaurante          │ │
│ │    (dentro de la app)   │ │
│ │                         │ │
│ │ NO abre navegador       │ │
│ │ externo, es un WebView  │ │
│ │ embebido                │ │
│ └─────────────────────────┘ │
└─────────────────────────────┘
```

---

## 🧩 Los Componentes Clave Explicados

### RecipeAdapter (El Lista-Cosas)
```
RecetaAdapter
├── submitList(recetas)      ← Recibe la lista del ViewModel
├── currentTimeFilter        ← Guarda el filtro de tiempo actual
├── applyFilter(TimeFilter)  ← Filtra la lista (QUICK/MEDIUM/LONG)
└── onBindViewHolder()       ← Dibuja cada item de la lista
```

### TimeFilter (Enum)
```kotlin
enum class TimeFilter {
    ALL,    // Sin filtro
    QUICK,  // ≤30 minutos
    MEDIUM, // 31-60 minutos
    LONG    // >60 minutos
}
```

### Mappers.kt (Conversión de datos)
```kotlin
// Convierte "Por Casa Fermín" → "Casa Fermín"
extractRestaurantName("Por Casa Fermín") = "Casa Fermín"

// Convierte "1 hora 30 minutos" → 90
parseTimeToMinutes("1 hora 30 minutos") = 90

// Convierte JSON de imagen → URL real
extractImageUrl("{\"groupId\":\"39908\",\"title\":\"foto.jpg\",\"uuid\":\"abc\"}")
= "https://www.turismoasturias.es/documents/39908/0/foto.jpg/abc?version=1.0"

// Convierte HTML de enlace → URL limpia
extractRestaurantUrl("<a href=\"/ruta/restaurante\">Casa Fermín</a>")
= "https://www.turismoasturias.es/ruta/restaurante"
```

---

## 📦 SharedPreferences (Ajustes Guardados)

Los ajustes del usuario se guardan como "pares clave-valor":

| Clave | Tipo | Default | Descripción |
|-------|------|---------|-------------|
| `refresh_hours` | String | "24" | Cada cuántas horas refrescar |
| `default_sort` | String | "name" | Orden por defecto |
| `hide_no_time` | Boolean | false | Ocultar recetas sin tiempo |
| `app_theme` | String | "system" | Tema (light/dark/system) |

---

## 🎨 Navegación (Navigation Component)

```
┌──────────────────────────────────────────────────────────┐
│                    MainActivity                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │              NavHostFragment                       │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │         RecipeListFragment (INICIO)          │  │  │
│  │  │                    │                         │  │  │
│  │  │                    ↓ click                   │  │  │
│  │  │         RecipeDetailFragment                 │  │  │
│  │  │                    │                         │  │  │
│  │  │                    ↓ click "Ver restaurante" │  │  │
│  │  │         RestaurantWebViewFragment            │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  ┌─────────┬─────────┬─────────┐                          │
│  │ Recetas │Favoritos│ Ajustes │  ← BottomNavigation     │
│  └─────────┴─────────┴─────────┘                          │
└──────────────────────────────────────────────────────────┘
```

---

## 🧪 Testing

### Unit Tests (MappersTest.kt) — 20 tests
Prueban que las funciones de conversión funcionan bien:
```kotlin
@Test
fun extractRestaurantName_withPrefix() {
    assertEquals("Casa Fermín", extractRestaurantName("Por Casa Fermín"))
}

@Test
fun parseTimeToMinutes_withHoursAndMinutes() {
    assertEquals(90, parseTimeToMinutes("1 hora 30 minutos"))
}
```

### UI Tests (Barista + Espresso) — 10 tests
Prueban que la interfaz funciona:
```kotlin
@Test
fun clickOnRecipe_opensDetail() {
    clickOn(R.id.recyclerView)
    onView(withRecyclerView(R.id.recyclerView)
        .atPosition(0)).perform(click())
    assertDisplayed(R.id.recipe_name)
}
```

---

## 📋 Resumen de Archivos Importantes

| Archivo | ¿Qué hace? | Línea/Clase clave |
|---------|-------------|------------------|
| `RecipeRepository.kt` | Jefe de datos. Decide de dónde traer las recetas | línea 87: `refreshRecipesIfNeeded()` |
| `RecipeListViewModel.kt` | Cocinero de la lista. Carga y filtra recetas | línea 40: `loadRecipes()` |
| `RecipeAdapter.kt` | Muestra la lista. Tiene el filtro de tiempo | línea 85: `applyFilter()` |
| `RecipeDao.kt` | Consultas SQL. Búsqueda y filtrado | línea 32: `search()` |
| `Mappers.kt` | Convierte datos de la API al formato de la app | línea 14: `extractRestaurantName()` |
| `RetrofitClient.kt` | Configura la conexión HTTP con Moshi | línea 30: `Moshi.Builder()` |
| `MainActivity.kt` | Activity principal. Navegación y toolbar | línea 46: `setupNavigation()` |
| `nav_graph.xml` | Define las pantallas y cómo navegar entre ellas | `<fragment>` tags |

---

## 🏃 Comandos Útiles

```bash
# Compilar la app
./gradlew assembleDebug

# Instalar en el móvil
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Ver errores en el logcat
adb logcat -d | grep -E "RecetasAsturianas|FATAL"

# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests de interfaz
./gradlew connectedAndroidTest

# Limpiar y recompilar
./gradlew clean assembleDebug
```

---

## ❓ Preguntas Frecuentes

### ¿Por qué usa Moshi y no Gson?
Porque el profesor lo pidió. Moshi tiene mejor soporte para Kotlin.

### ¿Por qué hay un archivo recetas.json en assets?
Es un fallback — si la API no funciona (sin VPN), la app carga los datos de ahí.

### ¿Qué es un WebView y por qué se usa?
Es como un navegador dentro de la app. El profesor lo pidió porque los datos de recetas no tienen coordenadas GPS, así que en vez de abrir Google Maps, se abre la página del restaurante.

### ¿Qué diferencia hay entre Fragment y Activity?
- **Activity**: Pantalla completa. Solo hay una por app (normalmente).
- **Fragment**: Parte de una Activity. Permiten dividir la pantalla (ej: tablet con maestro-detalle).

### ¿Qué es LiveData?
Es un tipo de datos que "avisa" cuando cambian. El Fragment se "suscribe" y cuando los datos cambian, la pantalla se actualiza automáticamente.

---

## 📊 Diagrama Completo de la Arquitectura

```
                                    ┌─────────────────────────────────┐
                                    │          USUARIO               │
                                    └───────────────┬─────────────────┘
                                                    │
                                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           UI LAYER                                      │
│  ┌─────────────────┐        ┌─────────────────┐        ┌────────────┐ │
│  │ RecipeListFragment│        │RecipeDetailFrag │        │SettingsFrag│ │
│  │   (Lista)        │        │   (Detalle)     │        │ (Ajustes)  │ │
│  └────────┬────────┘        └────────┬────────┘        └──────┬─────┘ │
│           │ observe LiveData          │ observe                 │        │
│           │                           │                         │        │
│  ┌────────▼────────┐        ┌──────────▼──────────┐        ┌───▼───────┐ │
│  │ RecipeListVM   │        │ RecipeDetailViewModel│        │SettingsVM │ │
│  │  (ViewModel)   │        │   (ViewModel)        │        │ (ViewModel)│ │
│  └────────┬───────┘        └──────────┬──────────┘        └───────────┘ │
└───────────┼────────────────────────────┼───────────────────────────────┘
            │ llama                      │ llama
            ▼                            ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                         DOMAIN LAYER                                       │
│  ┌────────────────────────────────────────────────────────────────────┐   │
│  │                    RecipeRepository (Singleton)                     │   │
│  │   - ¿Cuándo refrescar? (cachedAt vs refresh_hours)                 │   │
│  │   - ¿De dónde traer datos? (API vs Assets vs Room)                  │   │
│  │   - Gestión de favoritos                                            │   │
│  └────────────────────────────────┬───────────────────────────────────┘   │
└──────────────────────────────────┼───────────────────────────────────────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │                             │
                    ▼                             ▼
┌─────────────────────────────────┐   ┌─────────────────────────────────────┐
│         DATA LAYER (Local)       │   │          DATA LAYER (Remote)        │
│                                 │   │                                     │
│  ┌───────────┐   ┌───────────┐  │   │  ┌────────────────────────────────┐  │
│  │   Room     │   │ Shared   │  │   │  │      Retrofit + Moshi           │  │
│  │ Database   │   │Prefs    │  │   │  │                                │  │
│  │            │   │         │  │   │  │  RecipeApiService               │  │
│  │ recipes ───┼───┤ refresh │  │   │  │  GET /json/RecetasCocina.json   │  │
│  │ favorites  │   │ _hours  │  │   │  │                                │  │
│  └───────────┘   └─────────┘  │   │  └────────────────────────────────┘  │
└─────────────────────────────────┘   └──────────────────────┬──────────────┘
                                                           │
                                            ┌──────────────┴───────────────┐
                                            │                               │
                                            ▼                               ▼
                                    ┌───────────────┐             ┌───────────────┐
                                    │  API Remota   │             │   Assets      │
                                    │ (VPN uniovi)  │             │ recetas.json  │
                                    └───────────────┘             └───────────────┘
```

---

*Última actualización: 19 de mayo de 2026*
*Proyecto: Recetas Asturianas — Informática Móvil — Universidad de Oviedo*