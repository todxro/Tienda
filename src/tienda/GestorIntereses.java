package tienda;

import java.io.*;
import java.util.ArrayList;

// administra la persistencia de las listas de deseos y alertas de stock
public class GestorIntereses {
    private static final String ARCHIVO_DESEOS = "deseos.csv";
    private static final String ARCHIVO_SUSCRIPCIONES = "suscripciones.csv";

    // carga los fav del usuario al iniciar sesión
    public static void cargarDeseos(Usuario usuario) {
        usuario.getListaDeseos().clear(); //si no tiene una lista se crea 
        File archivo = new File(ARCHIVO_DESEOS);
        if (!archivo.exists()) return; 

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {  //carga el archivo y filtra por correo
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 2 && datos[0].equalsIgnoreCase(usuario.getCorreo())) {
                    usuario.agregarDeseo(datos[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar deseos: " + e.getMessage());
        }
    }

    public static void agregarRegistro(String rutaArchivo, String correo, String idProducto) { //logica para agregar un objeto a fav o suscribirse
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo, true))) {
            bw.write(correo + "," + idProducto);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error al guardar en " + rutaArchivo + ": " + e.getMessage());
        }
    }

    public static void eliminarRegistro(String rutaArchivo, String correo, String idProducto) {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) return;

        ArrayList<String> lineasRestantes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 2 && datos[0].equalsIgnoreCase(correo) && datos[1].equalsIgnoreCase(idProducto)) {
                    continue; // se salta la linea que queremos borrar
                }
                lineasRestantes.add(linea);
            }
        } catch (IOException e) { return; }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (String l : lineasRestantes) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {}
    }
    public static ArrayList<String> obtenerCorreosSuscritos(String idProducto) { //obtenemos los que tienen una alerta 
        ArrayList<String> correos = new ArrayList<>();
        File archivo = new File(ARCHIVO_SUSCRIPCIONES);
        if (!archivo.exists()) return correos;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 2 && datos[1].equalsIgnoreCase(idProducto)) {
                    if (!correos.contains(datos[0])) correos.add(datos[0]);
                }
            }
        } catch (IOException e) {}
        return correos;
    }
}