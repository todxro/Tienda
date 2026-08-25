package tienda;

import java.util.ArrayList;

public class Carrito {
    private ArrayList<String> productos = new ArrayList<>();

    public ArrayList<String> getProductos() {
        return productos;
    }

    public void setProductos(ArrayList<String> productos) {
        this.productos = productos;
    }
}