package tienda;

// Representa un artículo del catálogo junto con su precio y existencias.
public class Producto {
    // datos que identifican al producto
    private String id;
    private String nombre;
    private double precio;
    private int stock;
    private String categoria;

    // crea un producto con su stock inicial
    public Producto(String id, String nombre, double precio, int stock, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    // entrega el identificador del producto
    public String getId() {
        return id;
    }

    // entrega el nombre del producto
    public String getNombre() {
        return nombre;
    }

    // cambia el nombre del producto
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // entrega el precio del producto
    public double getPrecio() {
        return precio;
    }

    // cambia el precio si es válido
    public void setPrecio(double precio) {
        if (precio >= 0) {
            this.precio = precio;
        } else {
            System.out.println("Error: el precio no puede ser negativo");
        }
    }

    // entrega el stock disponible del producto
    public int getStock() {
        return stock;
    }

    // cambia el stock si es válido
    public void setStock(int stock) {
        if (stock >= 0) {
            this.stock = stock;
        } else {
            System.out.println("Error: el stock no puede ser negativo");
        }
    }

    // entrega la categoría del producto
    public String getCategoria() {
        return categoria;
    }

    // cambia la categoría del producto
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | Producto: " + nombre + " | Precio: $" + precio + " | Stock: " + stock;
    }
}