package tienda;

import java.util.ArrayList;
import java.util.Date;

public class Usuario extends Cuenta {
    private ArrayList<String> Carrito = new ArrayList<>();

    public Usuario(String nombre, String apellido, String contrasenia, String direccion, String pais, String comuna, String region, String correo, int numeroDeTelefono, Date fechadeNacimiento, String rut) {
        super(nombre, apellido, contrasenia, direccion, pais, comuna, region, correo, numeroDeTelefono, fechadeNacimiento, rut);
    }

    public ArrayList<String> getCarrito() {
        return Carrito;
    }

    public void setCarrito(ArrayList<String> Carrito) {
        this.Carrito = Carrito;
    }
} //arraylist para el carrito de compras del usuario, se puede agregar productos a este carrito y luego proceder a la compra