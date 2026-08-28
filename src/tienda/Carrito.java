package tienda;

import java.util.ArrayList;

public class Carrito {

    private ArrayList<Producto> productos;
    private ArrayList<Integer> cantidades;

    public Carrito() {
        productos = new ArrayList<>();
        cantidades = new ArrayList<>();
    }

    public boolean agregarProducto(Producto producto) {

        if (producto == null) {
            return false;
        }

        if (producto.getStock() <= 0) {
            return false;
        }

        // Revisar si el producto ya está en el carrito
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId().equalsIgnoreCase(producto.getId())) {
                cantidades.set(i, cantidades.get(i) + 1);
                producto.setStock(producto.getStock() - 1);

                return true;
            }
        }

        //Si nada anterior funciona, entonces se triggea como producto nuevo
        productos.add(producto);
        cantidades.add(1);
        producto.setStock(producto.getStock() - 1);

        return true;
    }

    // Quitar un producto completo del carrito
    public boolean quitarProducto(String id) {

        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId().equalsIgnoreCase(id)) {
                Producto producto = productos.get(i);

                // Devolver al stock todas las unidades
                producto.setStock(producto.getStock() + cantidades.get(i));
                productos.remove(i);
                cantidades.remove(i);

                return true;
            }
        }
return false;
    }

    // Obtener los productos del carrito
    public ArrayList<Producto> getProductos() {
        return productos;
    }

    // Obtener la cantidad de un producto
    public int getCantidad(String id) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId().equalsIgnoreCase(id)) {
                return cantidades.get(i);
            }
        }

        return 0;
    }

    // Calcular el precio total del carrito
    public double calcularTotal() {
        double total = 0;
        for (int i = 0; i < productos.size(); i++) {
            total += productos.get(i).getPrecio() * cantidades.get(i);
        }
//funciona a partir de buscar el alargo del arraylist de productos y multiplicar el precio por la cantidad de cada producto, 
// sumando todo al total
        return total;
    }

    // Vaciar todo el carrito y devolver los productos al stock
    public void vaciarCarrito() {
        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            producto.setStock(producto.getStock() + cantidades.get(i));
        }

        productos.clear();
        cantidades.clear();
    }
}