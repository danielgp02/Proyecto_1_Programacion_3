package Proyecto01.servicio;

import Proyecto01.modelo.Plan;
import Proyecto01.modelo.PlanAnual;
import Proyecto01.modelo.PlanMensual;
import Proyecto01.modelo.PlanVIP;

import java.util.List;

/**
 * Catálogo de planes. Implementa el contrato IGestorPlanes y guarda los planes
 * en el mismo repositorio genérico que usan socios y membresías.
 */
public class GestorPlanes implements IGestorPlanes {

    private final Repositorio<Plan> planes;

    /**
     * Carga los tres planes del tema: Mensual, Anual y VIP.
     */
    public GestorPlanes() {
        this.planes = new Repositorio<>();
        planes.agregar(new PlanMensual());
        planes.agregar(new PlanAnual());
        planes.agregar(new PlanVIP());
    }

    @Override
    public List<Plan> listarPlanes() {
        return planes.obtenerTodos();
    }

    @Override
    public Plan buscarPlanPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        return planes.buscar(p -> p.getNombre().equalsIgnoreCase(nombre));
    }
}
