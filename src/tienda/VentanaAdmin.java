package tienda;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;


// Reúne las vistas y acciones con las que administración mantiene la tienda.
// Índice por línea:
// - Inicio y navegación: constructor VentanaAdmin (88), mostrarVentas (512)
// - Productos e imágenes: seleccionarImagenProducto (250), agregarProducto (575),
//   actualizarProducto (594), borrarProducto (616), idSeleccionado (633), cargarSeleccion (642)
// - Chat de administración: mostrarVentanaChat (237), crearPanelChat (299), enviarMensajeChat (333),
//   conexión y mensajes (348-507), formato JSON (419-449)
// - Interfaz y formularios: estilizarBotonStock (532), agregarCampo (540), agregarBoton (560),
//   actualizarVista (657), refrescarTabla (661), categoriaIngresada (671), limpiarCampos (676),
//   mostrarSeleccion (684), mostrarDatosInvalidos (689)
// - Inicio de la aplicación: main (695)
public class VentanaAdmin extends JFrame {
    private static final String HOST_CHAT = "172.26.39.8";
    private static final int PUERTO_CHAT = 65432;
    private static final Color AZUL = new Color(34, 96, 176);
    private static final Color AZUL_MARINO = new Color(18, 67, 121);
    private static final Color AMARILLO = new Color(250, 204, 76);
    private static final Color DORADO = AMARILLO;
    private static final Color FONDO = new Color(232, 217, 195);
    private static final Color SUPERFICIE = new Color(217, 197, 168);
    private static final Color TEXTO = new Color(25, 52, 76);
    private static final Color TEXTO_CLARO = new Color(72, 98, 126);
    private static final Color MARFIL = new Color(246, 233, 207);
    private final Inventario inventario;
    private final DefaultTableModel modeloTabla;
    private final JTable tablaProductos;
    private final JTextField txtNombre;
    private final JTextField txtPrecio;
    private final JTextField txtCantidad;
    private final JTextField txtCategoria;
    private final CardLayout disenoVistas;
    private final JPanel panelVistas;
    private final DefaultTableModel modeloVentas;
    private final JTextArea areaChat;
    private final JLabel estadoChat;
    private final JTextField campoMensajeChat;
    private final JButton botonEnviarChat;
    private JDialog ventanaChat;
    private ServerSocket servidorChat;
    private volatile Socket clienteChat;
    private volatile PrintWriter salidaChat;
    private volatile boolean servidorCerrado;

    public VentanaAdmin() {
        inventario = new Inventario();
        setTitle("Panel Administrador - Tienda");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout(14, 14));

        // La tabla principal muestra el inventario sin permitir editar celdas directamente.
        modeloTabla = new DefaultTableModel(
                new Object[] { "ID", "Producto", "Precio", "Stock", "Categoría", "Imagen" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setAutoCreateRowSorter(true);
        tablaProductos.setRowHeight(28);
        tablaProductos.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        tablaProductos.getTableHeader().setBackground(AZUL_MARINO);
        tablaProductos.getTableHeader().setForeground(MARFIL);
        tablaProductos.setBackground(SUPERFICIE);
        tablaProductos.setForeground(TEXTO_CLARO);
        tablaProductos.setSelectionBackground(DORADO);
        tablaProductos.setSelectionForeground(TEXTO);
        tablaProductos.setGridColor(AZUL);

        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        txtCantidad = new JTextField();
        txtCategoria = new JTextField();

        // Formulario lateral para agregar, editar, borrar y consultar productos.
        JPanel panelControles = new JPanel(new GridBagLayout());
        panelControles.setBackground(FONDO);
        panelControles.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panelControles.setPreferredSize(new Dimension(265, 0));
        agregarCampo(panelControles, "Nombre:", txtNombre, 0);
        agregarCampo(panelControles, "Precio:", txtPrecio, 1);
        agregarCampo(panelControles, "Cantidad:", txtCantidad, 2);
        agregarCampo(panelControles, "Categoría:", txtCategoria, 3);

        JButton btnAgregar = new JButton("Agregar producto");
        JButton btnActualizar = new JButton("Actualizar producto");
        JButton btnBorrar = new JButton("Borrar seleccionado");
        JButton btnValorTotal = new JButton("Ver valor total");
        JButton btnCargarImagen = new JButton("Cargar imagen");
        agregarBoton(panelControles, btnAgregar, 4);
        agregarBoton(panelControles, btnActualizar, 5);
        agregarBoton(panelControles, btnBorrar, 6);
        agregarBoton(panelControles, btnValorTotal, 7);
        agregarBoton(panelControles, btnCargarImagen, 8);
        btnCargarImagen.addActionListener(e -> seleccionarImagenProducto());

        JPanel panelStock = new JPanel(new BorderLayout(12, 12));
        panelStock.setBackground(FONDO);
        JScrollPane scrollProductos = new JScrollPane(tablaProductos);
        scrollProductos.getViewport().setBackground(SUPERFICIE);
        panelStock.add(scrollProductos, BorderLayout.CENTER);
        panelStock.add(panelControles, BorderLayout.WEST);

        // Las vistas de stock, ventas y chat comparten el espacio central de la ventana.
        modeloVentas = new DefaultTableModel(
                new Object[] { "Ticket", "Fecha", "RUT", "Cliente", "Correo", "Productos", "Subtotal", "IVA",
                        "Total" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tablaVentas = new JTable(modeloVentas);
        tablaVentas.setAutoCreateRowSorter(true);
        tablaVentas.setRowHeight(28);
        tablaVentas.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        tablaVentas.getTableHeader().setBackground(AZUL_MARINO);
        tablaVentas.getTableHeader().setForeground(MARFIL);
        tablaVentas.setBackground(SUPERFICIE);
        tablaVentas.setForeground(TEXTO_CLARO);
        tablaVentas.setSelectionBackground(DORADO);
        tablaVentas.setSelectionForeground(TEXTO);
        tablaVentas.setGridColor(AZUL);
        JScrollPane scrollVentas = new JScrollPane(tablaVentas);
        scrollVentas.getViewport().setBackground(SUPERFICIE);
        JPanel panelVentas = new JPanel(new BorderLayout());
        panelVentas.setBackground(FONDO);
        panelVentas.add(scrollVentas, BorderLayout.CENTER);

        areaChat = new JTextArea();
        estadoChat = new JLabel("Iniciando servidor...");
        campoMensajeChat = new JTextField();
        botonEnviarChat = new JButton("Enviar");

        disenoVistas = new CardLayout();
        panelVistas = new JPanel(disenoVistas);
        panelVistas.add(panelStock, "stock");
        panelVistas.add(panelVentas, "ventas");

        JLabel tituloAdmin = new JLabel("Admin");
        tituloAdmin.setFont(new Font("SansSerif", Font.BOLD, 20));
        tituloAdmin.setForeground(MARFIL);
        JButton btnStock = new JButton("Stock");
        JButton btnVentas = new JButton("Ventas");
        JButton btnChat = new JButton("Chat");

        JPanel barraNavegacion = new JPanel(new BorderLayout(16, 0));
        barraNavegacion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AZUL_MARINO),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        barraNavegacion.setBackground(AZUL_MARINO);
        estilizarBotonStock(btnStock);
        estilizarBotonStock(btnVentas);
        estilizarBotonStock(btnChat);
        JPanel accionesNavegacion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesNavegacion.setBackground(AZUL_MARINO);
        accionesNavegacion.add(btnStock);
        accionesNavegacion.add(btnVentas);
        accionesNavegacion.add(btnChat);
        barraNavegacion.add(tituloAdmin, BorderLayout.WEST);
        barraNavegacion.add(accionesNavegacion, BorderLayout.EAST);
        btnStock.addActionListener(e -> {
            inventario.recargarDesdeArchivo();
            refrescarTabla();
            disenoVistas.show(panelVistas, "stock");
        });
        btnVentas.addActionListener(e -> mostrarVentas());
        btnChat.addActionListener(e -> mostrarVentanaChat());
        add(barraNavegacion, BorderLayout.NORTH);
        add(panelVistas, BorderLayout.CENTER);

        tablaProductos.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());
        btnAgregar.addActionListener(e -> agregarProducto());
        btnActualizar.addActionListener(e -> actualizarProducto());
        btnBorrar.addActionListener(e -> borrarProducto());
        btnValorTotal.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Valor total del inventario: $" + inventario.calcularValorTotalInventario()));

        iniciarServidorChat();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarServidorChat();
            }
        });

        refrescarTabla();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void mostrarVentanaChat() {
        if (ventanaChat == null) {
            ventanaChat = new JDialog(this, "Chat con cliente", false);
            ventanaChat.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            ventanaChat.setContentPane(crearPanelChat());
            ventanaChat.setResizable(false);
            ventanaChat.setSize(480, 360);
            ventanaChat.setLocationRelativeTo(this);
        }
        ventanaChat.setVisible(true);
        ventanaChat.toFront();
    }

    private void seleccionarImagenProducto() {
        String id = idSeleccionado();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto en la tabla.",
                    "Falta selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Seleccionar imagen del producto " + id);
        selector.setFileFilter(new FileNameExtensionFilter(
                "Imágenes (PNG, JPG, JPEG, GIF, BMP)", "png", "jpg", "jpeg", "gif", "bmp"));
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File imagenSeleccionada = selector.getSelectedFile();
        File carpetaImagenes = new File("imagenes");
        File destino = new File(carpetaImagenes, id + ".png");
        try {
            if (!carpetaImagenes.isDirectory() && !carpetaImagenes.mkdirs()) {
                throw new IOException("No se pudo crear la carpeta de imágenes.");
            }

            java.awt.image.BufferedImage imagen = ImageIO.read(imagenSeleccionada);
            if (imagen == null) {
                throw new IOException("El archivo seleccionado no contiene una imagen válida.");
            }
            if (destino.exists()) {
                int respuesta = JOptionPane.showConfirmDialog(this,
                        "El producto ya tiene una imagen. ¿Quieres reemplazarla?",
                        "Reemplazar imagen", JOptionPane.YES_NO_OPTION);
                if (respuesta != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            if (!ImageIO.write(imagen, "png", destino)) {
                throw new IOException("No se pudo guardar la imagen en formato PNG.");
            }

            refrescarTabla();
            JOptionPane.showMessageDialog(this, "Imagen guardada para el producto " + id + ".",
                    "Imagen cargada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la imagen: " + ex.getMessage(),
                    "Error al cargar imagen", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel crearPanelChat() {
        // Panel de conversación del administrador con el cliente conectado.
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        areaChat.setBackground(SUPERFICIE);
        areaChat.setForeground(TEXTO_CLARO);
        areaChat.setCaretColor(TEXTO_CLARO);
        JScrollPane scrollChat = new JScrollPane(areaChat);
        scrollChat.getViewport().setBackground(SUPERFICIE);

        JPanel panelEnvio = new JPanel(new BorderLayout(8, 0));
        panelEnvio.setBackground(FONDO);
        campoMensajeChat.setBackground(SUPERFICIE);
        campoMensajeChat.setForeground(TEXTO_CLARO);
        campoMensajeChat.setCaretColor(TEXTO_CLARO);
        estilizarBotonStock(botonEnviarChat);
        botonEnviarChat.setEnabled(false);
        botonEnviarChat.addActionListener(e -> enviarMensajeChat());
        campoMensajeChat.addActionListener(e -> enviarMensajeChat());
        panelEnvio.add(campoMensajeChat, BorderLayout.CENTER);
        panelEnvio.add(botonEnviarChat, BorderLayout.EAST);

        estadoChat.setForeground(TEXTO_CLARO);
        panel.add(estadoChat, BorderLayout.NORTH);
        panel.add(scrollChat, BorderLayout.CENTER);
        panel.add(panelEnvio, BorderLayout.SOUTH);
        return panel;
    }

    private void enviarMensajeChat() {
        String mensaje = campoMensajeChat.getText().trim();
        if (mensaje.isEmpty()) {
            return;
        }
        try {
            enviarMensajeAlCliente(crearMensajeJson(mensaje));
            areaChat.append("Admin: " + mensaje + System.lineSeparator());
            campoMensajeChat.setText("");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo enviar el mensaje: " + ex.getMessage(),
                    "Error del chat", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void iniciarServidorChat() {
        // Abre el puerto configurado para que el cliente pueda establecer la conexión.
        try {
            servidorChat = new ServerSocket();
            servidorChat.setReuseAddress(true);
            servidorChat.bind(new InetSocketAddress(HOST_CHAT, PUERTO_CHAT));
            estadoChat.setText("Escuchando en " + HOST_CHAT + ":" + PUERTO_CHAT);

            Thread hiloServidor = new Thread(this::aceptarClientesChat, "servidor-chat");
            hiloServidor.setDaemon(true);
            hiloServidor.start();
        } catch (IOException ex) {
            estadoChat.setText("No se pudo iniciar el servidor: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "No se pudo iniciar el servidor del chat: " + ex.getMessage(),
                    "Error del chat", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aceptarClientesChat() {
        // Espera conexiones entrantes y mantiene una conversación activa a la vez.
        while (!servidorCerrado) {
            try {
                Socket nuevoCliente = servidorChat.accept();
                PrintWriter nuevaSalida = new PrintWriter(
                        new OutputStreamWriter(nuevoCliente.getOutputStream(), StandardCharsets.UTF_8), true);
                synchronized (this) {
                    if (clienteChat != null && !clienteChat.isClosed()) {
                        try {
                            nuevoCliente.close();
                        } catch (IOException ex) {
                            actualizarEstadoChat("No se pudo rechazar otra conexión: " + ex.getMessage());
                        }
                        continue;
                    }
                    clienteChat = nuevoCliente;
                    salidaChat = nuevaSalida;
                }
                actualizarEstadoChat("Cliente conectado");
                Thread hiloCliente = new Thread(() -> leerMensajesCliente(nuevoCliente), "cliente-chat");
                hiloCliente.setDaemon(true);
                hiloCliente.start();
            } catch (IOException ex) {
                if (!servidorCerrado) {
                    actualizarEstadoChat("Error del servidor: " + ex.getMessage());
                }
                return;
            }
        }
    }

    private void leerMensajesCliente(Socket socket) {
        // Recibe mensajes JSON del cliente fuera del hilo de la interfaz.
        try (BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                String contenido = extraerCampoJson(mensaje, "content");
                if ("message".equals(extraerCampoJson(mensaje, "type")) && contenido != null) {
                    SwingUtilities.invokeLater(
                            () -> areaChat.append("Cliente: " + contenido + System.lineSeparator()));
                }
            }
        } catch (IOException ex) {
            if (!servidorCerrado) {
                actualizarEstadoChat("Conexión interrumpida: " + ex.getMessage());
            }
        } finally {
            desconectarClienteChat(socket);
        }
    }

    private String crearMensajeJson(String contenido) {
        // Usa el mismo formato de mensaje que el cliente para comunicarse por el socket.
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

    private void enviarMensajeAlCliente(String mensaje) throws IOException {
        synchronized (this) {
            if (clienteChat == null || clienteChat.isClosed() || salidaChat == null) {
                throw new IOException("No hay un cliente conectado.");
            }
            salidaChat.println(mensaje);
            if (salidaChat.checkError()) {
                throw new IOException("No se pudo enviar el mensaje al cliente.");
            }
        }
    }

    private void desconectarClienteChat(Socket socket) {
        synchronized (this) {
            if (clienteChat != socket) {
                return;
            }
            clienteChat = null;
            salidaChat = null;
        }
        try {
            socket.close();
        } catch (IOException ex) {
            if (!servidorCerrado) {
                actualizarEstadoChat("No se pudo cerrar la conexión: " + ex.getMessage());
            }
        }
        if (!servidorCerrado) {
            actualizarEstadoChat("Esperando al cliente en " + HOST_CHAT + ":" + PUERTO_CHAT);
        }
    }

    private void actualizarEstadoChat(String estado) {
        SwingUtilities.invokeLater(() -> {
            estadoChat.setText(estado);
            botonEnviarChat.setEnabled("Cliente conectado".equals(estado));
        });
    }

    private void cerrarServidorChat() {
        servidorCerrado = true;
        if (servidorChat != null) {
            try {
                servidorChat.close();
            } catch (IOException ex) {
                System.err.println("No se pudo cerrar el servidor del chat: " + ex.getMessage());
            }
        }
        Socket clienteActual = clienteChat;
        if (clienteActual != null) {
            desconectarClienteChat(clienteActual);
        }
    }

    private void mostrarVentas() {
        // Recarga el historial y cambia la tarjeta visible a la tabla de ventas.
        modeloVentas.setRowCount(0);

        try {
            for (String[] venta : new GestorVentas().obtenerTodasLasVentas()) {
                modeloVentas.addRow(new Object[] {
                        venta[0], venta[1], venta[2], venta[3], venta[4], venta[5],
                        "$" + venta[6], "$" + venta[7], "$" + venta[8]
                });
            }
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar las ventas: " + ex.getMessage(),
                    "Error al cargar ventas", JOptionPane.ERROR_MESSAGE);
            return;
        }

        disenoVistas.show(panelVistas, "ventas");
    }

    private void estilizarBotonStock(JButton boton) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBackground(DORADO);
        boton.setForeground(TEXTO);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private void agregarCampo(JPanel panel, String etiqueta, JTextField campo, int fila) {
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridx = 0;
        restricciones.gridy = fila;
        restricciones.anchor = GridBagConstraints.WEST;
        restricciones.insets = new Insets(4, 0, 4, 0);
        JLabel etiquetaComponente = new JLabel(etiqueta);
        etiquetaComponente.setForeground(TEXTO_CLARO);
        campo.setFont(campo.getFont().deriveFont(Font.BOLD));
        campo.setBackground(SUPERFICIE);
        campo.setForeground(TEXTO_CLARO);
        campo.setCaretColor(TEXTO_CLARO);
        panel.add(etiquetaComponente, restricciones);

        restricciones.gridx = 1;
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.weightx = 1;
        panel.add(campo, restricciones);
        }

    private void agregarBoton(JPanel panel, JButton boton, int fila) {
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridx = 0;
        restricciones.gridy = fila;
        restricciones.gridwidth = 2;
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.insets = new Insets(6, 0, 0, 0);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD));
        boton.setBackground(DORADO);
        boton.setForeground(TEXTO);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        panel.add(boton, restricciones);
    }

    private void agregarProducto() {
        // Valida el formulario, agrega el producto al inventario y refresca la tabla.
        try {
            String nombre = txtNombre.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            int stock = Integer.parseInt(txtCantidad.getText().trim());
            if (nombre.isEmpty() || precio < 0 || stock < 0) {
                throw new IllegalArgumentException();
            }

            inventario.agregarProducto(new Producto(inventario.generarNuevoId(), nombre, precio, stock,
                    categoriaIngresada()));
            actualizarVista();
            limpiarCampos();
        } catch (IllegalArgumentException ex) {
            mostrarDatosInvalidos();
        }
    }

    private void actualizarProducto() {
        // Guarda los cambios usando el ID del producto seleccionado, no su nombre.
        String id = idSeleccionado();
        if (id == null) {
            mostrarSeleccion();
            return;
        }

        try {
            String nombre = txtNombre.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            int stock = Integer.parseInt(txtCantidad.getText().trim());
            if (!inventario.actualizarProducto(id, nombre, precio, stock, categoriaIngresada())) {
                throw new IllegalArgumentException();
            }
            actualizarVista();
            limpiarCampos();
        } catch (IllegalArgumentException ex) {
            mostrarDatosInvalidos();
        }
    }

    private void borrarProducto() {
        // Pide confirmación antes de eliminar el producto seleccionado del inventario.
        String id = idSeleccionado();
        if (id == null) {
            mostrarSeleccion();
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this, "¿Borrar el producto " + id + "?",
                "Confirmar borrado", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            inventario.eliminarProducto(id);
            actualizarVista();
            limpiarCampos();
        }
    }

    private String idSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        int filaModelo = tablaProductos.convertRowIndexToModel(fila);
        return modeloTabla.getValueAt(filaModelo, 0).toString();
    }

    private void cargarSeleccion() {
        // Copia a los campos del formulario los datos de la fila seleccionada.
        String id = idSeleccionado();
        if (id == null) {
            return;
        }
        Producto producto = inventario.buscarProducto(id);
        if (producto != null) {
            txtNombre.setText(producto.getNombre());
            txtPrecio.setText(String.valueOf(producto.getPrecio()));
            txtCantidad.setText(String.valueOf(producto.getStock()));
            txtCategoria.setText(producto.getCategoria());
        }
    }

    public void actualizarVista() {
        refrescarTabla();
    }

    private void refrescarTabla() {
        // Vuelve a construir las filas visibles usando la lista actual del inventario.
        modeloTabla.setRowCount(0);
        for (Producto producto : inventario.getListaProductos()) {
            File imagen = new File("imagenes", producto.getId() + ".png");
            modeloTabla.addRow(new Object[] { producto.getId(), producto.getNombre(), producto.getPrecio(),
                    producto.getStock(), producto.getCategoria(), imagen.isFile() ? "Sí" : "No" });
        }
    }

    private String categoriaIngresada() {
        String categoria = txtCategoria.getText().trim();
        return categoria.isEmpty() ? "BASE" : categoria;
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtPrecio.setText("");
        txtCantidad.setText("");
        txtCategoria.setText("");
        tablaProductos.clearSelection();
    }

    private void mostrarSeleccion() {
        JOptionPane.showMessageDialog(this, "Selecciona un producto en la tabla.", "Falta selección",
                JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarDatosInvalidos() {
        JOptionPane.showMessageDialog(this,
                "Ingresa un nombre, un precio válido y una cantidad entera no negativa.",
                "Datos inválidos", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaAdmin().setVisible(true));
    }
}
