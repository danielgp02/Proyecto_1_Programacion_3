package Proyecto01.vista;

import Proyecto01.servicio.IControlAcceso;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * ============================================================================
 *  SistemaAcceso - Panel de simulacion de acceso al gimnasio.
 * ============================================================================
 *  ¿QUE HACE ESTA CLASE?
 *  Simula la entrada de un socio al gimnasio: el usuario escribe su numero de
 *  socio y el sistema decide si le permite entrar o no.
 *
 *  Logica de verificacion:
 *    Delega en ControlAcceso, que revisa la fecha de vencimiento de la membresía
 *    (cobro -> vencimiento -> acceso). No usa la casilla manual de pago al día.
 *
 *   La verificacion es EN TIEMPO REAL: cada tecla que se escribe en la caja
 *   de texto dispara la consulta (DocumentListener), sin necesidad de pulsar
 *   el boton Ingresar.
 *
 *  Datos:
 *    Usa IControlAcceso, inyectado por la ventana principal. El mismo gestor
 *    de socios alimenta el mantenimiento y las membresías.
 */
public class SistemaAcceso extends JPanel {

    // Caja de texto donde se escribe el numero de socio a verificar
    protected JTextField txtNumeroUsuario;

    // Boton que muestra el resultado de la verificacion en una ventana
    private JButton btnIngresar;

    // Boton para borrar la caja de texto
    private JButton btnLimpiar;

    // Boton para ir al mantenimiento de usuarios (opcional, ver constructor).
    // Si el constructor no recibe una accion de navegacion, queda oculto.
    private JButton btnMantenimiento;

    // Etiqueta que muestra el resultado de la verificacion en tiempo real,
    // cambiando su texto y color segun el estado del socio.
    private JLabel lblResultado;

    // Servicio que decide el acceso con la fecha de vencimiento de la membresía
    private final IControlAcceso controlAcceso;

    /**
     * Constructor sin argumentos: crea el panel sin accion para el boton de
     * mantenimiento (llama al otro constructor pasandole null).
     * Con null, el boton "Inscripción de Usuarios" queda oculto.
     */
    public SistemaAcceso(IControlAcceso controlAcceso) {
        this(controlAcceso, null);
    }

    /**
     * Constructor principal: construye toda la interfaz grafica del panel.
     *
     * @param controlAcceso servicio que verifica vencimiento y morosidad
     * @param irAMantenimiento accion a ejecutar al pulsar "Inscripción de Usuarios".
     *                         Si es null, el boton se oculta.
     */
    public SistemaAcceso(IControlAcceso controlAcceso, Runnable irAMantenimiento) {
        this.controlAcceso = controlAcceso;
        setLayout(new BorderLayout());

        // Contenedor principal con BorderLayout (5 regiones) y 10px de separación
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));

        // Margen interno de 15px para no pegar el contenido con el borde
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Etiqueta de texto centrada para el título del formulario
        JLabel lblTitulo = new JLabel("Simulación de Acceso", SwingConstants.CENTER);

        // Aplica tipografía en negrita (BOLD) y tamaño 18
        lblTitulo.setFont(new Font("Nunito", Font.BOLD, 18));

        // Color del texto
        lblTitulo.setForeground(new Color(200, 16, 46));

        // Lo coloca en la región superior (NORTH) del panel principal
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // Formulario central: rejilla de 3 filas x 2 columnas [etiqueta, campo]
        // (la fila 3 queda sin usar; el espacio sobrante es para estetica)
        JPanel panelFormulario = new JPanel(new GridLayout(3, 2, 8, 8));

        // [Fila 1] Etiqueta + caja de texto para el número de socio
        panelFormulario.add(new JLabel("Número de Socio:"));
        txtNumeroUsuario = new JTextField();
        panelFormulario.add(txtNumeroUsuario);

        // [Fila 2] Etiqueta + resultado de la verificación del pago.
        // La etiqueta se actualiza sola (en tiempo real) mientras se escribe.
        panelFormulario.add(new JLabel("Estado del Pago:"));
        lblResultado = new JLabel("Ingrese su número de socio", SwingConstants.CENTER);
        lblResultado.setFont(new Font("Nunito", Font.BOLD, 18));
        lblResultado.setForeground(Color.GRAY);
        panelFormulario.add(lblResultado);

        // Coloca el formulario en el área central (CENTER) del panel principal
        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);

        // Panel de botones alineados a la derecha
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        // Botón para borrar el campo (etiqueta 'Limpiar')
        btnLimpiar = new JButton("Limpiar");

        // Botón para verificar el acceso (muestra el resultado en una ventana)
        btnIngresar = new JButton("Ingresar");

        // Botón para ir al registro de usuarios (mantenimiento)
        btnMantenimiento = new JButton("Inscripción de Usuarios");

        // Agrega los botones al panel secundario
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnIngresar);
        panelBotones.add(btnMantenimiento);

        // Coloca el panel de botones en la región inferior (SOUTH)
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        // Agrega el panel principal al panel de este componente
        add(panelPrincipal, BorderLayout.CENTER);


        // -----------------------------------------------------------------
        // LISTENERS (conexion entre los componentes y su comportamiento)
        // -----------------------------------------------------------------

        // Verificación en tiempo real: cada vez que se escribe o borra un dígito
        // en la caja de texto, se vuelve a consultar el registro y se actualiza
        // la etiqueta de resultado. Por eso los 3 eventos llaman a verificarAcceso().
        txtNumeroUsuario.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                verificarAcceso(); // Se inserto texto
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                verificarAcceso(); // Se borro texto
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                verificarAcceso(); // Cambio de atributos (formato)
            }
        });

        // Listener del botón Ingresar: verifica y muestra el resultado en una ventana.
        // No es obligatorio usarlo, el resultado ya se ve en la etiqueta.
        btnIngresar.addActionListener(e -> {
            verificarAcceso();
            JOptionPane.showMessageDialog(this,
                    lblResultado.getText(),
                    "Resultado de la verificación",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // Listener del botón Limpiar: borra la caja de texto y restablece el estado
        btnLimpiar.addActionListener(e -> restablecerEstado());

        // Listener del botón Mantenimiento: ejecuta la navegación indicada,
        // o se oculta si no se recibió ninguna acción
        if (irAMantenimiento != null) {
            btnMantenimiento.addActionListener(e -> irAMantenimiento.run());
        } else {
            btnMantenimiento.setVisible(false);
        }
    }

    /**
     * Corazon de la clase: verifica el numero de socio digitado y actualiza la
     * etiqueta de resultado con su texto y color correspondiente.
     *
     * Flujo de decision:
     *   1) Campo vacio               -> restablece el mensaje inicial.
     *   2) No es un numero entero    -> "Número de socio inválido" (naranja).
     *   3) No esta en el registro    -> "Acceso Denegado: socio no registrado" (naranja).
     *   4) Membresía al día          -> "Acceso Permitido" (verde).
     *   5) Membresía vencida        -> "Acceso Denegado por Morosidad" (rojo).
     *   6) Sin membresía            -> mensaje del servicio (naranja).
     */
    private void verificarAcceso() {

        // Leer lo que escribio el usuario; trim() elimina espacios en blanco
        String texto = txtNumeroUsuario.getText().trim();

        // Ajusta el tamaño de la fuente del resultado (un poco mas grande)
        lblResultado.setFont(new Font("Nunito", Font.BOLD, 22));

        // 1) Campo vacío: aún no hay nada que verificar
        if (texto.isEmpty()) {
            restablecerEstado();
            return;
        }

        // 2) Convierte el texto a numero entero; si no es numerico avisa y sale
        int numeroUsuario;
        try {
            numeroUsuario = Integer.parseInt(texto);
        } catch (NumberFormatException ex) {
            lblResultado.setText("Número de socio inválido");
            lblResultado.setForeground(new Color(255, 140, 0)); // naranja
            return;
        }

        // El servicio revisa la membresía y su fecha de vencimiento.
        String resultado = controlAcceso.verificarAcceso(numeroUsuario);
        lblResultado.setText(resultado);
        if ("Acceso Permitido".equals(resultado)) {
            lblResultado.setForeground(new Color(0, 150, 0));
        } else if ("Acceso Denegado por Morosidad".equals(resultado)) {
            lblResultado.setForeground(new Color(200, 16, 46));
        } else {
            lblResultado.setForeground(new Color(255, 140, 0));
        }
    }

    /**
     * Restablece el panel a su estado inicial: vacia la caja de texto y muestra
     * el mensaje por defecto "Ingrese su número de socio" en color gris.
     * Se usa tanto con el boton Limpiar como cuando el campo esta vacio.
     */
    private void restablecerEstado() {
        txtNumeroUsuario.setText("");        // Borra la caja de texto
        lblResultado.setFont(new Font("Nunito", Font.BOLD, 22));
        lblResultado.setText("Ingrese su número de socio");
        lblResultado.setForeground(Color.GRAY);
    }
}
