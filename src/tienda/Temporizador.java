package tienda;

import javax.swing.Timer;

// Cuenta regresiva
public class Temporizador {
    private static final int DURACION_SEGUNDOS = 300; // 5 minutos (placeholder)
    private static final int AVISO_SEGUNDOS = 60;     // avisa cuando queda 1 minuto (placeholder)

    private final Carrito carrito;
    private final Timer timer;
    private int tiempoRestante;

    public Temporizador(Carrito carrito) {
        this.carrito = carrito;
        this.timer = new Timer(1000, e -> avanzar());
    }

    public void iniciarSiNoCorre() {
        if (!timer.isRunning()) {
            iniciarTemporizador();
        }
    }

    // reinicia el conteo desde cero
    public void iniciarTemporizador() {
        tiempoRestante = DURACION_SEGUNDOS;
        timer.restart();
    }

    // si el carrito ta vacio, no avanza el tiempo, si no, reinicia el conteo
    public void reiniciarSegunCarrito() {
        if (carrito.getProductos().isEmpty()) {
            detener();
        } else {
            iniciarTemporizador();
        }
    }

    public void detener() {
        timer.stop();
    }

    public int getTiempoRestante() {
        return tiempoRestante;
    }

    // Cambiar por un JOptionPane o un JLabel despoe, ta feo asi q PLACEHOLDER
    private void avisarUsuario() {
        System.out.println("Tu carrito se vaciará en " + tiempoRestante + " segundos");
    }

    private void avanzar() {
        tiempoRestante--;

        if (tiempoRestante <= AVISO_SEGUNDOS && tiempoRestante > 0) {
            avisarUsuario();
        }

        if (tiempoRestante <= 0) {
            detener();
            carrito.vaciarCarrito();//la magia
        }
    }
}