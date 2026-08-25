package tienda;

import java.io.*;
import java.util.ArrayList;

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
        cargarDesdeArchivo(); // Relee del disco por si otra ventana lo modificó
        return listaProductos;
    }

    // --- MÉTODOS DE PERSISTENCIA EN DISCO ---
    private void guardarEnArchivo() {
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
        if (!archivo.exists()) {
            listaProductos.clear();
            listaProductos.add(new Producto("P01", "Teclado Mecánico", 45000, 10, "Teclados"));
            guardarEnArchivo();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            listaProductos.clear();
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                if (datos.length == 4) {
                    String id = datos[0];
                    String nombre = datos[1];
                    double precio = Double.parseDouble(datos[2]);
                    int stock = Integer.parseInt(datos[3]);
                    listaProductos.add(new Producto(id, nombre, precio, stock, "categoria"));
                }
            } //aa
        } catch (IOException e) {
            System.out.println("Error al cargar inventario: " + e.getMessage());
        }
    }
}