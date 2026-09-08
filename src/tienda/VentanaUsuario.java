package tienda;

import javax.swing.*;
import java.awt.*;

public class VentanaUsuario extends JFrame {
    // usuario que esta usando la ventana
    private final Usuario usuario;
    // inventario que se muestra al usuario
    private final Inventario inventario;
    // componentes principales de la ventana
    private final JTextArea areaTexto;
    private final JTextField campoIdProducto;
    private final JTextField campoCantidad;

    // crea una ventana de usuario de prueba
    public VentanaUsuario() {
        this(null);
    }

    // crea la ventana con el usuario recibido
    public VentanaUsuario(Usuario usuario) {
        this.usuario = usuario != null ? usuario
                : new Usuario("Usuario", "Demo", "1234", "", "", "", "",
                        "demo@tienda.cl", 0, new java.util.Date(), "11111111-1");
        this.inventario = new Inventario();

        setTitle("Catálogo - " + this.usuario.getNombre());
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panelControl = new JPanel(new GridLayout(2, 3, 8, 8));
        panelControl.add(new JLabel("ID del producto:"));
        campoIdProducto = new JTextField();
        panelControl.add(campoIdProducto);

        panelControl.add(new JLabel("Cantidad:"));
        campoCantidad = new JTextField("1");
        panelControl.add(campoCantidad);

        // botones para manejar la compra
        JButton btnAgregar = new JButton("Agregar al carrito");
        btnAgregar.addActionListener(e -> agregarAlCarrito());
        panelControl.add(btnAgregar);

        JButton btnComprar = new JButton("Comprar");
        btnComprar.addActionListener(e -> comprarProductos());
        panelControl.add(btnComprar);

        add(panelControl, BorderLayout.NORTH);

        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Actualizar / Refrescar catálogo");
        btnRefrescar.addActionListener(e -> actualizarVista());

        // campo para buscar productos por nombre
        JTextField campoBuscar = new JTextField(10);
        JButton btnBuscar = new JButton("Buscar Nombre");

        // busca en el inventario y actualiza la lista
        btnBuscar.addActionListener(e -> {
            String texto = campoBuscar.getText().trim();
            if (!texto.isEmpty()) {
                StringBuilder sb = new StringBuilder("Productos encontrados:\n\n");
                for (Producto p : inventario.buscarPorNombre(texto)) {
                    sb.append(p).append("\n");
                }
                areaTexto.setText(sb.toString());
            }
        });
        
        // panel inferior de botones
        JPanel panelAbajo = new JPanel();
        panelAbajo.add(new JLabel("Buscar:"));
        panelAbajo.add(campoBuscar);
        panelAbajo.add(btnBuscar);
        panelAbajo.add(btnRefrescar);

        add(panelAbajo, BorderLayout.SOUTH);

        actualizarVista();
        setLocationRelativeTo(null);
    }

    private void agregarAlCarrito() {
        // agrega la cantidad indicada al carrito
        String id = campoIdProducto.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes ingresar el ID del producto.", "Falta información",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Producto producto = inventario.buscarProducto(id);
        if (producto == null) {
            JOptionPane.showMessageDialog(this, "No se encontró el producto con el ID " + id, "Producto no válido",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

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

        int cantidadEnCarrito = usuario.getCarrito().getCantidad(producto.getId());
        int disponible = producto.getStock() - cantidadEnCarrito;
        if (cantidad > disponible) {
            JOptionPane.showMessageDialog(this,
                    "Solo hay " + disponible + " unidad(es) disponible(s) para agregar.",
                    "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = 0; i < cantidad; i++) {
            usuario.getCarrito().agregarProducto(producto);
        }

        if (cantidad == 0) {
            JOptionPane.showMessageDialog(this, "No hay stock suficiente para ese producto.", "Stock insuficiente",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Se agregaron " + cantidad + " unidad(es) de " + producto.getNombre() + " al carrito.",
                    "Producto agregado", JOptionPane.INFORMATION_MESSAGE);
        }

        campoCantidad.setText("1");
        actualizarVista();
    }

    private void comprarProductos() {
        // valida y finaliza la compra
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
        // muestra inventario y carrito en pantalla
        StringBuilder sb = new StringBuilder("=== CATÁLOGO (USUARIO) ===\n\n");
        for (Producto p : inventario.getListaProductos()) {
            sb.append(p).append("\n");
        }

        sb.append("\n=== CARRITO ===\n");
        if (usuario.getCarrito().getProductos().isEmpty()) {
            sb.append("El carrito está vacío.\n");
        } else {
            for (Producto p : usuario.getCarrito().getProductos()) {
                sb.append("- ")
                        .append(p.getNombre())
                        .append(" | Cantidad: ")
                        .append(usuario.getCarrito().getCantidad(p.getId()))
                        .append(" | Subtotal: $")
                        .append(usuario.getCarrito().calcularSubtotal(p.getId()))
                        .append("\n");
            }
            sb.append("TOTAL: $")
                    .append(usuario.getCarrito().calcularTotal())
                    .append("\n");
        }

        areaTexto.setText(sb.toString());
    }

    // inicia la ventana de usuario
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaLogin().setVisible(true);
        });
    }
}