package tienda;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Despacho {
    public static final String RETIRO_TIENDA = "Retiro en tienda";
    public static final String DESPACHO_DOMICILIO = "Despacho a domicilio";

    private String modalidad;
    private Direccion direccion;
    private TarifaEnvio tarifaAplicada;
    private boolean envioGratisPorMonto;

    public Despacho() {
        this.modalidad = RETIRO_TIENDA;
    }

    public void configurarRetiroEnTienda() {
        this.modalidad = RETIRO_TIENDA;
        this.direccion = null;
        this.tarifaAplicada = null;
        this.envioGratisPorMonto = false;
    }

    public void configurarDespachoDomicilio(Direccion direccion, TarifaEnvio tarifa, double subtotalNeto,
                                            double montoMinimoGratis) {
        this.modalidad = DESPACHO_DOMICILIO;
        this.direccion = direccion;
        this.tarifaAplicada = tarifa;
        this.envioGratisPorMonto = montoMinimoGratis > 0 && subtotalNeto >= montoMinimoGratis;
    }

    public String getModalidad() { return modalidad; }
    public Direccion getDireccion() { return direccion; }
    public boolean esRetiroEnTienda() { return RETIRO_TIENDA.equals(modalidad); }

    public double calcularCosto(double subtotalNeto) {
        if (esRetiroEnTienda() || subtotalNeto <= 0 || tarifaAplicada == null) {
            return 0;
        }
        return envioGratisPorMonto ? 0 : tarifaAplicada.getCosto();
    }

    // Devuelve null si todo está válido, o el mensaje de error si falta algo.
    public String validar() {
        if (esRetiroEnTienda()) {
            return null;
        }
        if (direccion == null || !direccion.esValida()) {
            return "Completa nombre de quien recibe, dirección, comuna y región.";
        }
        if (tarifaAplicada == null) {
            return "La región seleccionada no existe en la tabla de tarifas de envío.";
        }
        return null;
    }

    public String obtenerFechaEstimadaTexto() {
        if (esRetiroEnTienda()) {
            return "Disponible hoy en tienda";
        }
        if (tarifaAplicada == null) {
            return "-";
        }
        LocalDate fecha = sumarDiasHabiles(LocalDate.now(), tarifaAplicada.getDiasHabiles());
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " (" + tarifaAplicada.getDiasHabiles() + " días hábiles)";
    }

    public String obtenerResumenDireccion() {
        if (esRetiroEnTienda() || direccion == null) {
            return "Retiro en local";
        }
        return direccion.toString();
    }

    private LocalDate sumarDiasHabiles(LocalDate inicio, int dias) {
        LocalDate resultado = inicio;
        int agregados = 0;
        while (agregados < dias) {
            resultado = resultado.plusDays(1);
            DayOfWeek dia = resultado.getDayOfWeek();
            if (dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY) {
                agregados++;
            }
        }
        return resultado;
    }
}