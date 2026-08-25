package tienda;


import java.util.Date;

public class Cuenta {
    private String nombre;
    private String apellido; 
    private String contrasenia;
    private String direccion;
    private String pais;
    private String comuna;
    private String region;
    private String correo;
    private int numeroDeTelefono;
    private Date fechadeNacimiento;
    private String rut;

    public Cuenta(String nombre, String apellido, String contrasenia, String direccion, String pais, String comuna, String region, String correo, int numeroDeTelefono, Date fechadeNacimiento, String rut) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.contrasenia = contrasenia;
        this.direccion = direccion;
        this.pais = pais;
        this.comuna = comuna;
        this.region = region;
        this.correo = correo;
        this.numeroDeTelefono = numeroDeTelefono;
        this.fechadeNacimiento = fechadeNacimiento;
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getNumeroDeTelefono() {
        return numeroDeTelefono;
    }

    public void setNumeroDeTelefono(int numeroDeTelefono) {
        this.numeroDeTelefono = numeroDeTelefono;
    }

    public Date getFechadeNacimiento() {
        return fechadeNacimiento;
    }

    public void setFechadeNacimiento(Date fechadeNacimiento) {
        this.fechadeNacimiento = fechadeNacimiento;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }
}