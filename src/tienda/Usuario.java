package tienda;

import java.util.Date;

public class Usuario extends Cuenta {
    // carrito personal del usuario
    private Carrito carrito;

    // crea un usuario con un carrito vacio
    public Usuario(String nombre, String apellido, String contrasenia, String direccion, String pais, String comuna, String region, String correo, int numeroDeTelefono, Date fechadeNacimiento, String rut) {
        super(nombre, apellido, contrasenia, direccion, pais, comuna, region, correo, numeroDeTelefono, fechadeNacimiento, rut);

        this.carrito = new Carrito();
    }

    public Carrito getCarrito() {
        // entrega el carrito del usuario
        return carrito;
    }
}