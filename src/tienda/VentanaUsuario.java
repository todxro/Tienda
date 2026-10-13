package tienda;

import java.awt.*;
import java.io.IOException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

// Presenta el catálogo, la compra, el inicio de sesión y el chat del cliente.
public class VentanaUsuario extends JFrame {
    private static final String HOST_CHAT = "172.26.39.8";
    private static final int PUERTO_CHAT = 65432;
    private static final Color AZUL_MARINO = new Color(46, 31, 71);   //morado oscuro
    private static final Color DORADO      = new Color(232, 184, 75); //dorado
    private static final Color FONDO       = new Color(18, 16, 27);   //fondo casi negro
    private static final Color TEXTO       = new Color(61, 43, 0);    //texto oscuro
    private static final Color TEXTO_CLARO = new Color(217, 210, 232);//texto claro
    private static final Color BLANCO      = Color.WHITE;
    private Usuario usuario;
    private Carrito carrito;
    private final Inventario inventario;
    private final GestorUsuarios gestorUsuarios;
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel paneles = new JPanel(tarjetas);

    // componentes de la interfaz
    private JTable tablaCatalogo;
    private DefaultTableModel modeloCatalogo;
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;
    private JTextField campoCantidad;
    private JLabel labelSubtotal;
    private JLabel labelIVA;
    private JLabel labelTotal;
    private JButton botonIniciarSesion;
    private final JTextArea areaChat = new JTextArea();
    private final JLabel estadoChat = new JLabel("Desconectado");
    private final JTextField campoMensajeChat = new JTextField();
    private final JButton botonEnviarChat = new JButton("Enviar");
    private final Queue<String> mensajesPendientesChat = new ArrayDeque<>();
    private JDialog ventanaChat;
    private volatile Socket socketChat;
    private volatile PrintWriter salidaChat;
    private volatile boolean clienteChatCerrado;
    private volatile boolean chatConectando;

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
        setContentPane(paneles);
        // Las tarjetas permiten cambiar de sección dentro de la misma ventana.
        paneles.add(crearPanelLogin(), "login");
        mostrarCatalogo();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarClienteChat();
            }
        });
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
        estilizarBoton(crearCuenta, AZUL_MARINO, BLANCO);
        estilizarBoton(continuarComoInvitado, AZUL_MARINO, BLANCO);
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
        catalogo.add(crearPanelCarrito(), BorderLayout.SOUTH);
        paneles.add(catalogo, "catalogo");
        tarjetas.show(paneles, "catalogo");
        getRootPane().setDefaultButton(null);
        actualizarVista();
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
        titulo.setForeground(BLANCO);
        panel.add(titulo, BorderLayout.WEST);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        busqueda.setOpaque(false);

        JLabel labelBuscar = new JLabel("Buscar:");
        labelBuscar.setForeground(BLANCO);

        JTextField campoBuscar = new JTextField(15);
        campoBuscar.setBackground(new Color(28, 24, 43));
        campoBuscar.setForeground(TEXTO_CLARO);
        campoBuscar.setCaretColor(TEXTO_CLARO);        JButton btnBuscar = new JButton("Buscar");
        JButton btnRefrescar = new JButton("Ver todo");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnChat = new JButton("Chat con admin");

        btnBuscar.addActionListener(e -> {
            String texto = campoBuscar.getText().trim();
            if (!texto.isEmpty()) {
                cargarProductosEnTabla(inventario.buscarPorNombre(texto));
            }
        });
        btnRefrescar.addActionListener(e -> actualizarVista());
        btnActualizar.addActionListener(e -> {
            inventario.recargarDesdeArchivo();
            actualizarVista();
        });
        estilizarBoton(btnChat, DORADO, TEXTO);
        btnChat.addActionListener(e -> mostrarVentanaChat());

        busqueda.add(labelBuscar);
        busqueda.add(campoBuscar);
        busqueda.add(btnBuscar);
        busqueda.add(btnRefrescar);
        busqueda.add(btnActualizar);
        busqueda.add(btnChat);
        JButton btnCuenta = new JButton("Cuenta");
        estilizarBoton(btnCuenta, DORADO, TEXTO);
        btnCuenta.addActionListener(e -> mostrarCuenta());
        busqueda.add(btnCuenta);

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
            saludo.setForeground(BLANCO);
            busqueda.add(saludo);

            JButton btnCerrarSesion = new JButton("Cerrar sesión");
            estilizarBoton(btnCerrarSesion, DORADO, TEXTO);
            btnCerrarSesion.addActionListener(e -> {
                usuario = null;
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

    private void mostrarVentanaChat() {
        if (ventanaChat == null) {
            ventanaChat = new JDialog(this, "Chat con administración", false);
            ventanaChat.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            ventanaChat.setContentPane(crearPanelChat());
            ventanaChat.setResizable(false);
            ventanaChat.setSize(480, 360);
            ventanaChat.setLocationRelativeTo(this);
        }
        ventanaChat.setVisible(true);
        ventanaChat.toFront();
        conectarChat();
    }

    private JPanel crearPanelChat() {
        // Vista del cliente para conectarse y conversar con el servidor de administración.
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel encabezado = new JPanel(new BorderLayout(8, 0));
        encabezado.setBackground(FONDO);
        JButton botonCerrar = new JButton("Cerrar chat");
        estilizarBoton(botonCerrar, AZUL_MARINO, BLANCO);
        botonCerrar.addActionListener(e -> ventanaChat.setVisible(false));

        encabezado.add(botonCerrar, BorderLayout.WEST);

        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        areaChat.setBackground(new Color(28, 24, 43));
        areaChat.setForeground(TEXTO_CLARO);
        areaChat.setCaretColor(TEXTO_CLARO);
        JScrollPane scrollChat = new JScrollPane(areaChat);
        scrollChat.getViewport().setBackground(new Color(28, 24, 43));

        JPanel panelEnvio = new JPanel(new BorderLayout(8, 0));
        panelEnvio.setBackground(FONDO);
        campoMensajeChat.setBackground(new Color(28, 24, 43));
        campoMensajeChat.setForeground(TEXTO_CLARO);
        campoMensajeChat.setCaretColor(TEXTO_CLARO);
        estilizarBoton(botonEnviarChat, DORADO, TEXTO);
        botonEnviarChat.setEnabled(false);
        botonEnviarChat.addActionListener(e -> enviarMensajeChat());
        campoMensajeChat.addActionListener(e -> enviarMensajeChat());
        panelEnvio.add(campoMensajeChat, BorderLayout.CENTER);
        panelEnvio.add(botonEnviarChat, BorderLayout.EAST);

        JPanel pie = new JPanel(new BorderLayout(8, 0));
        pie.setBackground(FONDO);
        estadoChat.setForeground(TEXTO_CLARO);
        pie.add(estadoChat, BorderLayout.WEST);
        pie.add(panelEnvio, BorderLayout.CENTER);

        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(scrollChat, BorderLayout.CENTER);
        panel.add(pie, BorderLayout.SOUTH);
        return panel;
    }

    private void conectarChat() {
        // Abre el socket en segundo plano para que la ventana siga respondiendo.
        if (chatConectando || (socketChat != null && !socketChat.isClosed())) {
            return;
        }
        clienteChatCerrado = false;
        chatConectando = true;
        botonEnviarChat.setEnabled(false);
        estadoChat.setText("Conectando...");

        Thread hiloConexion = new Thread(() -> {
            Socket nuevaConexion = new Socket();
            try {
                nuevaConexion.connect(new InetSocketAddress(HOST_CHAT, PUERTO_CHAT), 5000);
                PrintWriter nuevaSalida = new PrintWriter(
                        new OutputStreamWriter(nuevaConexion.getOutputStream(), StandardCharsets.UTF_8), true);
                if (clienteChatCerrado) {
                    nuevaConexion.close();
                    return;
                }
                socketChat = nuevaConexion;
                salidaChat = nuevaSalida;
                SwingUtilities.invokeLater(() -> {
                    estadoChat.setText("Conectado a " + HOST_CHAT + ":" + PUERTO_CHAT);
                    botonEnviarChat.setEnabled(true);
                    enviarMensajesPendientesChat();
                });

                Thread hiloRecepcion = new Thread(() -> recibirMensajesChat(nuevaConexion), "recepcion-chat");
                hiloRecepcion.setDaemon(true);
                hiloRecepcion.start();
            } catch (IOException ex) {
                try {
                    nuevaConexion.close();
                } catch (IOException errorCierre) {
                    ex.addSuppressed(errorCierre);
                }
                chatConectando = false;
                if (!clienteChatCerrado) {
                    SwingUtilities.invokeLater(() -> {
                        estadoChat.setText("No se pudo conectar: " + ex.getMessage());
                        areaChat.append("Error de conexión: " + ex.getMessage() + System.lineSeparator());
                    });
                }
            }
        }, "conexion-chat");
        hiloConexion.setDaemon(true);
        hiloConexion.start();
    }

    private void enviarMensajeChat() {
        String mensaje = campoMensajeChat.getText().trim();
        if (mensaje.isEmpty()) {
            return;
        }
        campoMensajeChat.setText("");
        areaChat.append("Tú: " + mensaje + System.lineSeparator());

        if (salidaChat == null || socketChat == null || socketChat.isClosed()) {
            synchronized (mensajesPendientesChat) {
                mensajesPendientesChat.add(mensaje);
            }
            estadoChat.setText("Mensaje pendiente; conectando...");
            conectarChat();
            return;
        }

        try {
            enviarMensajeAlServidor(crearMensajeJson(mensaje));
        } catch (IOException ex) {
            synchronized (mensajesPendientesChat) {
                mensajesPendientesChat.add(mensaje);
            }
            estadoChat.setText("Mensaje pendiente; reconectando...");
            desconectarServidorChat();
            conectarChat();
        }
    }

    private void enviarMensajesPendientesChat() {
        while (salidaChat != null && socketChat != null && !socketChat.isClosed()) {
            String mensaje;
            synchronized (mensajesPendientesChat) {
                mensaje = mensajesPendientesChat.peek();
            }
            if (mensaje == null) {
                return;
            }
            try {
                enviarMensajeAlServidor(crearMensajeJson(mensaje));
                synchronized (mensajesPendientesChat) {
                    mensajesPendientesChat.remove();
                }
            } catch (IOException ex) {
                estadoChat.setText("No se pudo enviar el mensaje: " + ex.getMessage());
                desconectarServidorChat();
                return;
            }
        }
    }

    private void recibirMensajesChat(Socket socket) {
        // Escucha mensajes del administrador y actualiza la conversación en pantalla.
        try (BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                String contenido = extraerCampoJson(mensaje, "content");
                if ("message".equals(extraerCampoJson(mensaje, "type")) && contenido != null) {
                    SwingUtilities.invokeLater(
                            () -> areaChat.append("Admin: " + contenido + System.lineSeparator()));
                }
            }
        } catch (IOException ex) {
            if (!clienteChatCerrado && socketChat == socket) {
                SwingUtilities.invokeLater(
                        () -> estadoChat.setText("Conexión interrumpida: " + ex.getMessage()));
            }
        } finally {
            boolean conexionActual = socketChat == socket;
            if (socketChat == socket) {
                socketChat = null;
                salidaChat = null;
            }
            try {
                socket.close();
            } catch (IOException ex) {
                if (!clienteChatCerrado && conexionActual) {
                    SwingUtilities.invokeLater(
                            () -> estadoChat.setText("No se pudo cerrar la conexión: " + ex.getMessage()));
                }
            }
            if (!clienteChatCerrado && conexionActual) {
                chatConectando = false;
                SwingUtilities.invokeLater(() -> {
                    estadoChat.setText("Desconectado");
                    botonEnviarChat.setEnabled(false);
                });
            }
        }
    }

    private String crearMensajeJson(String contenido) {
        // Empaqueta cada texto con el formato JSON que entiende el servidor.
        return "{\"type\":\"message\",\"content\":\"" + escaparJson(contenido) + "\"}";
    }

    private String extraerCampoJson(String json, String campo) {
        Pattern patron = Pattern.compile("\"" + Pattern.quote(campo)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher coincidencia = patron.matcher(json);
        return coincidencia.find() ? desescaparJson(coincidencia.group(1)) : null;
    }

    private String escaparJson(String texto) {
        return texto.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String desescaparJson(String texto) {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char caracter = texto.charAt(i);
            if (caracter == '\\' && i + 1 < texto.length()) {
                char escapado = texto.charAt(++i);
                switch (escapado) {
                    case 'n': resultado.append('\n'); break;
                    case 'r': resultado.append('\r'); break;
                    case 't': resultado.append('\t'); break;
                    default: resultado.append(escapado); break;
                }
            } else {
                resultado.append(caracter);
            }
        }
        return resultado.toString();
    }

    private void enviarMensajeAlServidor(String mensaje) throws IOException {
        PrintWriter salida = salidaChat;
        Socket socket = socketChat;
        if (salida == null || socket == null || socket.isClosed()) {
            throw new IOException("No hay conexión con el administrador.");
        }
        salida.println(mensaje);
        if (salida.checkError()) {
            throw new IOException("No se pudo enviar el mensaje al administrador.");
        }
    }

    private void desconectarServidorChat() {
        clienteChatCerrado = true;
        chatConectando = false;
        Socket socket = socketChat;
        socketChat = null;
        salidaChat = null;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                estadoChat.setText("No se pudo cerrar la conexión: " + ex.getMessage());
            }
        }
    }

    private void cerrarClienteChat() {
        desconectarServidorChat();
    }

    // arma la tabla del catalogo en el centro de la ventana
    private JPanel crearPanelCatalogo() {
        // Tabla central con los productos disponibles para seleccionar.
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        String[] columnas = {"ID", "Producto", "Precio", "Stock"};
        modeloCatalogo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tablaCatalogo = new JTable(modeloCatalogo);
        tablaCatalogo.setRowHeight(28);
        tablaCatalogo.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaCatalogo.getTableHeader().setBackground(AZUL_MARINO);
        tablaCatalogo.getTableHeader().setForeground(BLANCO);
        tablaCatalogo.setSelectionBackground(DORADO);
        tablaCatalogo.setSelectionForeground(TEXTO);
        tablaCatalogo.setGridColor(new Color(60, 52, 80));
        tablaCatalogo.setBackground(new Color(28, 24, 43));
        tablaCatalogo.setForeground(TEXTO_CLARO);

        JScrollPane scrollCatalogo = new JScrollPane(tablaCatalogo);
        scrollCatalogo.getViewport().setBackground(new Color(28, 24, 43));
        panel.add(scrollCatalogo, BorderLayout.CENTER);
        return panel;
    }

    // arma el panel de abajo con el carrito y los botones
    private JPanel crearPanelCarrito() {
        // Panel inferior con cantidades, acciones de compra y resumen de precios.
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));

        String[] columnas = {"Producto", "Cantidad", "Subtotal"};
        modeloCarrito = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setRowHeight(24);
        tablaCarrito.getTableHeader().setBackground(AZUL_MARINO);
        tablaCarrito.getTableHeader().setForeground(BLANCO);
        tablaCarrito.setBackground(new Color(28, 24, 43));
        tablaCarrito.setForeground(TEXTO_CLARO);
        JScrollPane scrollCarrito = new JScrollPane(tablaCarrito);
        scrollCarrito.getViewport().setBackground(new Color(28, 24, 43));
        scrollCarrito.setPreferredSize(new Dimension(0, 140));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controles.setBackground(FONDO);

        JLabel labelCantidad = new JLabel("Cantidad:");
        labelCantidad.setForeground(TEXTO_CLARO);
        controles.add(labelCantidad);
        campoCantidad = new JTextField("1", 4);
        campoCantidad.setBackground(new Color(28, 24, 43));
        campoCantidad.setForeground(TEXTO_CLARO);
        campoCantidad.setCaretColor(TEXTO_CLARO);        controles.add(campoCantidad);

        JButton btnAgregar = new JButton("Agregar al carrito");
        estilizarBoton(btnAgregar, DORADO, TEXTO);
        btnAgregar.addActionListener(e -> agregarAlCarrito());
        controles.add(btnAgregar);

        JButton btnComprar = new JButton("Comprar");
        estilizarBoton(btnComprar, AZUL_MARINO, BLANCO);
        btnComprar.addActionListener(e -> comprarProductos());
        controles.add(btnComprar);

        labelSubtotal = new JLabel("Subtotal: $0");
        labelSubtotal.setForeground(TEXTO_CLARO);
        controles.add(labelSubtotal);

        labelIVA = new JLabel("IVA (19%): $0");
        labelIVA.setForeground(TEXTO_CLARO);
        controles.add(labelIVA);

        labelTotal = new JLabel("TOTAL: $0");
        labelTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        labelTotal.setForeground(DORADO);
        controles.add(labelTotal);

        panel.add(controles, BorderLayout.NORTH);
        panel.add(scrollCarrito, BorderLayout.CENTER);
        return panel;
    }

    // le da el mismo estilo a los botones para no repetir codigo
    private void estilizarBoton(JButton boton, Color fondo, Color texto) {
        boton.setBackground(fondo);
        boton.setForeground(texto);
        boton.setFocusPainted(false);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    // llena la tabla con la lista de productos recibida
    private void cargarProductosEnTabla(java.util.List<Producto> productos) {
        // Reemplaza las filas del catálogo con los productos recibidos.
        modeloCatalogo.setRowCount(0);
        for (Producto p : productos) {
            modeloCatalogo.addRow(new Object[]{
                    p.getId(), p.getNombre(), "$" + p.getPrecio(), p.getStock()
            });
        }
    }

    private void agregarAlCarrito() {
        // toma el producto seleccionado en la tabla en vez de un id escrito a mano
        int fila = tablaCatalogo.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla.",
                    "Falta selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) modeloCatalogo.getValueAt(fila, 0);
        Producto producto = inventario.buscarProducto(id);

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
            JOptionPane.showMessageDialog(this, "El carrito está vacío.", "Compra",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rutCliente = usuario == null ? "" : usuario.getRut();

        // extrae los datos
        double subtotal = carrito.calcularSubtotalCarrito();
        double iva = carrito.calcularIVA();
        double total = carrito.calcularTotal();

        // construye el detalle 
        StringBuilder detalles = new StringBuilder();
        for (Producto p : carrito.getProductos()) {
            int cant = carrito.getCantidad(p.getId());
            detalles.append(cant).append("x ").append(p.getNombre()).append(" | ");
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
            subtotal,
            iva,
            total
        );

        // procesa la compra en el sistema 
        boolean compraExitosa = carrito.finalizarCompra();

        if (!compraExitosa) {
            JOptionPane.showMessageDialog(this, "No hay stock suficiente para completar la compra.", "Compra fallida",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // guarda la venta y el inventario actualizados
        // guarda la venta y el inventario actualizados 
        try {
            if (usuario == null) {
                numeroTicket = gestorVentas.guardarVentaInvitado(nuevaVenta);
            } else {
                gestorVentas.guardarVenta(nuevaVenta);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar la venta: " + e.getMessage(),
                    "Error al guardar la venta", JOptionPane.ERROR_MESSAGE);
            return;
        }
        inventario.guardarEnArchivo();
        
        JOptionPane.showMessageDialog(this, "Tu ticket de venta es el número: " + numeroTicket,
                "Ticket de venta",
            JOptionPane.INFORMATION_MESSAGE);
        actualizarVista();
    }

    public void actualizarVista() {
        // Sincroniza catálogo, carrito y totales con los datos actuales de la compra.
        // vuelve a llenar la tabla del catalogo con lo que hay en inventario
        cargarProductosEnTabla(inventario.getListaProductos());

        // vuelve a llenar la tabla del carrito
        modeloCarrito.setRowCount(0);
        for (Producto p : carrito.getProductos()) {
            modeloCarrito.addRow(new Object[]{
                    p.getNombre(),
                    carrito.getCantidad(p.getId()),
                    "$" + carrito.calcularSubtotal(p.getId())
            });
        }

        labelSubtotal.setText("Subtotal: $" + carrito.calcularSubtotalCarrito());
        labelIVA.setText("IVA (19%): $" + carrito.calcularIVA());
        labelTotal.setText("TOTAL: $" + carrito.calcularTotal());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaUsuario().setVisible(true));
    }
}