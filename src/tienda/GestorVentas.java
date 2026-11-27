package tienda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Guarda las ventas y consulta el historial persistido en ventas.csv.
public class GestorVentas {
    private static final String RUTA_ARCHIVO = "ventas.csv";
    private static final String ENCABEZADO = "numeroTicket,fecha,rutCliente,nombreCliente,correoCliente,"
            + "detalleProductos,subtotal,iva,total,cupon,descuento,estado,costoEnvio,"
            + "modalidadEntrega,direccionEntrega,itemsInventario";
    private static final Object BLOQUEO_ARCHIVO = new Object();

    public String guardarVenta(Venta venta) throws IOException {
        if (venta == null) {
            throw new IllegalArgumentException("La venta es obligatoria.");
        }
        synchronized (BLOQUEO_ARCHIVO) {
            File archivo = new File(RUTA_ARCHIVO);
            prepararArchivo(archivo);
            String numeroPedido = generarNumeroPedidoUnico(archivo);
            venta.setIdVenta(numeroPedido);

            try (BufferedWriter bw = Files.newBufferedWriter(archivo.toPath(), StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
                bw.write(venta.toCSV());
                bw.newLine();
            }
            return numeroPedido;
        }
    }

    public String guardarVentaInvitado(Venta venta) throws IOException {
        venta.setNombreCliente("Invitado");
        return guardarVenta(venta);
    }

    private String generarNumeroPedidoUnico(File archivo) throws IOException {
        Set<String> existentes = new HashSet<>();
        if (archivo.exists()) {
            List<String> lineas = Files.readAllLines(archivo.toPath(), StandardCharsets.UTF_8);
            for (int i = 1; i < lineas.size(); i++) {
                String[] datos = parsearLineaCSV(lineas.get(i));
                if (datos.length > 0) {
                    existentes.add(datos[0]);
                }
            }
        }

        String numeroPedido;
        do {
            numeroPedido = "PED-" + UUID.randomUUID().toString().toUpperCase(java.util.Locale.ROOT);
        } while (existentes.contains(numeroPedido));
        return numeroPedido;
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

    public ArrayList<String[]> obtenerHistorialCliente(String correoCliente) {
        // Devuelve solo fecha, detalle y total de las compras del correo indicado.
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
                
                    String[] completa = completarCamposVenta(datos); // las ventas antiguas se completan para leer el estado en la columna 11
                    historial.add(new String[]{completa[1], completa[5], "$" + completa[8],
                            EstadoPedido.desdeTexto(completa[11]).getTexto()});                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer historial: " + e.getMessage());
        }
        

        return historial;
    }

    public ArrayList<String[]> obtenerTodasLasVentas() throws IOException {
        // Lee las filas completas para mostrarlas en la vista de administración.
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
                // las ventas antiguas no tienen cupon, descuento ni estado
                ventas.add(completarCamposVenta(datos));
        }
    }
        return ventas;
    
    }
    
    // Avanza un paso el estado del pedido, o null si no se encontró o ya es terminal.
    public EstadoPedido avanzarEstado(String ticket, String fecha) throws IOException {
        synchronized (BLOQUEO_ARCHIVO) {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                return null;
            }
            prepararArchivo(archivo);

            List<String> lineas = Files.readAllLines(archivo.toPath(), StandardCharsets.UTF_8);
            for (int i = 1; i < lineas.size(); i++) {
                String[] datos = parsearLineaCSV(lineas.get(i));
                if (datos.length < 9 || !datos[0].equals(ticket) || !datos[1].equals(fecha)) {
                    continue;
                }

                String[] completa = completarCamposVenta(datos);
                EstadoPedido siguiente = EstadoPedido.desdeTexto(completa[11]).siguiente();
                if (siguiente == null) {
                    return null;
                }

                completa[11] = siguiente.name();
                lineas.set(i, reconstruirLineaCSV(completa));
                guardarLineasVentas(lineas);
                return siguiente;
            }
            return null;
        }
    }

    public EstadoPedido cancelarPedido(String ticket, String fecha, Inventario inventario)
            throws IOException {
        if (inventario == null) {
            throw new IllegalArgumentException("El inventario es obligatorio.");
        }

        synchronized (BLOQUEO_ARCHIVO) {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                return null;
            }
            prepararArchivo(archivo);

            List<String> lineas = Files.readAllLines(archivo.toPath(), StandardCharsets.UTF_8);
            for (int i = 1; i < lineas.size(); i++) {
                String[] datos = parsearLineaCSV(lineas.get(i));
                if (datos.length < 9 || !datos[0].equals(ticket) || !datos[1].equals(fecha)) {
                    continue;
                }

                String[] completa = completarCamposVenta(datos);
                EstadoPedido estadoActual = EstadoPedido.desdeTexto(completa[11]);
                if (!estadoActual.puedeCancelarse()) {
                    throw new IllegalStateException("Solo se pueden cancelar pedidos pendientes "
                            + "o en preparación; este pedido está " + estadoActual.getTexto() + ".");
                }

                Map<String, Integer> cantidades = obtenerProductosParaRestaurar(
                        completa[15], completa[5], inventario);
                inventario.ajustarStock(cantidades);

                completa[11] = EstadoPedido.CANCELADO.name();
                lineas.set(i, reconstruirLineaCSV(completa));
                try {
                    guardarLineasVentas(lineas);
                } catch (IOException ex) {
                    try {
                        inventario.ajustarStock(invertirAjustes(cantidades));
                    } catch (IOException rollbackError) {
                        ex.addSuppressed(rollbackError);
                    }
                    throw ex;
                }
                return EstadoPedido.CANCELADO;
            }
            return null;
        }
    }

    private Map<String, Integer> obtenerProductosParaRestaurar(String itemsInventario,
                                                                String detalleProductos,
                                                                Inventario inventario)
            throws IOException {
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        if (itemsInventario != null && !itemsInventario.isBlank()) {
            for (String item : itemsInventario.split(";")) {
                String[] partes = item.split(":", -1);
                if (partes.length != 2 || partes[0].isBlank()) {
                    throw new IOException("El detalle de productos del pedido no es válido.");
                }
                agregarCantidad(cantidades, partes[0], partes[1]);
            }
        } else {
            Pattern patronProducto = Pattern.compile("^(\\d+)x\\s+(.+)$");
            for (String item : detalleProductos.split("\\|")) {
                Matcher matcher = patronProducto.matcher(item.trim());
                if (!matcher.matches()) {
                    continue;
                }
                String nombre = matcher.group(2).trim();
                Producto encontrado = null;
                for (Producto producto : inventario.getListaProductos()) {
                    if (producto.getNombre().equalsIgnoreCase(nombre)) {
                        if (encontrado != null) {
                            throw new IOException("No se puede identificar de forma única el producto "
                                    + nombre + " de este pedido.");
                        }
                        encontrado = producto;
                    }
                }
                if (encontrado == null) {
                    throw new IOException("No se encontró el producto " + nombre
                            + " para restaurar el stock del pedido.");
                }
                agregarCantidad(cantidades, encontrado.getId(), matcher.group(1));
            }
        }
        if (cantidades.isEmpty()) {
            throw new IOException("El pedido no contiene productos que se puedan restaurar.");
        }
        return cantidades;
    }

    private void agregarCantidad(Map<String, Integer> cantidades, String id, String cantidadTexto)
            throws IOException {
        try {
            int cantidad = Integer.parseInt(cantidadTexto);
            if (cantidad <= 0) {
                throw new NumberFormatException();
            }
            cantidades.merge(id, cantidad, Math::addExact);
        } catch (NumberFormatException | ArithmeticException ex) {
            throw new IOException("La cantidad de productos del pedido no es válida.", ex);
        }
    }

    private Map<String, Integer> invertirAjustes(Map<String, Integer> ajustes) {
        Map<String, Integer> inversos = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> ajuste : ajustes.entrySet()) {
            inversos.put(ajuste.getKey(), -ajuste.getValue());
        }
        return inversos;
    }

    private void guardarLineasVentas(List<String> lineas) throws IOException {
        Path destino = Path.of(RUTA_ARCHIVO).toAbsolutePath();
        Path temporal = Files.createTempFile(destino.getParent(), "ventas-", ".tmp");
        try {
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            try {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    private String[] parsearLineaCSV(String linea) {
        // Separa comas solo cuando están fuera de campos entrecomillados.
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
    private String[] completarCamposVenta(String[] datos) {
        String[] completa = new String[16];
        for (int i = 0; i < completa.length; i++) {
            completa[i] = i < datos.length ? datos[i] : "";
        }
        if (completa[10].isEmpty()) {
            completa[10] = "0";
        }
        if (completa[12].isEmpty()) {
            completa[12] = "0";
        }
        if (completa[13].isEmpty()) {
            completa[13] = Despacho.RETIRO_TIENDA;
        }
        if (completa[14].isEmpty()) {
            completa[14] = "Retiro en local";
        }
        return completa;
    }
    private String reconstruirLineaCSV(String[] campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escaparCSV(campos[i]));
        }
        return sb.toString();
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
