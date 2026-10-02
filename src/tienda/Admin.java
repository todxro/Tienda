package tienda;


import java.util.ArrayList;
import java.util.Date;

public class Admin extends Cuenta {
    // lista de cuentas administradas
    private ArrayList<String> cuentas = new ArrayList<>();

    // crea una cuenta de administrador
    public Admin(String nombre, String apellido, String contrasenia, String direccion, String pais, String comuna, String region, String correo, int numeroDeTelefono, Date fechadeNacimiento, String rut) {
        super(nombre, apellido, contrasenia, direccion, pais, comuna, region, correo, numeroDeTelefono, fechadeNacimiento, rut);
    }

    public ArrayList<String> getCuentas() {
        // entrega las cuentas administradas
        return cuentas;
    }

    public void setCuentas(ArrayList<String> cuentas) {
        // actualiza las cuentas administradas
        this.cuentas = cuentas;
    }
}