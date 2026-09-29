package tienda;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class VentanaAdmin extends JFrame {
    private static final Color AZUL_MARINO = new Color(46, 31, 71);
    private static final Color DORADO = new Color(232, 184, 75);
    private static final Color FONDO = new Color(18, 16, 27);
    private static final Color SUPERFICIE = new Color(28, 24, 43);
    private static final Color TEXTO = new Color(61, 43, 0);
    private static final Color TEXTO_CLARO = new Color(217, 210, 232);
    private final Inventario inventario;
    private final DefaultTableModel modeloTabla;
    private final JTable tablaProductos;
    private final JTextField txtNombre;
    private final JTextField txtPrecio;
    private final JTextField txtCantidad;
    private final JTextField txtCategoria;

    public VentanaAdmin() {
        inventario = new Inventario();

        setTitle("Panel Administrador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout(12, 12));

        modeloTabla = new DefaultTableModel(
                new Object[] { "ID", "Producto", "Precio", "Stock", "Categoría" }, 0) {
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
        tablaProductos.getTableHeader().setForeground(Color.WHITE);
        tablaProductos.setBackground(SUPERFICIE);
        tablaProductos.setForeground(TEXTO_CLARO);
        tablaProductos.setSelectionBackground(DORADO);
        tablaProductos.setSelectionForeground(TEXTO);
        tablaProductos.setGridColor(new Color(60, 52, 80));

        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        txtCantidad = new JTextField();
        txtCategoria = new JTextField();

        JPanel panelControles = new JPanel(new GridBagLayout());
        panelControles.setBackground(FONDO);
        panelControles.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panelControles.setPreferredSize(new Dimension(265, 0));
        agregarCampo(panelControles, "Nombre:", txtNombre, 0);
        agregarCampo(panelControles, "Precio:", txtPrecio, 1);
        agregarCampo(panelControles, "Cantidad:", txtCantidad, 2);
        agregarCampo(panelControles, "Categoría:", txtCategoria, 3);

        JButton btnAgregar = new JButton("Agregar producto");
        JButton btnEditar = new JButton("Editar seleccionado");
        JButton btnBorrar = new JButton("Borrar seleccionado");
        JButton btnValorTotal = new JButton("Ver valor total");
        agregarBoton(panelControles, btnAgregar, 4);
        agregarBoton(panelControles, btnEditar, 5);
        agregarBoton(panelControles, btnBorrar, 6);
        agregarBoton(panelControles, btnValorTotal, 7);

        JPanel panelStock = new JPanel(new BorderLayout(12, 12));
        panelStock.setBackground(FONDO);
        JScrollPane scrollProductos = new JScrollPane(tablaProductos);
        scrollProductos.getViewport().setBackground(SUPERFICIE);
        panelStock.add(scrollProductos, BorderLayout.CENTER);
        panelStock.add(panelControles, BorderLayout.WEST);

        JLabel tituloAdmin = new JLabel("Admin");
        tituloAdmin.setFont(new Font("SansSerif", Font.BOLD, 20));
        tituloAdmin.setForeground(Color.WHITE);
        JButton btnStock = new JButton("Stock");

        JPanel barraNavegacion = new JPanel(new BorderLayout(16, 0));
        barraNavegacion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AZUL_MARINO),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            barraNavegacion.setBackground(AZUL_MARINO);
        estilizarBotonStock(btnStock);
        barraNavegacion.add(tituloAdmin, BorderLayout.WEST);
        barraNavegacion.add(btnStock, BorderLayout.EAST);
        btnStock.addActionListener(e -> {
            inventario.recargarDesdeArchivo();
            refrescarTabla();
        });
        add(barraNavegacion, BorderLayout.NORTH);
        add(panelStock, BorderLayout.CENTER);

        tablaProductos.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());
        btnAgregar.addActionListener(e -> agregarProducto());
        btnEditar.addActionListener(e -> editarProducto());
        btnBorrar.addActionListener(e -> borrarProducto());
        btnValorTotal.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Valor total del inventario: $" + inventario.calcularValorTotalInventario()));

        refrescarTabla();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
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

    private void editarProducto() {
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
        modeloTabla.setRowCount(0);
        for (Producto producto : inventario.getListaProductos()) {
            modeloTabla.addRow(new Object[] { producto.getId(), producto.getNombre(), producto.getPrecio(),
                    producto.getStock(), producto.getCategoria() });
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
