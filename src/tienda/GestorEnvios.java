package tienda;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GestorEnvios {
    private static final String ARCHIVO_TARIFAS = "tarifas_envio.csv";
    private static final String ARCHIVO_CONFIG = "config_envio.txt";
    private static final String ENCABEZADO = "region,costo,diasHabiles";

    private final List<TarifaEnvio> tarifas = new ArrayList<>();
    private double montoMinimoGratis = 100000;

    public GestorEnvios() {
        cargar();
    }

    public final void cargar() {
        tarifas.clear();
        Path rutaTarifas = Paths.get(ARCHIVO_TARIFAS);
        if (!Files.exists(rutaTarifas)) {
            tarifas.add(new TarifaEnvio("Región del Maule", 4990, 2));
            tarifas.add(new TarifaEnvio("Región Metropolitana", 5990, 5));
            tarifas.add(new TarifaEnvio("Región de O'Higgins", 5990, 5));
            tarifas.add(new TarifaEnvio("Región del Biobío", 5990, 5));
            tarifas.add(new TarifaEnvio("Región de Valparaíso", 7990, 5));
            tarifas.add(new TarifaEnvio("Resto del país", 7990, 5));
            guardar();
        } else {
            try {
                List<String> lineas = Files.readAllLines(rutaTarifas, StandardCharsets.UTF_8);
                for (int i = 1; i < lineas.size(); i++) {
                    String[] partes = lineas.get(i).split(",", -1);
                    if (partes.length >= 3) {
                        tarifas.add(new TarifaEnvio(
                                partes[0].trim(),
                                Double.parseDouble(partes[1].trim()),
                                Integer.parseInt(partes[2].trim())
                        ));
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error al cargar tarifas de envío: " + e.getMessage());
            }
        }

        Path rutaConfig = Paths.get(ARCHIVO_CONFIG);
        if (!Files.exists(rutaConfig)) {
            guardarConfig();
        } else {
            try {
                String contenido = Files.readString(rutaConfig, StandardCharsets.UTF_8).trim();
                if (!contenido.isEmpty()) {
                    montoMinimoGratis = Double.parseDouble(contenido);
                }
            } catch (IOException | NumberFormatException e) {
                montoMinimoGratis = 100000;
            }
        }
    }

    public void guardar() {
        List<String> lineas = new ArrayList<>();
        lineas.add(ENCABEZADO);
        for (TarifaEnvio t : tarifas) {
            lineas.add(t.toCSV());
        }
        try {
            Files.write(Paths.get(ARCHIVO_TARIFAS), lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error al guardar tarifas: " + e.getMessage());
        }
        guardarConfig();
    }

    private void guardarConfig() {
        try {
            Files.writeString(Paths.get(ARCHIVO_CONFIG), String.valueOf(montoMinimoGratis), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error al guardar config de envío: " + e.getMessage());
        }
    }

    public List<TarifaEnvio> getTarifas() {
        return new ArrayList<>(tarifas);
    }

    public TarifaEnvio buscarPorRegion(String region) {
        if (region == null) {
            return null;
        }
        for (TarifaEnvio t : tarifas) {
            if (t.getRegion().equalsIgnoreCase(region.trim())) {
                return t;
            }
        }
        return null;
    }

    public void guardarOActualizarTarifa(String region, double costo, int diasHabiles) {
        TarifaEnvio existente = buscarPorRegion(region);
        if (existente != null) {
            existente.setCosto(costo);
            existente.setDiasHabiles(diasHabiles);
        } else {
            tarifas.add(new TarifaEnvio(region, costo, diasHabiles));
        }
        guardar();
    }

    public boolean eliminarTarifa(String region) {
        boolean eliminado = tarifas.removeIf(t -> t.getRegion().equalsIgnoreCase(region.trim()));
        if (eliminado) {
            guardar();
        }
        return eliminado;
    }

    public double getMontoMinimoGratis() {
        return montoMinimoGratis;
    }

    public void setMontoMinimoGratis(double montoMinimoGratis) {
        this.montoMinimoGratis = Math.max(0, montoMinimoGratis);
        guardarConfig();
    }
}