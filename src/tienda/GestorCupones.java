package tienda;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorCupones {
    private static final String RUTA_ARCHIVO = "cupones.csv";
    private static final String ENCABEZADO = "codigo,tipo,valor,fechaVencimiento,usosMaximos,usosRealizados";

    private final List<Cupon> cupones = new ArrayList<>();

    public GestorCupones() throws IOException {
        cargar();
    }

    private void cargar() throws IOException {
        cupones.clear();
        Path ruta = Paths.get(RUTA_ARCHIVO);
        if (!Files.exists(ruta)) {
            return;
        }
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        for (int i = 1; i < lineas.size(); i++) {
            String[] d = lineas.get(i).split(",", -1);
            if (d.length < 6) {
                continue;
            }
            try {
                cupones.add(new Cupon(d[0].trim(), d[1].trim(), Double.parseDouble(d[2]),
                        LocalDate.parse(d[3].trim()), Integer.parseInt(d[4].trim()),
                        Integer.parseInt(d[5].trim())));
            } catch (RuntimeException ex) {
                System.err.println("Se ignoró la línea inválida " + (i + 1) + " de cupones.csv: "
                        + ex.getMessage());
            }
        }
    }

    private void guardar() throws IOException {
        List<String> lineas = new ArrayList<>();
        lineas.add(ENCABEZADO);
        for (Cupon cupon : cupones) {
            lineas.add(cupon.toCSV());
        }
        Files.write(Paths.get(RUTA_ARCHIVO), lineas, StandardCharsets.UTF_8);
    }

    public Cupon buscar(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (Cupon cupon : cupones) {
            if (cupon.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return cupon;
            }
        }
        return null;
    }

    public boolean agregar(Cupon cupon) throws IOException {
        if (buscar(cupon.getCodigo()) != null) {
            return false;
        }
        cupones.add(cupon);
        guardar();
        return true;
    }

    public void registrarUso(String codigo) throws IOException {
        Cupon cupon = buscar(codigo);
        if (cupon != null) {
            cupon.registrarUso();
            guardar();
        }
    }

    public List<Cupon> getCupones() {
        return new ArrayList<>(cupones);
    }
}
