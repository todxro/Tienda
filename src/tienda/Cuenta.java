package tienda;

// Agrupa los datos personales compartidos por usuarios y administradores.
public class Cuenta {
    // datos generales de una cuenta
    private String nombre;
    private String apellido;
    private String contrasenia;
    private String correo;
    private String rut;

    // crea una cuenta con sus datos personales básicos
    public Cuenta(String nombre, String apellido, String contrasenia, String correo, String rut) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.contrasenia = contrasenia;
        this.correo = correo;
        this.rut = rut;
    }

    // entrega el nombre de la cuenta
    public String getNombre() {
        return nombre;
    }

    // cambia el nombre de la cuenta
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // entrega el apellido de la cuenta
    public String getApellido() {
        return apellido;
    }

    // cambia el apellido de la cuenta
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    // entrega la contraseña asociada a la cuenta
    public String getContrasenia() {
        return contrasenia;
    }

    // cambia la contraseña de la cuenta
    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    // entrega el correo principal de la cuenta
    public String getCorreo() {
        return correo;
    }

    // cambia el correo principal de la cuenta
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    // entrega el rut asociado a la cuenta
    public String getRut() {
        return rut;
    }

    // cambia el rut asociado a la cuenta
    public void setRut(String rut) {
        this.rut = rut;
    }
}