package Proyecto01.persistencia;

import Proyecto01.modelo.Plan;
import Proyecto01.modelo.PlanAnual;
import Proyecto01.modelo.PlanMensual;
import Proyecto01.modelo.PlanVIP;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de IPlanDao. Toda la SQL de la tabla planes vive aquí.
 * Todas las consultas con datos variables usan PreparedStatement para prevenir
 * inyección SQL.
 */
public class PlanDaoJDBC implements IPlanDao {

    private final ConexionBD conexion = new ConexionBD();

    @Override
    public Plan crear(Plan plan) throws SQLException {
        String sql = "INSERT INTO planes (nombre, precio_base, beneficios, duracion_dias) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, plan.getNombre());
            ps.setDouble(2, plan.calcularPrecio());
            ps.setString(3, plan.getBeneficios());
            ps.setInt(4, plan.getDuracionDias());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    plan.setIdPlan(generatedKeys.getInt(1));
                }
            }
            return plan;
        }
    }

    @Override
    public List<Plan> listarTodos() throws SQLException {
        String sql = "SELECT id_plan, nombre, precio_base, beneficios, duracion_dias "
                + "FROM planes ORDER BY nombre";

        List<Plan> resultado = new ArrayList<>();
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        }
        return resultado;
    }

    @Override
    public Plan buscarPorId(int idPlan) throws SQLException {
        String sql = "SELECT id_plan, nombre, precio_base, beneficios, duracion_dias "
                + "FROM planes WHERE id_plan = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPlan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Plan buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT id_plan, nombre, precio_base, beneficios, duracion_dias "
                + "FROM planes WHERE nombre = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean actualizar(Plan plan) throws SQLException {
        String sql = "UPDATE planes SET nombre = ?, precio_base = ?, beneficios = ?, "
                + "duracion_dias = ? WHERE id_plan = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plan.getNombre());
            ps.setDouble(2, plan.calcularPrecio());
            ps.setString(3, plan.getBeneficios());
            ps.setInt(4, plan.getDuracionDias());
            ps.setInt(5, plan.getIdPlan());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int idPlan) throws SQLException {
        String sql = "DELETE FROM planes WHERE id_plan = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPlan);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Convierte una fila del ResultSet en un objeto Plan concreto.
     *
     * @param rs resultado apuntando a la fila actual
     * @return plan mapeado (PlanMensual, PlanAnual o PlanVIP)
     * @throws SQLException si falta o es de tipo incorrecto algún campo
     */
    private Plan mapear(ResultSet rs) throws SQLException {
        String nombre = rs.getString("nombre");
        double precioBase = rs.getDouble("precio_base");
        String beneficios = rs.getString("beneficios");
        int duracionDias = rs.getInt("duracion_dias");
        int idPlan = rs.getInt("id_plan");

        Plan plan;
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
                plan = new PlanBaseConcreto(nombre, precioBase, beneficios, duracionDias);
        }
        plan.setIdPlan(idPlan);
        return plan;
    }

    /**
     * Implementación concreta de Plan para registros de la base de datos que no
     * coincidan con los tres planes estándar.
     */
    private static class PlanBaseConcreto extends Proyecto01.modelo.PlanBase {
        public PlanBaseConcreto(String nombre, double precioBase, String beneficios, int duracionDias) {
            super(nombre, precioBase, beneficios, duracionDias);
        }

        @Override
        public double calcularPrecio() {
            return precioBase;
        }
    }
}
