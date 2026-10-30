# Sistema de Control de Membresías para Gimnasio

Proyecto 2 de **Programación III (EIF206)** — Universidad Nacional de Costa Rica.
Es la evolución del Proyecto 1: el mismo sistema, ahora con persistencia real en **MySQL** por medio de JDBC, hilos de fondo para no congelar la interfaz y sincronización entre instancias por **sockets**.

## Integrantes

- Mariano Martinez Alfaro
- Daniel Granados Perez

## ¿Qué hace el sistema?

Tres pestañas principales:

1. **Control de Acceso** — se escribe el número de socio y el sistema dice si el acceso está **Permitido** o **Denegado por Morosidad**, según la fecha de vencimiento de la membresía.
2. **Mantenimiento de Socios** — CRUD completo de socios (crear, ver, editar, eliminar) contra la base de datos.
3. **Membresías** — asignar un plan (Mensual, Anual o VIP) y cobrar, registrar renovaciones, ver el historial de pagos y los reportes.

## Qué se agregó en el Proyecto 2

| Tema | Detalle |
|---|---|
| Base de datos | Esquema relacional en 3FN con 4 tablas: `usuarios`, `planes`, `membresias`, `pagos` (llaves foráneas, `UNIQUE`, `CHECK`, `ON DELETE CASCADE`). Script en `database/schema.sql`. |
| Persistencia JDBC | Patrón DAO: interfaces `IUsuarioDao`, `IPlanDao`, `IMembresiaDao` + implementaciones `*DaoJDBC`. Los servicios reciben el DAO por constructor (inyección de dependencias). Todas las consultas con datos variables usan `PreparedStatement`. |
| Concurrencia | Toda operación de base de datos de la interfaz corre en un hilo de fondo (`SwingWorker`, ver `vista/EjecutorBD.java`); el resultado vuelve al hilo de Swing para actualizar las tablas. |
| Funcionalidades extra | **Socios morosos** (consulta de membresías vencidas), **historial de pagos** por membresía y **reporte de ingresos por plan** (`JOIN` + `GROUP BY` en SQL). |
| Punto extra: sockets | Paquete `red/`: varias instancias de la aplicación se sincronizan entre sí (ver abajo). |

## Sincronización por sockets (punto extra)

- `ServidorSync` (`ServerSocket`): acepta clientes y retransmite cada aviso a los demás.
- `ClienteSync` (`Socket`): envía y recibe avisos.
- `SincronizadorRed`: la primera instancia que se abre levanta el servidor (puerto 5050) y las demás se conectan. Si el servidor se cierra, otra instancia asume ese rol.
- Cuando una instancia crea, edita o elimina datos, avisa `CAMBIO_DATOS`; las demás vuelven a leer MySQL y refrescan sus tablas sin tocar nada. El estado de la conexión se muestra en el título de la ventana.
- Para usar dos computadoras (ambas conectadas a la misma base de datos), ejecutar la segunda con `-Dsync.host=IP_DE_LA_PRIMERA`. El puerto se cambia con `-Dsync.puerto=NUMERO`.

**Cómo probarlo:** ejecutar `Main` dos veces (en IntelliJ: *Edit Configurations → Allow multiple instances*), crear un socio en una ventana y ver cómo aparece en la otra.

## Tecnologías usadas

- **Java** (JDK 25) y **Swing**
- **MySQL 8** y **JDBC** (`lib/mysql-connector-j-8.0.33.jar`)
- **MySQL Workbench** para crear y explorar el esquema

## Estructura del proyecto

```
src/Proyecto01/
├── Main.java          → punto de entrada
├── modelo/            → clases de datos (Persona, Usuario, Membresia, Pago, Plan...)
├── persistencia/      → interfaces DAO + implementaciones JDBC + ConexionBD
├── servicio/          → lógica de negocio (Gestores, ControlAcceso, Repositorio<T>)
├── red/               → sockets: ServidorSync, ClienteSync, SincronizadorRed
└── vista/             → pantallas Swing + EjecutorBD (hilos de fondo)
database/schema.sql    → script de creación de la base de datos y datos de prueba
```

## Conceptos de POO que aplicamos

- **Herencia:** `Usuario extends Persona`; `PlanMensual`, `PlanAnual` y `PlanVIP` extienden `PlanBase`.
- **Interfaces:** `Plan`, `IGestor*`, `IControlAcceso` y los `I*Dao` separan la lógica del GUI y de la base de datos (Inversión de Dependencias, SOLID).
- **Polimorfismo:** `plan.calcularPrecio()` se resuelve según el tipo de plan.
- **Genéricos:** `Repositorio<T>` se usa como caché de socios.
- **Concurrencia:** `SwingWorker` para la base de datos y hilos para los sockets.

## Cómo correrlo

1. Instalar MySQL 8 y abrir **MySQL Workbench**.
2. Ejecutar `database/schema.sql` para crear `gimnasio_db` con sus tablas y datos de prueba.
3. Revisar usuario y contraseña en `persistencia/ConexionBD.java` (por defecto `root` / `rootroot`, `localhost:3306`).
4. Abrir el proyecto en IntelliJ IDEA, agregar `lib/mysql-connector-j-8.0.33.jar` a las librerías si hace falta y correr `Main.java`.
5. `PruebaConexion.java` sirve para verificar la conexión antes de abrir la interfaz.

## Estado del proyecto

- [x] Estructura base (modelo, servicio, GUI)
- [x] Modelo relacional en MySQL (3FN, llaves foráneas)
- [x] CRUD completo con JDBC y `PreparedStatement`
- [x] Acceso a la base en hilos de fondo
- [x] Funcionalidades extra: morosos, historial de pagos, reporte de ingresos
- [x] Punto extra: sincronización por sockets

## Notas

Este proyecto se hizo usando herramientas de IA como apoyo (permitido según la política del curso), pero todo el código fue revisado y entendido por ambos antes de subirlo.

---
Curso: EIF206 - Programación III · Profesora: Kristel Duarte Pérez
