package Proyecto01.vista;

import Proyecto01.modelo.PlanAnual;
import Proyecto01.servicio.ControlAcceso;
import Proyecto01.servicio.GestorMembresias;
import Proyecto01.servicio.GestorPlanes;
import Proyecto01.servicio.GestorUsuarios;
import Proyecto01.servicio.IControlAcceso;
import Proyecto01.servicio.IGestorMembresias;
import Proyecto01.servicio.IGestorPlanes;
import Proyecto01.servicio.IGestorUsuarios;

import javax.swing.*;

/**
 * Ventana principal. Arma los servicios una sola vez y se los pasa a las pestañas,
 * para que acceso, socios y membresías compartan los mismos datos.
 */
public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal() {
        super("Gimnasio - Sistema de Gestión");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        IGestorMembresias gestorMembresias = new GestorMembresias();
        IGestorUsuarios gestorUsuarios = new GestorUsuarios(gestorMembresias);
        IGestorPlanes gestorPlanes = new GestorPlanes();
        IControlAcceso controlAcceso = new ControlAcceso(gestorUsuarios, gestorMembresias);

        // Cobro de ejemplo: Maria queda al día (Mensual) y Lucia al día (Anual).
        // Carlos y Jorge no tienen membresía, así que el acceso los rechaza.
        gestorMembresias.asignarPlanYCobrar(
                gestorUsuarios.buscarUsuarioPorNumero(101), gestorPlanes.buscarPlanPorNombre("Mensual"));
        gestorMembresias.asignarPlanYCobrar(
                gestorUsuarios.buscarUsuarioPorNumero(103), new PlanAnual());
        gestorUsuarios.sincronizarEstadoDePago(gestorUsuarios.buscarUsuarioPorNumero(101));
        gestorUsuarios.sincronizarEstadoDePago(gestorUsuarios.buscarUsuarioPorNumero(103));

        JTabbedPane pestanas = new JTabbedPane();
        PanelUsuarios panelMantenimiento = new PanelUsuarios(gestorUsuarios);
        SistemaAcceso panelAcceso = new SistemaAcceso(controlAcceso, () -> pestanas.setSelectedIndex(1));
        PanelMembresias panelMembresias = new PanelMembresias(gestorMembresias, gestorUsuarios, gestorPlanes);

        pestanas.addTab("Control de Acceso", panelAcceso);
        pestanas.addTab("Mantenimiento de Socios", panelMantenimiento);
        pestanas.addTab("Membresías", panelMembresias);

        setContentPane(pestanas);
    }
}
