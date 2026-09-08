package tienda;

import java.awt.*;
import javax.swing.*;

public class VentanaAdmin extends JFrame {
    // inventario que administra esta ventana
    private Inventario inventario;
    // area donde se muestra el inventario
    private JTextArea areaTexto;

    // crea la ventana del administrador
    public VentanaAdmin() {
        this.inventario = new Inventario();

        setTitle("Panel Administrador");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        JPanel panelControles = new JPanel(new GridLayout(3, 2, 5, 5));
        // campos para ingresar un producto
        JTextField txtNombre = new JTextField();
        JTextField txtCantidad = new JTextField();
        // botones de acciones del administrador
        JButton btnAgregar = new JButton("Agregar Producto");
        JButton btnValorTotal = new JButton("Valor Total");

        // agregar botones a pantalla
        panelControles.add(new JLabel(" Nombre:"));
        panelControles.add(txtNombre);
        panelControles.add(new JLabel(" Cantidad:"));
        panelControles.add(txtCantidad);
        panelControles.add(btnAgregar);
        panelControles.add(btnValorTotal);

        add(panelControles, BorderLayout.SOUTH);
        
        // funcion boton valor total
        btnValorTotal.addActionListener(e -> {
            double total = inventario.calcularValorTotalInventario();
            JOptionPane.showMessageDialog(this, "Valor Total del Inventario: $" + total);
        });

        // funcion boton agregar producto
        btnAgregar.addActionListener(e -> {
            String nombre = txtNombre.getText();
            try {
                int cantidad = Integer.parseInt(txtCantidad.getText());
                if (nombre.isBlank() || cantidad < 0) {
                    throw new IllegalArgumentException();
                }

                String newId = inventario.generarNuevoId();
                inventario.agregarProducto(new Producto(newId, nombre, 15000, cantidad, "BASE"));
                actualizarVista();
                txtNombre.setText("");
                txtCantidad.setText("");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        "Ingresa un nombre y una cantidad entera no negativa.",
                        "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            }
        });

        actualizarVista();
        setLocationRelativeTo(null);
    }

    public void actualizarVista() {
        // actualiza el texto mostrado en pantalla
        StringBuilder sb = new StringBuilder("INVENTARIO (ADMIN)\n\n");
        for (Producto p : inventario.getListaProductos()) {
            sb.append(p).append("\n");
        }
        areaTexto.setText(sb.toString());
    }

    public static void main(String[] args) {
        // inicia la ventana del administrador
        SwingUtilities.invokeLater(() -> {
            new VentanaAdmin().setVisible(true);
        });
    }
}