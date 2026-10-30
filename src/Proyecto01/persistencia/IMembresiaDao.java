package Proyecto01.persistencia;

import Proyecto01.modelo.Membresia;
import Proyecto01.modelo.Usuario;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de persistencia de membresías.
 * La aplicación no conoce SQL: solo usa esta interfaz; el SQL vive en la
 * implementación JDBC.
 */
public interface IMembresiaDao {

    /**
     * Crea una membresía nueva en la base de datos.
     *
     * @param membresia membresía a crear (sin id asignado)
     * @return la membresía con su id generado por la base de datos
     * @throws SQLException si falla la inserción
     */
    Membresia crear(Membresia membresia) throws SQLException;

    /**
     * Lista todas las membresías registradas.
     *
     * @return lista de membresías (puede estar vacía)
     * @throws SQLException si falla la consulta
     */
    List<Membresia> listarTodos() throws SQLException;

    /**
     * Busca una membresía por su identificador interno.
     *
     * @param idMembresia identificador interno de la membresía
     * @return la membresía encontrada, o null si no existe
     * @throws SQLException si falla la consulta
     */
    Membresia buscarPorId(int idMembresia) throws SQLException;

    /**
     * Busca la membresía asociada a un usuario.
     *
     * @param usuario socio del que se busca la membresía
     * @return la membresía del usuario, o null si no tiene una
     * @throws SQLException si falla la consulta
     */
    Membresia buscarPorUsuario(Usuario usuario) throws SQLException;

    /**
     * Actualiza los datos de una membresía existente (plan y fecha de vencimiento).
     *
     * @param membresia membresía con los datos actualizados (debe tener idMembresia asignado)
     * @return true si se actualizó al menos una fila
     * @throws SQLException si falla la actualización
     */
    boolean actualizar(Membresia membresia) throws SQLException;

    /**
     * Elimina una membresía por su identificador interno.
     *
     * @param idMembresia identificador de la membresía a eliminar
     * @return true si se eliminó al menos una fila
     * @throws SQLException si falla la eliminación
     */
    boolean eliminar(int idMembresia) throws SQLException;

    /**
     * Lista las membresías cuya fecha de vencimiento es anterior a una fecha dada
     * (útil para detectar morosos).
     *
     * @param fechaLimite fecha límite (excluyente)
     * @return lista de membresías vencidas
     * @throws SQLException si falla la consulta
     */
    List<Membresia> listarVencidas(LocalDate fechaLimite) throws SQLException;

    /**
     * Inserta un pago en la tabla pagos asociado a una membresía.
     *
     * @param idMembresia identificador de la membresía
     * @param fecha fecha del pago
     * @param monto monto del pago
     * @return true si se insertó al menos una fila
     * @throws SQLException si falla la inserción
     */
    boolean insertarPago(int idMembresia, java.time.LocalDate fecha, double monto) throws SQLException;

    /**
     * Calcula los ingresos acumulados por cada plan (suma de los pagos de sus membresías).
     *
     * @return una fila por plan: {nombre del plan (String), cantidad de pagos (Long), total cobrado (Double)},
     *         ordenadas de mayor a menor ingreso
     * @throws SQLException si falla la consulta
     */
    List<Object[]> ingresosPorPlan() throws SQLException;
}
