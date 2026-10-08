package Proyecto01.vista;

import Proyecto01.modelo.Usuario;
import Proyecto01.servicio.IGestorUsuarios;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * ============================================================================
 *  PanelUsuarios - Panel de Mantenimiento de Socios del gimnasio.
 * ============================================================================
 *  ¿QUE HACE ESTA CLASE?
 *  Es la pantalla de administracion (CRUD) de los socios del gimnasio. Permite:
 *    1. INGRESAR un socio nuevo              -> boton Guardar
 *    2. EDITAR los datos de un socio         -> boton "Editar seleccionado"
 *    3. ELIMINAR un socio                    -> boton "Eliminar seleccionado"
 *    4. LISTAR todos los socios              -> tabla central
 *
 *  La interfaz se divide en 3 zonas:
 *    - Arriba : formulario con los datos del socio: nombre, correo, telefono,
 *               edad, numero de socio, ID de usuario, casilla "pago al dia",
 *               contacto de emergencia y condiciones medicas.
 *    - Centro : tabla con todos los socios registrados.
 *    - Abajo  : botones Limpiar / Guardar / Editar / Eliminar.
 *
 *  Datos:
 *    Trabaja sobre IGestorUsuarios. El almacenamiento está en el servicio
 *    (Repositorio genérico), no en el modelo. El estado "pago al día" no se
 *    edita aquí: lo actualiza el cobro de la membresía.
 */
public class PanelUsuarios extends JPanel {

    // ===================== CAMPOS DE TEXTO DEL FORMULARIO =====================
    // Guardan los datos personales que el usuario escribe en la pantalla.
    // Son 'protected' para permitir acceso desde subclases si hiciera falta.
    protected JTextField txtNombre;             // Nombre completo del socio
    protected JTextField txtCorreo;             // Correo electronico del socio
    protected JTextField txtTelefono;           // Telefono de contacto
    protected JTextField txtEdad;               // Edad (validada entre 0 y 120)
    protected JTextField txtNumeroSocio;        // Numero unico que identifica al socio
    protected JTextField txtIdUsuario;          // ID interno del usuario en el sistema
    protected JTextField txtContactoEmergencia; // Contacto de emergencia (opcional)
    protected JTextField txtCondicionesMedicas; // Condiciones medicas (opcional)

    // Casilla de solo lectura: refleja si la membresía está al día
    private JCheckBox chkPagoAlDia;

    private final IGestorUsuarios gestorUsuarios;

    // ================================ BOTONES ==================================
    private JButton btnGuardar;   // Registra un socio nuevo o guarda los cambios al editar
    private JButton btnLimpiar;   // Vacia todos los campos y sale del modo edicion
    private JButton btnEditar;    // Carga en el formulario los datos de la fila seleccionada
    private JButton btnEliminar;  // Borra al socio de la fila seleccionada

    // ========================== TABLA DE SOCIOS REGISTRADOS ====================
    private JTable tablaSocios;           // Componente visual que muestra los socios
    private DefaultTableModel modeloTabla;// Modelo que define columnas y filas de la tabla

    // Numero de socio que se esta editando actualmente:
    //   null  -> modo "nuevo ingreso"  (el Guardar llama a registrar())
    //   valor -> modo "edicion"        (el Guardar llama a actualizar())
    private Integer numeroSocioEnEdicion = null;

    /**
     * Constructor: construye toda la interfaz grafica del panel.
     * Organiza el contenido con BorderLayout:
     *   NORTH  -> titulo
     *   CENTER -> arriba el formulario de datos + abajo la tabla (GridLayout 2x1)
     *   SOUTH  -> botones de accion
     * Al final conecta los eventos (configurarListeners) y carga los socios
     * que ya existen en el registro (recargarTabla).
     */
    public PanelUsuarios(IGestorUsuarios gestorUsuarios) {
        this.gestorUsuarios = gestorUsuarios;
        setLayout(new BorderLayout());

        // Contenedor principal con BorderLayout y margen interno
        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Titulo del panel, centrado y en negrita
        JLabel lblTitulo = new JLabel("Mantenimiento de Socios", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Nunito", Font.BOLD, 30));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // Panel central que divide el espacio verticalmente: arriba el
        // formulario de datos y abajo la tabla de socios.
        JPanel panelCentral = new JPanel(new GridLayout(2, 1, 5, 10));

        // -----------------------------------------------------------------
        // FORMULARIO DE DATOS DEL SOCIO
        // Rejilla de 9 filas x 2 columnas: cada fila es [etiqueta, campo]
        // -----------------------------------------------------------------
        JPanel panelFormulario = new JPanel(new GridLayout(9, 2, 8, 8));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del socio"));

        // [Fila 1] Nombre
        panelFormulario.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelFormulario.add(txtNombre);

        // [Fila 2] Correo electrónico
        panelFormulario.add(new JLabel("Correo Electrónico:"));
        txtCorreo = new JTextField();
        panelFormulario.add(txtCorreo);

        // [Fila 3] Teléfono
        panelFormulario.add(new JLabel("Teléfono:"));
        txtTelefono = new JTextField();
        panelFormulario.add(txtTelefono);

        // [Fila 4] Edad
        panelFormulario.add(new JLabel("Edad:"));
        txtEdad = new JTextField();
        panelFormulario.add(txtEdad);

        // [Fila 5] Numero de socio: etiqueta y caja juntas en la misma celda,
        // para que el campo corto conserve su ancho (no se estire con el grid)
        JPanel filaNumeroSocio = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaNumeroSocio.add(new JLabel("Número de Socio:"));
        txtNumeroSocio = new JTextField();
        txtNumeroSocio.setColumns(8);   // Ancho acorde al tamaño de un número de socio
        filaNumeroSocio.add(envolverCampoCorto(txtNumeroSocio));
        panelFormulario.add(filaNumeroSocio);
        panelFormulario.add(new JLabel("")); // La celda de la derecha queda libre

        // [Fila 6] ID de usuario (campo corto, igual que el numero de socio)
        panelFormulario.add(new JLabel("ID de Usuario:"));
        txtIdUsuario = new JTextField();
        txtIdUsuario.setColumns(8);     // Ancho acorde al tamaño de un ID
        panelFormulario.add(envolverCampoCorto(txtIdUsuario));

        // [Fila 7] Pago al día: lo define el cobro de la membresía, no esta pantalla
        panelFormulario.add(new JLabel("Pago al día:"));
        chkPagoAlDia = new JCheckBox("Se actualiza al cobrar la membresía");
        chkPagoAlDia.setEnabled(false);
        chkPagoAlDia.setSelected(false);
        panelFormulario.add(chkPagoAlDia);

        // [Fila 8] Contacto de emergencia (campo opcional)
        panelFormulario.add(new JLabel("Contacto de Emergencia:"));
        txtContactoEmergencia = new JTextField();
        panelFormulario.add(txtContactoEmergencia);

        // [Fila 9] Condiciones medicas (campo opcional)
        panelFormulario.add(new JLabel("Condiciones Médicas:"));
        txtCondicionesMedicas = new JTextField();
        panelFormulario.add(txtCondicionesMedicas);

        // Se agrega el formulario a la mitad superior del panel central
        panelCentral.add(panelFormulario);

        // ----------------------------------------------------------------
        // TABLA DE SOCIOS REGISTRADOS (mitad inferior del panel central)
        // ----------------------------------------------------------------
        // Se definen las 7 columnas que mostrara la tabla
        modeloTabla = new DefaultTableModel(
                new String[]{"Nº Socio", "ID", "Nombre", "Edad", "Pago al día",
                        "Contacto de emergencia", "Condiciones médicas"}, 0);

        // Crea la tabla a partir del modelo y la pone dentro de un JScrollPane
        // (asi tiene titulo y barra de desplazamiento si hay muchos socios)
        tablaSocios = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaSocios);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Socios registrados"));
        panelCentral.add(scrollTabla);

        panelPrincipal.add(panelCentral, BorderLayout.CENTER);

        // -----------------------------------------------------------------
        // PANEL DE BOTONES (parte inferior de la ventana)
        // -----------------------------------------------------------------
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnLimpiar = new JButton("Limpiar");
        btnGuardar = new JButton("Guardar");
        btnEditar = new JButton("Editar seleccionado");
        btnEliminar = new JButton("Eliminar seleccionado");
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal, BorderLayout.CENTER);

        // Conecta los eventos de los botones y de la tabla
        configurarListeners();
        gestorUsuarios.agregarListener(this::recargarTabla);
        recargarTabla();
    }

    /**
     * Conecta cada componente con su comportamiento:
     * - Guardar  -> valida y registra/actualiza al socio
     * - Limpiar  -> vacia el formulario
     * - Editar   -> carga la fila seleccionada en el formulario
     * - Eliminar -> borra al socio de la fila seleccionada
     * - Tabla    -> al hacer clic en una fila, los datos se cargan para editar
     */
    private void configurarListeners() {
        // Al hacer clic en Guardar se valida y registra o actualiza al socio
        btnGuardar.addActionListener(e -> guardarSocio());

        // Al hacer clic en Limpiar se vacian los campos y se sale del modo edicion
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        // Al hacer clic en Editar se cargan los datos de la fila seleccionada
        btnEditar.addActionListener(e -> cargarSocioSeleccionado());

        // Al hacer clic en Eliminar se borra al socio de la fila seleccionada
        btnEliminar.addActionListener(e -> eliminarSocioSeleccionado());

        // Al seleccionar una fila, solo se habilitan los botones de accion.
        // El formulario no se rellena automaticamente ni se entra en modo edicion
        // al hacer click sobre la fila: la edicion debe producirse solo al pulsar
        // el boton "Editar seleccionado".
        tablaSocios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean haySeleccion = tablaSocios.getSelectedRow() >= 0;
                btnEditar.setEnabled(haySeleccion);
                btnEliminar.setEnabled(haySeleccion);
            }
        });
    }

    /**
     * Guarda al socio en el servicio de usuarios.
     * Flujo del metodo:
     *   1. Lee los valores de los campos del formulario.
     *   2. Valida que los campos personales obligatorios no esten vacios.
     *   3. Convierte los campos numericos; si no son numeros, avisa y aborta.
     *   4. Valida que la edad este entre 0 y 120.
     *   5. Si es un socio NUEVO, evita duplicar el numero de socio.
     *   6. Crea el objeto Usuario y lo REGISTRA (nuevo) o ACTUALIZA (edicion).
     *   7. Limpia el formulario, recarga la tabla y confirma con un mensaje.
     */
    private void guardarSocio() {
        // 1) Lee y limpia de espacios en blanco cada campo del formulario
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String edad = txtEdad.getText().trim();
        String numeroSocio = txtNumeroSocio.getText().trim();
        String idUsuario = txtIdUsuario.getText().trim();
        String contacto = txtContactoEmergencia.getText().trim();
        String condiciones = txtCondicionesMedicas.getText().trim();

        // 2) Valida que los campos personales obligatorios no esten vacios.
        //    (contacto de emergencia y condiciones medicas SI pueden quedar vacios)
        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || edad.isEmpty()
                || numeroSocio.isEmpty() || idUsuario.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Los campos personales son obligatorios (contacto y condiciones pueden quedar vacíos).",
                    "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3) Convierte los campos numericos y atrapa entradas no numericas.
        //    Si falla el parseInt, la excepcion se muestra como aviso y se salta.
        int edadInt;
        int telefonoInt;
        int numeroSocioInt;
        int idUsuarioInt;
        try {
            edadInt = Integer.parseInt(edad);
            telefonoInt = Integer.parseInt(telefono);
            numeroSocioInt = Integer.parseInt(numeroSocio);
            idUsuarioInt = Integer.parseInt(idUsuario);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Edad, Teléfono, Número de Socio e ID de Usuario deben ser valores numéricos.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 4) Rechaza edades fuera de un rango razonable
        if (edadInt < 0 || edadInt > 120) {
            JOptionPane.showMessageDialog(this,
                    "La edad debe estar entre 0 y 120 años.",
                    "Edad inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (numeroSocioEnEdicion == null) {
                gestorUsuarios.registrarUsuario(nombre, edadInt, correo, telefonoInt,
                        numeroSocioInt, idUsuarioInt, contacto, condiciones);
            } else {
                // Actualiza el mismo objeto: la membresía no pierde al socio.
                gestorUsuarios.actualizarUsuario(numeroSocioEnEdicion, nombre, edadInt, correo,
                        telefonoInt, numeroSocioInt, idUsuarioInt, contacto, condiciones);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo guardar", JOptionPane.WARNING_MESSAGE);
            return;
        }

        limpiarFormulario();
        recargarTabla();
        JOptionPane.showMessageDialog(this,
                "Datos guardados correctamente: " + nombre);
    }

    /**
     * Envuelve un campo en un panel FlowLayout para que conserve su ancho
     * preferido en lugar de estirarse con el GridLayout.
     * Recibe el campo a envolver y lo devuelve dentro de un panel angosto,
     * conservando el tamaño logico que indiquen sus columnas.
     * (Se usa en los campos cortos: numero de socio e ID de usuario.)
     */
    private JPanel envolverCampoCorto(JComponent campo) {
        JPanel contenedor = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contenedor.add(campo);
        return contenedor;
    }

    /**
     * Carga en el formulario los datos del socio de la fila seleccionada.
     * 1. Obtiene la fila seleccionada; si no hay ninguna, no hace nada.
     * 2. Toma numero de socio, ID, nombre y edad directamente de la tabla.
     * 3. Busca el resto de datos (correo, telefono, etc.) en el registro.
     * 4. Rellena los campos y activa el "modo edicion" guardando el numero
     *    de socio original en numeroSocioEnEdicion.
     */
    private void cargarSocioSeleccionado() {
        int fila = tablaSocios.getSelectedRow();
        if (fila < 0) {
            return; // No hay ninguna fila seleccionada
        }

        // Lee los datos que estan disponibles directamente en la tabla
        int numeroSocio = (Integer) modeloTabla.getValueAt(fila, 0);
        int idUsuario = (Integer) modeloTabla.getValueAt(fila, 1);
        String nombre = (String) modeloTabla.getValueAt(fila, 2);
        int edad = (Integer) modeloTabla.getValueAt(fila, 3);

        // Rellena los campos que ya estaban en la tabla
        txtNombre.setText(nombre);
        txtEdad.setText(String.valueOf(edad));
        txtNumeroSocio.setText(String.valueOf(numeroSocio));
        txtIdUsuario.setText(String.valueOf(idUsuario));

        // Toma el resto de datos desde el registro para completar el formulario
        Usuario socio = gestorUsuarios.buscarUsuarioPorNumero(numeroSocio);
        if (socio == null) {
            return; // Por seguridad: socio no encontrado
        }
        txtCorreo.setText(socio.getCorreoElectronico());
        txtTelefono.setText(String.valueOf(socio.getTelefono()));
        chkPagoAlDia.setSelected(socio.isPagoAlDia());
        txtContactoEmergencia.setText(socio.getContactoEmergencia());
        txtCondicionesMedicas.setText(socio.getCondicionesMedicas());

        // Activa el modo edicion recordando el numero de socio original.
        // Asi, al pulsar Guardar se actualizara ese socio en lugar de registrar
        // uno nuevo.
        numeroSocioEnEdicion = numeroSocio;
    }

    /**
     * Elimina al socio de la fila seleccionada, PERO primero pide confirmacion.
     * Si el usuario confirma, se borra del registro, se limpia el formulario
     * y se recarga la tabla para reflejar el cambio.
     */
    private void eliminarSocioSeleccionado() {
        int fila = tablaSocios.getSelectedRow();
        if (fila < 0) {
            // No hay nada seleccionado: se avisa y no se hace nada
            JOptionPane.showMessageDialog(this,
                    "Seleccione un socio en la tabla para eliminarlo.",
                    "Sin selección", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Toma el numero de socio de la fila seleccionada
        int numeroSocio = (Integer) modeloTabla.getValueAt(fila, 0);

        // Pide confirmacion al usuario antes de eliminar (ventana Si/No)
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar al socio número " + numeroSocio + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        // Solo borra si el usuario pulso "Si"
        if (opcion == JOptionPane.YES_OPTION) {
            Usuario socio = gestorUsuarios.buscarUsuarioPorNumero(numeroSocio);
            if (socio != null) {
                gestorUsuarios.eliminarUsuario(socio);
            }
            limpiarFormulario();
            recargarTabla();
        }
    }

    /**
     * Vuelve a llenar la tabla con todos los socios del registro.
     * Primero vacia el modelo (setRowCount(0)) y luego recorre la lista de
     * usuarios agregando una fila por cada uno, con el orden de columnas
     * definido en el modelo de la tabla.
     */
    private void recargarTabla() {
        modeloTabla.setRowCount(0); // Limpia las filas anteriores
        for (Usuario u : gestorUsuarios.listarUsuarios()) {
            // Agrega una fila por cada socio registrado
            modeloTabla.addRow(new Object[]{
                    u.getNumeroUsuario(),
                    u.getIdUsuario(),
                    u.getNombreCompleto(),
                    u.getEdad(),
                    u.isPagoAlDia() ? "Sí" : "No",
                    u.getContactoEmergencia(),
                    u.getCondicionesMedicas()
            });
        }
    }

    /**
     * Vacia los campos del formulario, restablece la casilla "pago al dia",
     * quita la seleccion de la tabla y sale del modo edicion (null).
     */
    private void limpiarFormulario() {
        txtNombre.setText("");
        txtCorreo.setText("");
        txtTelefono.setText("");
        txtEdad.setText("");
        txtNumeroSocio.setText("");
        txtIdUsuario.setText("");
        txtContactoEmergencia.setText("");
        txtCondicionesMedicas.setText("");
        chkPagoAlDia.setSelected(false);
        tablaSocios.clearSelection();    // Deselecciona cualquier fila
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
        numeroSocioEnEdicion = null;     // Sale del modo edicion: el proximo Guardar registra uno nuevo
    }
}
