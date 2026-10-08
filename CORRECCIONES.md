# Correcciones del Proyecto 1 — Control de membresía para gimnasio

Se atendieron los comentarios de la hoja de evaluación, excepto exposición y GitHub.

## Organización (el registro ya no vive en el modelo)
- Se eliminó el singleton `RegistroUsuarios`.
- Los socios se guardan en `GestorUsuarios` con `Repositorio<Usuario>`.
- Las pantallas reciben los servicios desde `VentanaPrincipal`; ya no consultan el modelo para almacenar datos.

## Capas que estaban creadas y no se usaban
- `GestorUsuarios` es el que registra, edita, elimina y busca socios.
- `ControlAcceso` es el que decide la entrada en `SistemaAcceso`.
- `IGestorPlanes` ahora tiene implementación (`GestorPlanes`) y la pestaña de membresías carga los planes desde ahí.
- El repositorio genérico se usa para socios, membresías y planes.

## Bug al editar un socio
- `Usuario` implementa `equals` y `hashCode` por número de socio.
- Editar ya no reemplaza el objeto: `actualizarUsuario` modifica la misma instancia, así la membresía sigue encontrando al socio.

## Flujo cobro → vencimiento → acceso
- Asignar plan o renovar llama a `setPagoAlDia` según `membresia.estaAlDia()`.
- La pantalla de acceso no usa la casilla manual. Usa `ControlAcceso`, que revisa la fecha de vencimiento.
- La casilla de Mantenimiento de Socios queda de solo lectura: el estado lo define el cobro.
- Datos de prueba: María (101, Mensual) y Lucía (103, Anual) entran; Carlos (102) y Jorge (104) no tienen membresía.

## No incluido, como se pidió
- Presentación y defensa.
- Prefijos de commits en GitHub (`Faltan prefijos`).
