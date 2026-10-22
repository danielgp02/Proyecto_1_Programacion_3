package Proyecto01.persistencia;

import Proyecto01.modelo.Plan;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de persistencia de planes de membresía.
 * La aplicación no conoce SQL: solo usa esta interfaz; el SQL vive en la
 * implementación JDBC.
 */
public interface IPlanDao {

    /**
     * Inserta un plan nuevo en la base de datos.
     *
     * @param plan plan a insertar (sin id asignado)
     * @return el plan con su id generado por la base de datos
     * @throws SQLException si falla la inserción
     */
    Plan crear(Plan plan) throws SQLException;

    /**
     * Lista todos los planes registrados.
     *
     * @return lista de planes (puede estar vacía)
     * @throws SQLException si falla la consulta
     */
    List<Plan> listarTodos() throws SQLException;

    /**
     * Busca un plan por su identificador interno.
     *
     * @param idPlan identificador interno del plan
     * @return el plan encontrado, o null si no existe
     * @throws SQLException si falla la consulta
     */
    Plan buscarPorId(int idPlan) throws SQLException;

    /**
     * Busca un plan por su nombre.
     *
     * @param nombre nombre del plan (Mensual, Anual, VIP...)
     * @return el plan encontrado, o null si no existe
     * @throws SQLException si falla la consulta
     */
    Plan buscarPorNombre(String nombre) throws SQLException;

    /**
     * Actualiza los datos de un plan existente.
     *
     * @param plan plan con los datos actualizados (debe tener idPlan asignado)
     * @return true si se actualizó al menos una fila
     * @throws SQLException si falla la actualización
     */
    boolean actualizar(Plan plan) throws SQLException;

    /**
     * Elimina un plan por su identificador interno.
     *
     * @param idPlan identificador del plan a eliminar
     * @return true si se eliminó al menos una fila
     * @throws SQLException si falla la eliminación
     */
    boolean eliminar(int idPlan) throws SQLException;
}
