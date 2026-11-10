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
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
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
import javax.swing.ListSelectionModel;
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
    private final DefaultTableModel modeloCupones;

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

        // Las vistas de stock y ventas comparten el espacio central de la ventana.
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

        JLabel tituloAdmin = new JLabel("Admin");
        tituloAdmin.setFont(new Font("SansSerif", Font.BOLD, 20));
        tituloAdmin.setForeground(MARFIL);
        JButton btnStock = new JButton("Stock");
        JButton btnVentas = new JButton("Ventas");
        JButton btnCupones = new JButton("Cupones");

        JPanel barraNavegacion = new JPanel(new BorderLayout(16, 0));
        barraNavegacion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AZUL_MARINO),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        barraNavegacion.setBackground(AZUL_MARINO);
        estilizarBotonStock(btnStock);
        estilizarBotonStock(btnVentas);
        estilizarBotonStock(btnCupones);
        JPanel accionesNavegacion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesNavegacion.setBackground(AZUL_MARINO);
        accionesNavegacion.add(btnStock);
        accionesNavegacion.add(btnVentas);
        accionesNavegacion.add(btnCupones);
        barraNavegacion.add(tituloAdmin, BorderLayout.WEST);
        barraNavegacion.add(accionesNavegacion, BorderLayout.EAST);
        btnStock.addActionListener(e -> {
            inventario.recargarDesdeArchivo();
            refrescarTabla();
            disenoVistas.show(panelVistas, "stock");
        });
        btnVentas.addActionListener(e -> mostrarVentas());
        btnCupones.addActionListener(e -> mostrarCupones());
        add(barraNavegacion, BorderLayout.NORTH);
        add(panelVistas, BorderLayout.CENTER);

        tablaProductos.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());
        btnAgregar.addActionListener(e -> agregarProducto());
        btnActualizar.addActionListener(e -> actualizarProducto());
        btnBorrar.addActionListener(e -> borrarProducto());
        btnValorTotal.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Valor total del inventario: $" + inventario.calcularValorTotalInventario()));

        refrescarTabla();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
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
                throw new IllegalArgumentException("El código solo puede contener letras, números, guiones y guiones bajos.");
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

    private void estilizarBotonStock(JButton boton) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBackground(DORADO);
        boton.setForeground(TEXTO);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
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
