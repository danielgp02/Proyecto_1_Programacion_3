package Proyecto01.servicio;

import Proyecto01.modelo.Plan;
import Proyecto01.modelo.PlanAnual;
import Proyecto01.modelo.PlanMensual;
import Proyecto01.modelo.PlanVIP;
import Proyecto01.persistencia.IPlanDao;

import java.sql.SQLException;
import java.util.List;

/**
 * Catálogo de planes. Implementa el contrato IGestorPlanes y delega la persistencia
 * en un IPlanDao (JDBC contra MySQL) en lugar de mantener los planes en memoria.
 */
public class GestorPlanes implements IGestorPlanes {

    private final IPlanDao dao;

    /**
     * Crea el gestor con su DAO de persistencia de planes.
     *
     * @param dao acceso a datos de los planes (JDBC contra MySQL)
     */
    public GestorPlanes(IPlanDao dao) {
        this.dao = dao;
        inicializarPlanesPorDefecto();
    }

    /**
     * Inserta los tres planes estándar (Mensual, Anual, VIP) en la base de datos
     * si aún no existen, para que el catálogo siempre esté disponible.
     */
    private void inicializarPlanesPorDefecto() {
        try {
            if (dao.buscarPorNombre("Mensual") == null) {
                dao.crear(new PlanMensual());
            }
            if (dao.buscarPorNombre("Anual") == null) {
                dao.crear(new PlanAnual());
            }
            if (dao.buscarPorNombre("VIP") == null) {
                dao.crear(new PlanVIP());
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron inicializar los planes en la base de datos: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public List<Plan> listarPlanes() {
        try {
            return dao.listarTodos();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo leer la lista de planes de la base de datos: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public Plan buscarPlanPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        try {
            Plan plan = dao.buscarPorNombre(nombre);
            if (plan == null) {
                // Si el plan no existe en la BD (por ejemplo, si schema.sql no se ejecutó),
                // lo creamos automáticamente para evitar fallos de clave foránea.
                switch (nombre) {
                    case "Mensual":
                        plan = new PlanMensual();
                        break;
                    case "Anual":
                        plan = new PlanAnual();
                        break;
                    case "VIP":
                        plan = new PlanVIP();
                        break;
                    default:
                        return null;
                }
                dao.crear(plan);
            }
            return plan;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el plan \"" + nombre
                    + "\" en la base de datos: " + e.getMessage(), e);
        }
    }
}
