package tienda;

import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class VentanaLogin extends JFrame {
    private final GestorUsuarios gestorUsuarios;
    private final JTextField campoCorreo = new JTextField();
    private final JPasswordField campoContrasenia = new JPasswordField();

    public VentanaLogin() {
        gestorUsuarios = new GestorUsuarios();
        setTitle("Iniciar sesión");
        setSize(360, 180);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel formulario = new JPanel(new GridLayout(3, 2, 8, 8));
        formulario.add(new JLabel("Correo:"));
        formulario.add(campoCorreo);
        formulario.add(new JLabel("Contraseña:"));
        formulario.add(campoContrasenia);

        JButton iniciar = new JButton("Iniciar sesión");
        JButton crearCuenta = new JButton("Crear cuenta");
        formulario.add(iniciar);
        formulario.add(crearCuenta);
        add(formulario);

        iniciar.addActionListener(e -> iniciarSesion());
        crearCuenta.addActionListener(e -> mostrarRegistro());
        getRootPane().setDefaultButton(iniciar);
    }

    private void iniciarSesion() {
        Usuario usuario = gestorUsuarios.autenticar(campoCorreo.getText().trim(),
                new String(campoContrasenia.getPassword()));
        if (usuario == null) {
            JOptionPane.showMessageDialog(this, "Correo o contraseña incorrectos.", "Acceso denegado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new VentanaUsuario(usuario).setVisible(true);
        dispose();
    }

    private void mostrarRegistro() {
        JTextField nombre = new JTextField();
        JTextField apellido = new JTextField();
        JTextField correo = new JTextField();
        JPasswordField contrasenia = new JPasswordField();
        Object[] campos = { "Nombre:", nombre, "Apellido:", apellido, "Correo:", correo,
                "Contraseña:", contrasenia };

        int resultado = JOptionPane.showConfirmDialog(this, campos, "Crear cuenta",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        boolean creada = gestorUsuarios.registrar(nombre.getText().trim(), apellido.getText().trim(),
                correo.getText().trim(), new String(contrasenia.getPassword()));
        if (creada) {
            campoCorreo.setText(correo.getText().trim());
            campoContrasenia.setText("");
            JOptionPane.showMessageDialog(this, "Cuenta creada. Ahora puedes iniciar sesión.");
        } else {
            JOptionPane.showMessageDialog(this, "Completa todos los campos y usa un correo disponible.",
                    "No se pudo crear la cuenta", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}