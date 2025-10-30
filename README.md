## equa-notepad-plats

Esta aplicación nace a partir de una necesidad común entre los estudiantes: poder centralizar y organizar de forma eficiente todos los conocimientos teóricos adquiridos durante las clases de matemáticas. En muchas ocasiones, los alumnos se enfrentan a la dificultad de recordar una gran cantidad de fórmulas, definiciones y conceptos fundamentales, especialmente durante la preparación para evaluaciones o trabajos académicos.
La propuesta de esta aplicación es ofrecer una herramienta intuitiva y accesible que permita almacenar fórmulas matemáticas de manera rápida, práctica y estructurada. Al estar disponible desde un dispositivo móvil, se convierte en un recurso útil para el estudio activo, permitiendo al usuario consultar sus apuntes en cualquier momento y lugar, sin depender de libros o cuadernos físicos.
Uno de los aspectos más destacados de esta herramienta es la posibilidad de exportar el contenido en diferentes formatos, lo cual facilita tener una versión impresa y organizada de todo el material recopilado. Esta funcionalidad es ideal para quienes prefieren estudiar en físico o necesitan presentar sus fórmulas en un entorno académico.
Además, la aplicación promueve el orden y la personalización del estudio, ya que permite al usuario crear carpetas temáticas, agrupar fórmulas por áreas (como álgebra, geometría, cálculo, etc.) y establecer conexiones entre ellas. Esto no solo mejora la comprensión del contenido, sino que también fomenta hábitos de estudio más organizados y eficientes.

Descripción detallada
---------------------
Esta aplicación permite a los usuarios crear, almacenar y sincronizar notas (incluyendo imágenes). Usa una arquitectura moderna de Android (Compose, ViewModel, Room/DataStore) y se integra con servicios externos para autenticación y backend en la nube.

Componentes principales
- UI: Jetpack Compose (composable screens y componentes reutilizables).
- Persistencia local: Room (entidades y DAOs) y DataStore para preferencias.
- Lógica de negocio: ViewModels (AndroidX Lifecycle) y coroutines para concurrencia.
- Autenticación / Backend: Supabase (auth, postgrest, realtime) como backend en la nube.
- Autenticación nativa con Google: Google Identity / Play Services para obtener ID tokens y credenciales.
- Carga de recursos remotos: Coil para imágenes y Google Fonts para tipografías (si se usan dinámicamente).

Requisitos
- JDK 11
- Android SDK (configurar `sdk.dir` en `local.properties`)
- Android Studio (recomendado) o Gradle wrapper (`gradlew` / `gradlew.bat`)

Clonar y compilar rápidamente
----------------------------
1. Clona el repositorio:

```powershell
git clone https://github.com/angcoder-c/equa-notepad-plats.git
cd equa-notepad-plats
```

2. Compilar debug (Windows PowerShell):

```powershell
.\gradlew.bat assembleDebug
```

Configuración local 
- El proyecto lee configuraciones privadas desde `local.properties` (Data privada).
- Valores esperados:
  - `sdk.dir` — ruta del Android SDK
  - `WEB_CLIENT_ID`, `ANDROID_CLIENT_ID` — OAuth client IDs de Google
  - `SUPABASE_URL`, `SUPABASE_API_KEY`, `SUPABASE_AUTH_CALLBACK_URL` — configuraciones de Supabase

Dependencias principales y para qué sirven
----------------------------------------
- com.google.android.gms:play-services-auth
  - Proporciona utilidades de autenticación con Google Play Services. Utilizado para flujos de sign-in y consent screens.
- com.google.android.libraries.identity.googleid:googleid
  - Librería para obtener credenciales y tokens de identidad (Google ID token credential).
- androidx.credentials (credentials + credentials-play-services-auth)
  - API de Credentials para gestionar credenciales y facilitar el sign-in con proveedores (integración con Play Services).
- io.github.jan-tennert.supabase (auth-kt, postgrest-kt, realtime-kt, compose-auth)
  - SDK Kotlin para comunicarse con Supabase (autenticación, consultas REST a PostgREST y canales realtime).
- io.ktor:ktor-client-okhttp (+ core, content-negotiation, serialization)
  - Cliente HTTP que el SDK de Supabase usa para hacer requests; OkHttp es el engine que maneja conexiones HTTP/HTTPS.
- io.coil-kt:coil-compose
  - Carga imágenes de URLs remotas de forma eficiente y las muestra en composables.
- AndroidX Compose, Lifecycle, Room, DataStore, Coroutines
  - Proporcionan UI moderna, gestión de estado, persistencia local y concurrencia.

Servicios que consumen datos de Internet 
------------------------------------------------------
En el proyecto hay varias piezas que realizan comunicación de red. A continuación se enlistan y explican sin incluir datos sensibles.

1) Google Sign-In / Google Identity / Play Services
   - Qué es: Servicios de Google que permiten autenticar usuarios mediante sus cuentas de Google y obtener un ID token o credenciales.
   - Bibliotecas relacionadas: `play-services-auth`, `googleid`, `androidx.credentials`.
   - Cómo y cuándo se usa: Cuando el usuario pulsa "Acceder con Google" se inicia un flujo que comunica con los servidores de Google para presentar cuentas disponibles y devolver un ID token firmado por Google.
   - Para qué se usa el dato: El ID token obtenido se envía al backend (Supabase) para crear/validar la sesión del usuario en el servicio propio.

2) Supabase (Auth, PostgREST, Realtime)
   - Qué es: Backend-as-a-Service que ofrece autenticación, base de datos (Postgres a través de PostgREST) y canales en tiempo real.
   - Bibliotecas relacionadas: `supabase-auth`, `postgrest-kt`, `realtime-kt`.
   - Cómo y cuándo se usa: Se utiliza después de autenticarse esta app realiza solicitudes HTTP(S) a la API de Supabase para leer/escribir notas, obtener metadatos y suscribirse a canales realtime.
   - Para qué se usa el dato: Guarda y sincroniza notas entre dispositivo y servidor; maneja sesiones de usuario y actualizaciones en tiempo real.

3) Ktor + OkHttp (cliente HTTP)
   - Qué es: Cliente HTTP (Ktor) con motor OkHttp para conexiones.
   - Cómo y cuándo se usa: Todas las llamadas REST hacia Supabase (o cualquier otra API externa) pasan por Ktor/OkHttp.
   - Para qué se usa el dato: Envía y recibe JSON/HTTP para operaciones CRUD y autenticación.

4) Coil (carga de imágenes)
   - Qué es: Biblioteca de carga de imágenes en Kotlin/Compose.
   - Cómo y cuándo se usa: Cuando la app muestra imágenes remotas (p. ej. fotos adjuntas a notas), Coil descarga esas imágenes por HTTP(S).
   - Para qué se usa el dato: Mostrar recursos multimedia remotos dentro de la UI.

5) Google Fonts (Compose UI Text Google Fonts)
   - Qué es: Acceso a tipografías alojadas en Google Fonts.
   - Cómo y cuándo se usa: Si la app solicita fuentes descargables en tiempo de ejecución, la librería puede descargar las fuentes desde la CDN de Google.
   - Para qué se usa el dato: Mejorar la tipografía y experiencia visual sin empaquetar todas las fuentes en el APK.

6) Otros componentes de Play Services
   - Notas: Dependencias adicionales (FIDO/identity-credentials) pueden incluir llamadas para verificación o gestión de credenciales. Aunque no siempre se usan directamente, sus componentes pueden activar comunicación con Google.

Permisos que habilitan red
--------------------------
- `android.permission.INTERNET` — necesario para cualquier comunicación HTTP/HTTPS.
- `android.permission.ACCESS_NETWORK_STATE` — usado para consultar estado de conexión.

Recursos y archivos de referencia
--------------------------------
- `app/build.gradle.kts` — dependencias y BuildConfig placeholders.
- `app/src/main/AndroidManifest.xml` — permisos e `intent-filter` para callbacks.
- `app/proguard-rules.pro` — reglas para mantener clases necesarias.

