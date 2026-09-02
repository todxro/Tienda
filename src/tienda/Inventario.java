package tienda;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Inventario {
    private ArrayList<Producto> listaProductos;
    private final String RUTA_ARCHIVO = "inventario.csv";

    public Inventario() {
        this.listaProductos = new ArrayList<>();
        cargarDesdeArchivo(); // Carga datos al iniciar
    }

    public void agregarProducto(Producto producto) {
        if (buscarProducto(producto.getId()) == null) {
            listaProductos.add(producto);
            guardarEnArchivo(); // Guarda en disco
        } else {
            System.out.println("Error: ya existe un producto con esa id");
        }
    }

    public boolean eliminarProducto(String id) {
        Producto p = buscarProducto(id);
        if (p != null) {
            listaProductos.remove(p);
            guardarEnArchivo();
            return true;
        } else {
            return false;
        }
    }

    public boolean actualizarStock(String id, int nuevoStock) {
        if (nuevoStock < 0) {
            return false;
        }
        Producto p = buscarProducto(id);
        if (p != null) {
            p.setStock(nuevoStock);
            guardarEnArchivo(); // Guarda en disco
            return true;
        }
        return false;
    }

    public Producto buscarProducto(String id) {
        for (Producto p : listaProductos) {
            if (p.getId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    public ArrayList<Producto> getListaProductos() {
        cargarDesdeArchivo();
        return listaProductos;
    }

    public void ordenarPor(int opcion, boolean ascendente) {
        Comparator<Producto> comparador = null; // es la regla que vamos a usar para comparar
        String nombreAtributo = "";

        switch (opcion) {
            case 1:
                comparador = Comparator.comparingDouble(Producto::getPrecio);
                nombreAtributo = "Precio";
                break;
            case 2:
                comparador = Comparator.comparingInt(Producto::getStock);
                nombreAtributo = "Stock";
                break;
            case 3:
                comparador = Comparator.comparing(Producto::getId);
                nombreAtributo = "ID";
                break;
            default:
                System.out.println("Opcion no valida, usa 1 (Precio), 2 (Stock) o 3 (ID).");
                return;
        }

        // si ingresas false es mayor a Menor
        if (!ascendente) {
            comparador = comparador.reversed();
        }

        this.listaProductos.sort(comparador);

        guardarEnArchivo();

        // Mensaje de confirmación en consola
        String orden = ascendente ? "(Menor a Mayor)" : "(Mayor a Menor)"; // if y else para saber si ordenamos de menor
                                                                           // a mayor
        System.out.println("Inventario ordenado por " + nombreAtributo + " " + orden);
    }

    // cambiar metodo inventario
    public void guardarEnArchivo() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            bw.write("id,nombre,precio,stock,categoria"); // encabezado del csv
            bw.newLine();
            for (Producto p : listaProductos) {
                bw.write(escaparCSV(p.getId()) + ","
                        + escaparCSV(p.getNombre()) + ","
                        + p.getPrecio() + ","
                        + p.getStock() + ","
                        + escaparCSV(p.getCategoria()));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar inventario: " + e.getMessage());
        }
    }

    private void cargarDesdeArchivo() {
        File archivo = new File(RUTA_ARCHIVO);
        listaProductos.clear();
        if (!archivo.exists()) {
            cargarProductosPorDefecto();
            guardarEnArchivo();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primeraLinea = true; // para saltarse el encabezado al leerlo
            boolean hayProductos = false;
            while ((linea = br.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] datos = parsearLineaCSV(linea); // lee la linea respetando las comillas
                if (datos.length == 5) {
                    String id = datos[0];
                    String nombre = datos[1];
                    double precio = Double.parseDouble(datos[2]);
                    int stock = Integer.parseInt(datos[3]);
                    String categoria = datos[4];
                    listaProductos.add(new Producto(id, nombre, precio, stock, categoria));
                    hayProductos = true;
                }
            }
            if (!hayProductos) {
                cargarProductosPorDefecto();
                guardarEnArchivo();
            }
        } catch (IOException e) {
            System.out.println("Error al cargar inventario: " + e.getMessage());
            cargarProductosPorDefecto();
            guardarEnArchivo();
        }
    }

    private void cargarProductosPorDefecto() {
        listaProductos.add(new Producto("P01", "Teclado Mecánico", 45000, 10, "Teclados"));
        listaProductos.add(new Producto("P02", "Mouse Gamer", 25000, 8, "Periféricos"));
        listaProductos.add(new Producto("P03", "Monitor 24\"", 120000, 5, "Monitores"));
    }

    // lee una linea del csv parte por parte
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

    // si la parte del codigo tiene coma o comillas lo envuelve en comillas para el
    // csv
    private String escaparCSV(String valor) {
        if (valor == null)
            return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            valor = valor.replace("\"", "\"\"");
            return "\"" + valor + "\"";
        }
        return valor;
    }

    // calcula precio promedio de una categoria
    public double calcularPrecioPromedioPorCategoria(String categoria) {
        double suma = 0;
        int cantidad = 0;
        for (Producto p : listaProductos) {
            if (p.getCategoria().equalsIgnoreCase(categoria)) {
                suma += p.getPrecio();
                cantidad++;
            }
        }
        if (cantidad == 0) {
            System.out.println("No hay productos en la categoría: " + categoria);
            return 0;
        }
        return suma / cantidad;
    }

    // retorna el producto con menos stock de una categoria
    public Producto productoConMenorStockEnCategoria(String categoria) {
        Producto menorStock = null;
        for (Producto p : listaProductos) {
            if (p.getCategoria().equalsIgnoreCase(categoria)) {
                if (menorStock == null || p.getStock() < menorStock.getStock()) {
                    menorStock = p;
                }
            }
        }
        if (menorStock == null) {
            System.out.println("No hay productos en la categoría: " + categoria);
        }
        return menorStock;
    }

    // suma precio * stock de los productos
    public double calcularValorTotalInventario() {
        double total = 0;
        for (Producto p : listaProductos) {
            total += p.getPrecio() * p.getStock();
        }   
        return total;
    }
}