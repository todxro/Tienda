package tienda;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
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
    public ArrayList<String[]> obtenerHistorialCliente(String correoCliente) {
        ArrayList<String[]> historial = new ArrayList<>();
        File archivo = new File(RUTA_ARCHIVO);
        
        if (!archivo.exists()) {
            return historial;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primeraLinea = true; 
            
            while ((linea = lector.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue; // salta el encabezado
                }
                if (linea.trim().isEmpty()) continue;
                
                String[] datos = parsearLineaCSV(linea);
            
                if (datos.length >= 9 && datos[4].equalsIgnoreCase(correoCliente)) {
                
                    historial.add(new String[]{datos[1], datos[5], "$" + datos[8]});
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer historial: " + e.getMessage());
        }
        

        return historial;
    }

    public ArrayList<String[]> obtenerTodasLasVentas() throws IOException {
        ArrayList<String[]> ventas = new ArrayList<>();
        File archivo = new File(RUTA_ARCHIVO);

        if (!archivo.exists()) {
            return ventas;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroLinea = 0;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (numeroLinea == 1 || linea.trim().isEmpty()) {
                    continue;
                }

                String[] datos = parsearLineaCSV(linea);
                if (datos.length < 9) {
                    throw new IOException("La venta en la línea " + numeroLinea + " está incompleta.");
                }
                ventas.add(datos);
            }
        }

        return ventas;
    }

    private String[] parsearLineaCSV(String linea) {
        ArrayList<String> campos = new ArrayList<>();
        StringBuilder campoActual = new StringBuilder();
        boolean dentroDeComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (dentroDeComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    campoActual.append('"');
                    i++;
                } else {
                    dentroDeComillas = !dentroDeComillas;
                }
            } else if (c == ',' && !dentroDeComillas) {
                campos.add(campoActual.toString().trim());
                campoActual.setLength(0);
            } else {
                campoActual.append(c);
            }
        }
        campos.add(campoActual.toString().trim());
        return campos.toArray(new String[0]);
    }
}
