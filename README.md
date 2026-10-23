# Tienda

Aplicación de escritorio en Java Swing para gestionar un catálogo, registrar compras y consultar ventas.

## Índice del proyecto

### Interfaz
- `src/tienda/VentanaUsuario.java`: catálogo, cuenta, carrito y chat del cliente.
- `src/tienda/VentanaAdmin.java`: inventario, historial de ventas y chat de administración.

Dentro de `VentanaUsuario.java`, el índice al inicio de la clase agrupa los métodos de acceso,
catálogo, compra, chat e inicio de la aplicación.

### Lógica y datos
- `src/tienda/Inventario.java` y `src/tienda/Producto.java`: productos y persistencia del inventario.
- `src/tienda/Cuenta.java`, `src/tienda/Usuario.java` y `src/tienda/Carrito.java`: cuentas de cliente y compras.
- `src/tienda/GestorUsuarios.java` y `src/tienda/GestorVentas.java`: persistencia de usuarios y ventas.
- `src/tienda/Venta.java`: datos y formato CSV de una venta.

### Archivos y recursos
- `inventario.csv`, `usuarios.csv` y `ventas.csv`: datos persistidos. Se leen desde la carpeta raíz del proyecto.
- `imagenes/`: imágenes de productos, nombradas con su ID (por ejemplo, `P02.png`).
- `bin/`: clases compiladas; no contiene el código fuente.

## Compilar y ejecutar

Se necesita Java 11 o posterior. Desde la carpeta raíz del proyecto, ejecuta en PowerShell:

```powershell
javac -encoding UTF-8 -d bin src\tienda\*.java
java -cp bin tienda.VentanaUsuario
```

Para abrir el panel de administración, usa:

```powershell
java -cp bin tienda.VentanaAdmin
```

En VS Code también puedes seleccionar **Tienda - Cliente** o **Tienda - Administrador** en las configuraciones de ejecución.
