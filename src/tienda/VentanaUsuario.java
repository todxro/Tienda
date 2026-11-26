package tienda;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

// Presenta el catálogo, la compra, el inicio de sesión y las consultas de productos con Gemini.
// Métodos principales: crearPanelLogin, mostrarRegistro, mostrarCatalogo, crearPanelBusqueda,
// crearPanelCatalogo, crearTarjeta, consultarProducto, mostrarVentanaChat, mostrarCuenta,
// mostrarCarrito, agregarAlCarrito, comprarProductos y actualizarVista.
public class VentanaUsuario extends JFrame {
    private static final String IP_ADMIN = "192.168."; //poner ip no olvidar ._.
    private static final Color AZUL        = new Color(34, 96, 176);
    private static final Color AZUL_MARINO = new Color(18, 67, 121);
    private static final Color NARANJA     = new Color(244, 132, 54);
    private static final Color AMARILLO    = new Color(250, 204, 76);
    private static final Color DORADO      = AMARILLO;
    private static final Color FONDO       = new Color(234, 241, 248);
    private static final Color SUPERFICIE  = new Color(248, 251, 254);
    private static final Color TEXTO       = new Color(25, 52, 76);
    private static final Color TEXTO_CLARO = new Color(72, 98, 126);
    private static final Color MARFIL      = Color.WHITE;
    private Usuario usuario;
    private Carrito carrito;
    private final Inventario inventario;
    private final GestorUsuarios gestorUsuarios;
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel paneles = new JPanel(tarjetas);
    private JFrame ventanaCarrito;

    // componentes de la interfaz
    private JPanel grillaCatalogo;
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;
    private JLabel cantidadProductosCarrito;
    private JLabel valorSubtotalCarrito;
    private JLabel valorDescuentoCarrito;
    private JLabel valorIVACarrito;
    private JPanel contenidoCarrito;
    private CardLayout tarjetasCarrito;
    private JButton botonComprarCarrito;
    private JButton botonVaciarCarrito;
    private JButton botonQuitarUnidadCarrito;
    private JTextField campoCantidad;
    private JTextField campoCupon;
    private JLabel labelTotal;
    private JButton botonIniciarSesion;
    private JButton botonCarrito;
    private final JTextArea areaChat = new JTextArea();
    private final JLabel estadoChat = new JLabel("Asistente de productos");
    private final JTextField campoMensajeChat = new JTextField();
    private final JButton botonEnviarChat = new JButton("Enviar");
    private final StringBuilder historialChatGemini = new StringBuilder();
    private JDialog ventanaChat;
    private Temporizador temporizador;
    private ClienteTiendaSocket clienteSocket;
    private JLabel lblcontador = new JLabel("--:--"); 
    private JLabel lblcontadorCarrito = new JLabel("--:--");
    private final GestorEnvios gestorEnvios = new GestorEnvios();
    private JComboBox<String> comboModalidadEntrega;
    private JComboBox<String> comboRegionEnvio;
    private JTextField campoComunaEnvio;
    private JTextField campoDireccionEnvio;
    private JPanel panelDatosDespacho;
    private JLabel labelPlazoEnvio;
    private JLabel valorEnvioCarrito;

    public VentanaUsuario() {
        this(null);
    }

    public VentanaUsuario(Usuario usuario) {
        this.usuario = usuario;
        this.carrito = usuario == null ? new Carrito() : usuario.getCarrito();
        this.inventario = new Inventario();
        this.gestorUsuarios = new GestorUsuarios();

        setTitle("Tienda");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                conectarConAdmin();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (clienteSocket != null) {
                    clienteSocket.close();
                }
            }
        });
        setContentPane(paneles);
        // Las tarjetas permiten cambiar de sección dentro de la misma ventana.
        paneles.add(crearPanelLogin(), "login");
        mostrarCatalogo();
        crearBotonAyuda();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JPanel crearPanelLogin() {
        // Panel para iniciar sesión, crear una cuenta o continuar como invitado.
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(FONDO);
        JPanel formulario = new JPanel(new BorderLayout(8, 12));
        formulario.setBackground(FONDO);
        formulario.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel titulo = new JLabel("Iniciar sesión", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(DORADO);
        JTextField campoCorreo = new JTextField(20);
        JPasswordField campoContrasenia = new JPasswordField(20);
        JLabel labelCorreo = new JLabel("Correo:");
        JLabel labelContrasenia = new JLabel("Contraseña:");
        labelCorreo.setForeground(TEXTO_CLARO);
        labelContrasenia.setForeground(TEXTO_CLARO);
        JPanel campos = new JPanel(new GridLayout(2, 2, 8, 8));
        campos.setBackground(FONDO);
        campos.add(labelCorreo);
        campos.add(campoCorreo);
        campos.add(labelContrasenia);
        campos.add(campoContrasenia);

        JButton iniciar = new JButton("Iniciar sesión");
        JButton crearCuenta = new JButton("Crear cuenta");
        JButton continuarComoInvitado = new JButton("Continuar como invitado");
        estilizarBoton(iniciar, DORADO, TEXTO);
        estilizarBoton(crearCuenta, AZUL_MARINO, MARFIL);
        estilizarBoton(continuarComoInvitado, AZUL_MARINO, MARFIL);
        JPanel botones = new JPanel(new GridLayout(1, 3, 8, 8));
        botones.setBackground(FONDO);
        botones.add(iniciar);
        botones.add(crearCuenta);
        botones.add(continuarComoInvitado);
        formulario.add(titulo, BorderLayout.NORTH);
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);
        panel.add(formulario);

        iniciar.addActionListener(e -> {
            Usuario autenticado = gestorUsuarios.autenticar(campoCorreo.getText().trim(),

                    new String(campoContrasenia.getPassword()));
            if (autenticado == null) {
                JOptionPane.showMessageDialog(this, "Correo o contraseña incorrectos.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                return;
            }
            usuario = autenticado;
            GestorIntereses.cargarDeseos(usuario);
            mostrarCatalogo();
        });
        crearCuenta.addActionListener(e -> mostrarRegistro(campoCorreo, campoContrasenia));
        continuarComoInvitado.addActionListener(e -> {
            getRootPane().setDefaultButton(null);
            tarjetas.show(paneles, "catalogo");
        });
        botonIniciarSesion = iniciar;
        getRootPane().setDefaultButton(iniciar);
        return panel;
    }

    private void mostrarRegistro(JTextField campoCorreo, JPasswordField campoContrasenia) {
        // Recoge los datos de registro y devuelve el correo al formulario de acceso.
        JTextField nombre = new JTextField();
        JTextField apellido = new JTextField();
        JTextField rut = new JTextField();
        ((AbstractDocument) rut.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass bypass, int offset, String texto, AttributeSet atributos)
                    throws BadLocationException {
                replace(bypass, offset, 0, texto, atributos);
            }

            @Override
            public void replace(FilterBypass bypass, int offset, int longitud, String texto,
                    AttributeSet atributos) throws BadLocationException {
                if (texto == null || texto.chars().allMatch(caracter -> caracter >= '0' && caracter <= '9')) {
                    bypass.replace(offset, longitud, texto, atributos);
                }
            }
        });
        JTextField correo = new JTextField();
        JPasswordField contrasenia = new JPasswordField();
        Object[] campos = {"Nombre:", nombre, "Apellido:", apellido, "RUT:", rut, "Correo:", correo,
                "Contraseña:", contrasenia};
        int resultado = JOptionPane.showConfirmDialog(this, campos, "Crear cuenta",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        boolean creada = gestorUsuarios.registrar(nombre.getText().trim(), apellido.getText().trim(),
                rut.getText().trim(), correo.getText().trim(), new String(contrasenia.getPassword()));
        if (creada) {
            campoCorreo.setText(correo.getText().trim());
            campoContrasenia.setText("");
            JOptionPane.showMessageDialog(this, "Cuenta creada. Ahora puedes iniciar sesión.");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Completa todos los campos, usa un correo con formato usuario@dominio.com y verifica que esté disponible.",
                    "No se pudo crear la cuenta", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarCatalogo() {
        // Arma la vista de compra y la muestra después de entrar o continuar como invitado.
        setTitle(usuario == null ? "Catálogo - Invitado" : "Catálogo - " + usuario.getNombre());
        JPanel catalogo = new JPanel(new BorderLayout(12, 12));
        catalogo.setBackground(FONDO);
        catalogo.add(crearPanelBusqueda(), BorderLayout.NORTH);
        catalogo.add(crearPanelCatalogo(), BorderLayout.CENTER);
        catalogo.add(crearPanelAcciones(), BorderLayout.SOUTH);
        paneles.add(catalogo, "catalogo");
        tarjetas.show(paneles, "catalogo");
        getRootPane().setDefaultButton(null);
        actualizarVista();
        if (temporizador != null) {
            temporizador.detener();
        }
        temporizador = new Temporizador(carrito, inventario, 5 , lblcontador, this); //cambiar el numero es para cambiar el coso
        temporizador.setLabelCarrito(lblcontadorCarrito);
        paneles.revalidate();
        paneles.repaint();
    }

    // construye la barra superior azul con el título y el buscador
    private JPanel crearPanelBusqueda() {
        // Barra superior para buscar, actualizar el catálogo y acceder al chat.
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AZUL_MARINO);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titulo = new JLabel("CATÁLOGO");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(MARFIL);
        panel.add(titulo, BorderLayout.WEST);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        busqueda.setOpaque(false);

        JLabel labelBuscar = new JLabel("Buscar:");
        labelBuscar.setForeground(MARFIL);

        JTextField campoBuscar = new JTextField(15);
        campoBuscar.setBackground(SUPERFICIE);
        campoBuscar.setForeground(TEXTO_CLARO);
        campoBuscar.setCaretColor(TEXTO_CLARO);        JButton btnBuscar = new JButton("Buscar");
        JButton btnRefrescar = new JButton("Ver todo");
        JButton btnActualizar = new JButton("Actualizar");

        JLabel labelMin = new JLabel("Min $:");
        labelMin.setForeground(MARFIL);
        JTextField campoMin = new JTextField(5);
        campoMin.setBackground(SUPERFICIE);
        campoMin.setForeground(TEXTO_CLARO);
        campoMin.setCaretColor(TEXTO_CLARO);

        JLabel labelMax = new JLabel("Max $:");
        labelMax.setForeground(MARFIL);
        JTextField campoMax = new JTextField(5);
        campoMax.setBackground(SUPERFICIE);
        campoMax.setForeground(TEXTO_CLARO);
        campoMax.setCaretColor(TEXTO_CLARO);

        JButton btnFiltrarPrecio = new JButton("Filtrar Precio");
        estilizarBoton(btnFiltrarPrecio, DORADO, TEXTO); //colores
        btnFiltrarPrecio.addActionListener(e -> {
    try {
        // si el campo está vacio asume 0, de lo contrario lee el valor
        double min = campoMin.getText().trim().isEmpty() ? 0 : Double.parseDouble(campoMin.getText().trim());
        
        // si el campo esta vacío asume el valor maximo posible, de lo contrario lee el valor
        double max = campoMax.getText().trim().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(campoMax.getText().trim());
        
        cargarProductosEnGrilla(inventario.filtrarPorRangoPrecio(min, max));  
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, 
            "Por favor, ingresa montos numéricos válidos.", 
            "Formato incorrecto", 
            JOptionPane.WARNING_MESSAGE);
    }
});

        btnBuscar.addActionListener(e -> {
            String texto = campoBuscar.getText().trim();
            if (!texto.isEmpty()) {
                cargarProductosEnGrilla(inventario.buscarPorNombre(texto));
            }
        });
        btnRefrescar.addActionListener(e -> actualizarVista());
        btnActualizar.addActionListener(e -> {
            inventario.recargarDesdeArchivo();
            actualizarVista();
        });

        busqueda.add(labelBuscar);
        busqueda.add(campoBuscar);
        busqueda.add(btnBuscar);
        busqueda.add(btnRefrescar);
        busqueda.add(btnActualizar);
        busqueda.add(labelMin);
        busqueda.add(campoMin);
        busqueda.add(labelMax);
        busqueda.add(campoMax);
        busqueda.add(btnFiltrarPrecio);
        JButton btnCuenta = new JButton("Cuenta");
        estilizarBoton(btnCuenta, DORADO, TEXTO);
        btnCuenta.addActionListener(e -> mostrarCuenta());
        busqueda.add(btnCuenta);
        JButton btnDeseos = new JButton("★ Lista de Deseos");
        estilizarBoton(btnDeseos, DORADO, TEXTO);
        btnDeseos.addActionListener(e -> mostrarVentanaDeseos());
        busqueda.add(btnDeseos);

        if (usuario == null) {
            JButton btnIniciarSesion = new JButton("Iniciar sesión");
            estilizarBoton(btnIniciarSesion, DORADO, TEXTO);
            btnIniciarSesion.addActionListener(e -> {
                tarjetas.show(paneles, "login");
                getRootPane().setDefaultButton(botonIniciarSesion);
            });
            busqueda.add(btnIniciarSesion);

        } else {
            JLabel saludo = new JLabel("Hola, " + usuario.getNombre());
            saludo.setForeground(MARFIL);
            busqueda.add(saludo);

            JButton btnCerrarSesion = new JButton("Cerrar sesión");
            estilizarBoton(btnCerrarSesion, DORADO, TEXTO);
            btnCerrarSesion.addActionListener(e -> {
                usuario = null;
                carrito.vaciarCarrito();
                inventario.guardarEnArchivo();
                carrito = new Carrito();
                mostrarCatalogo();
            });
            busqueda.add(btnCerrarSesion);
        }

        panel.add(busqueda, BorderLayout.EAST);
        return panel;
    }

    private void mostrarCuenta() {
        JDialog ventana = new JDialog(this, "Datos de la cuenta", true);
        ventana.setSize(400, 330);
        ventana.setLocationRelativeTo(this);
        ventana.getContentPane().setBackground(FONDO);

        JPanel contenido = new JPanel(new BorderLayout(10, 15));
        contenido.setBackground(FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel titulo = new JLabel("DATOS DE LA CUENTA", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(DORADO);
        contenido.add(titulo, BorderLayout.NORTH);

        JPanel datos = new JPanel(new GridLayout(5, 2, 10, 12));
        datos.setBackground(FONDO);

        String nombre = usuario == null ? "Invitado" : usuario.getNombre();
        String apellido = usuario == null ? "Invitado" : usuario.getApellido();
        String rut = usuario == null ? "Invitado" : usuario.getRut();
        String correo = usuario == null ? "Invitado" : usuario.getCorreo();
        String tipo = usuario == null ? "Invitado" : "Registrada";

        String[] etiquetas = {"Tipo de cuenta:", "Nombre:", "Apellido:", "RUT:", "Correo:"};
        String[] valores = {tipo, nombre, apellido, rut, correo};

        for (int i = 0; i < etiquetas.length; i++) {
            JLabel etiqueta = new JLabel(etiquetas[i]);
            etiqueta.setForeground(DORADO);
            etiqueta.setFont(new Font("SansSerif", Font.BOLD, 13));

            JLabel valor = new JLabel(valores[i]);
            valor.setForeground(TEXTO_CLARO);
            valor.setFont(new Font("SansSerif", Font.PLAIN, 13));

            datos.add(etiqueta);
            datos.add(valor);
        }

        contenido.add(datos, BorderLayout.CENTER);

        JButton aceptar = new JButton("Aceptar");
        estilizarBoton(aceptar, DORADO, TEXTO);
        aceptar.addActionListener(e -> ventana.dispose());

        JPanel pie = new JPanel();
        pie.setBackground(FONDO);
        pie.add(aceptar);
        contenido.add(pie, BorderLayout.SOUTH);

        ventana.setContentPane(contenido);
        ventana.setResizable(false);
    ventana.setVisible(true);
    }
    
    // Botón de acceso rápido al asistente de productos con Gemini.
    private void crearBotonAyuda() {
        JButton botonAyuda = new JButton("?") {
            @Override
            protected void paintComponent(Graphics g) {
                // dibuja un circulo amarillo y despues el "?" encima
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AMARILLO);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        // sin fondo ni borde cuadrado, asi solo se ve el circulo
        botonAyuda.setContentAreaFilled(false);
        botonAyuda.setBorderPainted(false);
        botonAyuda.setFocusPainted(false);
        botonAyuda.setForeground(TEXTO);
        botonAyuda.setFont(new Font("SansSerif", Font.BOLD, 24));
        botonAyuda.setToolTipText("¿Tienes dudas sobre los productos? Pregunta a Gemini");
        botonAyuda.addActionListener(e -> mostrarVentanaChat());

        // la capa PALETTE queda encima de todo lo demas de la ventana
        JLayeredPane capas = getLayeredPane();
        capas.add(botonAyuda, JLayeredPane.PALETTE_LAYER);

        // si cambia el tamaño de la ventana, el boton vuelve a la esquina
        capas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                botonAyuda.setBounds(capas.getWidth() - 80, capas.getHeight() - 80, 56, 56);
            }
        });
    }

    private void mostrarVentanaChat() {
        if (ventanaChat == null) {
            ventanaChat = new JDialog(this, "Asistente de productos", false);
            ventanaChat.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            ventanaChat.setContentPane(crearPanelChat());
            ventanaChat.setResizable(false);
            ventanaChat.setSize(520, 400);
            ventanaChat.setLocationRelativeTo(this);
        }
        ventanaChat.setVisible(true);
        ventanaChat.toFront();
    }

    private JPanel crearPanelChat() {
        // Chat general de productos; las consultas se envían a Gemini en segundo plano.
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        areaChat.setBackground(SUPERFICIE);
        areaChat.setForeground(TEXTO_CLARO);
        areaChat.setCaretColor(TEXTO_CLARO);
        if (areaChat.getText().isEmpty()) {
            areaChat.setText("Asistente: Hola, puedo responder tus dudas sobre los productos del catálogo."
                    + System.lineSeparator());
        }
        JScrollPane scrollChat = new JScrollPane(areaChat);
        scrollChat.getViewport().setBackground(SUPERFICIE);

        JPanel panelEnvio = new JPanel(new BorderLayout(8, 0));
        panelEnvio.setBackground(FONDO);
        campoMensajeChat.setBackground(SUPERFICIE);
        campoMensajeChat.setForeground(TEXTO_CLARO);
        campoMensajeChat.setCaretColor(TEXTO_CLARO);
        estilizarBoton(botonEnviarChat, NARANJA, MARFIL);
        botonEnviarChat.addActionListener(e -> enviarConsultaGemini());
        campoMensajeChat.addActionListener(e -> enviarConsultaGemini());
        panelEnvio.add(campoMensajeChat, BorderLayout.CENTER);
        panelEnvio.add(botonEnviarChat, BorderLayout.EAST);

        JPanel pie = new JPanel(new BorderLayout(8, 0));
        pie.setBackground(FONDO);
        estadoChat.setForeground(TEXTO_CLARO);
        pie.add(estadoChat, BorderLayout.NORTH);
        pie.add(panelEnvio, BorderLayout.CENTER);

        panel.add(scrollChat, BorderLayout.CENTER);
        panel.add(pie, BorderLayout.SOUTH);
        return panel;
    }

    private void enviarConsultaGemini() {
        String mensaje = campoMensajeChat.getText().trim();
        if (mensaje.isEmpty() || !botonEnviarChat.isEnabled()) {
            return;
        }
        campoMensajeChat.setText("");
        areaChat.append("Tú: " + mensaje + System.lineSeparator());
        botonEnviarChat.setEnabled(false);
        campoMensajeChat.setEnabled(false);
        estadoChat.setText("Consultando a Gemini...");

        StringBuilder contexto = new StringBuilder("Productos disponibles en el catálogo:\n");
        for (Producto producto : inventario.getListaProductos()) {
            contexto.append("- ").append(producto.getNombre())
                    .append(" | categoría: ").append(producto.getCategoria())
                    .append(" | precio: $").append(producto.getPrecio())
                    .append(" | stock: ").append(producto.getStock()).append('\n');
        }
        contexto.append("\nConversación anterior:\n").append(historialChatGemini)
                .append("\nPregunta del cliente: ").append(mensaje);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return GeminiAPIClient.responder(contexto.toString());
            }

            @Override
            protected void done() {
                try {
                    String respuesta = get();
                    areaChat.append("Gemini: " + respuesta + System.lineSeparator());
                    historialChatGemini.append("Cliente: ").append(mensaje)
                            .append("\nAsistente: ").append(respuesta).append('\n');
                    estadoChat.setText("Asistente de productos");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    areaChat.append("Error: se interrumpió la consulta." + System.lineSeparator());
                    estadoChat.setText("Consulta interrumpida");
                } catch (java.util.concurrent.ExecutionException ex) {
                    Throwable causa = ex.getCause();
                    String detalle = causa == null || causa.getMessage() == null
                            ? ex.getMessage() : causa.getMessage();
                    areaChat.append("Error al consultar: " + detalle + System.lineSeparator());
                    estadoChat.setText("No se pudo completar la consulta");
                } finally {
                    botonEnviarChat.setEnabled(true);
                    campoMensajeChat.setEnabled(true);
                    campoMensajeChat.requestFocusInWindow();
                }
            }
        }.execute();
    }

    // arma la grilla de tarjetas del catalogo en el centro de la ventana
    private JPanel crearPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // 0 filas y 4 columnas: crea las filas que hagan falta
        grillaCatalogo = new JPanel(new GridLayout(0, 5, 12, 12));
        grillaCatalogo.setBackground(FONDO);

        // la grilla va arriba para que las tarjetas no se estiren hacia abajo
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(FONDO);
        contenedor.add(grillaCatalogo, BorderLayout.NORTH);

        JScrollPane scrollCatalogo = new JScrollPane(contenedor);
        scrollCatalogo.getViewport().setBackground(FONDO);
        scrollCatalogo.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollCatalogo, BorderLayout.CENTER);
        return panel;
    }

    // una tarjeta por producto: imagen arriba, datos al medio y boton abajo
private JPanel crearTarjeta(Producto producto) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 8));
        tarjeta.setBackground(SUPERFICIE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AZUL, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel nombre = new JLabel(producto.getNombre());
        nombre.setFont(new Font("SansSerif", Font.BOLD, 14));
        nombre.setForeground(TEXTO);
        JLabel precio = new JLabel("$" + producto.getPrecio());
        precio.setFont(new Font("SansSerif", Font.BOLD, 16));
        precio.setForeground(AZUL_MARINO);
        JLabel stock = new JLabel("Stock: " + producto.getStock());
        stock.setForeground(TEXTO_CLARO);

        JPanel datos = new JPanel(new GridLayout(3, 1, 0, 4));
        datos.setOpaque(false);
        datos.add(nombre);
        datos.add(precio);
        datos.add(stock);

        JPanel panelAccionesTarjeta = new JPanel(new GridLayout(3, 1, 0, 6));
        panelAccionesTarjeta.setOpaque(false);

        if (producto.getStock() > 0) {
            JButton btnAgregar = new JButton("Agregar al carrito");
            estilizarBoton(btnAgregar, AMARILLO, TEXTO);
            btnAgregar.addActionListener(e -> agregarAlCarrito(producto));
            panelAccionesTarjeta.add(btnAgregar);
        } else {
            JButton btnAvisar = new JButton("Avísame cuando llegue");
            estilizarBoton(btnAvisar, NARANJA, MARFIL);
            btnAvisar.addActionListener(e -> {
                if (usuario == null) {
                    JOptionPane.showMessageDialog(this, "Inicia sesión para suscribirte a reposiciones.");
                    return;
                }
                GestorIntereses.agregarRegistro("suscripciones.csv", usuario.getCorreo(), producto.getId());
                JOptionPane.showMessageDialog(this, "Te avisaremos apenas repongamos stock de " + producto.getNombre());
            });
            panelAccionesTarjeta.add(btnAvisar);
        }

        boolean esFavorito = usuario != null && usuario.getListaDeseos().contains(producto.getId());
        JButton btnFavorito = new JButton(esFavorito ? "★ En Deseos" : "☆ Favorito");
        estilizarBoton(btnFavorito, SUPERFICIE, TEXTO);
        btnFavorito.addActionListener(e -> {
            if (usuario == null) {
                JOptionPane.showMessageDialog(this, "Inicia sesión para guardar productos en tu lista de deseos.");
                return;
            }
            if (usuario.getListaDeseos().contains(producto.getId())) {
                usuario.quitarDeseo(producto.getId());
                GestorIntereses.eliminarRegistro("deseos.csv", usuario.getCorreo(), producto.getId());
            } else {
                usuario.agregarDeseo(producto.getId());
                GestorIntereses.agregarRegistro("deseos.csv", usuario.getCorreo(), producto.getId());
            }
            actualizarVista();
        });
        panelAccionesTarjeta.add(btnFavorito);

        JButton btnConsultar = new JButton("Consultar sobre este producto");
        estilizarBoton(btnConsultar, AZUL_MARINO, MARFIL);
        btnConsultar.addActionListener(e -> consultarProducto(producto));
        panelAccionesTarjeta.add(btnConsultar);

        tarjeta.add(crearImagen(producto.getId()), BorderLayout.NORTH);
        tarjeta.add(datos, BorderLayout.CENTER);
        tarjeta.add(panelAccionesTarjeta, BorderLayout.SOUTH);
        return tarjeta;
    }

    private void consultarProducto(Producto producto) {
        JDialog dialogo = new JDialog(this, "Consulta sobre " + producto.getNombre(), false);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTextArea conversacion = new JTextArea();
        conversacion.setEditable(false);
        conversacion.setLineWrap(true);
        conversacion.setWrapStyleWord(true);
        conversacion.setBackground(SUPERFICIE);
        conversacion.setForeground(TEXTO_CLARO);
        conversacion.append("Asistente: Hola, puedo responder preguntas sobre este producto."
                + System.lineSeparator());
        JScrollPane desplazamiento = new JScrollPane(conversacion);
        desplazamiento.getViewport().setBackground(SUPERFICIE);

        JTextField campoPregunta = new JTextField();
        campoPregunta.setBackground(SUPERFICIE);
        campoPregunta.setForeground(TEXTO_CLARO);
        campoPregunta.setCaretColor(TEXTO_CLARO);
        JButton botonEnviar = new JButton("Enviar");
        estilizarBoton(botonEnviar, NARANJA, MARFIL);
        StringBuilder historial = new StringBuilder();

        Runnable enviarPregunta = () -> {
            String pregunta = campoPregunta.getText().trim();
            if (pregunta.isEmpty() || !botonEnviar.isEnabled()) {
                return;
            }

            campoPregunta.setText("");
            conversacion.append("Tú: " + pregunta + System.lineSeparator());
            botonEnviar.setEnabled(false);
            campoPregunta.setEnabled(false);

            String contexto = "Producto: " + producto.getNombre()
                    + "\nCategoría: " + producto.getCategoria()
                    + "\nPrecio: $" + producto.getPrecio()
                    + "\nStock disponible: " + producto.getStock()
                    + "\n\nConversación anterior:\n" + historial
                    + "\nPregunta del cliente: " + pregunta;

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return GeminiAPIClient.responder(contexto);
                }

                @Override
                protected void done() {
                    try {
                        String respuesta = get();
                        conversacion.append("Asistente: " + respuesta + System.lineSeparator());
                        historial.append("Cliente: ").append(pregunta)
                                .append("\nAsistente: ").append(respuesta).append('\n');
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        conversacion.append("Error: se interrumpió la consulta."
                                + System.lineSeparator());
                    } catch (java.util.concurrent.ExecutionException ex) {
                        Throwable causa = ex.getCause();
                        String mensaje = causa == null || causa.getMessage() == null
                                ? ex.getMessage() : causa.getMessage();
                        conversacion.append("Error al consultar: " + mensaje + System.lineSeparator());
                    } finally {
                        botonEnviar.setEnabled(true);
                        campoPregunta.setEnabled(true);
                        campoPregunta.requestFocusInWindow();
                    }
                }
            }.execute();
        };
        botonEnviar.addActionListener(e -> enviarPregunta.run());
        campoPregunta.addActionListener(e -> enviarPregunta.run());

        JPanel envio = new JPanel(new BorderLayout(8, 0));
        envio.setBackground(FONDO);
        envio.add(campoPregunta, BorderLayout.CENTER);
        envio.add(botonEnviar, BorderLayout.EAST);

        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.setBackground(FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contenido.add(desplazamiento, BorderLayout.CENTER);
        contenido.add(envio, BorderLayout.SOUTH);
        dialogo.setContentPane(contenido);
        dialogo.setSize(520, 400);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
        campoPregunta.requestFocusInWindow();
    }

    // busca la foto en la carpeta imagenes con el id del producto
    private JLabel crearImagen(String id) {
        JLabel imagen = new JLabel("sin imagen", SwingConstants.CENTER);
        imagen.setForeground(TEXTO_CLARO);
        imagen.setPreferredSize(new Dimension(0, 220));
        java.io.File archivo = new java.io.File("imagenes/" + id + ".png");
        if (archivo.exists()) {
            ImageIcon iconoOriginal = new ImageIcon(archivo.getPath());
            Image fotoOriginal = iconoOriginal.getImage();
            if (iconoOriginal.getIconWidth() > 0 && iconoOriginal.getIconHeight() > 0) {
                imagen.setText("");
                imagen.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        ajustarImagen(imagen, fotoOriginal);
                    }
                });
                ajustarImagen(imagen, fotoOriginal);
            }
        }
        return imagen;
    }

    private void ajustarImagen(JLabel etiqueta, Image imagenOriginal) {
        int anchoDisponible = Math.min(etiqueta.getWidth() - 16, 280);
        int altoDisponible = etiqueta.getHeight() - 16;
        if (anchoDisponible <= 0) {
            anchoDisponible = 260;
        }
        if (altoDisponible <= 0) {
            altoDisponible = 204;
        }

        double escala = Math.min(
                (double) anchoDisponible / imagenOriginal.getWidth(null),
                (double) altoDisponible / imagenOriginal.getHeight(null));
        int ancho = Math.max(1, (int) (imagenOriginal.getWidth(null) * escala));
        int alto = Math.max(1, (int) (imagenOriginal.getHeight(null) * escala));
        Image fotoEscalada = imagenOriginal.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        etiqueta.setIcon(new ImageIcon(fotoEscalada));
    }
    
    private JPanel crearPanelAcciones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        panel.setBackground(FONDO);

        JLabel labelCantidad = new JLabel("Cantidad:");
        labelCantidad.setForeground(TEXTO_CLARO);
        panel.add(labelCantidad);

        campoCantidad = new JTextField("1", 4);
        campoCantidad.setBackground(SUPERFICIE);
        campoCantidad.setForeground(TEXTO_CLARO);
        campoCantidad.setCaretColor(TEXTO_CLARO);
        panel.add(campoCantidad);

        JButton btnAgregar = new JButton("Agregar al carrito");
        estilizarBoton(btnAgregar, AMARILLO, TEXTO);
        panel.add(btnAgregar);

        botonCarrito = new JButton("Carrito (0)");
        estilizarBoton(botonCarrito, DORADO, TEXTO);
        botonCarrito.addActionListener(e -> mostrarCarrito());
        panel.add(botonCarrito);
        panel.add(new JLabel("Reserva:"));
        panel.add(lblcontador);
        return panel;
     }
    
    private void mostrarCarrito() {
        if (ventanaCarrito != null && ventanaCarrito.isDisplayable()) {
            recargarRegionesEnvio();
            actualizarVista();
            ventanaCarrito.setVisible(true);
            ventanaCarrito.toFront();
            return;
        }
        ventanaCarrito = new JFrame("Mi carrito - Tienda");
        ventanaCarrito.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        ventanaCarrito.setContentPane(crearPanelCarrito());
        ventanaCarrito.setSize(getSize());
        ventanaCarrito.setMinimumSize(new Dimension(620, 500));
        ventanaCarrito.setLocationRelativeTo(this);
        ventanaCarrito.setExtendedState(getExtendedState());
        ventanaCarrito.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                volverAlCatalogo();
            }
        });
        actualizarVista();
        setVisible(false);
        ventanaCarrito.setVisible(true);
    }

    private void volverAlCatalogo() {
        if (ventanaCarrito != null) {
            ventanaCarrito.dispose();
            ventanaCarrito = null;
        }
        setVisible(true);
        toFront();
    }

    private Component ventanaActiva() {
        return ventanaCarrito == null ? this : ventanaCarrito;
    }

    private JPanel crearPanelCarrito() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JPanel encabezado = new JPanel(new BorderLayout(12, 4));
        encabezado.setBackground(AZUL_MARINO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Tu carrito");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(MARFIL);
        JLabel subtitulo = new JLabel("Revisa tus productos antes de finalizar la compra.");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(220, 230, 240));
        titulos.add(titulo);
        titulos.add(Box.createVerticalStrut(4));
        titulos.add(subtitulo);
        cantidadProductosCarrito = new JLabel("0 productos");
        cantidadProductosCarrito.setFont(new Font("SansSerif", Font.BOLD, 14));
        cantidadProductosCarrito.setForeground(MARFIL);
        cantidadProductosCarrito.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(95, 133, 171)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        encabezado.add(titulos, BorderLayout.CENTER);
        encabezado.add(cantidadProductosCarrito, BorderLayout.EAST);
        lblcontadorCarrito.setForeground(MARFIL);            
        encabezado.add(lblcontadorCarrito, BorderLayout.SOUTH);
        panel.add(encabezado, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(MARFIL);
        centro.setBorder(BorderFactory.createLineBorder(new Color(215, 203, 184)));
        String[] columnas = {"Producto", "Cantidad", "Subtotal"};
        modeloCarrito = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setRowHeight(40);
        tablaCarrito.setBackground(MARFIL);
        tablaCarrito.setForeground(TEXTO);
        tablaCarrito.setSelectionBackground(new Color(220, 232, 244));
        tablaCarrito.setSelectionForeground(TEXTO);
        tablaCarrito.setGridColor(new Color(229, 220, 205));
        tablaCarrito.setShowVerticalLines(false);
        tablaCarrito.setIntercellSpacing(new Dimension(0, 1));
        tablaCarrito.setFillsViewportHeight(true);
        tablaCarrito.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCarrito.getTableHeader().setBackground(AZUL_MARINO);
        tablaCarrito.getTableHeader().setForeground(MARFIL);
        tablaCarrito.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tablaCarrito.getTableHeader().setPreferredSize(new Dimension(0, 38));
        tablaCarrito.getColumnModel().getColumn(0).setPreferredWidth(380);
        tablaCarrito.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablaCarrito.getColumnModel().getColumn(2).setPreferredWidth(150);
        JScrollPane scroll = new JScrollPane(tablaCarrito);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(MARFIL);

        tarjetasCarrito = new CardLayout();
        contenidoCarrito = new JPanel(tarjetasCarrito);
        contenidoCarrito.setBackground(MARFIL);
        JPanel vacio = new JPanel();
        vacio.setBackground(MARFIL);
        vacio.setLayout(new BoxLayout(vacio, BoxLayout.Y_AXIS));
        JLabel iconoVacio = new JLabel("♡", SwingConstants.CENTER);
        iconoVacio.setFont(new Font("SansSerif", Font.PLAIN, 52));
        iconoVacio.setForeground(AZUL);
        iconoVacio.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel mensajeVacio = new JLabel("Tu carrito está vacío", SwingConstants.CENTER);
        mensajeVacio.setFont(new Font("SansSerif", Font.BOLD, 20));
        mensajeVacio.setForeground(TEXTO);
        mensajeVacio.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel ayudaVacio = new JLabel("Explora el catálogo y agrega tus productos favoritos.",
                SwingConstants.CENTER);
        ayudaVacio.setFont(new Font("SansSerif", Font.PLAIN, 14));
        ayudaVacio.setForeground(TEXTO_CLARO);
        ayudaVacio.setAlignmentX(Component.CENTER_ALIGNMENT);
        vacio.add(Box.createVerticalGlue());
        vacio.add(iconoVacio);
        vacio.add(Box.createVerticalStrut(8));
        vacio.add(mensajeVacio);
        vacio.add(Box.createVerticalStrut(6));
        vacio.add(ayudaVacio);
        vacio.add(Box.createVerticalGlue());
        contenidoCarrito.add(scroll, "productos");
        contenidoCarrito.add(vacio, "vacio");
        centro.add(contenidoCarrito, BorderLayout.CENTER);
        panel.add(centro, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout(12, 14));
        inferior.setBackground(FONDO);

        JPanel zonaMedios = new JPanel(new GridLayout(1, 2, 14, 0));
        zonaMedios.setBackground(FONDO);

        JPanel seccionEntrega = new JPanel(new BorderLayout(8, 8));
        seccionEntrega.setBackground(MARFIL);
        seccionEntrega.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 203, 184)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JPanel filaModalidad = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaModalidad.setBackground(MARFIL);
        JLabel tituloEntrega = new JLabel("Entrega:");
        tituloEntrega.setForeground(TEXTO);
        tituloEntrega.setFont(new Font("SansSerif", Font.BOLD, 13));
        comboModalidadEntrega = new JComboBox<>(new String[] { "Retiro en tienda", "Despacho a domicilio" });
        filaModalidad.add(tituloEntrega);
        filaModalidad.add(comboModalidadEntrega);

        panelDatosDespacho = new JPanel(new GridLayout(3, 2, 8, 6));
        panelDatosDespacho.setBackground(MARFIL);
        comboRegionEnvio = new JComboBox<>();
        campoComunaEnvio = new JTextField();
        campoDireccionEnvio = new JTextField();

        JLabel lblRegion = new JLabel("Región:");
        JLabel lblComuna = new JLabel("Comuna:");
        JLabel lblDireccion = new JLabel("Dirección:");
        for (JLabel lbl : new JLabel[] { lblRegion, lblComuna, lblDireccion }) {
            lbl.setForeground(TEXTO_CLARO);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        }
        panelDatosDespacho.add(lblRegion);
        panelDatosDespacho.add(comboRegionEnvio);
        panelDatosDespacho.add(lblComuna);
        panelDatosDespacho.add(campoComunaEnvio);
        panelDatosDespacho.add(lblDireccion);
        panelDatosDespacho.add(campoDireccionEnvio);
        panelDatosDespacho.setVisible(false);

        labelPlazoEnvio = new JLabel("Retiro disponible hoy en tienda.");
        labelPlazoEnvio.setForeground(AZUL_MARINO);
        labelPlazoEnvio.setFont(new Font("SansSerif", Font.ITALIC, 12));

        comboModalidadEntrega.addActionListener(e -> {
            boolean esDespacho = "Despacho a domicilio".equals(comboModalidadEntrega.getSelectedItem());
            panelDatosDespacho.setVisible(esDespacho);
            actualizarVista();
        });
        comboRegionEnvio.addActionListener(e -> actualizarVista());

        seccionEntrega.add(filaModalidad, BorderLayout.NORTH);
        seccionEntrega.add(panelDatosDespacho, BorderLayout.CENTER);
        seccionEntrega.add(labelPlazoEnvio, BorderLayout.SOUTH);

        // Panel derecho: cupón y resumen de precios
        JPanel detalleCompra = new JPanel(new BorderLayout(8, 8));
        detalleCompra.setBackground(FONDO);
        JPanel seccionCupon = new JPanel(new BorderLayout(8, 6));
        seccionCupon.setBackground(MARFIL);
        seccionCupon.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 203, 184)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JLabel tituloCupon = new JLabel("¿Tienes un cupón de descuento?");
        tituloCupon.setForeground(TEXTO);
        tituloCupon.setFont(new Font("SansSerif", Font.BOLD, 13));
        Cupon cuponActual = carrito.getCupon();
        campoCupon = new JTextField(cuponActual == null ? "" : cuponActual.getCodigo(), 14);
        JButton botonAplicarCupon = new JButton("Aplicar");
        estilizarBoton(botonAplicarCupon, AZUL_MARINO, MARFIL);
        botonAplicarCupon.addActionListener(e -> aplicarCupon());
        JButton botonQuitarCupon = new JButton("Quitar");
        estilizarBoton(botonQuitarCupon, SUPERFICIE, TEXTO);
        botonQuitarCupon.addActionListener(e -> quitarCupon());
        JPanel controlesCupon = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controlesCupon.setBackground(MARFIL);
        controlesCupon.add(campoCupon);
        controlesCupon.add(botonAplicarCupon);
        controlesCupon.add(botonQuitarCupon);
        seccionCupon.add(tituloCupon, BorderLayout.NORTH);
        seccionCupon.add(controlesCupon, BorderLayout.CENTER);

        JPanel resumen = new JPanel(new GridLayout(5, 2, 12, 6));
        resumen.setBackground(MARFIL);
        resumen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 203, 184)),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        JLabel labelSubtotal = new JLabel("Subtotal");
        JLabel labelDescuento = new JLabel("Descuento");
        JLabel labelEnvio = new JLabel("Envío");
        JLabel labelIVA = new JLabel("IVA (19%)");
        JLabel labelTotalTexto = new JLabel("Total");
        valorSubtotalCarrito = new JLabel("$0", SwingConstants.RIGHT);
        valorDescuentoCarrito = new JLabel("$0", SwingConstants.RIGHT);
        valorEnvioCarrito = new JLabel("$0", SwingConstants.RIGHT);
        valorIVACarrito = new JLabel("$0", SwingConstants.RIGHT);
        labelTotal = new JLabel("$0", SwingConstants.RIGHT);
        for (JLabel etiqueta : new JLabel[] {labelSubtotal, labelDescuento, labelEnvio, labelIVA}) {
            etiqueta.setForeground(TEXTO_CLARO);
            etiqueta.setFont(new Font("SansSerif", Font.PLAIN, 14));
        }
        for (JLabel valor : new JLabel[] {valorSubtotalCarrito, valorDescuentoCarrito, valorEnvioCarrito, valorIVACarrito}) {
            valor.setForeground(TEXTO);
            valor.setFont(new Font("SansSerif", Font.PLAIN, 14));
        }
        labelTotalTexto.setForeground(TEXTO);
        labelTotalTexto.setFont(new Font("SansSerif", Font.BOLD, 17));
        labelTotal.setForeground(AZUL_MARINO);
        labelTotal.setFont(new Font("SansSerif", Font.BOLD, 20));
        resumen.add(labelSubtotal);
        resumen.add(valorSubtotalCarrito);
        resumen.add(labelDescuento);
        resumen.add(valorDescuentoCarrito);
        resumen.add(labelEnvio);
        resumen.add(valorEnvioCarrito);
        resumen.add(labelIVA);
        resumen.add(valorIVACarrito);
        resumen.add(labelTotalTexto);
        resumen.add(labelTotal);
        detalleCompra.add(seccionCupon, BorderLayout.NORTH);
        detalleCompra.add(resumen, BorderLayout.CENTER);

        zonaMedios.add(seccionEntrega);
        zonaMedios.add(detalleCompra);
        inferior.add(zonaMedios, BorderLayout.CENTER);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pie.setBackground(FONDO);
        JButton btnCerrar = new JButton("← Volver al catálogo");
        estilizarBoton(btnCerrar, AZUL_MARINO, MARFIL);
        btnCerrar.addActionListener(e -> volverAlCatalogo());
        pie.add(btnCerrar);
        botonVaciarCarrito = new JButton("Vaciar carrito");
        estilizarBoton(botonVaciarCarrito, SUPERFICIE, TEXTO);
        botonVaciarCarrito.addActionListener(e -> {
            if (carrito.getProductos().isEmpty()) {
                JOptionPane.showMessageDialog(ventanaActiva(), "El carrito ya está vacío.",
                        "Carrito", JOptionPane.WARNING_MESSAGE);
                return;
            }
            carrito.vaciarCarrito();
            inventario.guardarEnArchivo();
            temporizador.reiniciar();
            campoCupon.setText("");
            actualizarVista();
        });
        pie.add(botonVaciarCarrito);
        botonQuitarUnidadCarrito = new JButton("Quitar unidad");
        estilizarBoton(botonQuitarUnidadCarrito, SUPERFICIE, TEXTO);
        botonQuitarUnidadCarrito.setToolTipText("Quita una unidad del producto seleccionado.");
        botonQuitarUnidadCarrito.addActionListener(e -> {
            int filaSeleccionada = tablaCarrito.getSelectedRow();
            if (filaSeleccionada < 0) {
                JOptionPane.showMessageDialog(ventanaActiva(), "Selecciona un producto para quitar una unidad.",
                        "Carrito", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            carrito.quitarUnidad(tablaCarrito.convertRowIndexToModel(filaSeleccionada));
            inventario.guardarEnArchivo();
            temporizador.reiniciar();
            actualizarVista();
        });
        pie.add(botonQuitarUnidadCarrito);
        botonComprarCarrito = new JButton("Comprar");
        estilizarBoton(botonComprarCarrito, NARANJA, MARFIL);
        botonComprarCarrito.addActionListener(e -> comprarProductos());
        botonComprarCarrito.setFont(new Font("SansSerif", Font.BOLD, 14));
        pie.add(botonComprarCarrito);
        inferior.add(pie, BorderLayout.SOUTH);
        panel.add(inferior, BorderLayout.SOUTH);
        recargarRegionesEnvio();
        return panel;
    }

    private String formatearPrecio(double monto) {
        return "$" + NumberFormat.getIntegerInstance(new Locale("es", "CL"))
                .format(Math.round(monto));
    }


    // le da el mismo estilo a los botones para no repetir codigo
    private void estilizarBoton(JButton boton, Color fondo, Color texto) {
        boton.setBackground(fondo);
        boton.setForeground(texto);
        boton.setFocusPainted(false);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    // borra las tarjetas y pone una por cada producto recibido
    private void cargarProductosEnGrilla(java.util.List<Producto> productos) {
        grillaCatalogo.removeAll();
        for (Producto p : productos) {
            grillaCatalogo.add(crearTarjeta(p));
        }
        grillaCatalogo.revalidate();
        grillaCatalogo.repaint();
    }

    private void agregarAlCarrito(Producto producto) {
        // el producto ahora llega desde el boton de su tarjeta
        int cantidad;
        try {
            cantidad = Integer.parseInt(campoCantidad.getText().trim());
            if (cantidad <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número mayor a 0.", "Cantidad inválida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cantidad > producto.getStock()) {
            JOptionPane.showMessageDialog(this,
                    "La cantidad ingresada es mayor a la que existe. Solo hay " + producto.getStock() + " unidad(es) disponibles de "
                            + producto.getNombre() + ".",
                    "Cantidad inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // cuenta cuantas unidades se lograron agregar de verdad, por si el stock no alcanza
        int agregadas = 0;
        for (int i = 0; i < cantidad; i++) {
            if (!carrito.agregarProducto(producto)) {
                break;
            }
            agregadas++;
        }
        inventario.guardarEnArchivo(); // persiste la reserva
        temporizador.reiniciar();

        if (agregadas == 0) {
            JOptionPane.showMessageDialog(this, "No hay stock suficiente para ese producto.", "Stock insuficiente",
                    JOptionPane.WARNING_MESSAGE);
        }

        campoCantidad.setText("1");
        actualizarVista();
    }

    private void comprarProductos() {
        // Calcula los importes, registra la venta y persiste el stock actualizado.
        if (carrito.getProductos().isEmpty()) {
            JOptionPane.showMessageDialog(ventanaActiva(), "El carrito está vacío.", "Compra",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rutCliente = usuario == null ? "" : usuario.getRut();

        GestorCupones gestorCupones = null;
        Cupon cuponCompra = carrito.getCupon();
        if (cuponCompra != null) {
            try {
                gestorCupones = new GestorCupones();
                cuponCompra = gestorCupones.buscar(cuponCompra.getCodigo());
                String motivo = cuponCompra == null ? "El cupón ya no está disponible."
                        : carrito.aplicarCupon(cuponCompra);
                if (motivo != null) {
                    quitarCupon();
                    JOptionPane.showMessageDialog(ventanaActiva(), motivo, "Cupón no disponible",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ventanaActiva(),
                        "No se pudieron cargar los cupones: " + ex.getMessage(),
                        "Error al cargar cupones", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String modalidad = comboModalidadEntrega == null
                ? "Retiro en tienda" : comboModalidadEntrega.getSelectedItem().toString();
        String direccionFinal = "Retiro en tienda";
        if ("Despacho a domicilio".equals(modalidad)) {
            TarifaEnvio tarifa = obtenerTarifaSeleccionada();
            if (tarifa == null) {
                JOptionPane.showMessageDialog(ventanaActiva(),
                        "No hay regiones de envío configuradas.", "Envío no disponible",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String comuna = campoComunaEnvio.getText().trim();
            String calle = campoDireccionEnvio.getText().trim();
            if (comuna.isEmpty() || calle.isEmpty()) {
                JOptionPane.showMessageDialog(ventanaActiva(),
                        "Ingresa la comuna y dirección para el despacho a domicilio.",
                        "Falta dirección", JOptionPane.WARNING_MESSAGE);
                return;
            }
            direccionFinal = (calle + ", " + comuna + " (" + tarifa.getRegion() + ")").replace(";", ",");
        }

        double descuento = carrito.calcularDescuento();
        double subtotalNeto = carrito.calcularSubtotalNeto();
        double costoEnvio = calcularCostoEnvioActual();
        double iva = carrito.calcularIVA();
        double total = carrito.calcularTotal() + costoEnvio;

        // construye el detalle 
        StringBuilder detalles = new StringBuilder();
        for (Producto p : carrito.getProductos()) {
            int cant = carrito.getCantidad(p.getId());
            detalles.append(cant).append("x ").append(p.getNombre()).append(" | ");
        }
        if (cuponCompra != null) {
            detalles.append("Cupón ").append(cuponCompra.getCodigo()).append(" (-")
                    .append(formatearPrecio(descuento)).append(") | ");
        }

        // crea el gestor y genera una nueva venta 
        GestorVentas gestorVentas = new GestorVentas();
        String numeroTicket = usuario == null ? "" : usuario.getRut();
        java.util.Date fechaActual = new java.util.Date(); 
        String nombreCompleto = usuario == null ? "Invitado" : usuario.getNombre() + " " + usuario.getApellido();

        Venta nuevaVenta = new Venta(
            numeroTicket,
            fechaActual,
            rutCliente,
            nombreCompleto,
            usuario == null ? "" : usuario.getCorreo(),
            detalles.toString(),
            subtotalNeto,
            iva,
            total,
            cuponCompra == null ? "" : cuponCompra.getCodigo(),
            descuento,
            costoEnvio,
            modalidad,
            direccionFinal
        );

        // Guarda la venta antes de consumir el cupón para no descontar usos si falla el registro.
        try {
            if (usuario == null) {
                numeroTicket = gestorVentas.guardarVentaInvitado(nuevaVenta);
            } else {
                gestorVentas.guardarVenta(nuevaVenta);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(ventanaActiva(), "No se pudo guardar la venta: " + e.getMessage(),
                    "Error al guardar la venta", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cuponCompra != null) {
            try {
                gestorCupones.registrarUso(cuponCompra.getCodigo());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ventanaActiva(),
                        "La compra quedó registrada, pero no se pudo actualizar el uso del cupón: "
                                + ex.getMessage(),
                        "Error al actualizar cupón", JOptionPane.ERROR_MESSAGE);
            }
        }
        carrito.finalizarCompra();
        inventario.guardarEnArchivo();
        temporizador.reiniciar();
        campoCupon.setText("");
        
        JOptionPane.showMessageDialog(ventanaActiva(), "Tu ticket de venta es el número: " + numeroTicket,
                "Ticket de venta",
            JOptionPane.INFORMATION_MESSAGE);
        actualizarVista();
    }

    
    public void actualizarVista() {

        // Sincroniza catálogo, carrito y totales con los datos actuales de la compra.
        // vuelve a armar las tarjetas con lo que hay en inventario
        cargarProductosEnGrilla(inventario.getListaProductos());

        // Actualiza el contador del carrito
        int cantidadTotal = 0;

        for (Producto p : carrito.getProductos()) {
            cantidadTotal += carrito.getCantidad(p.getId());
    }

        if (botonCarrito != null) {
            botonCarrito.setText("Carrito (" + cantidadTotal + ")");
        }
        if (cantidadProductosCarrito != null) {
            cantidadProductosCarrito.setText(cantidadTotal + (cantidadTotal == 1 ? " producto" : " productos"));
        }
        boolean carritoTieneProductos = !carrito.getProductos().isEmpty();
        if (botonComprarCarrito != null) {
            botonComprarCarrito.setEnabled(carritoTieneProductos);
        }
        if (botonVaciarCarrito != null) {
            botonVaciarCarrito.setEnabled(carritoTieneProductos);
        }
        if (botonQuitarUnidadCarrito != null) {
            botonQuitarUnidadCarrito.setEnabled(carritoTieneProductos);
        }

        // Actualiza la tabla solo si fue creada al abrir el carrito
        if (modeloCarrito != null) {
            modeloCarrito.setRowCount(0);

            for (Producto p : carrito.getProductos()) {
                modeloCarrito.addRow(new Object[]{
                    p.getNombre(),
                    carrito.getCantidad(p.getId()),
                    formatearPrecio(carrito.calcularSubtotal(p.getId()))
                });
            }
        }

        double costoEnvio = calcularCostoEnvioActual();
        if (valorSubtotalCarrito != null && valorDescuentoCarrito != null && valorIVACarrito != null) {
            valorSubtotalCarrito.setText(formatearPrecio(carrito.calcularSubtotalCarrito()));
            valorDescuentoCarrito.setText(formatearPrecio(carrito.calcularDescuento()));
            if (valorEnvioCarrito != null) {
                boolean esDespacho = comboModalidadEntrega != null
                        && "Despacho a domicilio".equals(comboModalidadEntrega.getSelectedItem());
                if (!esDespacho || carrito.getProductos().isEmpty()) {
                    valorEnvioCarrito.setText("$0");
                } else {
                    valorEnvioCarrito.setText(costoEnvio == 0 ? "$0 (Gratis)" : formatearPrecio(costoEnvio));
                }
            }
            valorIVACarrito.setText(formatearPrecio(carrito.calcularIVA()));
        }
        if (labelTotal != null) {
            labelTotal.setText(formatearPrecio(carrito.calcularTotal() + costoEnvio));
        }
        actualizarInfoPlazoEnvio();
        if (contenidoCarrito != null && tarjetasCarrito != null) {
            tarjetasCarrito.show(contenidoCarrito,
                    carrito.getProductos().isEmpty() ? "vacio" : "productos");
        }
    }

    private void recargarRegionesEnvio() {
        if (comboRegionEnvio == null) {
            return;
        }
        gestorEnvios.cargar();
        Object seleccionPrevia = comboRegionEnvio.getSelectedItem();
        comboRegionEnvio.removeAllItems();
        for (TarifaEnvio t : gestorEnvios.getTarifas()) {
            comboRegionEnvio.addItem(t.getRegion());
        }
        if (seleccionPrevia != null) {
            comboRegionEnvio.setSelectedItem(seleccionPrevia);
        }
    }

    private TarifaEnvio obtenerTarifaSeleccionada() {
        if (comboRegionEnvio == null || comboRegionEnvio.getSelectedItem() == null) {
            return null;
        }
        String region = comboRegionEnvio.getSelectedItem().toString();
        for (TarifaEnvio t : gestorEnvios.getTarifas()) {
            if (t.getRegion().equalsIgnoreCase(region)) {
                return t;
            }
        }
        return null;
    }

    private double calcularCostoEnvioActual() {
        if (carrito.getProductos().isEmpty() || comboModalidadEntrega == null
                || !"Despacho a domicilio".equals(comboModalidadEntrega.getSelectedItem())) {
            return 0.0;
        }
        TarifaEnvio tarifa = obtenerTarifaSeleccionada();
        if (tarifa == null) {
            return 0.0;
        }
        double minimoGratis = gestorEnvios.getMontoMinimoGratis();
        if (minimoGratis > 0 && carrito.calcularSubtotalNeto() >= minimoGratis) {
            return 0.0;
        }
        return tarifa.getCosto();
    }

    private void actualizarInfoPlazoEnvio() {
        if (labelPlazoEnvio == null || comboModalidadEntrega == null) {
            return;
        }
        if (!"Despacho a domicilio".equals(comboModalidadEntrega.getSelectedItem())) {
            labelPlazoEnvio.setText("Retiro disponible hoy en tienda.");
            return;
        }
        TarifaEnvio tarifa = obtenerTarifaSeleccionada();
        if (tarifa == null) {
            labelPlazoEnvio.setText("Sin regiones de envío disponibles.");
            return;
        }
        LocalDate fechaEstimada = sumarDiasHabiles(LocalDate.now(), tarifa.getDiasHabiles());
        String fechaTexto = fechaEstimada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        double minimoGratis = gestorEnvios.getMontoMinimoGratis();
        String extraGratis = "";
        if (minimoGratis > 0 && carrito.calcularSubtotalNeto() >= minimoGratis) {
            extraGratis = " — ¡Envío GRATIS aplicado!";
        } else if (minimoGratis > 0) {
            extraGratis = " (Gratis desde " + formatearPrecio(minimoGratis) + ")";
        }
        labelPlazoEnvio.setText("Plazo: " + tarifa.getDiasHabiles()
                + " días hábiles (aprox. " + fechaTexto + ")" + extraGratis);
    }

    private LocalDate sumarDiasHabiles(LocalDate inicio, int diasHabiles) {
        LocalDate fecha = inicio;
        int agregados = 0;
        while (agregados < diasHabiles) {
            fecha = fecha.plusDays(1);
            DayOfWeek dia = fecha.getDayOfWeek();
            if (dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY) {
                agregados++;
            }
        }
        return fecha;
    }
    private void mostrarVentanaDeseos() {
        if (usuario == null) {
            JOptionPane.showMessageDialog(this, "Inicia sesión para ver tu lista de deseos.");
            return;
        }

        JDialog ventanaDeseos = new JDialog(this, "Lista de Deseos", true);
        ventanaDeseos.setSize(500, 400);
        ventanaDeseos.setLocationRelativeTo(this);
        ventanaDeseos.setLayout(new BorderLayout(10, 10));

        DefaultTableModel modeloDeseos = new DefaultTableModel(new Object[]{"ID", "Producto", "Precio", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tablaDeseos = new JTable(modeloDeseos);

        for (String idProd : usuario.getListaDeseos()) {
            Producto p = inventario.buscarProducto(idProd);
            if (p != null) {
                modeloDeseos.addRow(new Object[]{p.getId(), p.getNombre(), "$" + p.getPrecio(), p.getStock()});
            }
        }

        JButton btnMoverCarrito = new JButton("Mover al carrito");
        estilizarBoton(btnMoverCarrito, AMARILLO, TEXTO);
        btnMoverCarrito.addActionListener(e -> {
            int fila = tablaDeseos.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(ventanaDeseos, "Selecciona un producto de la lista.");
                return;
            }
            String idProd = modeloDeseos.getValueAt(fila, 0).toString();
            Producto p = inventario.buscarProducto(idProd);
            if (p != null) {
                if (p.getStock() <= 0) {
                    JOptionPane.showMessageDialog(ventanaDeseos, "Este producto no tiene stock disponible.");
                    return;
                }
                agregarAlCarrito(p);
                usuario.quitarDeseo(idProd);
                GestorIntereses.eliminarRegistro("deseos.csv", usuario.getCorreo(), idProd);
                ventanaDeseos.dispose();
                mostrarVentanaDeseos();
            }
        });

        ventanaDeseos.add(new JScrollPane(tablaDeseos), BorderLayout.CENTER);
        JPanel panelBajo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBajo.add(btnMoverCarrito);
        ventanaDeseos.add(panelBajo, BorderLayout.SOUTH);
        ventanaDeseos.setVisible(true);
        
    }
    private void aplicarCupon() {
        if (carrito.getProductos().isEmpty()) {
            JOptionPane.showMessageDialog(ventanaActiva(), "Agrega productos antes de aplicar un cupón.",
                    "Carrito vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String codigo = campoCupon.getText().trim();
            Cupon cupon = new GestorCupones().buscar(codigo);
            String motivo = carrito.aplicarCupon(cupon);
            if (motivo != null) {
                Cupon cuponActual = carrito.getCupon();
                campoCupon.setText(cuponActual == null ? "" : cuponActual.getCodigo());
                JOptionPane.showMessageDialog(ventanaActiva(), motivo, "Cupón no disponible",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            campoCupon.setText(cupon.getCodigo());
            actualizarVista();
            JOptionPane.showMessageDialog(ventanaActiva(),
                    "Cupón aplicado. Ahorras "
                            + formatearPrecio(carrito.calcularDescuento()) + ".",
                    "Descuento aplicado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(ventanaActiva(), "No se pudieron cargar los cupones: " + ex.getMessage(),
                    "Error al cargar cupones", JOptionPane.ERROR_MESSAGE);
        }
    
    }

    private void quitarCupon() {
        carrito.quitarCupon();
        if (campoCupon != null) {
            campoCupon.setText("");
        }
        actualizarVista();
    }

    private void conectarConAdmin() {
        if (!esIpv4Valida(IP_ADMIN)) {
            JOptionPane.showMessageDialog(this,
                    "Completa la IPv4 del equipo ADMIN en la constante IP_ADMIN de VentanaUsuario.",
                    "Falta configurar la IP de ADMIN", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ClienteTiendaSocket[] referenciaCliente = new ClienteTiendaSocket[1];
        ClienteTiendaSocket nuevoCliente = new ClienteTiendaSocket(new ClienteTiendaSocket.Escucha() {
            private boolean sincronizacionInicial = true;

            @Override
            public void alConectar() {
                SwingUtilities.invokeLater(() -> {
                    sincronizacionInicial = false;
                });
            }

            @Override
            public void alActualizarProducto(Producto producto) {
                SwingUtilities.invokeLater(() -> {
                    int stockAnterior = inventario.aplicarActualizacionRemota(producto);
                    actualizarVista();
                    if (!sincronizacionInicial && stockAnterior == 0 && producto.getStock() > 0) {
                        JOptionPane.showMessageDialog(VentanaUsuario.this,
                                "Hay stock disponible de " + producto.getNombre() + ".",
                                "Reposición disponible", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }

            @Override
            public void alEliminarProducto(String idProducto) {
                SwingUtilities.invokeLater(() -> {
                    inventario.eliminarProductoRemoto(idProducto);
                    actualizarVista();
                });
            }

            @Override
            public void alDesconectar(IOException error) {
                SwingUtilities.invokeLater(() -> {
                    if (clienteSocket == referenciaCliente[0]) {
                        clienteSocket = null;
                        JOptionPane.showMessageDialog(VentanaUsuario.this,
                                "Se perdió la conexión con ADMIN: " + error.getMessage(),
                                "Conexión interrumpida", JOptionPane.WARNING_MESSAGE);
                    }
                });
            }
        });
        referenciaCliente[0] = nuevoCliente;
        clienteSocket = nuevoCliente;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                nuevoCliente.conectar(IP_ADMIN, ServidorTiendaSocket.PUERTO_PREDETERMINADO);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    nuevoCliente.close();
                    if (clienteSocket == nuevoCliente) {
                        clienteSocket = null;
                    }
                    JOptionPane.showMessageDialog(VentanaUsuario.this,
                            "Se interrumpió la conexión con ADMIN.",
                            "Error de conexión", JOptionPane.ERROR_MESSAGE);
                } catch (java.util.concurrent.ExecutionException ex) {
                    nuevoCliente.close();
                    if (clienteSocket == nuevoCliente) {
                        clienteSocket = null;
                    }
                    Throwable causa = ex.getCause();
                    String detalle = causa == null || causa.getMessage() == null
                            ? ex.getMessage() : causa.getMessage();
                    JOptionPane.showMessageDialog(VentanaUsuario.this,
                            "No se pudo conectar con ADMIN: " + detalle,
                            "Error de conexión", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static boolean esIpv4Valida(String direccion) {
        String[] partes = direccion.split("\\.", -1);
        if (partes.length != 4) {
            return false;
        }
        for (String parte : partes) {
            if (parte.isEmpty() || !parte.matches("\\d{1,3}")) {
                return false;
            }
            int octeto = Integer.parseInt(parte);
            if (octeto > 255) {
                return false;
            }
        }
        return true;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaUsuario().setVisible(true));
    }
}
