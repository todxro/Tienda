package tienda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class GestorUsuarios {
    // archivo donde se guardan los usuarios
    private static final String RUTA_ARCHIVO = "usuarios.csv";
    // usuarios cargados en memoria
    private final ArrayList<Usuario> usuarios = new ArrayList<>();

    // carga los usuarios al iniciar el gestor
    public GestorUsuarios() {
        cargarUsuarios();
    }

    public Usuario autenticar(String correo, String contrasenia) {
        // busca un usuario con las credenciales recibidas
        for (Usuario usuario : usuarios) {
            if (usuario.getCorreo().equalsIgnoreCase(correo)
                    && usuario.getContrasenia().equals(contrasenia)) {
                return usuario;
            }
        }
        return null;
    }

    public boolean registrar(String nombre, String apellido, String correo, String contrasenia) {
        // valida los datos antes de registrar la cuenta
        if (nombre.isBlank() || apellido.isBlank() || correo.isBlank() || contrasenia.isBlank()
                || correo.contains(",") || contrasenia.contains(",")) {
            return false;
        }

        if (autenticarPorCorreo(correo) != null) {
            return false;
        }

        usuarios.add(new Usuario(nombre, apellido, contrasenia, "", "", "", "", correo, 0,
                new Date(), ""));
        // guarda el nuevo usuario en el archivo
        guardarUsuarios();
        return true;
    }

    private Usuario autenticarPorCorreo(String correo) {
        // evita registrar correos repetidos
        for (Usuario usuario : usuarios) {
            if (usuario.getCorreo().equalsIgnoreCase(correo)) {
                return usuario;
            }
        }
        return null;
    }

    private void cargarUsuarios() {
        // lee los usuarios guardados en el archivo
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) {
            return;
        }
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primeraLinea = true; // salta el encabezado al leer
            while ((linea = lector.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }
                if (linea.trim().isEmpty())
                    continue;
                String[] datos = parsearLineaCSV(linea); // lee la linea respetando comillas
                if (datos.length == 4) {
                    String nombre = datos[0];
                    String apellido = datos[1];
                    String correo = datos[2];
                    String contrasenia = datos[3];
                    usuarios.add(new Usuario(nombre, apellido, contrasenia,
                            "", "", "", "", correo, 0, new Date(), ""));
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void guardarUsuarios() {
        // escribe todos los usuarios en el archivo
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            escritor.write("nombre,apellido,correo,contrasenia"); // escribe el encabezado del csv
            escritor.newLine();
            for (Usuario usuario : usuarios) {
                escritor.write(escaparCSV(usuario.getNombre()) + ","
                        + escaparCSV(usuario.getApellido()) + ","
                        + escaparCSV(usuario.getCorreo()) + ","
                        + escaparCSV(usuario.getContrasenia()));
                escritor.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar usuarios: " + e.getMessage());
        }
    }

    // separa una linea csv respetando las comillas
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
                campos.add(campoActual.toString());
                campoActual.setLength(0);
            } else {
                campoActual.append(c);
            }
        }
        campos.add(campoActual.toString());
        return campos.toArray(new String[0]);
    }

    // protege los campos especiales del csv
    private String escaparCSV(String valor) {
        if (valor == null)
            return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            valor = valor.replace("\"", "\"\"");
            return "\"" + valor + "\"";
        }
        return valor;
    }
}