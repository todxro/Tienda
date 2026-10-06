package tienda;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class GestorVentas {
    private static final String RUTA_ARCHIVO = "ventas.csv";

    public void guardarVenta(Venta venta) {
        File archivo = new File(RUTA_ARCHIVO);
        boolean existe = archivo.exists();

        // Abre el archivo en modo "append" (adicion) para añadir datos al final del archivo 
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, true))) {
            if (!existe) {
                bw.write("idVenta,fecha,rutCliente,nombreCliente,correoCliente,detalleProductos,subtotal,iva,total");
                bw.newLine();
            }
            // guarda la informacion del cliente, productos, subtotal, IVA y total
            bw.write(venta.toCSV());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error al guardar la venta: " + e.getMessage());
        }
    }

    public String generarIdVenta() {
        // genera un ID unico gracias a java uuid 
        return "V-" + UUID.randomUUID().toString().substring(0, 8);
    }
}