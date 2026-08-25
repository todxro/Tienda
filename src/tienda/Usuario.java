package tienda;


import java.util.Date;

public class Usuario extends Cuenta {

    public Usuario(String nombre, String apellido, String contrasenia, String direccion, String pais, String comuna, String region, String correo, int numeroDeTelefono, Date fechadeNacimiento, String rut) {
        super(nombre, apellido, contrasenia, direccion, pais, comuna, region, correo, numeroDeTelefono, fechadeNacimiento, rut);
    }
}