package tienda;


import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class GestorVentas {
    private static final String RUTA_ARCHIVO = "ventas.csv";
    private static final String ENCABEZADO = "numeroTicket,fecha,rutCliente,nombreCliente,correoCliente,detalleProductos,subtotal,iva,total";

    public void guardarVenta(Venta venta) throws IOException {
        File archivo = new File(RUTA_ARCHIVO);
        prepararArchivo(archivo);

        // Abre el archivo en modo "append" (adicion) para añadir datos al final del archivo 
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, true))) {
            // guarda la informacion del cliente, productos, subtotal, IVA y total
            bw.write(venta.toCSV());
            bw.newLine();
        }
    }

    public String guardarVentaInvitado(Venta venta) throws IOException {
        String numeroVenta = String.valueOf(contarVentasInvitado() + 1);
        venta.setIdVenta(numeroVenta);
        venta.setNombreCliente("Invitado");
        guardarVenta(venta);
        return numeroVenta;
    }

    private void prepararArchivo(File archivo) throws IOException {
        if (!archivo.exists() || archivo.length() == 0) {
            Files.write(archivo.toPath(), List.of(ENCABEZADO), StandardCharsets.UTF_8);
            return;
        }

        List<String> lineas = Files.readAllLines(archivo.toPath(), StandardCharsets.UTF_8);
        if (lineas.isEmpty()) {
            lineas.add(ENCABEZADO);
            Files.write(archivo.toPath(), lineas, StandardCharsets.UTF_8);
        } else if (!ENCABEZADO.equals(lineas.get(0))) {
            lineas.set(0, ENCABEZADO);
            Files.write(archivo.toPath(), lineas, StandardCharsets.UTF_8);
        }
    }

    private int contarVentasInvitado() throws IOException {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) {
            return 0;
        }

        int cantidad = 0;
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primeraLinea = true;
            while ((linea = lector.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }

                String[] datos = linea.split(",", -1);
                if (datos.length > 3 && (datos[3].equals("Invitado")
                        || datos[3].startsWith("Invitado - Venta "))) {
                    cantidad++;
                }
            }
        }
        return cantidad;
    }
}