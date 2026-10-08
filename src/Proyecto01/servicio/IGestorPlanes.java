package Proyecto01.servicio;

import Proyecto01.modelo.Plan;

import java.util.List;

/**
 * Contrato del catálogo de planes del gimnasio.
 */
public interface IGestorPlanes {

    /**
     * Lista los planes disponibles.
     *
     * @return planes Mensual, Anual y VIP
     */
    List<Plan> listarPlanes();

    /**
     * Busca un plan por su nombre.
     *
     * @param nombre nombre del plan
     * @return el plan, o null si no existe
     */
    Plan buscarPlanPorNombre(String nombre);
}
