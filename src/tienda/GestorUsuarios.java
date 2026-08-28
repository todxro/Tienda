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
    private static final String RUTA_ARCHIVO = "usuarios.txt";
    private final ArrayList<Usuario> usuarios = new ArrayList<>();

    public GestorUsuarios() {
        cargarUsuarios();
    }

    public Usuario autenticar(String correo, String contrasenia) {
        for (Usuario usuario : usuarios) {
            if (usuario.getCorreo().equalsIgnoreCase(correo)
                    && usuario.getContrasenia().equals(contrasenia)) {
                return usuario;
            }
        }
        return null;
    }

    public boolean registrar(String nombre, String apellido, String correo, String contrasenia) {
        if (nombre.isBlank() || apellido.isBlank() || correo.isBlank() || contrasenia.isBlank()
                || correo.contains(";") || contrasenia.contains(";")) {
            return false;
        }

        if (autenticarPorCorreo(correo) != null) {
            return false;
        }

        usuarios.add(new Usuario(nombre, apellido, contrasenia, "", "", "", "", correo, 0,
                new Date(), ""));
        guardarUsuarios();
        return true;
    }

    private Usuario autenticarPorCorreo(String correo) {
        for (Usuario usuario : usuarios) {
            if (usuario.getCorreo().equalsIgnoreCase(correo)) {
                return usuario;
            }
        }
        return null;
    }

    private void cargarUsuarios() {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) {
            return;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] datos = linea.split(";", -1);
                if (datos.length == 4) {
                    usuarios.add(new Usuario(datos[0], datos[1], datos[3], "", "", "", "",
                            datos[2], 0, new Date(), ""));
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void guardarUsuarios() {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Usuario usuario : usuarios) {
                escritor.write(usuario.getNombre() + ";" + usuario.getApellido() + ";"
                        + usuario.getCorreo() + ";" + usuario.getContrasenia());
                escritor.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar usuarios: " + e.getMessage());
        }
    }
}