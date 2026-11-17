package tienda;

import java.util.ArrayList;

// Cuenta de cliente que conserva su propio carrito y lista de deseos.
public class Usuario extends Cuenta {
    private Carrito carrito;
    private ArrayList<String> listaDeseos;

    public Usuario(String nombre, String apellido, String contrasenia, String correo, String rut) {
        super(nombre, apellido, contrasenia, correo, rut);
        this.carrito = new Carrito();
        this.listaDeseos = new ArrayList<>();
    }

    public Carrito getCarrito() { return carrito; }
    
    public ArrayList<String> getListaDeseos() { return listaDeseos; }

    public void agregarDeseo(String idProducto) {
        if (!listaDeseos.contains(idProducto)) {  //si la lista no lo tiene, lo agrega
            listaDeseos.add(idProducto);
        }
    }

    public void quitarDeseo(String idProducto) { //quita el deseo 
        listaDeseos.remove(idProducto);
    }
}