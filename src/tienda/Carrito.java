package tienda;

import java.util.ArrayList;
public class Carrito {

private ArrayList<Producto> productos;
private ArrayList<Integer> cantidades;

public Carrito() {
    productos = new ArrayList<>();
    cantidades = new ArrayList<>();
}

// Agregar un producto al carrito
public boolean agregarProducto(Producto producto) {

    // Verificar que el producto existe dentro del array
    if (producto == null) {
        return false;
    }

    // Buscamos si el producto ya anda en el carrito
    for (int i = 0; i < productos.size(); i++) {

        if (productos.get(i).getId().equalsIgnoreCase(producto.getId())) {

            int cantidadActual = cantidades.get(i);

            // esto sirve para que no pueda agregar más stock del que hay disponible
            if (cantidadActual >= producto.getStock()) {
                return false;
            }

            // aumenta la cantidad de ese producto en el carrito
            cantidades.set(i, cantidadActual + 1);

            return true;
        }
    }

    // si todo falla y no esta en el carrito verifica si hay stock
    if (producto.getStock() <= 0) {
        return false;
    }
    //caso contrario, agrega el producto al carrito
    productos.add(producto);
    cantidades.add(1);

    return true;
}


// esto agarra los productos que hay en el carrito y los devuelve en un arraylist
public ArrayList<Producto> getProductos() {
    return productos;
}


// pa saber la cantidad de un producto en el carrito, se le pasa el id del producto y devuelve la cantidad
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


// precio total
public double calcularTotal() {

    double total = 0;

    for (int i = 0; i < productos.size(); i++) {

        double subtotal = productos.get(i).getPrecio()
                * cantidades.get(i);

        total += subtotal;
    }

    return total;
}


// finalizar y recien aqui descuento el stock
public boolean finalizarCompra() {

    // verifica si hay stock suficiente de los productos
    for (int i = 0; i < productos.size(); i++) {

        Producto producto = productos.get(i);
        int cantidadPedida = cantidades.get(i);

        if (cantidadPedida > producto.getStock()) {
            return false;
        }
    }

    // si es así, entonces descontamos los productos
    for (int i = 0; i < productos.size(); i++) {

        Producto producto = productos.get(i);
        int cantidadPedida = cantidades.get(i);

        producto.setStock(
                producto.getStock() - cantidadPedida
        );
    }

    // vaciamos el carrito despues de comprar
    productos.clear();
    cantidades.clear();

    return true;
}


// aqui un vaciar carrito pero coso, no descuesnta ni nada el stock por si luego ponen algo
public void vaciarCarrito() {

    productos.clear();
    cantidades.clear();
}
}