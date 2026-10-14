# Cajero Electrónico — Android App

Aplicación Android desarrollada en **Kotlin** para simular las operaciones básicas de un cajero electrónico: consignaciones, retiros y persistencia del saldo entre sesiones.

Proyecto correspondiente a la **Tarea 3 - Unidad 2** del curso de desarrollo de aplicaciones móviles, enfocado en los conceptos de **Programación Orientada a Objetos** y **almacenamiento de información** con `SharedPreferences`.

---

## Tabla de contenido

- [Características](#características)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Clases principales](#clases-principales)
- [Flujo de la aplicación](#flujo-de-la-aplicación)
- [Requisitos técnicos](#requisitos-técnicos)
- [Configuración del entorno](#configuración-del-entorno)
- [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
- [Posibles mejoras y escalabilidad](#posibles-mejoras-y-escalabilidad)
- [Decisiones de diseño](#decisiones-de-diseño)

---

## Características

- Consulta del saldo disponible en pantalla principal
- Consignación de dinero con validación de monto positivo
- Retiro de dinero con validación de saldo suficiente
- Persistencia automática del saldo con `SharedPreferences`
- Historial de transacciones de la sesión actual
- Mensajes visuales de éxito y error con colores diferenciados
- Interfaz responsive compatible con diferentes tamaños de pantalla

---

## Arquitectura

El proyecto sigue un patrón de **separación de responsabilidades** en tres capas:

```
┌─────────────────────────────┐
│         UI Layer            │  MainActivity.kt + layouts XML
│  (Presentación / Vista)     │
└─────────────┬───────────────┘
              │ llama a
┌─────────────▼───────────────┐
│       Domain Layer          │  CuentaBancaria.kt + Transaccion
│  (Lógica de negocio)        │
└─────────────┬───────────────┘
              │ persiste en
┌─────────────▼───────────────┐
│        Data Layer           │  BalanceRepository.kt
│  (Almacenamiento de datos)  │  SharedPreferences
└─────────────────────────────┘
```

Esta separación facilita el mantenimiento, las pruebas unitarias y la evolución del proyecto sin afectar capas no relacionadas.

---

## Estructura del proyecto

```
CajeroElectronico/
├── README.md                               ← Este archivo
├── build.gradle                            ← Config raíz (AGP + Kotlin version)
├── settings.gradle                         ← Módulos del proyecto
├── gradle.properties                       ← Propiedades globales de Gradle
├── gradle/wrapper/
│   └── gradle-wrapper.properties          ← Versión de Gradle (9.3.0)
└── app/
    ├── build.gradle                        ← Dependencias y config del módulo
    ├── proguard-rules.pro                  ← Reglas de ofuscación (release)
    └── src/main/
        ├── AndroidManifest.xml             ← Declaración de actividades y permisos
        ├── java/com/example/cajeroelectronico/
        │   ├── CuentaBancaria.kt           ← Dominio: lógica de cuenta y transacciones
        │   ├── BalanceRepository.kt        ← Datos: persistencia con SharedPreferences
        │   └── MainActivity.kt             ← UI: controlador principal
        └── res/
            ├── layout/
            │   ├── activity_main.xml       ← Layout principal (ScrollView)
            │   └── item_transaccion.xml    ← Ítem del historial de transacciones
            ├── drawable/
            │   ├── bg_card_primary.xml     ← Fondo degradado verde (tarjeta saldo)
            │   ├── bg_card_white.xml       ← Fondo blanco con borde (tarjetas)
            │   ├── bg_mensaje.xml          ← Fondo mensaje de éxito
            │   ├── bg_mensaje_error.xml    ← Fondo mensaje de error
            │   ├── bg_item_transaccion.xml ← Fondo ítem historial
            │   ├── bg_icono_transaccion.xml← Fondo circular ícono historial
            │   ├── ic_launcher_background.xml
            │   └── ic_launcher_foreground.xml
            ├── mipmap-*/                   ← Íconos de la app (hdpi a xxxhdpi)
            └── values/
                ├── strings.xml             ← Textos de la UI
                ├── colors.xml              ← Paleta de colores
                └── themes.xml              ← Tema Material Components
```

---

## Clases principales

### `CuentaBancaria.kt`

Clase de dominio que encapsula el estado y la lógica de la cuenta bancaria.

| Elemento | Tipo | Descripción |
|---|---|---|
| `saldo` | `Double` (val) | Saldo actual, solo lectura desde fuera |
| `historial` | `List<Transaccion>` (val) | Lista inmutable de transacciones de la sesión |
| `consignar(monto)` | `Result<Double>` | Deposita dinero; falla si monto ≤ 0 |
| `retirar(monto)` | `Result<Double>` | Retira dinero; falla si monto ≤ 0 o supera el saldo |

El uso de `Result<T>` permite manejar éxito y error sin excepciones no controladas, siguiendo el estilo idiomático de Kotlin.

### `Transaccion.kt` (data class dentro de CuentaBancaria.kt)

| Campo | Tipo | Descripción |
|---|---|---|
| `tipo` | `String` | `"CONSIGNACIÓN"` o `"RETIRO"` |
| `monto` | `Double` | Valor de la operación |
| `saldoResultante` | `Double` | Saldo después de la operación |

### `BalanceRepository.kt`

Repositorio que abstrae el acceso a `SharedPreferences`.

| Método | Descripción |
|---|---|
| `guardarSaldo(saldo)` | Persiste el saldo como `Long` (bits del Double) para máxima precisión |
| `obtenerSaldo()` | Recupera el saldo; retorna `0.0` si no existe valor guardado |
| `tieneSaldoGuardado()` | Indica si es la primera ejecución de la app |

> **¿Por qué guardar como Long?** `SharedPreferences` no tiene soporte nativo para `Double`. Convertir a bits con `Double.doubleToRawLongBits()` garantiza precisión decimal exacta, a diferencia de guardarlo como `String` o `Float`.

### `MainActivity.kt`

Controlador principal que coordina la UI con el dominio y la persistencia.

| Método | Descripción |
|---|---|
| `onCreate()` | Inicializa binding, datos y listeners |
| `onPause()` | Persiste el saldo al salir o minimizar la app |
| `inicializarDatos()` | Crea `BalanceRepository` y `CuentaBancaria` con saldo recuperado |
| `procesarOperacion(esConsignacion)` | Valida entrada, llama al dominio, actualiza UI |
| `actualizarSaldoUI()` | Refresca el TextView del saldo |
| `mostrarMensaje(mensaje, esError)` | Muestra feedback visual con colores según resultado |
| `agregarItemHistorial(transaccion)` | Infla y agrega una fila al historial en pantalla |
| `formatearMonto(valor)` | Formatea Double como moneda: `$1,500,000.50` |

---

## Flujo de la aplicación

```
Inicio de app
     │
     ▼
onCreate() → BalanceRepository.obtenerSaldo()
     │              │
     │         SharedPreferences
     │              │ saldo guardado (o 0.0)
     ▼
CuentaBancaria(saldoGuardado)
     │
     ▼
Mostrar saldo en pantalla
     │
     ├── Usuario ingresa monto y toca CONSIGNAR
     │       │
     │       ▼
     │   procesarOperacion(esConsignacion=true)
     │       │
     │       ├── Validar campo vacío
     │       ├── Validar monto > 0
     │       └── CuentaBancaria.consignar(monto)
     │               │
     │               ├── Result.success → guardarSaldo() + actualizar UI
     │               └── Result.failure → mostrar error
     │
     ├── Usuario ingresa monto y toca RETIRAR
     │       │
     │       ▼
     │   procesarOperacion(esConsignacion=false)
     │       │
     │       ├── Validar campo vacío
     │       ├── Validar monto > 0
     │       └── CuentaBancaria.retirar(monto)
     │               │
     │               ├── Result.success → guardarSaldo() + actualizar UI
     │               └── Result.failure → mostrar "Saldo insuficiente"
     │
     └── App pasa a segundo plano
             │
             ▼
         onPause() → BalanceRepository.guardarSaldo(cuenta.saldo)
```

---

## Requisitos técnicos

| Componente | Versión |
|---|---|
| Android Studio | Hedgehog 2023.1.1 o superior |
| Gradle | 9.3.0 |
| Android Gradle Plugin (AGP) | 8.7.3 |
| Kotlin | 2.0.21 |
| `compileSdk` | 35 |
| `minSdk` | 24 (Android 7.0) |
| `targetSdk` | 35 |
| Java compatibility | VERSION_17 |

### Dependencias

```gradle
implementation 'androidx.core:core-ktx:1.13.1'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
```

---

## Configuración del entorno

### Propiedades requeridas en `gradle.properties`

```properties
android.useAndroidX=true        # Habilita las librerías AndroidX
kotlin.code.style=official
android.nonTransitiveRClass=true
org.gradle.jvmargs=-Xmx2048m
```

> `android.useAndroidX=true` es obligatorio. Sin esta propiedad el build falla con error de `debugRuntimeClasspath`.

---

## Cómo ejecutar el proyecto

### Desde Android Studio

1. Abre Android Studio → **File → Open** → selecciona la carpeta `CajeroElectronico`
2. Espera que Gradle sincronice (**File → Sync Project with Gradle Files**)
3. Conecta un dispositivo físico o inicia un emulador
4. Presiona **▶ Run** (`Shift + F10`)

### Conectar dispositivo físico (Xiaomi / MIUI)

1. **Ajustes → Acerca del teléfono** → toca **Versión de MIUI** 7 veces
2. **Ajustes → Ajustes adicionales → Opciones de desarrollador**
3. Activa **Depuración USB** e **Instalar vía USB**
4. Conecta el cable USB y selecciona **Transferencia de archivos (MTP)**
5. Acepta el permiso de depuración en el teléfono
6. Verifica con `adb devices` en la terminal

---

## Posibles mejoras y escalabilidad

### Corto plazo

- [ ] Agregar pantalla de PIN/contraseña para proteger el acceso
- [ ] Persistir el historial de transacciones (actualmente solo vive en memoria durante la sesión)
- [ ] Agregar animaciones en las transacciones exitosas
- [ ] Soporte para modo oscuro (`Night` theme)

### Mediano plazo

- [ ] **Migrar a Room Database** para reemplazar `SharedPreferences` y soportar historial persistente con consultas
- [ ] **Arquitectura MVVM** — extraer la lógica de UI hacia un `ViewModel` para separar mejor las responsabilidades y sobrevivir rotaciones de pantalla
- [ ] Múltiples tipos de cuenta (ahorros, corriente)
- [ ] Límites diarios de retiro configurables

### Largo plazo

- [ ] **Inyección de dependencias con Hilt** para desacoplar la construcción de `BalanceRepository`
- [ ] **API REST** — reemplazar `SharedPreferences` por llamadas a un servidor real con Retrofit
- [ ] **Autenticación con biometría** (huella dactilar) usando `BiometricPrompt`
- [ ] Pruebas unitarias con JUnit para `CuentaBancaria` y pruebas de integración con Espresso para la UI

---

## Decisiones de diseño

**¿Por qué `Result<T>` en lugar de excepciones?**
Hace explícito en la firma del método que la operación puede fallar, sin necesidad de bloques `try/catch` en el llamador. Es el patrón idiomático de Kotlin para operaciones que pueden tener dos caminos: éxito o error conocido.

**¿Por qué `SharedPreferences` y no Room?**
El requisito de la tarea especifica `SharedPreferences` explícitamente. Para un solo valor numérico (el saldo), es la solución correcta y suficiente. Room sería la mejora natural cuando se necesite persistir el historial completo.

**¿Por qué guardar el saldo en `onPause()` y no solo al cerrar?**
`onDestroy()` no siempre se llama (el sistema puede matar el proceso). `onPause()` se ejecuta siempre que la app pierde el foco, garantizando que el saldo se guarde incluso si el sistema termina la app abruptamente.

**¿Por qué `ViewBinding` y no `findViewById`?**
`ViewBinding` es nulo-seguro y tipado en tiempo de compilación. Elimina los errores de `ClassCastException` y `NullPointerException` en tiempo de ejecución que son comunes con `findViewById`.

---

*Desarrollado como práctica académica — Tarea 3, Unidad 2*
