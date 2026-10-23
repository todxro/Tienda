package tienda;

// Cuenta de cliente que conserva su propio carrito de compras.
public class Usuario extends Cuenta {
    // carrito personal del usuario
    private Carrito carrito;

    // crea un usuario con un carrito vacío
    public Usuario(String nombre, String apellido, String contrasenia, String correo, String rut) {
        super(nombre, apellido, contrasenia, correo, rut);
        this.carrito = new Carrito();
    }

    // entrega el carrito del usuario
    public Carrito getCarrito() {
        return carrito;
    }
}