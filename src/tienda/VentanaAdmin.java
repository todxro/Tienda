package tienda;
 
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
 
 
// Reúne las vistas y acciones con las que administración mantiene la tienda.
// Índice por línea:
// - Inicio y navegación: constructor VentanaAdmin (88), mostrarVentas (512)
// - Productos e imágenes: seleccionarImagenProducto (250), agregarProducto (575),
//   actualizarProducto (594), borrarProducto (616), idSeleccionado (633), cargarSeleccion (642)
// - Interfaz y formularios: estilizarBotonStock (532), agregarCampo (540), agregarBoton (560),
//   actualizarVista (657), refrescarTabla (661), categoriaIngresada (671), limpiarCampos (676),
//   mostrarSeleccion (684), mostrarDatosInvalidos (689)
// - Inicio de la aplicación: main (695)
public class VentanaAdmin extends JFrame {
    private static final Color AZUL = new Color(50, 116, 174);
    private static final Color AZUL_MARINO = new Color(22, 50, 78);
    private static final Color AMARILLO = new Color(242, 184, 75);
    private static final Color DORADO = AMARILLO;
    private static final Color FONDO = new Color(241, 244, 248);
    private static final Color SUPERFICIE = Color.WHITE;
    private static final Color TEXTO = new Color(31, 41, 55);
    private static final Color TEXTO_CLARO = new Color(82, 101, 121);
    private static final Color MARFIL = Color.WHITE;
    private final Inventario inventario;
    private final JPanel panelTarjetasProductos;
    private String productoSeleccionadoId;
    private final JTextField txtNombre;
    private final JTextField txtPrecio;
    private final JTextField txtCantidad;
    private final JTextField txtCategoria;
    private final CardLayout disenoVistas;
    private final JPanel panelVistas;
    private final DefaultTableModel modeloVentas;
    private final DefaultTableModel modeloCupones;
 
    public VentanaAdmin() {
        inventario = new Inventario();
        setTitle("Panel Administrador - Tienda");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout(14, 14));
 
        // El inventario se presenta en tarjetas para mostrar las fotos de los productos.
        panelTarjetasProductos = new PanelTarjetasProductos();
        panelTarjetasProductos.setLayout(new GridLayout(0, 3, 14, 14));
        panelTarjetasProductos.setBackground(FONDO);
        panelTarjetasProductos.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
 
        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        txtCantidad = new JTextField();
        txtCategoria = new JTextField();
 
        // Formulario lateral para agregar, editar, borrar y consultar productos.
        JPanel panelControles = new JPanel(new GridBagLayout());
        panelControles.setBackground(SUPERFICIE);
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
        panelStock.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JScrollPane scrollProductos = new JScrollPane(panelTarjetasProductos);
        scrollProductos.setBorder(BorderFactory.createEmptyBorder());
        scrollProductos.getViewport().setBackground(FONDO);
        scrollProductos.getVerticalScrollBar().setUnitIncrement(16);
        scrollProductos.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                actualizarColumnasTarjetas(scrollProductos.getViewport().getExtentSize().width);
            }
        });
        panelStock.add(crearEncabezado("Inventario", "Administra productos, precios y existencias."),
            BorderLayout.NORTH);
        panelStock.add(scrollProductos, BorderLayout.CENTER);
        panelStock.add(panelControles, BorderLayout.WEST);
 
        // Las vistas de stock y ventas comparten el espacio central de la ventana.
        modeloVentas = new DefaultTableModel(
                new Object[] { "Ticket", "Fecha", "RUT", "Cliente", "Correo", "Productos", "Subtotal", "IVA",
                        "Total", "Estado" }, 0) {
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
        JPanel panelVentas = new JPanel(new BorderLayout(12, 12));
        panelVentas.setBackground(FONDO);
        panelVentas.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panelVentas.add(crearEncabezado("Ventas", "Consulta el historial de ventas de la tienda."),
        BorderLayout.NORTH);
        panelVentas.add(scrollVentas, BorderLayout.CENTER);
        JButton btnAvanzarEstado = new JButton("Avanzar estado");
        estilizarBotonStock(btnAvanzarEstado);
        btnAvanzarEstado.addActionListener(e -> avanzarEstadoSeleccionado(tablaVentas));
        JPanel barraVentas = new JPanel(new BorderLayout());
        barraVentas.setBackground(FONDO);
        barraVentas.add(btnAvanzarEstado, BorderLayout.EAST);
        panelVentas.add(barraVentas, BorderLayout.SOUTH);
 
        modeloCupones = new DefaultTableModel(
                new Object[] { "Código", "Tipo", "Descuento", "Vencimiento", "Usos realizados / máximos" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tablaCupones = new JTable(modeloCupones);
        tablaCupones.setAutoCreateRowSorter(true);
        tablaCupones.setRowHeight(28);
        tablaCupones.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        tablaCupones.getTableHeader().setBackground(AZUL_MARINO);
        tablaCupones.getTableHeader().setForeground(MARFIL);
        tablaCupones.setBackground(SUPERFICIE);
        tablaCupones.setForeground(TEXTO_CLARO);
        tablaCupones.setSelectionBackground(DORADO);
        tablaCupones.setSelectionForeground(TEXTO);
        tablaCupones.setGridColor(AZUL);
        JScrollPane scrollCupones = new JScrollPane(tablaCupones);
        scrollCupones.getViewport().setBackground(SUPERFICIE);
        JButton btnNuevoCupon = new JButton("Agregar cupón");
        estilizarBotonStock(btnNuevoCupon);
        btnNuevoCupon.addActionListener(e -> agregarCupon());
        JPanel encabezadoCupones = new JPanel(new BorderLayout());
        encabezadoCupones.setBackground(FONDO);
        JLabel tituloCupones = new JLabel("Cupones disponibles para clientes");
        tituloCupones.setFont(new Font("SansSerif", Font.BOLD, 18));
        tituloCupones.setForeground(TEXTO);
        encabezadoCupones.add(tituloCupones, BorderLayout.WEST);
        encabezadoCupones.add(btnNuevoCupon, BorderLayout.EAST);
        JPanel panelCupones = new JPanel(new BorderLayout(12, 12));
        panelCupones.setBackground(FONDO);
        panelCupones.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panelCupones.add(encabezadoCupones, BorderLayout.NORTH);
        panelCupones.add(scrollCupones, BorderLayout.CENTER);
 
        disenoVistas = new CardLayout();
        panelVistas = new JPanel(disenoVistas);
        panelVistas.add(panelStock, "stock");
        panelVistas.add(panelVentas, "ventas");
        panelVistas.add(panelCupones, "cupones");
 
        JLabel tituloAdmin = new JLabel("<html>TIENDA<br><b>ADMINISTRACIÓN</b></html>");
        tituloAdmin.setFont(new Font("SansSerif", Font.PLAIN, 18));
        tituloAdmin.setForeground(MARFIL);
        tituloAdmin.setBorder(BorderFactory.createEmptyBorder(8, 4, 20, 4));
        JButton btnStock = new JButton("Stock");
        JButton btnVentas = new JButton("Ventas");
        JButton btnCupones = new JButton("Cupones");
        JButton btnTiempo = new JButton("Tiempo de caja");
        agregarBoton(panelControles, btnTiempo, 9);
        btnTiempo.addActionListener(e -> {
            String r = JOptionPane.showInputDialog(this, "Minutos para expirar el carrito (minimo 2):");
            try {
                if (r == null) return;
                if (Integer.parseInt(r.trim()) < 2) {
                    JOptionPane.showMessageDialog(this, "No se puede poner menor a 2 minutos");
                    return;
                }
                java.nio.file.Files.writeString(java.nio.file.Path.of("tiempo_caja.txt"), r.trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Numero invalido");
            }
        });
 
        JPanel barraNavegacion = new JPanel(new BorderLayout(0, 18));
        barraNavegacion.setPreferredSize(new Dimension(205, 0));
        barraNavegacion.setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));
        barraNavegacion.setBackground(AZUL_MARINO);
        estilizarBotonNavegacion(btnStock);
        estilizarBotonNavegacion(btnVentas);
        estilizarBotonNavegacion(btnCupones);
        JPanel accionesNavegacion = new JPanel();
        accionesNavegacion.setLayout(new BoxLayout(accionesNavegacion, BoxLayout.Y_AXIS));
        accionesNavegacion.setBackground(AZUL_MARINO);
        accionesNavegacion.add(btnStock);
        accionesNavegacion.add(Box.createVerticalStrut(8));
        accionesNavegacion.add(btnVentas);
        accionesNavegacion.add(Box.createVerticalStrut(8));
        accionesNavegacion.add(btnCupones);
        barraNavegacion.add(tituloAdmin, BorderLayout.NORTH);
        barraNavegacion.add(accionesNavegacion, BorderLayout.CENTER);
        btnStock.addActionListener(e -> {
            inventario.recargarDesdeArchivo();
            refrescarTarjetas();
            disenoVistas.show(panelVistas, "stock");
        });
        btnVentas.addActionListener(e -> mostrarVentas());
        btnCupones.addActionListener(e -> mostrarCupones());
        add(barraNavegacion, BorderLayout.WEST);
        add(panelVistas, BorderLayout.CENTER);
 
        btnAgregar.addActionListener(e -> agregarProducto());
        btnActualizar.addActionListener(e -> actualizarProducto());
        btnBorrar.addActionListener(e -> borrarProducto());
        btnValorTotal.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Valor total del inventario: $" + inventario.calcularValorTotalInventario()));
 
        refrescarTarjetas();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
 
    private void seleccionarImagenProducto() {
        String id = idSeleccionado();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una tarjeta de producto.",
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
 
            refrescarTarjetas();
            JOptionPane.showMessageDialog(this, "Imagen guardada para el producto " + id + ".",
                    "Imagen cargada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la imagen: " + ex.getMessage(),
                    "Error al cargar imagen", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    private void mostrarVentas() {
        // Recarga el historial y cambia la tarjeta visible a la tabla de ventas.
        modeloVentas.setRowCount(0);
 
        try {
            for (String[] venta : new GestorVentas().obtenerTodasLasVentas()) {
                modeloVentas.addRow(new Object[] {
                        venta[0], venta[1], venta[2], venta[3], venta[4], venta[5],
                        "$" + venta[6], "$" + venta[7], "$" + venta[8],
                        EstadoPedido.desdeTexto(venta[11]).getTexto()                
                        });
            }
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar las ventas: " + ex.getMessage(),
                    "Error al cargar ventas", JOptionPane.ERROR_MESSAGE);
            return;
        }
 
        disenoVistas.show(panelVistas, "ventas");
    }
 
    // pasa el pedido seleccionado al siguiente estado y recarga la tabla
    private void avanzarEstadoSeleccionado(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una venta de la tabla.", "Seguimiento",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        // la tabla se puede ordenar, por eso se convierte la fila a la del modelo
        int filaModelo = tabla.convertRowIndexToModel(fila);
        String ticket = String.valueOf(modeloVentas.getValueAt(filaModelo, 0));
        String fecha = String.valueOf(modeloVentas.getValueAt(filaModelo, 1));
 
        try {
            EstadoPedido nuevo = new GestorVentas().avanzarEstado(ticket, fecha);
            if (nuevo == null) {
                JOptionPane.showMessageDialog(this, "Este pedido ya fue entregado.", "Seguimiento",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar el estado: " + ex.getMessage(),
                    "Error al actualizar", JOptionPane.ERROR_MESSAGE);
            return;
        }
        mostrarVentas();
    }
 
    private void mostrarCupones() {
        try {
            modeloCupones.setRowCount(0);
            for (Cupon cupon : new GestorCupones().getCupones()) {
                String descuento = cupon.getTipo().equals(Cupon.PORCENTAJE)
                        ? cupon.getValor() + "%" : "$" + cupon.getValor();
                modeloCupones.addRow(new Object[] {
                        cupon.getCodigo(), cupon.getTipo(), descuento, cupon.getFechaVencimiento(),
                        cupon.getUsosRealizados() + " / " + cupon.getUsosMaximos()
                });
            }
            disenoVistas.show(panelVistas, "cupones");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los cupones: " + ex.getMessage(),
                    "Error al cargar cupones", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    private void agregarCupon() {
        JTextField campoCodigo = new JTextField();
        JComboBox<String> campoTipo = new JComboBox<>(new String[] { "Porcentaje (%)", "Monto fijo ($)" });
        JTextField campoDescuento = new JTextField();
        JTextField campoVencimiento = new JTextField(LocalDate.now().plusMonths(1).toString());
        JTextField campoUsos = new JTextField("100");
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(FONDO);
        agregarCampo(formulario, "Código:", campoCodigo, 0);
        agregarCampo(formulario, "Tipo de descuento:", campoTipo, 1);
        agregarCampo(formulario, "Descuento:", campoDescuento, 2);
        agregarCampo(formulario, "Vencimiento (AAAA-MM-DD):", campoVencimiento, 3);
        agregarCampo(formulario, "Usos máximos:", campoUsos, 4);
 
        int resultado = JOptionPane.showConfirmDialog(this, formulario, "Crear cupón",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }
 
        String codigo = campoCodigo.getText().trim();
        try {
            if (!codigo.matches("[A-Za-z0-9_-]+")) {
                throw new IllegalArgumentException(
                        "El código solo puede contener letras, números, guiones y guiones bajos.");
            }
            String tipo = campoTipo.getSelectedIndex() == 0 ? Cupon.PORCENTAJE : Cupon.MONTO;
            double valor = Double.parseDouble(campoDescuento.getText().trim());
            if (!Double.isFinite(valor) || valor <= 0
                    || (tipo.equals(Cupon.PORCENTAJE) && valor > 100)) {
                throw new IllegalArgumentException(tipo.equals(Cupon.PORCENTAJE)
                        ? "El porcentaje debe ser mayor que 0 y no superar el 100."
                        : "El monto del descuento debe ser mayor que 0.");
            }
            LocalDate vencimiento = LocalDate.parse(campoVencimiento.getText().trim());
            if (vencimiento.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de vencimiento no puede estar en el pasado.");
            }
            int usosMaximos = Integer.parseInt(campoUsos.getText().trim());
            if (usosMaximos <= 0) {
                throw new IllegalArgumentException("Los usos máximos deben ser mayores que 0.");
            }
 
            Cupon cupon = new Cupon(codigo, tipo, valor, vencimiento, usosMaximos, 0);
            if (!new GestorCupones().agregar(cupon)) {
                JOptionPane.showMessageDialog(this, "Ya existe un cupón con ese código.",
                        "Código duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            mostrarCupones();
            JOptionPane.showMessageDialog(this, "El cupón " + cupon.getCodigo() + " quedó disponible para los clientes.",
                    "Cupón creado", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException | DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa un descuento numérico, una fecha válida (AAAA-MM-DD) y un límite de usos entero.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el cupón: " + ex.getMessage(),
                    "Error al guardar cupón", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    private JPanel crearEncabezado(String titulo, String descripcion) {
        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.setBackground(FONDO);
 
        JLabel etiquetaTitulo = new JLabel(titulo);
        etiquetaTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        etiquetaTitulo.setForeground(TEXTO);
 
        JLabel etiquetaDescripcion = new JLabel(descripcion);
        etiquetaDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        etiquetaDescripcion.setForeground(TEXTO_CLARO);
 
        encabezado.add(etiquetaTitulo, BorderLayout.NORTH);
        encabezado.add(etiquetaDescripcion, BorderLayout.CENTER);
        return encabezado;
    }
 
    private void estilizarBotonStock(JButton boton) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBackground(DORADO);
        boton.setForeground(TEXTO);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }
 
    private void estilizarBotonNavegacion(JButton boton) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setBackground(new Color(33, 70, 105));
        boton.setForeground(MARFIL);
        boton.setHorizontalAlignment(JButton.LEFT);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        boton.setPreferredSize(new Dimension(177, 38));
        boton.setMaximumSize(new Dimension(177, 38));
        boton.setAlignmentX(JButton.LEFT_ALIGNMENT);
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
 
    private void agregarCampo(JPanel panel, String etiqueta, JComponent campo, int fila) {
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
        if (campo instanceof JTextField) {
            ((JTextField) campo).setCaretColor(TEXTO_CLARO);
        }
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
        return productoSeleccionadoId;
    }
 
    private void cargarSeleccion(Producto producto) {
        productoSeleccionadoId = producto.getId();
        txtNombre.setText(producto.getNombre());
        txtPrecio.setText(String.valueOf(producto.getPrecio()));
        txtCantidad.setText(String.valueOf(producto.getStock()));
        txtCategoria.setText(producto.getCategoria());
        actualizarEstiloSeleccionTarjetas();
    }
 
    public void actualizarVista() {
        refrescarTarjetas();
    }
 
    private void refrescarTarjetas() {
        panelTarjetasProductos.removeAll();
        boolean seleccionSigueExistiendo = false;
        for (Producto producto : inventario.getListaProductos()) {
            if (producto.getId().equals(productoSeleccionadoId)) {
                seleccionSigueExistiendo = true;
                break;
            }
        }
        if (!seleccionSigueExistiendo) {
            productoSeleccionadoId = null;
        }
 
        if (inventario.getListaProductos().isEmpty()) {
            panelTarjetasProductos.setLayout(new GridBagLayout());
            JLabel sinProductos = new JLabel("Aún no hay productos en el inventario.");
            sinProductos.setFont(new Font("SansSerif", Font.PLAIN, 16));
            sinProductos.setForeground(TEXTO_CLARO);
            panelTarjetasProductos.add(sinProductos, new GridBagConstraints());
        } else {
            panelTarjetasProductos.setLayout(new GridLayout(0, 3, 14, 14));
            for (Producto producto : inventario.getListaProductos()) {
                panelTarjetasProductos.add(crearTarjetaProducto(producto));
            }
            int anchoDisponible = panelTarjetasProductos.getParent() == null
                    ? panelTarjetasProductos.getWidth() : panelTarjetasProductos.getParent().getWidth();
            actualizarColumnasTarjetas(anchoDisponible);
        }
        panelTarjetasProductos.revalidate();
        panelTarjetasProductos.repaint();
    }
 
    private void actualizarEstiloSeleccionTarjetas() {
        for (Component componente : panelTarjetasProductos.getComponents()) {
            if (componente instanceof JPanel) {
                JPanel tarjeta = (JPanel) componente;
                String id = (String) tarjeta.getClientProperty("productoId");
                tarjeta.setBorder(crearBordeTarjeta(productoSeleccionadoId.equals(id)));
            }
        }
        panelTarjetasProductos.repaint();
    }
 
    private javax.swing.border.Border crearBordeTarjeta(boolean seleccionada) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(seleccionada ? AZUL : new Color(220, 226, 233),
                        seleccionada ? 2 : 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
 
    private void actualizarColumnasTarjetas(int anchoDisponible) {
        if (!(panelTarjetasProductos.getLayout() instanceof GridLayout)
                || panelTarjetasProductos.getComponentCount() == 0) {
            return;
        }
        int separacion = 14;
        int anchoTarjetaMinimo = 230;
        int ancho = anchoDisponible > 0 ? anchoDisponible : anchoTarjetaMinimo * 3 + separacion * 2;
        int columnas = Math.max(1, (ancho + separacion) / (anchoTarjetaMinimo + separacion));
        GridLayout distribucion = (GridLayout) panelTarjetasProductos.getLayout();
        if (distribucion.getColumns() != columnas) {
            distribucion.setColumns(columnas);
            panelTarjetasProductos.revalidate();
        }
    }
 
    private JPanel crearTarjetaProducto(Producto producto) {
        boolean seleccionado = producto.getId().equals(productoSeleccionadoId);
        JPanel tarjeta = new JPanel(new BorderLayout(0, 10));
        tarjeta.setBackground(SUPERFICIE);
        tarjeta.putClientProperty("productoId", producto.getId());
        tarjeta.setBorder(crearBordeTarjeta(seleccionado));
        tarjeta.setPreferredSize(new Dimension(220, 240));
        tarjeta.setToolTipText("Haz clic para seleccionar este producto");
 
        JLabel imagen = new JLabel("Sin imagen", JLabel.CENTER);
        imagen.setOpaque(true);
        imagen.setBackground(FONDO);
        imagen.setForeground(TEXTO_CLARO);
        imagen.setPreferredSize(new Dimension(170, 110));
        File archivoImagen = new File("imagenes", producto.getId() + ".png");
        if (archivoImagen.isFile()) {
            try {
                java.awt.image.BufferedImage imagenOriginal = ImageIO.read(archivoImagen);
                if (imagenOriginal != null) {
                        double escala = Math.min(160.0 / imagenOriginal.getWidth(),
                            100.0 / imagenOriginal.getHeight());
                    int ancho = Math.max(1, (int) (imagenOriginal.getWidth() * escala));
                    int alto = Math.max(1, (int) (imagenOriginal.getHeight() * escala));
                    Image imagenEscalada = imagenOriginal.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                    imagen.setText("");
                    imagen.setIcon(new ImageIcon(imagenEscalada));
                }
            } catch (IOException ex) {
                imagen.setText("Imagen no disponible");
            }
        }
        tarjeta.add(imagen, BorderLayout.NORTH);
 
        JPanel detalles = new JPanel();
        detalles.setLayout(new BoxLayout(detalles, BoxLayout.Y_AXIS));
        detalles.setBackground(SUPERFICIE);
        JLabel id = new JLabel("ID: " + producto.getId());
        id.setFont(new Font("SansSerif", Font.PLAIN, 11));
        id.setForeground(TEXTO_CLARO);
        JLabel nombre = new JLabel(producto.getNombre());
        nombre.setFont(new Font("SansSerif", Font.BOLD, 16));
        nombre.setForeground(TEXTO);
        JLabel categoria = new JLabel("Categoría: " + producto.getCategoria());
        categoria.setFont(new Font("SansSerif", Font.PLAIN, 12));
        categoria.setForeground(TEXTO_CLARO);
        JLabel precio = new JLabel("$" + producto.getPrecio());
        precio.setFont(new Font("SansSerif", Font.BOLD, 17));
        precio.setForeground(AZUL_MARINO);
        JLabel stock = new JLabel("Stock: " + producto.getStock());
        stock.setFont(new Font("SansSerif", Font.BOLD, 12));
        stock.setForeground(producto.getStock() > 0 ? new Color(35, 130, 85) : new Color(190, 62, 62));
        detalles.add(id);
        detalles.add(Box.createVerticalStrut(4));
        detalles.add(nombre);
        detalles.add(Box.createVerticalStrut(4));
        detalles.add(categoria);
        detalles.add(Box.createVerticalStrut(8));
        detalles.add(precio);
        detalles.add(Box.createVerticalStrut(4));
        detalles.add(stock);
        tarjeta.add(detalles, BorderLayout.CENTER);
 
        MouseAdapter selector = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarSeleccion(producto);
            }
        };
        agregarListenerSeleccion(tarjeta, selector);
        return tarjeta;
    }
 
    private void agregarListenerSeleccion(Component componente, MouseAdapter selector) {
        componente.addMouseListener(selector);
        componente.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (componente instanceof Container) {
            for (Component hijo : ((Container) componente).getComponents()) {
                agregarListenerSeleccion(hijo, selector);
            }
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
        productoSeleccionadoId = null;
        refrescarTarjetas();
    }
 
    private void mostrarSeleccion() {
        JOptionPane.showMessageDialog(this, "Selecciona una tarjeta de producto.", "Falta selección",
                JOptionPane.WARNING_MESSAGE);
    }
 
    private void mostrarDatosInvalidos() {
        JOptionPane.showMessageDialog(this,
                "Ingresa un nombre, un precio válido y una cantidad entera no negativa.",
                "Datos inválidos", JOptionPane.WARNING_MESSAGE);
    }
 
    private static class PanelTarjetasProductos extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(720, 520);
        }
 
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }
 
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(20, visibleRect.height - 20);
        }
 
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
 
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaAdmin().setVisible(true));
    }
}
 