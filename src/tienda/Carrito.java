package tienda;

import java.util.ArrayList;
public class Carrito {

// productos que fueron agregados
private ArrayList<Producto> productos;
// cantidad de cada producto
private ArrayList<Integer> cantidades;

public Carrito() {
    // inicia las listas del carrito
    productos = new ArrayList<>();
    cantidades = new ArrayList<>();
}

// agrega un producto al carrito
public boolean agregarProducto(Producto producto) {

    // verifica que el producto exista
    if (producto == null) {
        return false;
    }

    // busca si el producto ya esta en el carrito
    for (int i = 0; i < productos.size(); i++) {

        if (productos.get(i).getId().equalsIgnoreCase(producto.getId())) {

            int cantidadActual = cantidades.get(i);

            // evita superar el stock disponible
            if (cantidadActual >= producto.getStock()) {
                return false;
            }

            // aumenta la cantidad del producto
            cantidades.set(i, cantidadActual + 1);

            return true;
        }
    }

    // verifica el stock antes de agregar un producto nuevo
    if (producto.getStock() <= 0) {
        return false;
    }
    // agrega el producto y su primera unidad
    productos.add(producto);
    cantidades.add(1);

    return true;
}


// entrega los productos del carrito
public ArrayList<Producto> getProductos() {
    return productos;
}


// busca la cantidad de un producto por su id
public int getCantidad(String id) {

    for (int i = 0; i < productos.size(); i++) {

        if (productos.get(i).getId().equalsIgnoreCase(id)) {

            return cantidades.get(i);
        }
    }

    return 0;
}


// calcula el subtotal de un producto
public double calcularSubtotal(String id) {

    for (int i = 0; i < productos.size(); i++) {

        if (productos.get(i).getId().equalsIgnoreCase(id)) {

            Producto producto = productos.get(i);

            return producto.getPrecio() * cantidades.get(i);
        }
    }

    return 0;
}


// calcula el precio total del carrito
public double calcularTotal() {

    double total = 0;

    for (int i = 0; i < productos.size(); i++) {

        double subtotal = productos.get(i).getPrecio()
                * cantidades.get(i);

        total += subtotal;
    }

    return total;
}


// finaliza la compra y descuenta el stock
public boolean finalizarCompra() {

    // verifica el stock de todos los productos
    for (int i = 0; i < productos.size(); i++) {

        Producto producto = productos.get(i);
        int cantidadPedida = cantidades.get(i);

        if (cantidadPedida > producto.getStock()) {
            return false;
        }
    }

    // descuenta el stock despues de validar
    for (int i = 0; i < productos.size(); i++) {

        Producto producto = productos.get(i);
        int cantidadPedida = cantidades.get(i);

        producto.setStock(
                producto.getStock() - cantidadPedida
        );
    }

    // vacia el carrito despues de comprar
    productos.clear();
    cantidades.clear();

    return true;
}


// vacia el carrito sin modificar el stock
public void vaciarCarrito() {

    productos.clear();
    cantidades.clear();
}
}