# Tienda

Aplicación de escritorio en Java Swing para gestionar un catálogo, registrar compras y consultar ventas.

## Enlaces de entrega

- Repositorio: [https://github.com/todxro/Tienda](https://github.com/todxro/Tienda)
- Video de demostración: pendiente de grabar y publicar.

## Índice del proyecto

### Interfaz
- `src/tienda/VentanaUsuario.java`: catálogo, cuenta, carrito y asistente Gemini del cliente.
- `src/tienda/GeminiAPIClient.java`: consultas de clientes sobre productos mediante Gemini.
- `src/tienda/VentanaAdmin.java`: inventario, historial de ventas y administración de cupones.

Dentro de `VentanaUsuario.java`, el índice al inicio de la clase agrupa los métodos de acceso,
catálogo, compra, consultas de productos e inicio de la aplicación.

### Lógica y datos
- `src/tienda/Inventario.java` y `src/tienda/Producto.java`: productos y persistencia del inventario.
- `src/tienda/Cuenta.java`, `src/tienda/Usuario.java` y `src/tienda/Carrito.java`: cuentas de cliente y compras.
- `src/tienda/GestorUsuarios.java` y `src/tienda/GestorVentas.java`: persistencia de usuarios y ventas.
- `src/tienda/Venta.java`: datos y formato CSV de una venta.

### Archivos y recursos
- `inventario.csv`, `usuarios.csv`, `ventas.csv` y `cupones.csv`: datos persistidos. Se leen desde la carpeta raíz del proyecto.
- `imagenes/`: imágenes de productos, nombradas con su ID (por ejemplo, `P02.png`).
- `bin/`: clases compiladas; no contiene el código fuente.

En **Cupones** del panel de administración puedes crear descuentos por porcentaje o por monto fijo,
con fecha de vencimiento y límite de usos. Los clientes aplican el código en su carrito; el descuento
se calcula antes del IVA y el uso se registra al guardar la compra.

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

### Consultas sobre productos

Cada producto del catálogo incluye el botón **Consultar sobre este producto**. Para habilitar
las respuestas de Gemini, pega tu clave en `gemini-api-key.txt`, en la carpeta raíz del proyecto.
El archivo está ignorado por Git, así que la clave no se agrega al repositorio. También puedes
configurar la variable de entorno `GEMINI_API_KEY` en PowerShell:

```powershell
$env:GEMINI_API_KEY = "TU_CLAVE_DE_GEMINI"
javac -encoding UTF-8 -d bin src\tienda\*.java
java -cp bin tienda.VentanaUsuario
```

La variable de entorno tiene prioridad sobre el archivo local. Ejecuta los comandos desde la
carpeta raíz del proyecto. Revoca y reemplaza cualquier clave que ya se haya expuesto.

También puedes compilar manualmente y ejecutar desde `bin`:

```powershell
javac -encoding UTF-8 -d bin src\tienda\*.java
java -cp bin tienda.VentanaUsuario
java -cp bin tienda.VentanaAdmin
```

En VS Code puedes seleccionar **Tienda - Cliente** o **Tienda - Administrador** en las configuraciones de ejecución.
