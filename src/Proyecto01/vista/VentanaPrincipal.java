package Proyecto01.vista;

import Proyecto01.modelo.PlanAnual;
import Proyecto01.persistencia.UsuarioDaoJDBC;
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
 */
public class VentanaPrincipal extends JFrame {

    private final IGestorMembresias gestorMembresias;
    private final IGestorUsuarios gestorUsuarios;
    private final IGestorPlanes gestorPlanes;
    private final IControlAcceso controlAcceso;

    public VentanaPrincipal() {
        super("Gimnasio - Sistema de Gestión");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        gestorMembresias = new GestorMembresias();
        gestorUsuarios = new GestorUsuarios(gestorMembresias, new UsuarioDaoJDBC());
        gestorPlanes = new GestorPlanes();
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
    }

    /**
     * Carga los datos de ejemplo (membresías de María y Lucía) en un hilo de fondo
     * usando SwingWorker, de modo que la interfaz gráfica permanezca responsiva
     * mientras se consultan y escriben datos en MySQL.
     */
    private void cargarDatosIniciales() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Operaciones de base de datos (lentas) en hilo de fondo
                gestorMembresias.asignarPlanYCobrar(
                        gestorUsuarios.buscarUsuarioPorNumero(101),
                        gestorPlanes.buscarPlanPorNombre("Mensual"));
                gestorMembresias.asignarPlanYCobrar(
                        gestorUsuarios.buscarUsuarioPorNumero(103),
                        new PlanAnual());
                gestorUsuarios.sincronizarEstadoDePago(
                        gestorUsuarios.buscarUsuarioPorNumero(101));
                gestorUsuarios.sincronizarEstadoDePago(
                        gestorUsuarios.buscarUsuarioPorNumero(103));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Propaga excepciones si ocurrieron en doInBackground
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(VentanaPrincipal.this,
                                    "No se pudieron cargar los datos iniciales: " + e.getMessage(),
                                    "Error de inicialización", JOptionPane.ERROR_MESSAGE));
                }
            }
        };
        worker.execute();
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
