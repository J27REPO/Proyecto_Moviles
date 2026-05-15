# MACRO-MARKDOWN: Documento Word de Arquitectura
## Recetas Asturianas - Informatica Movil 2026

Usa este documento como guia completa para crear tu Word a mano con dibujos y diagramas.

===============================================================================
### PORTADA
===============================================================================

[Titulo centrado, letra grande]
ARQUITECTURA DE RECETAS ASTURIANAS

[Subtitulo]
Aplicacion Android con MVVM + Room + Retrofit + Moshi

[Informacion]
Asignatura: Informatica Movil
Universidad de Oviedo
Curso 2025/2026
Alumno: [Tu nombre y apellidos]
Email: [Tu email]
Fecha: Mayo 2026

===============================================================================
### 1. INTRODUCCION
===============================================================================

Recetas Asturianas es una aplicacion Android que permite explorar, buscar y
guardar recetas tipicas de la gastronomia asturiana. Los datos se obtienen del
**catalogo de datos abiertos del Principado de Asturias**, disponible en
`https://www.turismoasturiasprofesional.es/open-data/turismoasturias`,
concretamente el conjunto **"Recetas de cocina"** servido desde
`http://156.35.163.145/json/RecetasCocina.json` (requiere VPN universidad),
con un sistema de fallback a `assets/recetas.json` si la API no esta disponible.

La aplicacion sigue la arquitectura MVVM (Model-View-ViewModel) recomendada
por Google, con una clara separacion de responsabilidades en tres capas:
Data Layer, Domain Layer y UI Layer.

**Condicion especial**: Al no incluir datos GPS el conjunto original, el profesor
solicito que los enlaces a los restaurantes se muestren embebidos dentro de la
propia aplicacion mediante un WebView, sin abrir el navegador externo.

===============================================================================
### 2. ARQUITECTURA MVVM - DIAGRAMA GENERAL
===============================================================================

[DIBUJO: Diagrama de capas con flechas]

                    +---------------------------+
                    |       UI LAYER            |
                    |  (Fragmentos + ViewModel) |
                    +-----------+---------------+
                                |
                    (LiveData observado)
                                |
                    +-----------v---------------+
                    |     DOMAIN LAYER          |
                    |  (Repositorio)            |
                    +-----------+---------------+
                    |           |               |
            +-------v--+ +-----v----+  +-------v------+
            | API REST | |  Room DB |  | assets/json  |
            | (Retrofit| | (Cache)  |  | (Fallback)   |
            | + Moshi) | |          |  |              |
            +----------+ +----------+  +--------------+

FLUJO DE DATOS:
  1. UI (Fragment) observa LiveData del ViewModel
  2. ViewModel solicita datos al Repository
  3. Repository intenta API remota (Retrofit + Moshi)
  4. Si API falla -> carga desde assets/recetas.json (Moshi)
  5. Los datos se cachean en Room (SQLite)
  6. ViewModel expone LiveData -> UI se actualiza reactivamente

===============================================================================
### 3. ESQUEMA DE CLASES PRINCIPALES
===============================================================================

[DIBUJO: Diagrama de clases con cajas y flechas, similar a UML]

+============================+
|         App.kt             |    <- Application class (punto de entrada)
+============================+
        |
+=======+============================+
|         MainActivity.kt            |    <- Activity con Navigation Component
|  + BottomNavigationView (3 tabs)   |
|  + NavHostFragment                 |
+====================================+
        |
        +------------------+------------------+
        |                  |                  |
+-------v--------+ +------v-------+ +--------v-------+
| RecipeList     | | Favorites    | | Settings       |
| Fragment       | | Fragment     | | Fragment       |
| + RecyclerView | | + RecyclerV  | | + Preference   |
| + SearchView   | | + EmptyState | |   FragmentCompat|
| + Chips filtro | |              | |                |
+-------+--------+ +------+-------+ +----------------+
        |                  |
+-------v--------+ +------v-------+
| RecipeDetail   | | Restaurant   |
| Fragment       | | WebView      |
| + Imagen       | | Fragment     |
| + Ingredientes | | + WebView    |
| + Preparacion  | | + ProgressBar|
| + FAB favorito | |              |
+----------------+ +--------------+

+====================================+
|        RECIPE LIST VIEW MODEL      |
|  - recipes: LiveData<List<Recipe>> |
|  - searchQuery: MutableLiveData    |
|  - timeFilter: MutableLiveData     |
|  - isLoading: MutableLiveData      |
|  - error: MutableLiveData          |
|  - isOffline: MutableLiveData      |
|  + loadRecipes()                   |
|  + refresh()                       |
|  + search(query)                   |
|  + applyTimeFilter(filter)         |
+====================================+
        |  usa
+=======+============================+
|        RECIPE REPOSITORY           |  <- Fuente unica de datos
|  + getAllRecipes(): LiveData       |
|  + searchRecipes(): LiveData       |
|  + refreshRecipesIfNeeded()        |
|  + getFavorites(): LiveData        |
|  + toggleFavorite()                |
|  - loadFromAssets(): List<Recipe>  |
+====================================+
        |           |           |
+-------v---+ +---v------+ +--v----------+
| Retrofit  | | Room DB  | | Assets      |
|Client.kt  | | (SQLite) | | recetas.json|
| + Moshi   | | 2 tablas | | (Moshi)     |
+-----------+ +----------+ +-------------+

+====================================+
|      RECIPE RESPONSE (DTOs)       |
|  RecipeResponse                    |
|    +-> ArticlesWrapper             |
|          +-> List<ArticleDto>      |
|                +-> ContentWrapper  |
|                +-> VisualizadorWr  |
|                +-> InformacionWr   |
|                +-> ContactoWrapper |
|                +-> ObservacionesWr |
|  FlexibleAdapterFactory            |
|    (maneja objeto/array inconsist.)|
+====================================+

===============================================================================
### 4. CAPA DE DATOS (DATA LAYER)
===============================================================================

Paquete: `es.uniovi.recetasasturianas.data`

#### 4.1 Modelo de Datos (Room)

[DIBUJO: Diagrama entidad-relacion de las tablas]

+---------------------+          +---------------------+
|      recipes        |          |     favorites       |
+---------------------+          +---------------------+
| PK id: Int          |          | PK recipeId: Int   |
| name: String        |<---------+ FK -> recipes.id    |
| restaurant: String  |          | savedAt: Long       |
| preparationHtml:Str |          +---------------------+
| ingredientsHtml:Str |
| imageUrl: String?   |
| restaurantUrl: Str? |
| timeMinutes: Int?   |
| tipsHtml: String?   |
| notesHtml: String?  |
| cachedAt: Long      |
+---------------------+

NOTA: Recipe implementa Parcelable para paso seguro entre fragments.

#### 4.2 Acceso Remoto (Retrofit + Moshi)

**Fuente de datos oficial**: Catalogo de datos abiertos del Principado de Asturias
(`https://www.turismoasturiasprofesional.es/open-data/turismoasturias`),
conjunto "Recetas de cocina".

| Tipo | URL | Proposito |
|------|-----|-----------|
| Principal | `http://156.35.163.145/json/RecetasCocina.json` | API via VPN universidad |
| Alternativa | `https://www.turismoasturiasprofesional.es/open-data/` | API publica HTTPS |
| Fallback | `app/src/main/assets/recetas.json` | Archivo local (~276 KB) |

- **RecipeApiService**: Interface Retrofit con `@GET("RecetasCocina.json")`.
- **RetrofitClient**: Singleton que configura OkHttp + Moshi.
- **Moshi** deserializa JSON con `FlexibleAdapterFactory`.

El `FlexibleAdapterFactory` es un `JsonAdapter.Factory` que maneja
campos JSON inconsistentes (a veces objeto, a veces array de objetos).
Funciona extrayendo el primer elemento si detecta BEGIN_ARRAY.

**Condicion extra**: Al no haber datos GPS en el conjunto, el profesor exigio
que los enlaces a restaurantes se abran dentro de la app. Ver seccion 8.4.

#### 4.3 Acceso Local (Room)

- **RecipeDatabase**: Singleton Room con 2 DAOs.
- **RecipeDao**: Consultas SQL (getAll, search, getByMaxTime, getById).
- **FavoriteDao**: Operaciones CRUD sobre favoritos.

Los DAOs retornan `LiveData` para que los ViewModels observen cambios.

#### 4.4 Repositorio

**RecipeRepository** es la fuente unica de datos y encapsula:
1. Intento de API remota (Retrofit)
2. Fallback a assets/recetas.json si API falla
3. Cache en Room para acceso offline
4. Control de refresco por intervalo configurable (SharedPreferences)

===============================================================================
### 5. CAPA DE PRESENTACION (UI LAYER)
===============================================================================

#### 5.1 Navegacion

[DIBUJO: Arbol de navegacion con flechas entre fragments]

   [SplashActivity] -> [MainActivity]
                            |
              +-------------+-------------+
              |             |             |
         [RecipeList]  [Favorites]  [Settings]
              |
         [RecipeDetail]
              |
         [RestaurantWebView]

Esquema: BottomNavigation con 3 tabs.
Navigation Component gestiona la navegacion con fragmentos.

#### 5.2 ViewModels

Los ViewModels extienden `AndroidViewModel` y usan `LiveData` para la
comunicacion reactiva con los Fragments.

- **RecipeListViewModel**: Gestiona lista, busqueda, filtros, estado de carga.
- **RecipeDetailViewModel**: Gestiona detalle de receta y favoritos.
- **FavoritesViewModel**: Gestiona lista de favoritos.

#### 5.3 Adaptacion a Tablet

Layout alternativo en `res/layout-sw600dp/` que muestra el patron
maestro-detalle: lista de recetas a la izquierda, detalle a la derecha.

#### 5.4 Soporte Multiidioma

- `res/values/strings.xml`: Espanol (idioma por defecto)
- `res/values-en/strings.xml`: Ingles

===============================================================================
### 6. TECNOLOGIAS Y LIBRERIAS
===============================================================================

[DIBUJO: Tabla con dos columnas]

+====================================+========================================+
| Libreria                          | Proposito                              |
+====================================+========================================+
| Kotlin 1.9.22                     | Lenguaje de programacion               |
+------------------------------------+----------------------------------------+
| Android SDK 26-35                 | SDK minimo (Oreo) y objetivo           |
+------------------------------------+----------------------------------------+
| Room 2.6.1                        | Base de datos local SQLite             |
+------------------------------------+----------------------------------------+
| Retrofit 2.9.0                    | Cliente HTTP para API REST             |
+------------------------------------+----------------------------------------+
| Moshi 1.15.1                      | Deserializacion JSON (sustituye a Gson)|
+------------------------------------+----------------------------------------+
| OkHttp 4.12.0                     | Cliente HTTP subyacente + logging      |
+------------------------------------+----------------------------------------+
| Glide 4.16.0                      | Carga y cache de imagenes              |
+------------------------------------+----------------------------------------+
| Navigation Comp. 2.7.7            | Navegacion entre fragments             |
+------------------------------------+----------------------------------------+
| Material 3                        | Diseno de interfaz (MaterialComponents)|
+------------------------------------+----------------------------------------+
| Espresso 3.5.1 + Barista 4.3.0   | Tests de interfaz de usuario (UI) — API fluida `clickOn()`, `assertDisplayed()`, `writeTo()` |
+------------------------------------+----------------------------------------+
| ViewBinding                       | Vinculacion de vistas type-safe        |
+------------------------------------+----------------------------------------+
| LiveData                          | Datos observables reactivos            |
+------------------------------------+----------------------------------------+
| Coroutines                        | Operaciones asincronas en segundo plano|
+------------------------------------+----------------------------------------+

===============================================================================
### 7. DECISIONES DE DISENO
===============================================================================

#### 7.1 Moshi vs Gson
Se utiliza Moshi en lugar de Gson por exigencia del profesor y porque
Moshi ofrece mejor soporte para Kotlin (null safety, clases data,
@JsonClass) y esta mejor integrado con Retrofit (mismo ecosistema Square).

#### 7.2 Navegacion con BottomNavigation
Se eligio BottomNavigation con 3 tabs porque la aplicacion tiene
pocas pantallas principales y la navegacion es sencilla. Es el patron
recomendado por Material Design para este tipo de aplicaciones.

#### 7.3 Room como cache local
Room se utiliza para cachear los datos obtenidos de la API, permitiendo
el acceso offline y busquedas SQL eficientes. El repositorio controla
el intervalo de refresco mediante SharedPreferences.

#### 7.4 Fallback a assets
Ante la imposibilidad de acceder a la API (VPN requerida), la aplicacion
carga automaticamente los datos desde `assets/recetas.json`, garantizando
que siempre haya datos disponibles.

#### 7.5 FlexibleAdapterFactory (Moshi)
La API devuelve campos JSON inconsistentes: a veces son objetos
`{"content": "..."}` y a veces arrays `[{"content": "..."}]`.
`FlexibleAdapterFactory` resuelve esto probando primero BEGIN_OBJECT
y, si es BEGIN_ARRAY, tomando el primer elemento del array.

#### 7.6 WebView Embebido (condicion extra del profesor)
Dado que el conjunto de datos "Recetas de cocina" no incluye coordenadas GPS,
el profesor impuso la condicion de que los enlaces a los restaurantes se abran
**dentro de la aplicacion**, no en el navegador externo. Se implemento en
`RestaurantWebViewFragment.kt` con un `WebViewClient` que:
- Retorna `false` en `shouldOverrideUrlLoading()` (no sobrescribe la navegacion).
- Muestra una ProgressBar mientras carga.
- Muestra un mensaje de error si la pagina falla.
- Permite zoom y JavaScript para compatibilidad total.

#### 7.7 Preferencias y Persistencia
Las preferencias de usuario se gestionan con `PreferenceFragmentCompat` y `SharedPreferences`.
- **Intervalo de refresco**: Controla cada cuanto tiempo se re-intenta la API.
- **Orden por defecto**: Nombre, restaurante o tiempo.
- **Ocultar sin tiempo**: Filtra recetas sin tiempo de preparacion.
- **Tema**: Claro, oscuro o seguir sistema (implementado con `AppCompatDelegate`).

NOTA: Los `ListPreference` usan `app:useSimpleSummaryProvider="true"` para actualizar
automaticamente el sumario. Los listeners de cambio solo se usan para el tema
(con `Handler.post()` para evitar crash por recreacion de Activity dentro del callback).

#### 7.8 Soporte Tablet (Maestro-Detalle)
Para adaptarse a tablets, se implemento un layout alternativo en
`res/layout-sw600dp/` que divide la pantalla en dos paneles:
- Izquierdo: lista de recetas (RecyclerView seleccionable).
- Derecho: detalle de la receta seleccionada (o WebView del restaurante).
La seleccion se marca visualmente con color de fondo.

===============================================================================
### 8. CHECKLIST COMPLETO DE REQUISITOS
===============================================================================

#### 8.0 Especificacion Contractual (datos abiertos Principado de Asturias)

| # | Requisito | Estado | Implementacion en codigo |
|---|-----------|--------|--------------------------|
| 1 | **Fuente datos**: catalogo abierto del Principado de Asturias (`https://www.turismoasturiasprofesional.es/open-data/turismoasturias`) | ✅ | `RetrofitClient.kt` URL publica + uni + fallback |
| 2 | **Conjunto**: "Recetas de cocina" via `156.35.163.145/json/RecetasCocina.json` | ✅ | `RetrofitClient.kt:18` + `RecipeApiService.kt:14` |
| 3 | **Listado**: nombre, imagen, restaurante autor | ✅ | `RecipeAdapter.kt:111-123` + Glide |
| 4 | **Detalle**: ingredientes, preparacion, tiempo, trucos | ✅ | `RecipeDetailFragment.kt:99-172` (HTML renderizado) |
| 5 | **Enlace restaurante embebido** (NO navegador externo) - **condicion extra** | ✅ | `RestaurantWebViewFragment.kt` WebViewClient propio |
| 6 | **Buscar**: por nombre y restaurante/autor | ✅ | `RecipeDao.kt:32-39` SQL LIKE |
| 7 | **Filtrar**: rapida (<=30), media (31-60), larga (>60) | ✅ | `RecipeAdapter.kt:85-90` + TimeFilter enum |
| 8 | **Guardar favoritos** | ✅ | `FavoriteDao.kt` + FAB toggle |
| 9 | **Recordar preferencias** (orden, filtros activos) | ✅ | `SettingsFragment.kt` + SharedPreferences |

#### 8.1 tenerEnCuenta.txt
[ ] 1. minSdk 26 (justificar si API > 26) -> CUMPLIDO (minSdk=26)
[ ] 2. Moshi en vez de Gson -> CUMPLIDO
[ ] 3. Espresso + Barista para UI tests -> CUMPLIDO (3 ficheros, 10 tests con Barista 4.3.0)
[ ] 4. Documento Word explicativo -> CUMPLIDO (este documento)

#### 8.2 Requisitos funcionales minimos (Campus Virtual)
[ ] Desarrollo personal y original -> OK
[ ] Adaptacion idioma (2 idiomas: ES + EN) -> OK
[ ] Adaptacion pantalla (telefono + tablet sw600dp) -> OK
[ ] API 26+ -> OK (minSdk=26)
[ ] Navegacion justificada -> OK (BottomNavigation)
[ ] Navigation Component + fragments -> OK
[ ] Robustez ante interrupciones -> OK (ViewModel + LiveData)
[ ] Guia de diseno Google -> OK (Material 3)
[ ] Imagenes y recursos propios -> OK (vector drawables + Glide)

#### 8.3 Requisitos de diseno minimos (Campus Virtual)
[ ] Arquitectura MVVM -> OK
[ ] Codigo legible y comentado -> OK
[ ] Nomenclatura Kotlin correcta -> OK
[ ] Division en paquetes por finalidad -> OK (data, ui, util)
[ ] Acceso a datos con Room -> OK (2 tablas, 2 DAOs)
[ ] Librerias externas justificadas -> OK
[ ] Pruebas de interfaz de usuario -> OK (Barista 4.3.0 + Espresso 3.5.1, 10 tests)

#### 8.4 Condicion Extra del Profesor (WebView embebido por falta de GPS)
Dado que el conjunto de datos "Recetas de cocina" del Principado de Asturias **no incluye
coordenadas GPS**, el profesor anadio esta condicion especial:

> "Al no incorporar datos gps en los datos, te he anadido una condicion a la hora de
> tratar los enlaces a la pagina web del autor/restaurante. Te pido que incluyas esa
> informacion embebida dentro de la propia aplicacion y no uses un navegador externo."

**Implementacion**: `RestaurantWebViewFragment.kt`
- `WebViewClient` con `shouldOverrideUrlLoading()` retornando `false` (NO abre navegador).
- ProgressBar de carga visible durante la navegacion.
- Manejo de error con vista alternativa si la pagina no carga.
- Comentario en cabecera del fichero: "IMPORTANTE: Este es un requisito del profesor."

===============================================================================
### 9. ESTRUCTURA DEL PROYECTO (ARBOL COMPLETO)
===============================================================================

[DIBUJO: Arbol de directorios del proyecto]

RecetasAsturianas/
+-- app/
|   +-- build.gradle.kts
|   +-- src/
|       +-- main/
|       |   +-- AndroidManifest.xml
|       |   +-- java/es/uniovi/recetasasturianas/
|       |   |   +-- App.kt
|       |   |   +-- MainActivity.kt
|       |   |   +-- SplashActivity.kt
|       |   |   +-- data/
|       |   |   |   +-- local/   (RecipeDao, FavoriteDao, RecipeDatabase)
|       |   |   |   +-- model/   (Recipe, Favorite)
|       |   |   |   +-- remote/  (RetrofitClient, RecipeApiService, dto/)
|       |   |   |   +-- repository/ (RecipeRepository)
|       |   |   +-- ui/
|       |   |   |   +-- list/    (Fragment, ViewModel, Adapter)
|       |   |   |   +-- detail/  (Fragment, ViewModel)
|       |   |   |   +-- favorites/ (Fragment, ViewModel)
|       |   |   |   +-- settings/ (Fragment)
|       |   |   |   +-- webview/ (Fragment)
|       |   |   +-- util/ (NetworkUtils)
|       |   +-- res/
|       |       +-- layout/  (6 layouts)
|       |       +-- layout-sw600dp/ (tablet)
|       |       +-- values/  (ES)
|       |       +-- values-en/ (EN)
|       |       +-- drawable/ (iconos vectoriales)
|       |       +-- anim/ (animaciones navegacion)
|       |       +-- navigation/ (nav_graph.xml)
|       |       +-- xml/ (network_security_config)
|       +-- test/    (MappersTest.kt - 20 tests)
|       +-- androidTest/ (10 tests Barista+Espresso + DataLoadingIdlingResource)
+-- build.gradle.kts
+-- settings.gradle.kts
+-- plan.md
+-- PROYECTO_ESTADO.md
+-- CHANGELOG_FALLBACK_API.md
+-- esquema.txt
+-- doc/Arquitectura_RecetasAsturianas.docx

===============================================================================
### 10. COMANDOS UTILES
===============================================================================

```bash
# Compilar APK
./gradlew assembleDebug

# Tests unitarios (JVM)
./gradlew test

# Tests UI (requiere emulador/dispositivo)
./gradlew connectedAndroidTest

# Limpiar y compilar
./gradlew clean assembleDebug

# Generar APK release (firmado)
./gradlew assembleRelease

# Ver el APK generado
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Exportar proyecto ZIP (Android Studio)
File -> Export -> Export to Zip file
```

===============================================================================
### 11. GLOSARIO
===============================================================================

| Termino | Significado |
|---------|-------------|
| MVVM | Model-View-ViewModel, patron de arquitectura |
| Room | Biblioteca de persistencia SQLite para Android |
| Retrofit | Cliente HTTP type-safe para Android/Java |
| Moshi | Libreria de serializacion/deserializacion JSON |
| OkHttp | Cliente HTTP eficiente para Android |
| Glide | Libreria de carga y cache de imagenes |
| ViewModel | Clase que almacena datos del UI (sobrevive cambios configuracion) |
| LiveData | Clase observable de datos con conciencia del ciclo de vida |
| Navigation Component | Framework para navegacion dentro de la app |
| Espresso | Framework de tests de UI para Android |
| ViewBinding | Generacion de clases de binding para layouts |
| KSP | Kotlin Symbol Processing (para Room y Moshi) |
| DTO | Data Transfer Object |
| DAO | Data Access Object |
| FAB | Floating Action Button |

===============================================================================
### 12. NOTAS PARA LOS DIBUJOS
===============================================================================

Dibujos recomendados para incluir en el Word:

1.  DIAGRAMA DE ARQUITECTURA MVVM (Seccion 2):
    - Tres cajas apiladas: UI Layer, Domain Layer, Data Layer
    - Flechas hacia abajo (solicitudes) y hacia arriba (datos/eventos)
    - Dentro de Data Layer: tres subcajas: API REST, Room DB, Assets

2.  DIAGRAMA DE CLASES (Seccion 3):
    - Caja para cada clase principal con sus metodos/public fields
    - Flechas de dependencia (usa) entre capas
    - Colores diferentes para cada capa (data=azul, domain=verde, ui=naranja)

3.  DIAGRAMA ENTIDAD-RELACION (Seccion 4.1):
    - Dos tablas: recipes y favorites
    - Relacion one-to-many con flecha
    - Campos clave resaltados

4.  ARBOL DE NAVEGACION (Seccion 5.1):
    - Nodos rectangulares para cada fragment
    - Flechas dirigidas con etiquetas de accion
    - BottomNavigation con 3 ramas principales

5.  ARBOL DE DIRECTORIOS (Seccion 9):
    - Estilo arbol con sangrias
    - Carpetas principales resaltadas
    - Archivos clave con descripcion breve

6.  DIAGRAMA DE FLUJO DE DATOS (Seccion 4.2):
    - Tres fuentes: API VPN (156.35.163.145), API publica (turismoasturiasprofesional), assets locales
    - Flecha principal hacia RecipeRepository
    - Flecha de fallback desde assets cuando API falla
    - Etiquetas con las URLs concretas

7.  DESTACADO VISUAL DE LA CONDICION EXTRA (Seccion 8.4):
    - Recuadro o nota al margen con el texto literal del profesor
    - Icono de WebView junto al requisito
    - Flecha senalando a RestaurantWebViewFragment como implementacion
