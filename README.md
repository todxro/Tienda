# Tienda

Aplicación de escritorio en Java Swing para gestionar un catálogo, registrar compras y consultar ventas.

## Enlaces de entrega

- Repositorio: [https://github.com/todxro/Tienda](https://github.com/todxro/Tienda)
- Video de demostración: pendiente de grabar y publicar.

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

Se necesita Java 11 o posterior, con `java`, `javac` y `jar` disponibles en el `PATH`.
Desde la carpeta raíz del proyecto, compila ambas interfaces en PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

El script crea `Tienda-Usuario.jar` y `Tienda-Admin.jar` en la raíz. Para ejecutar el cliente:

```powershell
java -jar .\Tienda-Usuario.jar
```

Para abrir el panel de administración:

```powershell
java -jar .\Tienda-Admin.jar
```

Ejecuta los comandos desde la carpeta raíz del proyecto para que la aplicación encuentre
`inventario.csv`, `usuarios.csv`, `ventas.csv` e `imagenes/`. Esos archivos deben mantenerse
junto a los JAR; los ejecutables no incluyen los datos ni las imágenes.

También puedes compilar manualmente y ejecutar desde `bin`:

```powershell
javac -encoding UTF-8 -d bin src\tienda\*.java
java -cp bin tienda.VentanaUsuario
java -cp bin tienda.VentanaAdmin
```

En VS Code puedes seleccionar **Tienda - Cliente** o **Tienda - Administrador** en las configuraciones de ejecución.
