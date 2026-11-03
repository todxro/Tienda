package tienda;

import java.time.LocalDate;

// cupon de descuento que crea el admin y que el cliente aplica en su carrito
public class Cupon {
    // tipos de descuento posibles
    public static final String PORCENTAJE = "PORCENTAJE";
    public static final String MONTO = "MONTO";

    private String codigo;
    private String tipo;
    private double valor;
    private LocalDate fechaVencimiento;
    private int usosMaximos;
    private int usosRealizados;

    public Cupon(String codigo, String tipo, double valor, LocalDate fechaVencimiento,
            int usosMaximos, int usosRealizados) {
        this.codigo = codigo.toUpperCase();
        this.tipo = tipo;
        this.valor = valor;
        this.fechaVencimiento = fechaVencimiento;
        this.usosMaximos = usosMaximos;
        this.usosRealizados = usosRealizados;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public int getUsosMaximos() {
        return usosMaximos;
    }

    public int getUsosRealizados() {
        return usosRealizados;
    }

    // el cupon sirve hasta el mismo dia de vencimiento
    public boolean estaVigente() {
        return !LocalDate.now().isAfter(fechaVencimiento);
    }

    public boolean tieneUsosDisponibles() {
        return usosRealizados < usosMaximos;
    }

    // devuelve el motivo si el cupon no se puede usar, o null si esta todo bien
    public String validar() {
        if (!estaVigente()) {
            return "El cupón " + codigo + " está vencido.";
        }
        if (!tieneUsosDisponibles()) {
            return "El cupón " + codigo + " ya no tiene usos disponibles.";
        }
        return null;
    }

    // calcula cuanto se descuenta del subtotal, sin pasarse del subtotal
    public double calcularDescuento(double subtotal) {
        double descuento;
        if (tipo.equals(PORCENTAJE)) {
            descuento = subtotal * valor / 100;
        } else {
            descuento = valor;
        }
        return Math.min(descuento, subtotal);
    }

    // se llama cuando se confirma una compra con este cupon
    public void registrarUso() {
        usosRealizados++;
    }

    // linea para guardar el cupon en cupones.csv
    public String toCSV() {
        return codigo + "," + tipo + "," + valor + "," + fechaVencimiento + "," + usosMaximos + ","
                + usosRealizados;
    }
}	