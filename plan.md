# Plan de Correcciones — Proyecto Recetas Asturianas

## Objetivo
Cumplir todos los requisitos del profesor y del Campus Virtual para la entrega del trabajo individual de Informatica Movil.

---

## Tareas ejecutadas (orden cronologico)

### 1. Subir minSdk de 21 a 26
- **Archivo**: `app/build.gradle.kts`
- **Cambio**: `minSdk = 21` -> `minSdk = 26`
- **Razon**: Requisito explicito del profesor (tenerEnCuenta.txt) y del campus (API 26+)

### 2. Migrar Gson -> Moshi
- **2a. Dependencias**: Eliminado `converter-gson`, anadido `moshi-kotlin` + `converter-moshi`
- **2b. DTOs**: `@SerializedName` -> `@Json(name=...)`, `JsonDeserializer` -> `FlexibleAdapterFactory`
- **2c. RetrofitClient.kt**: `GsonConverterFactory` -> `MoshiConverterFactory`
- **2d. RecipeRepository.kt**: `Gson()` assets -> Moshi `adapter()`
- **2e. Mappers.kt**: `slideUrl` como `ContentWrapper?`, eliminado `Log.d()`

### 3. Crear documento Word (.docx)
- **Archivo**: `doc/Arquitectura_RecetasAsturianas.docx`
- **Contenido**: Arquitectura MVVM, esquema de clases, tecnologias, decisiones de diseno

### 4. Verificar compilacion
- [x] `./gradlew assembleDebug` -> BUILD SUCCESSFUL
- [x] `./gradlew test` -> 20/20 tests pasan
- [x] `./gradlew clean assembleDebug test` -> todo OK

### 5. Arreglar crash en Ajustes (ListPreference)
- **Archivo**: `SettingsFragment.kt`
- **Problema**: Al cambiar cualquier `ListPreference` (tema, intervalo, orden) la app se cerraba.
- **Causa raiz**: `useSimpleSummaryProvider` + `updateListPreferenceSummary` manual entraban en conflicto. El cambio de tema recreaba la Activity dentro del callback de preferencia.
- **Solucion**: Eliminar listeners redundantes de `refresh_hours` y `default_sort`. El listener del tema ahora envuelve `applyThemeValue()` en `Handler(Looper.getMainLooper()).post {}` para que la recreacion ocurra en el siguiente ciclo del message loop.
- **Pruebas**: Verificado manualmente en dispositivo — las 3 opciones funcionan sin crash.

### 6. Implementar Barista + mejorar tests UI
- **Archivo**: `app/build.gradle.kts`
- **Cambio**: Anadida dependencia `com.adevinta.android:barista:4.3.0` (excluyendo kotlin stdlib)
- **Tests reescritos**:
  - `NavigationTest.kt` (2 tests): Navegacion BottomNavigation, titulo toolbar
  - `RecipeListFragmentTest.kt` (5 tests): Carga lista, click receta, busqueda, filtro, pull-to-refresh
  - `RecipeDetailFragmentTest.kt` (3 tests): Datos detalle, toggle favorito, boton restaurante
- **API Barista**: `clickOn()`, `assertDisplayed()`, `writeTo()` — API mas legible que Espresso puro
- **Mejora robustez**: Esperas condicionales (polling con timeout 30s) en lugar de `Thread.sleep()` fijo
- **Pruebas**: `./gradlew connectedAndroidTest` -> **10/10 tests pasan en SM-G990B**
- **Documentacion**: Actualizados PROYECTO_ESTADO.md, MEMORY.md, CHANGELOG.md, MACRO_WORD.md

---

## Verificacion final contra el contrato

| # | Requisito | Estado | Implementacion |
|---|-----------|--------|----------------|
| 1 | **Fuente datos**: catalogo abierto Principado Asturias | ✅ | 3 URLs (VPN uni + publica + assets) |
| 2 | **Conjunto**: "Recetas de cocina" | ✅ | 78 recetas via Retrofit + Moshi |
| 3 | **Listado**: nombre, imagen, restaurante | ✅ | RecyclerView + Cards + Glide |
| 4 | **Detalle**: ingredientes, preparacion, tiempo, trucos | ✅ | HTML renderizado con HtmlCompat |
| 5 | **Enlace restaurante embebido** (condicion GPS) | ✅ | WebViewClient personalizado |
| 6 | **Busqueda**: por nombre, restaurante e ingredientes | ✅ | Room SQL LIKE (3 campos) |
| 7 | **Filtro tiempo**: rapida, media, larga | ✅ | Chips Material + TimeFilter enum |
| 8 | **Favoritos**: guardar y consultar | ✅ | Room + FAB toggle + Snackbar |
| 9 | **Preferencias**: orden, filtro, ocultar sin tiempo | ✅ | PreferenceFragmentCompat |
| | | | |
| | **Tests unitarios** (MappersTest.kt) | ✅ | 20/20 tests |
| | **Tests UI** (Barista + Espresso) | ✅ | 10/10 tests en SM-G990B |
| | **Ajustes sin crash** (ListPreference) | ✅ | Handler.post() fix |
| | **APK generado** | ✅ | ~8.4 MB |

---

## Archivos modificados

| Archivo | Tipo de cambio |
|---------|----------------|
| `app/build.gradle.kts` | minSdk 21->26; Gson->Moshi; +Barista 4.3.0 |
| `data/remote/dto/RecipeResponse.kt` | Moshi `@Json`, `@JsonClass`, `FlexibleAdapterFactory` |
| `data/remote/dto/Mappers.kt` | `slideUrl: ContentWrapper?`; eliminar `Log.d()` |
| `data/remote/RetrofitClient.kt` | `MoshiConverterFactory` + `FlexibleAdapterFactory` |
| `data/repository/RecipeRepository.kt` | Moshi para assets; eliminar `Gson()` |
| `ui/settings/SettingsFragment.kt` | Fix crash ListPreference con Handler.post() |
| `MainActivity.kt` | Metodos hideToolbar() / showToolbar() |
| `RestaurantWebViewFragment.kt` | Ocultar toolbar en modo telefono |
| `activity_main.xml` + sw600dp | fitsSystemWindows=true |
| `app/src/androidTest/.../NavigationTest.kt` | Reesecrito con Barista + esperas condicionales |
| `app/src/androidTest/.../RecipeListFragmentTest.kt` | Reesecrito con Barista + esperas condicionales |
| `app/src/androidTest/.../RecipeDetailFragmentTest.kt` | Reesecrito con Barista + esperas condicionales |
| `app/src/test/.../MappersTest.kt` | Actualizadas aserciones URL imagenes |
| `PROYECTO_ESTADO.md` | Documentacion exhaustiva del proyecto |
| `MEMORY.md` | Memoria tecnica detallada |
| `CHANGELOG_FALLBACK_API.md` | Registro de cambios |
| `doc/MACRO_WORD.md` | Guia para documento Word |
| `doc/Arquitectura_RecetasAsturianas.docx` | Nuevo — documento Word (pendiente generar) |
| `plan.md` | Este archivo |

---

## Estado actual del proyecto

**TODO COMPLETADO ✅**

La aplicacion cumple con:
- [x] Todos los requisitos funcionales del enunciado
- [x] Todos los requisitos de diseno (MVVM, Room, tests UI, etc.)
- [x] Todos los requisitos del profesor (minSdk 26, Moshi, Espresso, Word)
- [x] Condicion extra: WebView embebido por falta de datos GPS
- [x] 20 tests unitarios + 10 tests UI pasan correctamente
- [x] Compilacion sin errores
- [x] Documentacion completa (6 ficheros .md + guia Word)
