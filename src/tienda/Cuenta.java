package tienda;

// Agrupa los datos personales compartidos por usuarios y administradores.
public class Cuenta {
    // datos generales de una cuenta
    private String nombre;
    private String apellido; 
    private String contrasenia;
    private String correo;
    private String rut;

    // crea una cuenta con sus datos personales
    public Cuenta(String nombre, String apellido, String contrasenia, String direccion, String correo, String rut) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.contrasenia = contrasenia;
        this.correo = correo;
        this.rut = rut;
    }

    public String getNombre() {
        // entrega el nombre de la cuenta
        return nombre;
    }

    public void setNombre(String nombre) {
        // cambia el nombre de la cuenta
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }
}