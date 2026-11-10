package tienda;

import java.time.LocalDate;

public class Cupon {
    public static final String PORCENTAJE = "PORCENTAJE";
    public static final String MONTO = "MONTO";

    private final String codigo;
    private final String tipo;
    private final double valor;
    private final LocalDate fechaVencimiento;
    private final int usosMaximos;
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

    public boolean estaVigente() {
        return !LocalDate.now().isAfter(fechaVencimiento);
    }

    public boolean tieneUsosDisponibles() {
        return usosRealizados < usosMaximos;
    }

    public String validar() {
        if (!estaVigente()) {
            return "El cupón " + codigo + " está vencido.";
        }
        if (!tieneUsosDisponibles()) {
            return "El cupón " + codigo + " ya no tiene usos disponibles.";
        }
        return null;
    }

    public double calcularDescuento(double subtotal) {
        double descuento = tipo.equals(PORCENTAJE) ? subtotal * valor / 100 : valor;
        return Math.min(descuento, subtotal);
    }

    public void registrarUso() {
        usosRealizados++;
    }

    public String toCSV() {
        return codigo + "," + tipo + "," + valor + "," + fechaVencimiento + "," + usosMaximos + ","
                + usosRealizados;
    }
}
