package tienda;

import java.text.SimpleDateFormat;
import java.util.Date;

// Conserva los datos de una compra y los prepara para guardarlos como CSV.
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
    private String cupon;
    private double descuento;
    private EstadoPedido estado;
    
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
        this.cupon = "";
        this.descuento = 0;
        this.estado = EstadoPedido.PENDIENTE; // toda venta nueva parte como pendiente
    }

    // constructor para ventas con cupon, guarda el codigo y el monto descontado
    public Venta(String idVenta, Date fecha, String rutCliente, String nombreCliente,
                 String correoCliente, String detalleProductos, double subtotal, double iva, double total,
                 String cupon, double descuento) {
        this(idVenta, fecha, rutCliente, nombreCliente, correoCliente, detalleProductos, subtotal, iva, total);
        this.cupon = cupon == null ? "" : cupon;
        this.descuento = descuento;
    }

    public String getIdVenta() { return idVenta; }
    public Date getFecha() { return fecha; }
    public String getRutCliente() { return rutCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public EstadoPedido getEstado() { return estado; }
    public void setIdVenta(String idVenta) {
        this.idVenta = idVenta;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
    
    // genera un string de la fecha y hora exacta
    public String getFechaFormateada() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(fecha);
    }

    // convierte la venta a una linea csv
    public String toCSV() {
        // Escapa los campos de texto para mantener intacta la estructura de columnas.
        return escaparCSV(idVenta) + "," + getFechaFormateada() + "," + escaparCSV(rutCliente) + ","
                + escaparCSV(nombreCliente) + "," + escaparCSV(correoCliente) + ","
                + escaparCSV(detalleProductos) + "," + subtotal + "," + iva + "," + total + ","
                + escaparCSV(cupon) + "," + descuento + "," + estado.name();
    }
    private String escaparCSV(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}