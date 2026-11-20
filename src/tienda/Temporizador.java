package tienda;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

// Cuenta regresiva de la reserva del carrito
public class Temporizador {
    private static final int AVISO_SEGUNDOS = 60; // 1 minuto antes

    private final Carrito carrito;
    private final Inventario inventario;
    private final VentanaUsuario ventana;
    private final JLabel lblContador;
    private Timer timer;
    private int duracionSegundos;
    private int tiempoRestante;
    private JLabel lblCarrito;

    public Temporizador(Carrito carrito, Inventario inventario, int minutos,
                        JLabel lblContador, VentanaUsuario ventana) {
        this.carrito = carrito;
        this.inventario = inventario;
        this.ventana = ventana;
        this.lblContador = lblContador;
        this.duracionSegundos = leerSegundos();
        this.tiempoRestante = duracionSegundos;
        this.timer = new Timer(1000, e -> avanzar());
    }

    public void setLabelCarrito(JLabel l) {
        lblCarrito = l;
    }

    private void mostrarTiempo(String texto) {
        lblContador.setText(texto);
        if (lblCarrito != null) lblCarrito.setText(texto);
    }

    private void avanzar() {
        tiempoRestante--;
        actualizarLabel();

        // aviso 1 minuto antes
        if (tiempoRestante == AVISO_SEGUNDOS) {
            JOptionPane.showMessageDialog(ventana,
                    "Te queda 1 minuto para que expire tu carrito, interactue para evitarlo",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }

        // TimeOut
        if (tiempoRestante <= 0) {
            timer.stop();
            carrito.vaciarCarrito();       // devuelve el stock
            inventario.guardarEnArchivo(); // guarda el stock en el CSV
            ventana.actualizarVista();     // refresca catalogo y carrito
            mostrarTiempo("--:--");
            JOptionPane.showMessageDialog(ventana, "Tu carrito se ha vaciado",
                    "Carrito expirado", JOptionPane.ERROR_MESSAGE);
        }
    }

    // se llama cada vez que el cliente modifica el carrito
    public void reiniciar() {
        duracionSegundos = leerSegundos();
        tiempoRestante = duracionSegundos;
        if (carrito.getProductos().isEmpty()) {
            timer.stop();
            mostrarTiempo("--:--");
        } else {
            actualizarLabel();
            timer.restart();
        }
    }

    // ---- métodos que venían de origin/main (se conservan por compatibilidad) ----
    public void iniciarSiNoCorre() {
        if (!timer.isRunning()) {
            iniciarTemporizador();
        }
    }

    // reinicia el conteo desde cero
    public void iniciarTemporizador() {
        duracionSegundos = leerSegundos();
        tiempoRestante = duracionSegundos;
        actualizarLabel();
        timer.restart();
    }

    // si el carrito está vacío no avanza el tiempo, si no, reinicia el conteo
    public void reiniciarSegunCarrito() {
        reiniciar();
    }

    public int getTiempoRestante() {
        return tiempoRestante;
    }
    // -----------------------------------------------------------------------------

    public void detener() {
        timer.stop();
    }

    private int leerSegundos() {
        try {
            return Integer.parseInt(
                    java.nio.file.Files.readString(java.nio.file.Path.of("tiempo_caja.txt")).trim()) * 60;
        } catch (Exception e) {
            return 5 * 60; // por defecto
        }
    }

    private void actualizarLabel() {
        mostrarTiempo(String.format("%02d:%02d", tiempoRestante / 60, tiempoRestante % 60));
    }
}
