package tienda;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Inventario {
    // productos disponibles en el inventario
    private ArrayList<Producto> listaProductos;
    // archivo donde se guarda el inventario
    private static final String RUTA_ARCHIVO = "inventario.csv";

    // carga el inventario al iniciar
    public Inventario() {
        this.listaProductos = new ArrayList<>();
        cargarDesdeArchivo(); // carga datos al iniciar
    }

    // agrega un producto si su id no existe
    public void agregarProducto(Producto producto) {
        if (buscarProducto(producto.getId()) == null ){
        listaProductos.add(producto);
        guardarEnArchivo(); // guarda los cambios
        }
        else {
            System.out.println("Error: ya existe un producto con esa id");
        }
    }
    public boolean eliminarProducto (String id){
        // elimina un producto por su id
        Producto p = buscarProducto(id);
        if (p != null) {
            listaProductos.remove(p);
            guardarEnArchivo();
            return true;
        }
        else {
            return false;
        }
    }
    public boolean actualizarStock(String id, int nuevoStock) {
        // cambia el stock de un producto
        if (nuevoStock < 0) {return false;}
        Producto p = buscarProducto(id);
        if (p != null) {
            p.setStock(nuevoStock);
            guardarEnArchivo(); // guarda los cambios
            return true;
        }
        return false;
    }

    public Producto buscarProducto(String id) {
        // busca un producto por su id
        cargarDesdeArchivo();
        for (Producto p : listaProductos) {
            if (p.getId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    public ArrayList<Producto> getListaProductos() {
        // entrega la lista de productos
        cargarDesdeArchivo();
        return listaProductos;
    }
    public void ordenarPor(int opcion, boolean ascendente){
        // selecciona el criterio para ordenar
        Comparator<Producto> comparador = null;
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

        // invierte el orden cuando corresponde
        if (!ascendente) {
            comparador = comparador.reversed();
        }
        
        this.listaProductos.sort(comparador);
        
       guardarEnArchivo(); 
        
        // muestra el orden aplicado
        String orden = ascendente ? "(Menor a Mayor)" : "(Mayor a Menor)";
        System.out.println("Inventario ordenado por " + nombreAtributo + " " + orden);
    }
        // metodos de busqueda
    public ArrayList<Producto> buscarPorNombre(String texto) {
        // filtra productos que contienen el texto buscado
        ArrayList<Producto> resultados = new ArrayList<>();
        for (Producto p : listaProductos) {
            if (p.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                resultados.add(p);
            }
        }
        return resultados; 
    }
    public ArrayList<Producto> buscarPorCategoria(String categoria) {
        ArrayList<Producto> resultados = new ArrayList<>();
        for (Producto p : listaProductos) {
            if (p.getCategoria().equalsIgnoreCase(categoria)) {
                resultados.add(p);
            }
        }
        return resultados;
    }
    public ArrayList<Producto> filtrarPorRangoPrecio(double min, double max) {
        ArrayList<Producto> resultados = new ArrayList<>();
        for (Producto p : listaProductos) {
            if (p.getPrecio() >= min && p.getPrecio() <= max) {
                resultados.add(p);
            }
        }
        return resultados;
    }
    public ArrayList<Producto> obtenerProductosSinStock() {
        ArrayList<Producto> resultados = new ArrayList<>();
        for (Producto p : listaProductos) {
            if (p.getStock() == 0) {
                resultados.add(p);
            }
        }
        return resultados;
    }
    public ArrayList<Producto> obtenerProductosDisponibles() {
        ArrayList<Producto> resultados = new ArrayList<>();
        for (Producto p : listaProductos) {
            if (p.getStock() > 0) {
                resultados.add(p);
            }
        }
        return resultados;
    }
    // genera el siguiente id disponible
    public String generarNuevoId() {
        if (listaProductos.isEmpty()) return "P01";
        
        int maxId = 0; 
        for (Producto p : listaProductos) {
            try {
                int numId = Integer.parseInt(p.getId().replace("P", ""));
                if (numId > maxId) {
                    maxId = numId; 
                }
            } catch (NumberFormatException e) {
            }
        }
        return "P" + String.format("%02d", maxId + 1);
    }
    public double calcularPrecioPromedioPorCategoria(String categoria) {
        // calcula el precio promedio de una categoria
        double sumaPrecios = 0;
        int cantidadProductos = 0;

        for (Producto p : listaProductos) {
            if (p.getCategoria().equalsIgnoreCase(categoria)) {
                sumaPrecios += p.getPrecio();
                cantidadProductos++;
            }
        }

        if (cantidadProductos == 0) {
            System.out.println("No hay productos en la categoría: " + categoria);
            return 0.0; 
        }

        return sumaPrecios / cantidadProductos;
    }
    public Producto obtenerProductoMenorStockPorCategoria(String categoria) {
        // busca el producto con menor stock de una categoria
        Producto productoMenorStock = null;
        int menorStock = Integer.MAX_VALUE;

        for (Producto p : listaProductos) {
            if (p.getCategoria().equalsIgnoreCase(categoria)) {
                if (p.getStock() < menorStock) {
                    menorStock = p.getStock();
                    productoMenorStock = p;
                }
            }
        }

        if (productoMenorStock == null) {
            System.out.println("No hay productos en la categoría: " + categoria);
        }

        return productoMenorStock;
    }
    public double calcularValorTotalInventario() {
        // calcula el valor de todos los productos
        double valorTotal = 0;
        for (Producto p : listaProductos) {
            valorTotal += (p.getPrecio() * p.getStock());
        }
        return valorTotal;
    }
    // guarda el inventario en el archivo
    public void guardarEnArchivo() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            bw.write("id,nombre,precio,stock,categoria");
            bw.newLine();
            for (Producto p : listaProductos) {
                bw.write(campoCsv(p.getId()) + ","
                        + campoCsv(p.getNombre()) + ","
                        + p.getPrecio() + ","
                        + p.getStock() + ","
                        + campoCsv(p.getCategoria()));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar inventario: " + e.getMessage());
        }
    }

    private void cargarDesdeArchivo() {
        // lee los productos guardados en el archivo
        File archivo = new File(RUTA_ARCHIVO);

        // crea un archivo vacio si no existe
        if (!archivo.exists()) {
            guardarEnArchivo();
            return;
        }

        ArrayList<Producto> productosArchivo = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty() || linea.toLowerCase().startsWith("id,")) {
                    continue;
                }
                String[] datos = separarCsv(linea);
                if (datos.length >= 4) {
                    try {
                        String id = datos[0];
                        String nombre = datos[1];
                        double precio = Double.parseDouble(datos[2]);
                        int stock = Integer.parseInt(datos[3]);
                        String categoria = (datos.length >= 5 && !datos[4].isEmpty())
                                ? datos[4] : "Sin Categoría";
                        
                        productosArchivo.add(new Producto(id, nombre, precio, stock, categoria));
                    } catch (NumberFormatException e) {
                        // ignora filas con datos numericos invalidos
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar inventario: " + e.getMessage());
            return;
        }

        for (Producto productoArchivo : productosArchivo) {
            Producto productoActual = buscarProductoEnLista(productoArchivo.getId());
            if (productoActual == null) {
                listaProductos.add(productoArchivo);
            } else {
                productoActual.setStock(productoArchivo.getStock());
                productoActual.setPrecio(productoArchivo.getPrecio());
                productoActual.setCategoria(productoArchivo.getCategoria());
            }
        }

        listaProductos.removeIf(productoActual -> buscarProductoEnLista(productoActual.getId(), productosArchivo) == null);
    }

    private Producto buscarProductoEnLista(String id) {
        return buscarProductoEnLista(id, listaProductos);
    }

    private Producto buscarProductoEnLista(String id, ArrayList<Producto> productos) {
        for (Producto producto : productos) {
            if (producto.getId().equalsIgnoreCase(id)) {
                return producto;
            }
        }
        return null;
    }

    private String campoCsv(String valor) {
        String campo = valor == null ? "" : valor.replace("\"", "\"\"");
        return "\"" + campo + "\"";
    }

    private String[] separarCsv(String linea) {
        ArrayList<String> campos = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean entreComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char caracter = linea.charAt(i);
            if (caracter == '"') {
                if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    campo.append('"');
                    i++;
                } else {
                    entreComillas = !entreComillas;
                }
            } else if (caracter == ',' && !entreComillas) {
                campos.add(campo.toString().trim());
                campo.setLength(0);
            } else {
                campo.append(caracter);
            }
        }

        campos.add(campo.toString().trim());
        return campos.toArray(new String[0]);
    }
}