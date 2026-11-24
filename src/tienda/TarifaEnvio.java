package tienda;

public class TarifaEnvio {
    private String region;
    private double costo;
    private int diasHabiles;

    public TarifaEnvio(String region, double costo, int diasHabiles) {
        this.region = region.trim();
        this.costo = Math.max(0, costo);
        this.diasHabiles = Math.max(1, diasHabiles);
    }

    public String getRegion() { return region; }
    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = Math.max(0, costo); }
    public int getDiasHabiles() { return diasHabiles; }
    public void setDiasHabiles(int diasHabiles) { this.diasHabiles = Math.max(1, diasHabiles); }

    public String toCSV() {
        return region + "," + costo + "," + diasHabiles;
    }
}