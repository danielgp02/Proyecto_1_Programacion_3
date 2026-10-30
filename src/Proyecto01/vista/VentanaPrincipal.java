package Proyecto01.vista;


import Proyecto01.modelo.Usuario;
import Proyecto01.persistencia.MembresiaDaoJDBC;
import Proyecto01.persistencia.PlanDaoJDBC;
import Proyecto01.persistencia.UsuarioDaoJDBC;
import Proyecto01.red.SincronizadorRed;
import Proyecto01.servicio.ControlAcceso;
import Proyecto01.servicio.GestorMembresias;
import Proyecto01.servicio.GestorPlanes;
import Proyecto01.servicio.GestorUsuarios;
import Proyecto01.servicio.IControlAcceso;
import Proyecto01.servicio.IGestorMembresias;
import Proyecto01.servicio.IGestorPlanes;
import Proyecto01.servicio.IGestorUsuarios;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal. Arma los servicios una sola vez y se los pasa a las pestañas,
 * para que acceso, socios y membresías compartan los mismos datos.
 * <p>
 * La carga inicial de datos de prueba se ejecuta en un hilo separado
 * (SwingWorker) para no congelar la interfaz mientras consulta la base de datos.
 * <p>
 * También arranca la sincronización por sockets: cuando otra instancia de la
 * aplicación cambia datos, esta ventana se entera y vuelve a leer MySQL.
 */
public class VentanaPrincipal extends JFrame {

    private final IGestorMembresias gestorMembresias;
    private final IGestorUsuarios gestorUsuarios;
    private final IGestorPlanes gestorPlanes;
    private final IControlAcceso controlAcceso;
    private final SincronizadorRed sincronizador;

    /**
     * Construye la ventana: crea los DAOs JDBC, los servicios, las pestañas y
     * arranca la carga inicial y la sincronización por red.
     */
    public VentanaPrincipal() {
        super("Gimnasio - Sistema de Gestión");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Los DAOs se crean aquí y se inyectan: los servicios solo conocen las interfaces
        gestorMembresias = new GestorMembresias(new MembresiaDaoJDBC());
        gestorUsuarios = new GestorUsuarios(gestorMembresias, new UsuarioDaoJDBC());
        gestorPlanes = new GestorPlanes(new PlanDaoJDBC());
        controlAcceso = new ControlAcceso(gestorUsuarios, gestorMembresias);

        JTabbedPane pestanas = new JTabbedPane();
        PanelUsuarios panelMantenimiento = new PanelUsuarios(gestorUsuarios);
        SistemaAcceso panelAcceso = new SistemaAcceso(controlAcceso, () -> pestanas.setSelectedIndex(1));
        PanelMembresias panelMembresias = new PanelMembresias(gestorMembresias, gestorUsuarios, gestorPlanes);

        pestanas.addTab("Control de Acceso", panelAcceso);
        pestanas.addTab("Mantenimiento de Socios", panelMantenimiento);
        pestanas.addTab("Membresías", panelMembresias);

        setContentPane(pestanas);

        // Carga inicial de datos de prueba en hilo separado para no bloquear la GUI
        cargarDatosIniciales();

        // Sincronización por sockets: un cambio local avisa a las otras instancias y
        // un aviso remoto vuelve a leer la base (en el hilo de Swing las pantallas se refrescan solas)
        sincronizador = new SincronizadorRed(
                gestorUsuarios::notificarCambioExterno,
                estado -> SwingUtilities.invokeLater(() -> setTitle("Gimnasio - Sistema de Gestión  [" + estado + "]")));
        gestorUsuarios.agregarListenerCambioLocal(sincronizador::notificarCambio);
        sincronizador.iniciar();
    }

    @Override
    public void dispose() {
        sincronizador.detener();
        super.dispose();
    }

    /**
     * Asegura que los socios de ejemplo (101 María y 103 Lucía) tengan su membresía,
     * en un hilo de fondo para que la interfaz permanezca responsiva mientras se
     * consulta MySQL. Solo crea la membresía si todavía no existe, así abrir la
     * aplicación varias veces no duplica pagos.
     */
    private void cargarDatosIniciales() {
        EjecutorBD.ejecutarSinResultado(this, "Error de inicialización", () -> {
            asegurarMembresiaDeEjemplo(101, "Mensual");
            asegurarMembresiaDeEjemplo(103, "Anual");
        }, null);
    }

    /**
     * Crea la membresía de un socio de ejemplo si el socio existe y aún no tiene una.
     *
     * @param numeroSocio número del socio de ejemplo
     * @param nombrePlan nombre del plan a asignar (Mensual, Anual o VIP)
     */
    private void asegurarMembresiaDeEjemplo(int numeroSocio, String nombrePlan) {
        Usuario socio = gestorUsuarios.buscarUsuarioPorNumero(numeroSocio);
        if (socio == null || gestorMembresias.buscarMembresiaPorUsuario(socio) != null) {
            return;
        }
        gestorMembresias.asignarPlanYCobrar(socio, gestorPlanes.buscarPlanPorNombre(nombrePlan));
        gestorUsuarios.sincronizarEstadoDePago(socio);
    }

    public IGestorMembresias getGestorMembresias() {
        return gestorMembresias;
    }

    public IGestorUsuarios getGestorUsuarios() {
        return gestorUsuarios;
    }

    public IGestorPlanes getGestorPlanes() {
        return gestorPlanes;
    }

    public IControlAcceso getControlAcceso() {
        return controlAcceso;
    }
}
