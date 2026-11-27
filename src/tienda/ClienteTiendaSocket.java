package tienda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

// Cliente que recibe las actualizaciones de catálogo publicadas por ADMIN.
public final class ClienteTiendaSocket implements AutoCloseable {
    public interface Escucha {
        void alConectar();
        void alActualizarProducto(Producto producto);
        void alEliminarProducto(String idProducto);
        void alDesconectar(IOException error);
        default void alRespuestaIA(String idConsulta, String respuesta) {}
        default void alErrorIA(String idConsulta, String error) {}
    }

    private final Escucha escucha;
    private volatile boolean cerrado;
    private Socket socket;
    private PrintWriter salida;

    public ClienteTiendaSocket(Escucha escucha) {
        if (escucha == null) {
            throw new IllegalArgumentException("La escucha es obligatoria.");
        }
        this.escucha = escucha;
    }

    public synchronized void conectar(String host, int puerto) throws IOException {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("La IP o nombre del servidor es obligatorio.");
        }
        if (puerto < 1 || puerto > 65535) {
            throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535.");
        }

        Socket nuevaConexion = new Socket();
        nuevaConexion.connect(new InetSocketAddress(host.trim(), puerto), 5000);
        socket = nuevaConexion;
        salida = new PrintWriter(new OutputStreamWriter(
                nuevaConexion.getOutputStream(), StandardCharsets.UTF_8), true);
        salida.println("SUBSCRIBE");
        if (salida.checkError()) {
            close();
            throw new IOException("No se pudo solicitar el catálogo al servidor.");
        }

        Thread hiloLectura = new Thread(this::escuchar, "tienda-socket-lector");
        hiloLectura.setDaemon(true);
        hiloLectura.start();
    }

    public synchronized String consultarGemini(String pregunta, String historial,
                                               String idProductoEnfocado) throws IOException {
        if (cerrado || salida == null) {
            throw new IOException("No hay conexión con el servidor ADMIN.");
        }
        if (pregunta == null || pregunta.trim().isEmpty()) {
            throw new IOException("Escribe una pregunta para el asistente.");
        }

        String idConsulta = UUID.randomUUID().toString();
        String producto = idProductoEnfocado == null ? "" : idProductoEnfocado;
        String conversacion = historial == null ? "" : historial;
        salida.println("AI_ASK|" + codificar(idConsulta) + "|" + codificar(producto) + "|"
                + codificar(conversacion) + "|" + codificar(pregunta.trim()));
        if (salida.checkError()) {
            throw new IOException("No se pudo enviar la consulta al servidor ADMIN.");
        }
        return idConsulta;
    }

    private void escuchar() {
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), StandardCharsets.UTF_8))) {
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                procesarMensaje(mensaje);
            }
            if (!cerrado) {
                throw new IOException("El servidor cerró la conexión.");
            }
        } catch (IOException | IllegalArgumentException ex) {
            if (!cerrado) {
                escucha.alDesconectar(ex instanceof IOException
                        ? (IOException) ex : new IOException("El servidor envió datos no válidos.", ex));
            }
        }
    }

    private void procesarMensaje(String mensaje) throws IOException {
        if ("READY".equals(mensaje)) {
            escucha.alConectar();
            return;
        }

        String[] campos = mensaje.split("\\|", -1);
        if (campos.length == 3 && "AI_RESPONSE".equals(campos[0])) {
            escucha.alRespuestaIA(decodificar(campos[1]), decodificar(campos[2]));
            return;
        }
        if (campos.length == 3 && "AI_ERROR".equals(campos[0])) {
            escucha.alErrorIA(decodificar(campos[1]), decodificar(campos[2]));
            return;
        }
        if (campos.length == 2 && "DELETE".equals(campos[0])) {
            escucha.alEliminarProducto(decodificar(campos[1]));
            return;
        }
        if (campos.length == 6 && "PRODUCT".equals(campos[0])) {
            double precio = Double.parseDouble(campos[3]);
            int stock = Integer.parseInt(campos[4]);
            if (!Double.isFinite(precio) || precio < 0 || stock < 0) {
                throw new IOException("El servidor envió datos de producto no válidos.");
            }
            escucha.alActualizarProducto(new Producto(
                    decodificar(campos[1]),
                    decodificar(campos[2]),
                    precio,
                    stock,
                    decodificar(campos[5])));
            return;
        }
        if (campos.length >= 1 && "ERROR".equals(campos[0])) {
            throw new IOException(campos.length > 1 ? campos[1] : "Error informado por el servidor.");
        }
        throw new IOException("El servidor envió un mensaje no reconocido.");
    }

    private static String decodificar(String valor) {
        return new String(Base64.getUrlDecoder().decode(valor), StandardCharsets.UTF_8);
    }

    private static String codificar(String valor) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public synchronized void close() {
        cerrado = true;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                System.err.println("No se pudo cerrar la conexión: " + ex.getMessage());
            }
        }
    }
}
