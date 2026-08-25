package tienda;

import java.awt.*;
import javax.swing.*;

public class VentanaAdmin extends JFrame {
    private Inventario inventario;
    private JTextArea areaTexto;

    public VentanaAdmin() {
        this.inventario = new Inventario();

        setTitle("Panel Administrador");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        JPanel panelControles = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField txtNombre = new JTextField("Mouse Gamer");
        JButton btnAgregar = new JButton("Agregar Producto");
        JButton btnStock = new JButton("Modificar Stock P01");

        panelControles.add(new JLabel(" Nombre:"));
        panelControles.add(txtNombre);
        panelControles.add(btnAgregar);
        panelControles.add(btnStock);

        add(panelControles, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> {
            String nombre = txtNombre.getText();
            if (!nombre.isEmpty()) {
                String newId = "P0" + (inventario.getListaProductos().size() + 1);
                inventario.agregarProducto(new Producto(newId, nombre, 15000, 10, "BASE"));
                actualizarVista();
            }
        });

        btnStock.addActionListener(e -> {
            inventario.actualizarStock("P01", 5);
            actualizarVista();
        });

        actualizarVista();
        setLocationRelativeTo(null);
    }

    public void actualizarVista() {
        StringBuilder sb = new StringBuilder("=== INVENTARIO (ADMIN) ===\n\n");
        for (Producto p : inventario.getListaProductos()) {
            sb.append(p).append("\n");
        }
        areaTexto.setText(sb.toString());
    }

    // MAIN INDIVIDUAL PARA EJECUTAR SOLO EL ADMIN
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaAdmin().setVisible(true);
        });
    }
}