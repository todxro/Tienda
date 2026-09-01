package tienda;


public class Producto {
    private String id;
    private String nombre;
    private double precio;
    private int stock;
    private String categoria;

    public Producto(String id, String nombre, double precio, int stock, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) {
        if (stock >= 0) {
            this.stock = stock;
        } else {
            System.out.println("Error: el stock no puede ser negativo");
        }
    }
        public String getCategoria() { return categoria; }
    public void setPrecio(double precio) { 
        if (precio >= 0) {
        this.precio = precio; 
        }
        else {
            System.out.println ("Error: el precio no puede ser negativo");
        }
    }
    public void setCategoria (String categoria) { this.categoria = categoria; }

    @Override
    public String toString() {
        return "ID: " + id + " | Producto: " + nombre + " | Precio: $" + precio + " | Stock: " + stock;
    }
}