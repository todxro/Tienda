package tienda;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// lee y guarda los cupones en cupones.csv
public class GestorCupones {
    private static final String RUTA_ARCHIVO = "cupones.csv";
    private static final String ENCABEZADO = "codigo,tipo,valor,fechaVencimiento,usosMaximos,usosRealizados";

    private final List<Cupon> cupones = new ArrayList<>();

    public GestorCupones() {
        cargar();
    }

    // lee el archivo y llena la lista, salta las lineas con formato malo
    private void cargar() {
        cupones.clear();
        Path ruta = Paths.get(RUTA_ARCHIVO);
        if (!Files.exists(ruta)) {
            return;
        }
        try {
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
                } catch (RuntimeException e) {
                    // linea con datos invalidos, se ignora
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer cupones.csv: " + e.getMessage());
        }
    }

    // reescribe el archivo completo con los cupones de la lista
    private void guardar() throws IOException {
        List<String> lineas = new ArrayList<>();
        lineas.add(ENCABEZADO);
        for (Cupon c : cupones) {
            lineas.add(c.toCSV());
        }
        Files.write(Paths.get(RUTA_ARCHIVO), lineas, StandardCharsets.UTF_8);
    }

    // devuelve el cupon con ese codigo o null si no existe
    public Cupon buscar(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (Cupon c : cupones) {
            if (c.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return c;
            }
        }
        return null;
    }

    // agrega un cupon nuevo, devuelve false si el codigo ya existe
    public boolean agregar(Cupon cupon) throws IOException {
        if (buscar(cupon.getCodigo()) != null) {
            return false;
        }
        cupones.add(cupon);
        guardar();
        return true;
    }

    // suma un uso al cupon y lo deja guardado en el archivo
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