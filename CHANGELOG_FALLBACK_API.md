# Registro de Cambios: Implementacion de Fallback y Migracion a Moshi

Este documento resume las modificaciones realizadas para asegurar la carga de recetas
desde la API de la universidad y el cumplimiento de los requisitos del profesor.

## 1. Logica de Fallback y Persistencia
- **`RecipeRepository.kt`**: 
    - Se mejoro la funcion `refreshRecipes` para que intente cargar desde la API remota.
    - Si la API falla (por red o VPN) o si devuelve una lista vacia, el sistema carga
      automaticamente desde `app/src/main/assets/recetas.json`.
    - Los datos se guardan en Room para permitir el acceso offline posterior.
- **`recetas.json`**: Actualizado con la ultima version de datos.

## 2. Fuente de Datos y Configuracion de Red

### Endpoints disponibles
| Tipo | URL | Estado |
|------|-----|--------|
| Principal (VPN Universidad) | `http://156.35.163.145/json/RecetasCocina.json` | Activo por defecto |
| Alternativa publica | `https://www.turismoasturiasprofesional.es/open-data/` | Configurado, no activo |
| Fallback local | `app/src/main/assets/recetas.json` | Usado si API falla |

### Configuracion de Red
- **`RetrofitClient.kt`**:
    - URL base activa: `http://156.35.163.145/json/` (HTTP, requiere cleartext).
    - URL publica catalogada: `https://www.turismoasturiasprofesional.es/open-data/` (HTTPS).
    - OkHttp optimizado: timeouts de 30s y logs detallados (`BODY`).
- **`RecipeApiService.kt`**: Endpoint `RecetasCocina.json`.
- **`AndroidManifest.xml`**: Habilitado `android:usesCleartextTraffic="true"`.
- **`network_security_config.xml`**: Creado para permitir trafico HTTP a la IP de la universidad.

### Condicion Extra: WebView Embebido (sustituye a datos GPS)
Dado que el conjunto de datos "Recetas de cocina" no incluye coordenadas GPS, se añadio
la condicion de que los enlaces a los restaurantes se abran DENTRO de la aplicacion,
no en el navegador externo. Implementado en `RestaurantWebViewFragment.kt`:
- `WebViewClient` personalizado que devuelve `false` en `shouldOverrideUrlLoading`.
- Comentario explicito en el codigo: "requisito del profesor".
- ProgressBar de carga y manejo de errores.

## 3. Migracion de Gson a Moshi
- **`build.gradle.kts`**:
    - Eliminado `converter-gson`.
    - Anadido `com.squareup.moshi:moshi-kotlin:1.15.1`.
    - Anadido `com.squareup.retrofit2:converter-moshi:2.9.0`.
- **`RecipeResponse.kt`**:
    - Sustituidas anotaciones `@SerializedName` por `@Json(name=...)`.
    - Eliminados `JsonDeserializer` de Gson.
    - Creado `FlexibleAdapterFactory` (Moshi `JsonAdapter.Factory`) que maneja
      campos que pueden ser objeto o array JSON (BEGIN_ARRAY -> primer elemento).
    - Se registran adaptadores para `ContentWrapper`, `VisualizadorWrapper`,
      `InformacionWrapper`, `ContactoWrapper`, `ObservacionesWrapper`.
- **`RetrofitClient.kt`**:
    - `GsonConverterFactory.create()` reemplazado por `MoshiConverterFactory.create(moshi)`.
- **`RecipeRepository.kt`**:
    - `Gson().fromJson(...)` reemplazado por `moshi.adapter(...).fromJson(...)`.
- **`Mappers.kt`**:
    - `SlideWrapper.slideUrl` cambiado de `String?` a `ContentWrapper?`.
    - Eliminado `Log.d()` que causaba error en tests unitarios JVM.

## 4. Correccion de SDK Minimo
- **`build.gradle.kts`**: `minSdk` actualizado de 21 a 26, cumpliendo el requisito del profesor.

## 5. Mejoras en la Experiencia de Usuario (UX)
- **`RecipeListViewModel.kt`**:
    - La busqueda se inicializa con string vacio para carga inmediata.
    - Se elimino restriccion de red "no validada" (comun en VPN).
- **`NetworkUtils.kt`**: Relajada comprobacion de red para VPN.

## 6. Correccion critica: KotlinJsonAdapterFactory en Moshi
- **`RetrofitClient.kt`**: Se anadio `.add(KotlinJsonAdapterFactory())` al `Moshi.Builder()`.
  El `FlexibleAdapterFactory` necesita un adaptador base al que delegar el parseo de tipos simples
  (String, Int, etc.) y clases sin `@JsonClass(generateAdapter = true)`. Sin el `KotlinJsonAdapterFactory`,
  `moshi.nextAdapter()` lanzaba error y la app no cargaba ninguna receta.
- **`RecipeRepository.kt`**: Misma correccion para el Moshi usado en el fallback a assets.

## 7. Correcciones de UI
- **`activity_main.xml`**: Anadido `android:fitsSystemWindows="true"` para que el contenido no se solape con la barra de estado del sistema.
- **`layout-sw600dp/activity_main.xml`**: Misma correccion para el layout tablet.
- **`MainActivity.kt`**: Anadidos metodos `hideToolbar()` y `showToolbar()` para gestionar fragments que tienen su propia toolbar.
- **`RestaurantWebViewFragment.kt`**: En modo telefono se oculta la toolbar principal de la actividad cuando se muestra el WebView, evitando asi la barra de titulo duplicada. En modo tablet se mantiene visible porque el WebView esta en un panel separado.

## 8. Correccion de crash en Ajustes (ListPreference)
- **`SettingsFragment.kt`**: Las preferencias de tipo `ListPreference` (tema, intervalo de refresco, orden) causaban un crash instantaneo al seleccionar cualquier opcion distinta al valor por defecto.
  - **Causa raiz**: Conflicto entre el `useSimpleSummaryProvider="true"` del XML y la llamada manual a `updateListPreferenceSummary()` en el `setOnPreferenceChangeListener`. Ademas, el cambio de tema llamaba `AppCompatDelegate.setDefaultNightMode()` dentro del callback de la preferencia, lo que recreaba la Activity y destruia el fragment mientras el callback aun se ejecutaba.
  - **Solucion**: Eliminados los listeners redundantes de `refresh_hours` y `default_sort` (el `useSimpleSummaryProvider` ya actualiza los sumarios). El listener del tema ahora envuelve `applyThemeValue()` en `Handler(Looper.getMainLooper()).post {}` para que la recreacion ocurra en el siguiente ciclo del message loop.
- **Resultado**: Las tres opciones de lista funcionan sin crash. El interruptor `hide_no_time` ya funcionaba correctamente.

## 9. Estabilidad y Pruebas
- **`build.gradle.kts`**: Dependencia `org.json` para tests unitarios JVM.
- **`build.gradle.kts`**: Añadido `com.adevinta.android:barista:4.3.0` como wrapper de Espresso para tests UI más legibles y robustos.
- **`MappersTest.kt`**: Actualizadas aserciones de URL de imagenes.
- **`RecipeListFragmentTest.kt`**: Reesecrito con Barista (`clickOn`, `assertDisplayed`, `writeTo`) + espera condicional (polling con timeout 30s) en lugar de `Thread.sleep()` fijo. 5 tests: display lista, click receta, busqueda, filtro, pull-to-refresh.
- **`RecipeDetailFragmentTest.kt`**: Reesecrito con Barista, espera condicional para navegar al detalle. 3 tests: datos detalle, toggle favorito, boton restaurante.
- **`NavigationTest.kt`**: Reesecrito con Barista, espera condicional para carga de lista. 2 tests: navegacion BottomNavigation, titulo toolbar.
- **Beneficio de Barista**: Scroll automatico antes de interactuar, soporte NestedScrollView, API fluida (`clickOn(R.id.btn)` en vez de `onView(withId(R.id.btn)).perform(click())`), esperas integradas en `assertDisplayed()`.
- **Validacion**: **20/20 tests unitarios + 10/10 tests UI (Barista+Espresso) en SM-G990B (Android 14) pasan correctamente.**

## 10. Implementacion Barista 4.3.0
- **Motivacion**: Los tests originales con Espresso puro usaban `Thread.sleep()` para esperas, lo cual es fragil y ralentiza los tests. Ademas, la sintaxis de Espresso (`onView(withId(...)).perform(click())`) es verbosa.
- **Solucion**: Añadido `com.adevinta.android:barista:4.3.0` que proporciona:
  - API fluida: `clickOn(R.id.btn)`, `assertDisplayed(R.id.view)`, `writeTo(R.id.edit, "text")`
  - Scroll automatico antes de interactuar (soporta NestedScrollView, que Espresso no maneja)
  - Esperas condicionales integradas en `assertDisplayed()` (no requiere `IdlingResource` explicito)
  - Coexiste con Espresso para operaciones avanzadas (RecyclerViewActions, matchers complejos)
- **Dependencia**: `androidTestImplementation("com.adevinta.android:barista:4.3.0") { exclude(group = "org.jetbrains.kotlin") }`
- **Archivos modificados**: 3 ficheros de test + build.gradle.kts
