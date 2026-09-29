package tienda;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Venta {
    private String idVenta;
    private Date fecha;
    private String rutCliente;
    private String nombreCliente;
    private String correoCliente;
    private String detalleProductos;
    private double subtotal;
    private double iva;
    private double total;

    public Venta(String idVenta, Date fecha, String rutCliente, String nombreCliente, 
                 String correoCliente, String detalleProductos, double subtotal, double iva, double total) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.rutCliente = rutCliente;
        this.nombreCliente = nombreCliente;
        this.correoCliente = correoCliente;
        this.detalleProductos = detalleProductos;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
    }

    public String getIdVenta() { return idVenta; }
    public Date getFecha() { return fecha; }
    public String getRutCliente() { return rutCliente; }
    
    // genera un string de la fecha y hora exacta
    public String getFechaFormateada() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(fecha);
    }

    // convierte la venta a una linea csv
    public String toCSV() {
        String detalleEscapado = "\"" + detalleProductos.replace("\"", "\"\"") + "\"";
        return idVenta + "," + getFechaFormateada() + "," + rutCliente + "," + nombreCliente + "," + 
               correoCliente + "," + detalleEscapado + "," + subtotal + "," + iva + "," + total;
    }
}