package tienda;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class Temporizador {
    private static final int AVISO_SEGUNDOS = 60; // 1 minuto antes

    private final Carrito carrito;
    private final Inventario inventario;
    private final VentanaUsuario ventana;
    private final JLabel lblContador;
    private Timer timer; 
    private int duracionSegundos;
    private int tiempoRestante;

    public Temporizador(Carrito carrito, Inventario inventario, int minutos,
                        JLabel lblContador, VentanaUsuario ventana) {
        this.carrito = carrito;
        this.inventario = inventario;
        this.ventana = ventana;
        this.lblContador = lblContador;
        this.duracionSegundos = minutos * 60;
        this.tiempoRestante = duracionSegundos;

        // se ejecuta cada 1 segundo
        timer = new Timer(1000, e -> {
            tiempoRestante--;
            actualizarLabel();

            // aviso 1 minuto antes
            if (tiempoRestante == AVISO_SEGUNDOS) {
                JOptionPane.showMessageDialog(ventana, "Te queda 1 minuto para que expire tu carrito, interactue para evitarlo",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }

            // TimeOut
            if (tiempoRestante <= 0) {
                timer.stop();
                carrito.vaciarCarrito();       // devuelve el stock
                inventario.guardarEnArchivo(); // guarda el stock en el CSV
                ventana.actualizarVista();     // refresca catalogo y carrito
                lblContador.setText("--:--");
                JOptionPane.showMessageDialog(ventana, "Tu carrito se ha vaciado",
                        "Carrito expirado", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // se llama cada vez que el cliente modifica el carrito
    public void reiniciar() {
        tiempoRestante = duracionSegundos;
        if (carrito.getProductos().isEmpty()) {
            timer.stop();
            lblContador.setText("--:--");
        } else {
            actualizarLabel();
            timer.restart();
        }
    }

    public void detener() {
        timer.stop();
    }

    private void actualizarLabel() {
        int m = tiempoRestante / 60;
        int s = tiempoRestante % 60;
        lblContador.setText(String.format("%02d:%02d", m, s));
    }
}