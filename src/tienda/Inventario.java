package tienda;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Inventario {
    private ArrayList<Producto> listaProductos;
    private final String RUTA_ARCHIVO = "inventario.txt";

    public Inventario() {
        this.listaProductos = new ArrayList<>();
        cargarDesdeArchivo(); // Carga datos al iniciar
    }

    public void agregarProducto(Producto producto) {
        if (buscarProducto(producto.getId()) == null ){
        listaProductos.add(producto);
        guardarEnArchivo(); // Guarda en disco
        }
        else {
            System.out.println("Error: ya existe un producto con esa id");
        }
    }
    public boolean eliminarProducto (String id){
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
        if (nuevoStock < 0) {return false;}
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
    public void ordenarPor(int opcion, boolean ascendente){
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
        String orden = ascendente ? "(Menor a Mayor)" : "(Mayor a Menor)"; //if y else para saber si ordenamos de menor a mayor
        System.out.println("Inventario ordenado por " + nombreAtributo + " " + orden);
    }
        //METODOS DE BUSQUEDA 
    public ArrayList<Producto> buscarPorNombre(String texto) {
        ArrayList<Producto> resultados = new ArrayList<>(); //creamos una lista temporal para tener los productos que pasen el filtro
        for (Producto p : listaProductos) { //recorremos la lista
            if (p.getNombre().toLowerCase().contains(texto.toLowerCase())) { //el filtro convertimos todo en minisucula y usamos contains para saber si va coinicidiendo
                resultados.add(p); //si se cumple la coindicion se agrega a la lista temporal
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
    //generar nuevo id 
    public String generarNuevoId() {
        if (listaProductos.isEmpty()) return "P01"; //revisamos si hay productos si no asignamos la id P01
        
        int maxId = 0; 
        for (Producto p : listaProductos) { //buscamos el id mayor guardandola con el maxID
            try { //evitamos errores si la id por algun motivo tiene letras en vez de numeros al final
                int numId = Integer.parseInt(p.getId().replace("P", ""));  // quitamos la P  y convertimos el numero en entero
                if (numId > maxId) {  //comparamos si es mayor que el maxmio actual
                    maxId = numId; 
                }
            } catch (NumberFormatException e) {
            }
        }
        return "P" + String.format("%02d", maxId + 1); //asignamos la nueva id al tener el maximo
    }
    //cambiar metodo inventario
    public void guardarEnArchivo() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Producto p : listaProductos) {
                // Formato: ID;Nombre;Precio;Stock
                bw.write(p.getId() + ";" + p.getNombre() + ";" + p.getPrecio() + ";" + p.getStock());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar inventario: " + e.getMessage());
        }
    }

    private void cargarDesdeArchivo() {
        File archivo = new File(RUTA_ARCHIVO);
        listaProductos.clear();

        // si no existe, crear inventario vacio
        if (!archivo.exists()) {
            guardarEnArchivo();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] datos = linea.split(";");
                if (datos.length >= 4) {
                    try {
                        String id = datos[0];
                        String nombre = datos[1];
                        double precio = Double.parseDouble(datos[2]);
                        int stock = Integer.parseInt(datos[3]);
                        String categoria = (datos.length == 5) ? datos[4] : "Sin Categoría";
                        
                        listaProductos.add(new Producto(id, nombre, precio, stock, categoria));
                    } catch (NumberFormatException e) {
                        // evitamos que el programa muera si hay una letra en lugar de un numero
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar inventario: " + e.getMessage());
            listaProductos.clear(); // mantiene vacio si hay error grave
        }
    }
}