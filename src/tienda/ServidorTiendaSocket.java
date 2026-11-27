package tienda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Servidor ADMIN que publica el catálogo actual a un cliente conectado.
public final class ServidorTiendaSocket implements AutoCloseable {
    public static final int PUERTO_PREDETERMINADO = 25565;

    private final Inventario inventario;
    private final int puerto;
    private final Set<String> idsPublicados = new HashSet<>();
    private final Object bloqueoCliente = new Object();
    private volatile boolean activo;
    private ServerSocket servidor;
    private Socket socketCliente;
    private PrintWriter salidaCliente;

    public ServidorTiendaSocket(Inventario inventario) {
        this(inventario, PUERTO_PREDETERMINADO);
    }

    public ServidorTiendaSocket(Inventario inventario, int puerto) {
        if (inventario == null) {
            throw new IllegalArgumentException("El inventario es obligatorio.");
        }
        if (puerto < 1 || puerto > 65535) {
            throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535.");
        }
        this.inventario = inventario;
        this.puerto = puerto;
    }

    public synchronized void iniciar() throws IOException {
        if (activo) {
            return;
        }
        ServerSocket nuevoServidor = new ServerSocket();
        nuevoServidor.setReuseAddress(true);
        nuevoServidor.bind(new InetSocketAddress(puerto));
        servidor = nuevoServidor;
        activo = true;
        actualizarIdsPublicados();

        Thread hiloAceptacion = new Thread(this::aceptarClientes, "tienda-socket-servidor");
        hiloAceptacion.setDaemon(true);
        hiloAceptacion.start();
    }

    public boolean estaActivo() {
        return activo;
    }

    public int getPuerto() {
        return puerto;
    }

    // Publica una instantánea del inventario y los productos que fueron eliminados.
    public synchronized void publicarInventario() {
        if (!activo) {
            return;
        }

        List<Producto> productos = new ArrayList<>(inventario.getListaProductos());
        Set<String> idsActuales = new HashSet<>();
        for (Producto producto : productos) {
            idsActuales.add(producto.getId());
        }
        synchronized (bloqueoCliente) {
            if (salidaCliente != null) {
                for (Producto producto : productos) {
                    salidaCliente.println(mensajeProducto(producto));
                }
                for (String idAnterior : idsPublicados) {
                    if (!idsActuales.contains(idAnterior)) {
                        salidaCliente.println("DELETE|" + codificar(idAnterior));
                    }
                }
                if (salidaCliente.checkError()) {
                    cerrarCliente();
                }
            }
            for (Producto producto : productos) {
                idsActuales.add(producto.getId());
            }
        }
        idsPublicados.clear();
        idsPublicados.addAll(idsActuales);
    }

    private synchronized void actualizarIdsPublicados() {
        idsPublicados.clear();
        for (Producto producto : inventario.getListaProductos()) {
            idsPublicados.add(producto.getId());
        }
    }

    private void aceptarClientes() {
        while (activo) {
            try {
                Socket socket = servidor.accept();
                atenderCliente(socket);
            } catch (IOException ex) {
                if (activo) {
                    System.err.println("No se pudo aceptar una conexión: " + ex.getMessage());
                }
            }
        }
    }

    private void atenderCliente(Socket socket) {
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter escritor = new PrintWriter(new OutputStreamWriter(
                     socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            synchronized (bloqueoCliente) {
                socketCliente = socket;
                salidaCliente = escritor;
            }

            String mensaje;
            while (activo && (mensaje = entrada.readLine()) != null) {
                if ("SUBSCRIBE".equals(mensaje)) {
                    enviarInventario();
                } else if (mensaje.startsWith("AI_ASK|")) {
                    procesarConsultaIA(mensaje, escritor);
                } else {
                    enviar("ERROR|Comando no reconocido");
                }
            }
        } catch (IOException ex) {
            if (activo) {
                System.err.println("Conexión de cliente cerrada: " + ex.getMessage());
            }
        } finally {
            synchronized (bloqueoCliente) {
                if (socketCliente == socket) {
                    salidaCliente = null;
                    socketCliente = null;
                }
            }
        }
    }

    private void procesarConsultaIA(String mensaje, PrintWriter escritor) {
                String[] campos = mensaje.split("\\|", -1);
                if (campos.length != 5) {
                    return;
                }

                String idConsulta;
                String idProducto;
                String historial;
                String pregunta;
                try {
                    idConsulta = decodificar(campos[1]);
                    idProducto = decodificar(campos[2]);
                    historial = decodificar(campos[3]);
                    pregunta = decodificar(campos[4]);
                } catch (IllegalArgumentException ex) {
                    return;
                }

                Thread consulta = new Thread(() -> {
                    try {
                        String prompt = construirPromptIA(idProducto, historial, pregunta);
                        String respuesta = GeminiAPIClient.responder(prompt);
                        synchronized (bloqueoCliente) {
                            escritor.println("AI_RESPONSE|" + codificar(idConsulta) + "|"
                                    + codificar(respuesta));
                        }
                    } catch (IOException ex) {
                        String mensajeError = traducirErrorGemini(ex);
                        synchronized (bloqueoCliente) {
                            escritor.println("AI_ERROR|" + codificar(idConsulta) + "|"
                                    + codificar(mensajeError));
                        }
                    }
                }, "tienda-gemini-" + idConsulta);
                consulta.setDaemon(true);
                consulta.start();
            }

    private String construirPromptIA(String idProducto, String historial, String pregunta) {
                StringBuilder prompt = new StringBuilder();
                prompt.append("Eres el asistente de la tienda \"MiTiendita Pokémon\".\n");
                prompt.append("Responde únicamente con información presente en el catálogo vigente.\n");
                prompt.append("No inventes productos, precios, categorías ni stock. ");
                prompt.append("Si el producto no existe, indícalo y recomienda alternativas reales del catálogo.\n");
                prompt.append("Marca claramente que tus respuestas son generadas por inteligencia artificial.\n");
                prompt.append("Catálogo vigente:\n");

                for (Producto producto : new ArrayList<>(inventario.getListaProductos())) {
                    prompt.append("- ").append(producto.getNombre())
                            .append(" | categoría: ").append(producto.getCategoria())
                            .append(" | precio: $").append(Math.round(producto.getPrecio()))
                            .append(" | stock disponible: ").append(producto.getStock())
                            .append(" | descripción: producto de la categoría ")
                            .append(producto.getCategoria()).append('\n');
                }

                if (idProducto != null && !idProducto.isBlank()) {
                    Producto enfocado = inventario.buscarProducto(idProducto);
                    if (enfocado != null) {
                        prompt.append("\nProducto seleccionado por el cliente:\n")
                                .append(enfocado.getNombre()).append(" (")
                                .append(enfocado.getCategoria()).append(").\n");
                    }
                }
                if (historial != null && !historial.isBlank()) {
                    prompt.append("\nHistorial de la conversación:\n").append(historial).append('\n');
                }
                prompt.append("\nPregunta del cliente: ").append(pregunta);
                return prompt.toString();
            }

    private static String traducirErrorGemini(IOException ex) {
                String mensaje = ex.getMessage() == null ? "" : ex.getMessage();
                String inferior = mensaje.toLowerCase(java.util.Locale.ROOT);
                if (inferior.contains("api key") || inferior.contains("clave") || inferior.contains("401")
                        || inferior.contains("403")) {
                    return "La clave de Gemini no es válida o no está autorizada.";
                }
                if (inferior.contains("429") || inferior.contains("límite") || inferior.contains("limite")
                        || inferior.contains("quota") || inferior.contains("rate")) {
                    return "Gemini excedió el límite de solicitudes. Intenta nuevamente más tarde.";
                }
                if (inferior.contains("tardó") || inferior.contains("timeout") || inferior.contains("tiempo")) {
                    return "Gemini agotó el tiempo de espera.";
                }
                if (inferior.contains("conexión") || inferior.contains("conexion")
                        || inferior.contains("connect")) {
                    return "No hay conexión con el servicio de Gemini.";
                }
                return "No se pudo completar la consulta: " + mensaje;
    }

    private void enviarInventario() {
        List<Producto> productos = new ArrayList<>(inventario.getListaProductos());
        synchronized (bloqueoCliente) {
            if (salidaCliente == null) {
                return;
            }
            for (Producto producto : productos) {
                salidaCliente.println(mensajeProducto(producto));
            }
            salidaCliente.println("READY");
            if (salidaCliente.checkError()) {
                cerrarCliente();
            }
        }
    }

    private void enviar(String mensaje) {
        synchronized (bloqueoCliente) {
            if (salidaCliente != null) {
                salidaCliente.println(mensaje);
                if (salidaCliente.checkError()) {
                    cerrarCliente();
                }
            }
        }
    }

    private void cerrarCliente() {
        if (socketCliente != null) {
            try {
                socketCliente.close();
            } catch (IOException ex) {
                System.err.println("No se pudo cerrar la conexión: " + ex.getMessage());
            }
            socketCliente = null;
            salidaCliente = null;
        }
    }

    private static String mensajeProducto(Producto producto) {
        return "PRODUCT|" + codificar(producto.getId()) + "|" + codificar(producto.getNombre())
                + "|" + producto.getPrecio() + "|" + producto.getStock()
                + "|" + codificar(producto.getCategoria());
    }

    private static String codificar(String valor) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodificar(String valor) {
        return new String(Base64.getUrlDecoder().decode(valor), StandardCharsets.UTF_8);
    }

    @Override
    public synchronized void close() {
        activo = false;
        if (servidor != null) {
            try {
                servidor.close();
            } catch (IOException ex) {
                System.err.println("No se pudo cerrar el servidor: " + ex.getMessage());
            }
        }
        synchronized (bloqueoCliente) {
            cerrarCliente();
        }
    }
}
