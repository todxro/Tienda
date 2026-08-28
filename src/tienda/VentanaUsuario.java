package tienda;

import javax.swing.*;
import java.awt.*;

public class VentanaUsuario extends JFrame {
    private Inventario inventario;
    private JTextArea areaTexto;

    public VentanaUsuario() {
        this(null);
    }

    public VentanaUsuario(Usuario usuario) {
        this.inventario = new Inventario();

        setTitle(usuario == null ? "Vista Usuario - Catálogo" : "Catálogo - " + usuario.getNombre());
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Actualizar / Refrescar Catalogo");
        btnRefrescar.addActionListener(e -> actualizarVista());
        add(btnRefrescar, BorderLayout.SOUTH);

        actualizarVista();
        setLocationRelativeTo(null);
    }

    public void actualizarVista() {
        StringBuilder sb = new StringBuilder("=== CATÁLOGO (USUARIO) ===\n\n");
        for (Producto p : inventario.getListaProductos()) {
            sb.append(p).append("\n");
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