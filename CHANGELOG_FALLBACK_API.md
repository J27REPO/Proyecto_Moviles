# Registro de Cambios: Implementación de Fallback y Corrección de API

Este documento resume las modificaciones realizadas para asegurar la carga de recetas desde la API de la universidad o el archivo local `recetas.json`.

## 1. Lógica de Fallback y Persistencia
- **`RecipeRepository.kt`**: 
    - Se mejoró la función `refreshRecipes` para que intente cargar desde la API remota.
    - Si la API falla (por red o VPN) o si devuelve una lista vacía, el sistema carga automáticamente desde `app/src/main/assets/recetas.json`.
    - Los datos se guardan en Room para permitir el acceso offline posterior.
- **`recetas.json`**: Se actualizó el archivo en los assets con la última versión proporcionada.

## 2. Configuración de Red (VPN Universidad)
- **`RetrofitClient.kt`**:
    - Se configuró la URL base como `http://156.35.163.145/json/` (HTTP).
    - Se optimizó el cliente OkHttp para mayor tiempo de respuesta (30s) y logs detallados (`BODY`).
- **`RecipeApiService.kt`**: Se actualizó el endpoint a `RecetasCocina.json`.
- **`AndroidManifest.xml`**:
    - Se habilitó `android:usesCleartextTraffic="true"`.
    - Se añadió una referencia a `network_security_config.xml`.
- **`network_security_config.xml`**: Creado para permitir explícitamente tráfico HTTP a la IP de la universidad.

## 3. Robustez en el Parseo JSON (Solución BEGIN_ARRAY vs BEGIN_OBJECT)
- **`RecipeResponse.kt`**:
    - Se implementaron **Deserializadores Personalizados** (`JsonDeserializer`) para manejar inconsistencias en el JSON de la API.
    - El sistema ahora detecta si un campo (como `Imagen`) viene como un Objeto o como un Array. Si es un Array, extrae automáticamente el primer elemento.
    - Se solucionaron errores de `ClassCastException` y `JsonSyntaxException` que bloqueaban la carga de recetas específicas (como la receta nº 62).

## 4. Mejoras en la Experiencia de Usuario (UX)
- **`RecipeListViewModel.kt`**:
    - Se inicializó el estado de búsqueda con un string vacío para asegurar que la lista se cargue inmediatamente al abrir la app.
    - Se eliminó la restricción que impedía el refresco inicial si Android detectaba la red como "no validada" (común en entornos VPN).
- **`NetworkUtils.kt`**: Se relajó la comprobación de red para ser compatible con conexiones VPN que no reportan acceso total a internet.

## 5. Estabilidad y Pruebas
- **`build.gradle.kts`**: Se añadió la dependencia `org.json` para permitir la ejecución de tests unitarios de los Mappers en entorno JVM.
- **Validación**: La aplicación compila correctamente y supera los tests unitarios.
