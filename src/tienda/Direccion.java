package tienda;

public class Direccion {
    private String nombreReceptor;
    private String calle;
    private String comuna;
    private String region;

    public Direccion(String nombreReceptor, String calle, String comuna, String region) {
        this.nombreReceptor = nombreReceptor == null ? "" : nombreReceptor.trim();
        this.calle = calle == null ? "" : calle.trim();
        this.comuna = comuna == null ? "" : comuna.trim();
        this.region = region == null ? "" : region.trim();
    }

    public String getNombreReceptor() { return nombreReceptor; }
    public String getCalle() { return calle; }
    public String getComuna() { return comuna; }
    public String getRegion() { return region; }

    public boolean esValida() {
        return !nombreReceptor.isBlank()
                && !calle.isBlank()
                && !comuna.isBlank()
                && !region.isBlank();
    }

    @Override
    public String toString() {
        return calle + ", " + comuna + ", " + region + " (Recibe: " + nombreReceptor + ")";
    }
}