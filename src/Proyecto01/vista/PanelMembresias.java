package Proyecto01.vista;

import Proyecto01.modelo.Membresia;
import Proyecto01.modelo.Pago;
import Proyecto01.modelo.Plan;
import Proyecto01.modelo.Usuario;
import Proyecto01.servicio.IGestorMembresias;
import Proyecto01.servicio.IGestorPlanes;
import Proyecto01.servicio.IGestorUsuarios;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Panel de gestión de membresías del gimnasio.
 *
 * Permite:
 *   1. Asignar un plan (Mensual, Anual o VIP) a un socio y cobrarle de inmediato.
 *   2. Registrar un pago de renovación sobre la membresía de un socio.
 *   3. Ver la tabla de membresías activas (socio, plan, vencimiento y estado).
 *   4. Ver el historial de pagos de la membresía seleccionada en la tabla.
 *
 * Los socios salen de IGestorUsuarios y los planes de IGestorPlanes.
 * Las membresías se gestionan con IGestorMembresias. Al cobrar se actualiza
 * el estado de pago del socio con la fecha de vencimiento.
 */
public class PanelMembresias extends JPanel {

    // Servicio que administra las membresías y los pagos
    private final IGestorMembresias gestorMembresias;
    private final IGestorUsuarios gestorUsuarios;
    private final IGestorPlanes gestorPlanes;

    // Combo con los socios registrados en el sistema
    private final JComboBox<Usuario> comboSocios = new JComboBox<>();

    // Combo con los planes disponibles (Mensual, Anual, VIP)
    private final JComboBox<Plan> comboPlanes = new JComboBox<>();

    // Tabla y modelo que muestran todas las membresías registradas
    private DefaultTableModel modeloTablaMembresias;
    private JTable tablaMembresias;

    // Tabla y modelo que muestran el historial de pagos de la membresía elegida
    private DefaultTableModel modeloTablaPagos;
    private JTable tablaPagos;

    // Etiqueta donde se muestran los resultados de las operaciones
    private final JLabel etiquetaResultado = new JLabel(" ");

    // Formato de fecha usado para mostrar los vencimientos y pagos
    private final DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Constructor: construye la interfaz gráfica del panel.
     *
     * @param gestorMembresias servicio que administra las membresías y pagos.
     * @param gestorUsuarios servicio de socios, compartido con las otras pestañas
     * @param gestorPlanes catálogo de planes (Mensual, Anual, VIP)
     */
    public PanelMembresias(IGestorMembresias gestorMembresias, IGestorUsuarios gestorUsuarios,
                           IGestorPlanes gestorPlanes) {
        this.gestorMembresias = gestorMembresias;
        this.gestorUsuarios = gestorUsuarios;
        this.gestorPlanes = gestorPlanes;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ---------------- Título del panel ----------------
        JLabel lblTitulo = new JLabel("Gestión de Membresías", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Nunito", Font.BOLD, 28));
        add(lblTitulo, BorderLayout.NORTH);

        // ---------------- Panel central ----------------
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));

        panelCentral.add(construirFormulario(), BorderLayout.NORTH);

        JPanel panelTablas = new JPanel(new GridLayout(2, 1, 5, 10));
        panelTablas.add(construirTablaMembresias());
        panelTablas.add(construirTablaPagos());
        panelCentral.add(panelTablas, BorderLayout.CENTER);

        etiquetaResultado.setFont(new Font("Nunito", Font.BOLD, 14));
        etiquetaResultado.setForeground(new Color(60, 60, 60));
        panelCentral.add(etiquetaResultado, BorderLayout.SOUTH);

        add(panelCentral, BorderLayout.CENTER);

        // Carga inicial de los planes disponibles
        for (Plan plan : gestorPlanes.listarPlanes()) {
            comboPlanes.addItem(plan);
        }

        gestorUsuarios.agregarListener(this::actualizarSocios);
        actualizarSocios();

        // Al seleccionar una membresía se muestran sus pagos
        tablaMembresias.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarPagosDeSeleccion();
            }
        });
    }

    /**
     * Construye el formulario superior: selección de socio, selección de plan
     * y los botones de asignar plan y registrar pago.
     */
    private JPanel construirFormulario() {
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Asignar membresía"));

        // Renderer del combo de socios: muestra "N.º X — Nombre"
        comboSocios.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Usuario) {
                    Usuario usuario = (Usuario) value;
                    setText("N.º " + usuario.getNumeroUsuario() + " — " + usuario.getNombreCompleto());
                } else {
                    setText("Sin socios registrados");
                }
                return this;
            }
        });

        // Formulario de 2 filas: Socio y Plan (el combo de planes muestra
        // "Nombre (₡precio)" gracias al toString() de PlanBase).
        JPanel formulario = new JPanel(new GridLayout(2, 2, 8, 8));
        formulario.add(new JLabel("Socio:"));
        formulario.add(comboSocios);
        formulario.add(new JLabel("Plan:"));
        formulario.add(comboPlanes);

        // Botones de acción
        JButton botonAsignar = new JButton("Asignar plan y cobrar");
        botonAsignar.addActionListener(e -> asignarPlan());

        JButton botonRenovar = new JButton("Registrar pago de renovación");
        botonRenovar.addActionListener(e -> registrarPago());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.add(botonRenovar);
        panelBotones.add(botonAsignar);

        panelSuperior.add(formulario, BorderLayout.CENTER);
        panelSuperior.add(panelBotones, BorderLayout.SOUTH);
        return panelSuperior;
    }

    /**
     * Construye la tabla de membresías registradas en el sistema.
     */
    private JScrollPane construirTablaMembresias() {
        modeloTablaMembresias = new DefaultTableModel(
                new Object[]{"N.º Socio", "Nombre", "Plan", "Vencimiento", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }
        };
        tablaMembresias = new JTable(modeloTablaMembresias);
        JScrollPane scroll = new JScrollPane(tablaMembresias);
        scroll.setBorder(BorderFactory.createTitledBorder("Membresías registradas"));
        return scroll;
    }

    /**
     * Construye la tabla que muestra el historial de pagos de la membresía
     * seleccionada en la tabla de membresías.
     */
    private JScrollPane construirTablaPagos() {
        modeloTablaPagos = new DefaultTableModel(
                new Object[]{"Fecha de pago", "Monto"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }
        };
        tablaPagos = new JTable(modeloTablaPagos);
        JScrollPane scroll = new JScrollPane(tablaPagos);
        scroll.setBorder(BorderFactory.createTitledBorder("Historial de pagos"));
        return scroll;
    }

    /**
     * Carga en el combo todos los socios del registro y refresca la tabla
     * de membresías.
     *
     * Debe llamarse cuando se registran o eliminan socios en otras pantallas
     * para mantener la lista al día.
     */
    public void actualizarSocios() {
        comboSocios.removeAllItems();
        for (Usuario usuario : gestorUsuarios.listarUsuarios()) {
            comboSocios.addItem(usuario);
        }
        recargarMembresias();
        mostrarPagosDeSeleccion();
    }

    /**
     * Asigna el plan seleccionado al socio elegido y registra el cobro del plan.
     * Si el socio ya tiene membresía, se actualiza su plan y se extiende el
     * vencimiento; si no tiene, se crea una membresía nueva.
     */
    private void asignarPlan() {
        Usuario usuario = (Usuario) comboSocios.getSelectedItem();
        Plan plan = (Plan) comboPlanes.getSelectedItem();

        if (usuario == null) {
            JOptionPane.showMessageDialog(this,
                    "Registre al menos un socio en el sistema antes de asignar un plan.",
                    "Sin socios", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (plan == null) {
            return;
        }

        // El gestor crea la membresía (o actualiza la existente) y cobra el plan
        Membresia membresia = gestorMembresias.asignarPlanYCobrar(usuario, plan);
        gestorUsuarios.sincronizarEstadoDePago(usuario);

        etiquetaResultado.setText("Se asignó el plan " + plan.getNombre() + " a "
                + usuario.getNombreCompleto() + ". Próximo vencimiento: "
                + membresia.getFechaVencimiento().format(formato));

        recargarMembresias();
    }

    /**
     * Registra un pago de renovación sobre la membresía del socio seleccionado,
     * extendiendo su fecha de vencimiento según la duración del plan.
     */
    private void registrarPago() {
        Usuario usuario = (Usuario) comboSocios.getSelectedItem();

        if (usuario == null) {
            JOptionPane.showMessageDialog(this,
                    "Registre al menos un socio en el sistema antes de renovar una membresía.",
                    "Sin socios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            gestorMembresias.registrarPago(usuario);
            gestorUsuarios.sincronizarEstadoDePago(usuario);
            Membresia membresia = gestorMembresias.buscarMembresiaPorUsuario(usuario);

            etiquetaResultado.setText("Pago registrado para " + usuario.getNombreCompleto()
                    + ". Nuevo vencimiento: " + membresia.getFechaVencimiento().format(formato));

            recargarMembresias();
        } catch (IllegalStateException ex) {
            // Se lanza cuando el socio no tiene membresía asignada
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Sin membresía", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Vuelve a llenar la tabla de membresías con todas las membresías que hay
     * registradas en el gestor.
     */
    private void recargarMembresias() {
        modeloTablaMembresias.setRowCount(0);
        for (Membresia membresia : gestorMembresias.listarMembresias()) {
            modeloTablaMembresias.addRow(new Object[]{
                    membresia.getUsuario().getNumeroUsuario(),
                    membresia.getUsuario().getNombreCompleto(),
                    membresia.getPlan().getNombre(),
                    membresia.getFechaVencimiento().format(formato),
                    membresia.estaAlDia() ? "Al día" : "Vencida"
            });
        }
    }

    /**
     * Muestra en la tabla de pagos el historial de pagos de la membresía que
     * está seleccionada en la tabla de membresías.
     */
    private void mostrarPagosDeSeleccion() {
        modeloTablaPagos.setRowCount(0);

        int fila = tablaMembresias.getSelectedRow();
        if (fila < 0) {
            return; // No hay membresía seleccionada
        }

        int numeroSocio = (Integer) modeloTablaMembresias.getValueAt(fila, 0);
        Usuario usuario = gestorUsuarios.buscarUsuarioPorNumero(numeroSocio);
        if (usuario == null) {
            return;
        }

        Membresia membresia = gestorMembresias.buscarMembresiaPorUsuario(usuario);
        if (membresia == null) {
            return;
        }

        for (Pago pago : membresia.getPagos()) {
            modeloTablaPagos.addRow(new Object[]{
                    pago.getFecha().format(formato),
                    "₡" + pago.getMonto()
            });
        }
    }
}