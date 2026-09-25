package tienda;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VentanaUsuario extends JFrame {
    private static final Color AZUL_MARINO = new Color(27, 42, 107); //azul marino
    private static final Color DORADO      = new Color(242, 183, 5); //dorado
    private static final Color FONDO       = new Color(247, 247, 247); //fondo gris claro
    private static final Color TEXTO       = new Color(34, 34, 34);    //texto oscuro
    private static final Color BLANCO      = Color.WHITE;

    private final Usuario usuario;
    private final Inventario inventario;

    // componentes de la interfaz
    private JTable tablaCatalogo;
    private DefaultTableModel modeloCatalogo;
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;
    private JTextField campoCantidad;
    private JLabel labelTotal;

    public VentanaUsuario() {
        this(null);
    }

    public VentanaUsuario(Usuario usuario) {
        this.usuario = usuario != null ? usuario
                : new Usuario("Usuario", "Demo", "1234", "", "", "", "",
                        "demo@tienda.cl", 0, new java.util.Date(), "11111111-1");
        this.inventario = new Inventario();

        setTitle("Catálogo - " + this.usuario.getNombre());
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout(12, 12));

        add(crearPanelBusqueda(), BorderLayout.NORTH);
        add(crearPanelCatalogo(), BorderLayout.CENTER);
        add(crearPanelCarrito(), BorderLayout.SOUTH);

        actualizarVista();
        setLocationRelativeTo(null);
    }

    // construye la barra superior azul con el título y el buscador
    private JPanel crearPanelBusqueda() {
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
        JButton btnBuscar = new JButton("Buscar");
        JButton btnRefrescar = new JButton("Ver todo");

        btnBuscar.addActionListener(e -> {
            String texto = campoBuscar.getText().trim();
            if (!texto.isEmpty()) {
                cargarProductosEnTabla(inventario.buscarPorNombre(texto));
            }
        });
        btnRefrescar.addActionListener(e -> actualizarVista());

        busqueda.add(labelBuscar);
        busqueda.add(campoBuscar);
        busqueda.add(btnBuscar);
        busqueda.add(btnRefrescar);

        panel.add(busqueda, BorderLayout.EAST);
        return panel;
    }

    // arma la tabla del catalogo en el centro de la ventana
    private JPanel crearPanelCatalogo() {
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
        tablaCatalogo.setGridColor(new Color(220, 220, 220));

        panel.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);
        return panel;
    }

    // arma el panel de abajo con el carrito y los botones
    private JPanel crearPanelCarrito() {
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
        JScrollPane scrollCarrito = new JScrollPane(tablaCarrito);
        scrollCarrito.setPreferredSize(new Dimension(0, 140));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controles.setBackground(FONDO);

        controles.add(new JLabel("Cantidad:"));
        campoCantidad = new JTextField("1", 4);
        controles.add(campoCantidad);

        JButton btnAgregar = new JButton("Agregar al carrito");
        estilizarBoton(btnAgregar, DORADO, TEXTO);
        btnAgregar.addActionListener(e -> agregarAlCarrito());
        controles.add(btnAgregar);

        JButton btnComprar = new JButton("Comprar");
        estilizarBoton(btnComprar, AZUL_MARINO, BLANCO);
        btnComprar.addActionListener(e -> comprarProductos());
        controles.add(btnComprar);

        labelTotal = new JLabel("TOTAL: $0");
        labelTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        labelTotal.setForeground(AZUL_MARINO);
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

        // cuenta cuantas unidades se lograron agregar de verdad, por si el stock no alcanza
        int agregadas = 0;
        for (int i = 0; i < cantidad; i++) {
            if (!usuario.getCarrito().agregarProducto(producto)) {
                break;
            }
            agregadas++;
        }

        if (agregadas == 0) {
            JOptionPane.showMessageDialog(this, "No hay stock suficiente para ese producto.", "Stock insuficiente",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Se agregaron " + agregadas + " unidad(es) de " + producto.getNombre() + " al carrito.",
                    "Producto agregado", JOptionPane.INFORMATION_MESSAGE);
        }

        campoCantidad.setText("1");
        actualizarVista();
    }

    private void comprarProductos() {
        if (usuario.getCarrito().getProductos().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.", "Compra",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = usuario.getCarrito().calcularTotal();
        boolean compraExitosa = usuario.getCarrito().finalizarCompra();

        if (!compraExitosa) {
            JOptionPane.showMessageDialog(this, "No hay stock suficiente para completar la compra.", "Compra fallida",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        inventario.guardarEnArchivo();
        JOptionPane.showMessageDialog(this, "Compra realizada con éxito. Total: $" + total, "Compra exitosa",
                JOptionPane.INFORMATION_MESSAGE);
        actualizarVista();
    }

    public void actualizarVista() {
        // vuelve a llenar la tabla del catalogo con lo que hay en inventario
        cargarProductosEnTabla(inventario.getListaProductos());

        // vuelve a llenar la tabla del carrito
        modeloCarrito.setRowCount(0);
        for (Producto p : usuario.getCarrito().getProductos()) {
            modeloCarrito.addRow(new Object[]{
                    p.getNombre(),
                    usuario.getCarrito().getCantidad(p.getId()),
                    "$" + usuario.getCarrito().calcularSubtotal(p.getId())
            });
        }

        labelTotal.setText("TOTAL: $" + usuario.getCarrito().calcularTotal());
    }

    // inicia la ventana de usuario
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaLogin().setVisible(true);
        });
    }
}