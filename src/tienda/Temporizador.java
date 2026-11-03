package tienda;

import java.util.Timer;
import java.util.TimerTask;

public class Temporizador {
    private static final int DURACION_SEGUNDOS = 300;  // 5 minutos (placehodler)
    private static final int AVISO_SEGUNDOS = 60;      // avisar cuando quede 1 minuto (palceholder)

    private Timer timer;
    private int tiempoRestante;
    private final Carrito carrito;

    public Temporizador(Carrito carrito) {
        this.carrito = carrito;
    }

    public void iniciarSiNoCorre() {
        if (timer == null) {
            iniciarTemporizador();
        }
    }

    // Reinicia el conteo desde cero (reutilizable para cada accion del usuario en el carrito :b)
    public void iniciarTemporizador() {
        detener();
        tiempoRestante = DURACION_SEGUNDOS;
        timer = new Timer(true);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tiempoRestante--;

                if (tiempoRestante == AVISO_SEGUNDOS) {
                    System.out.println("Su carrito se borrara en 1 minuto"); //PLACEHOLDER           
                    // //cambiar por un jframe supongo, asi no es tan penca
                    }

                if (tiempoRestante <= 0) {
                    detener();
                    carrito.vaciarCarrito();
                }
            }
        }, 1000, 1000);
    }

    public void detener() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public int getTiempoRestante() {
        return tiempoRestante;
    }
}