package tienda;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
