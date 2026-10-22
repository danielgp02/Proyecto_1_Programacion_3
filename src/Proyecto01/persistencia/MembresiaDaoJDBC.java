package Proyecto01.persistencia;

import Proyecto01.modelo.Membresia;
import Proyecto01.modelo.Pago;
import Proyecto01.modelo.Plan;
import Proyecto01.modelo.Usuario;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de IMembresiaDao. Toda la SQL de la tabla membresias
 * vive aquí. Todas las consultas con datos variables usan PreparedStatement
 * para prevenir inyección SQL.
 */
public class MembresiaDaoJDBC implements IMembresiaDao {

    private final ConexionBD conexion = new ConexionBD();

    @Override
    public Membresia crear(Membresia membresia) throws SQLException {
        String sql = "INSERT INTO membresias (id_usuario, id_plan, fecha_vencimiento) "
                + "VALUES (?, ?, ?)";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, membresia.getUsuario().getIdUsuario());
            ps.setInt(2, membresia.getPlan().getIdPlan());
            ps.setDate(3, Date.valueOf(membresia.getFechaVencimiento()));
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    membresia.setIdMembresia(generatedKeys.getInt(1));
                }
            }
            return membresia;
        }
    }

    @Override
    public List<Membresia> listarTodos() throws SQLException {
        String sql = "SELECT m.id_membresia, m.id_usuario, m.id_plan, m.fecha_vencimiento, "
                + "u.numero_socio, u.nombre_completo, u.edad, u.correo_electronico, u.telefono, "
                + "u.contacto_emergencia, u.condiciones_medicas, u.pago_al_dia, "
                + "p.id_plan AS plan_id, p.nombre AS plan_nombre, p.precio_base AS plan_precio, "
                + "p.beneficios AS plan_beneficios, p.duracion_dias AS plan_duracion "
                + "FROM membresias m "
                + "INNER JOIN usuarios u ON m.id_usuario = u.id_usuario "
                + "INNER JOIN planes p ON m.id_plan = p.id_plan "
                + "ORDER BY u.numero_socio";

        List<Membresia> resultado = new ArrayList<>();
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
    public Membresia buscarPorId(int idMembresia) throws SQLException {
        String sql = "SELECT m.id_membresia, m.id_usuario, m.id_plan, m.fecha_vencimiento, "
                + "u.numero_socio, u.nombre_completo, u.edad, u.correo_electronico, u.telefono, "
                + "u.contacto_emergencia, u.condiciones_medicas, u.pago_al_dia, "
                + "p.id_plan AS plan_id, p.nombre AS plan_nombre, p.precio_base AS plan_precio, "
                + "p.beneficios AS plan_beneficios, p.duracion_dias AS plan_duracion "
                + "FROM membresias m "
                + "INNER JOIN usuarios u ON m.id_usuario = u.id_usuario "
                + "INNER JOIN planes p ON m.id_plan = p.id_plan "
                + "WHERE m.id_membresia = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMembresia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Membresia buscarPorUsuario(Usuario usuario) throws SQLException {
        String sql = "SELECT m.id_membresia, m.id_usuario, m.id_plan, m.fecha_vencimiento, "
                + "u.numero_socio, u.nombre_completo, u.edad, u.correo_electronico, u.telefono, "
                + "u.contacto_emergencia, u.condiciones_medicas, u.pago_al_dia, "
                + "p.id_plan AS plan_id, p.nombre AS plan_nombre, p.precio_base AS plan_precio, "
                + "p.beneficios AS plan_beneficios, p.duracion_dias AS plan_duracion "
                + "FROM membresias m "
                + "INNER JOIN usuarios u ON m.id_usuario = u.id_usuario "
                + "INNER JOIN planes p ON m.id_plan = p.id_plan "
                + "WHERE u.id_usuario = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuario.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean actualizar(Membresia membresia) throws SQLException {
        String sql = "UPDATE membresias SET id_plan = ?, fecha_vencimiento = ? "
                + "WHERE id_membresia = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, membresia.getPlan().getIdPlan());
            ps.setDate(2, Date.valueOf(membresia.getFechaVencimiento()));
            ps.setInt(3, membresia.getIdMembresia());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int idMembresia) throws SQLException {
        String sql = "DELETE FROM membresias WHERE id_membresia = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMembresia);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Membresia> listarVencidas(LocalDate fechaLimite) throws SQLException {
        String sql = "SELECT m.id_membresia, m.id_usuario, m.id_plan, m.fecha_vencimiento, "
                + "u.numero_socio, u.nombre_completo, u.edad, u.correo_electronico, u.telefono, "
                + "u.contacto_emergencia, u.condiciones_medicas, u.pago_al_dia, "
                + "p.id_plan AS plan_id, p.nombre AS plan_nombre, p.precio_base AS plan_precio, "
                + "p.beneficios AS plan_beneficios, p.duracion_dias AS plan_duracion "
                + "FROM membresias m "
                + "INNER JOIN usuarios u ON m.id_usuario = u.id_usuario "
                + "INNER JOIN planes p ON m.id_plan = p.id_plan "
                + "WHERE m.fecha_vencimiento < ? "
                + "ORDER BY m.fecha_vencimiento";

        List<Membresia> resultado = new ArrayList<>();
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaLimite));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
        }
        return resultado;
    }

    @Override
    public boolean insertarPago(int idMembresia, java.time.LocalDate fecha, double monto) throws SQLException {
        String sql = "INSERT INTO pagos (id_membresia, fecha_pago, monto) VALUES (?, ?, ?)";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMembresia);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setDouble(3, monto);
            return ps.executeUpdate() > 0;
        }
    }

    private Membresia mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(
                rs.getString("nombre_completo"),
                rs.getInt("edad"),
                rs.getString("correo_electronico"),
                rs.getInt("telefono"),
                rs.getInt("numero_socio"),
                rs.getInt("id_usuario"),
                rs.getBoolean("pago_al_dia"),
                rs.getString("contacto_emergencia"),
                rs.getString("condiciones_medicas")
        );

        String nombrePlan = rs.getString("plan_nombre");
        double precioBase = rs.getDouble("plan_precio");
        String beneficios = rs.getString("plan_beneficios");
        int duracionDias = rs.getInt("plan_duracion");
        int idPlan = rs.getInt("plan_id");

        Plan plan;
        switch (nombrePlan) {
            case "Mensual":
                plan = new Proyecto01.modelo.PlanMensual();
                break;
            case "Anual":
                plan = new Proyecto01.modelo.PlanAnual();
                break;
            case "VIP":
                plan = new Proyecto01.modelo.PlanVIP();
                break;
            default:
                plan = new PlanBaseConcreto(nombrePlan, precioBase, beneficios, duracionDias);
        }
        plan.setIdPlan(idPlan);

        LocalDate vencimiento = rs.getDate("fecha_vencimiento").toLocalDate();
        Membresia membresia = new Membresia(usuario, plan);
        // El constructor llama a registrarPago() que agrega un pago fantasma;
        // al cargar desde BD lo eliminamos y restauramos la fecha real.
        membresia.getPagos().clear();
        membresia.setIdMembresia(rs.getInt("id_membresia"));
        membresia.setFechaVencimiento(vencimiento);
        // Cargar los pagos reales desde la tabla pagos de la base de datos
        cargarPagos(membresia);
        return membresia;
    }

    /**
     * Carga los pagos asociados a una membresía desde la tabla pagos de la BD
     * y los agrega al historial de pagos de la membresía.
     *
     * @param membresia membresía cuyos pagos se deben cargar
     * @throws SQLException si falla la consulta
     */
    private void cargarPagos(Membresia membresia) throws SQLException {
        int idMembresia = membresia.getIdMembresia();
        String sql = "SELECT fecha_pago, monto FROM pagos "
                + "WHERE id_membresia = ? "
                + "ORDER BY fecha_pago";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMembresia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.time.LocalDate fecha = rs.getDate("fecha_pago").toLocalDate();
                    double monto = rs.getDouble("monto");
                    Pago pago = new Pago(monto);
                    pago.setFecha(fecha);
                    membresia.getPagos().add(pago);
                }
            }
        }
    }

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