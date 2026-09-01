package tienda;

import javax.swing.*;
import java.awt.*;

public class VentanaUsuario extends JFrame {
    private final Usuario usuario;
    private final Inventario inventario;
    private final JTextArea areaTexto;
    private final JTextField campoIdProducto;
    private final JTextField campoCantidad;

    public VentanaUsuario() {
        this(null);
    }

    public VentanaUsuario(Usuario usuario) {
        this.usuario = usuario != null ? usuario : new Usuario("Usuario", "Demo", "1234", "", "", "", "",
                "demo@tienda.cl", 0, new java.util.Date(), "11111111-1");
        this.inventario = new Inventario();

        setTitle("Catálogo - " + this.usuario.getNombre());
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panelControl = new JPanel(new GridLayout(2, 3, 8, 8));
        panelControl.add(new JLabel("ID del producto:"));
        campoIdProducto = new JTextField();
        panelControl.add(campoIdProducto);

        panelControl.add(new JLabel("Cantidad:"));
        campoCantidad = new JTextField("1");
        panelControl.add(campoCantidad);

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
        add(btnRefrescar, BorderLayout.SOUTH);

        actualizarVista();
        setLocationRelativeTo(null);
    }

    private void agregarAlCarrito() {
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
            JOptionPane.showMessageDialog(this, "Se agregaron " + agregadas + " unidad(es) de " + producto.getNombre() + " al carrito.",
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

    // MAIN INDIVIDUAL PARA EJECUTAR SOLO EL USUARIO
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaLogin().setVisible(true);
        });
    }
}