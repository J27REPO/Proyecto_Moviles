# AGENTS.md — Memoria persistente para sesiones con opencode

## Perfil del usuario
- Estudiante de Informatica Movil, Universidad de Oviedo
- Proyecto: Recetas Asturianas (Android/Kotlin)
- Dispositivo test: Samsung Galaxy S21 FE (SM-G990B), Android 14, conectado por USB
- Prefiere espanol para comunicacion
- Quiere respuestas concisas y directas, sin rodeos
- Le gusta que sea autonomo y pruebe cosas en su dispositivo sin pedir permiso cada vez
- Valida siempre con tests reales en dispositivo (no solo compilacion)
- Necesita documentacion detallada para el Word de entrega

## Estado del proyecto (15 Mayo 2026) — COMPLETO para entrega
- MVVM + Repository + Room 2.6.1 + Retrofit 2.9.0 + Moshi 1.15.1
- minSdk 26, targetSdk 35, Kotlin 1.9.22, ViewBinding, Material 3
- Glide 4.16.0, Navigation Component 2.7.7, Splash Screen API
- Barista 4.3.0 + Espresso 3.5.1 para UI tests
- 20 tests unitarios (MappersTest.kt) + 10 tests UI -> TODOS PASAN
- APK ~8.4 MB, probado en SM-G990B real

## Requisitos obligatorios del profesor
- Moshi (NO Gson) — CUMPLIDO
- minSdk 26 — CUMPLIDO
- Espresso/Barista para UI tests — CUMPLIDO
- Documento Word explicativo — lo hace el alumno a mano guiado por doc/MACRO_WORD.md

## Funcionalidades implementadas
- Lista recetas (RecyclerView + Cards + Glide)
- Busqueda (Room SQL LIKE: nombre, restaurante, ingredientes)
- Filtro tiempo (chips: rapido <=30, medio 31-60, largo >60)
- Detalle receta (HTML renderizado, imagenes, ingredientes, preparacion, trucos, notas)
- Favoritos (Room + FAB toggle + Snackbar)
- Preferencias (SettingsFragment: orden, intervalo refresco, ocultar sin tiempo)
- WebView embebido (NO navegador externo — requisito por falta de datos GPS)
- Soporte tablet (layout-sw600dp maestro-detalle)
- Multiidioma (espanol + ingles)
- Splash Screen (Android 12+)
- Modo offline (Room cache + banner + fallback assets)
- Animaciones de navegacion (slide)
- Compartir receta (ACTION_SEND)

## Arquitectura
- MVVM con LiveData, Repository Pattern (singleton)
- Data layer: Room (local) + Retrofit/Moshi (API) + assets/recetas.json (fallback)
- API: 156.35.163.145/json/RecetasCocina.json (requiere VPN universidad)
- Fallback automatico a assets si API falla
- FlexibleAdapterFactory de Moshi para JSON inconsistente (objeto vs array)

## Parseos especiales (Mappers.kt)
- Restaurante: removePrefix("Por ")
- Tiempo: regex horas + minutos -> minutos totales
- URL imagen: JSON groupId/uuid/title -> URL turismoasturias.es
- URL restaurante: extraer href del HTML

## Tests
- Unitarios: `./gradlew test`
- UI: `./gradlew connectedAndroidTest`
- Todo: `./gradlew test connectedAndroidTest`
- Compilar: `./gradlew assembleDebug`

## Documentacion
- PROYECTO_ESTADO.md — Estado completo del proyecto
- MEMORY.md — Memoria tecnica
- plan.md — Plan de correcciones
- CHANGELOG_FALLBACK_API.md — Registro de cambios
- doc/MACRO_WORD.md — Guia para el Word de entrega
- readme.md — Prompt/especificacion original
