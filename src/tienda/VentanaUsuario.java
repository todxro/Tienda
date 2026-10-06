package tienda;

import java.awt.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.table.DefaultTableModel;

public class VentanaUsuario extends JFrame {
    private static final Color AZUL_MARINO = new Color(46, 31, 71);   //morado oscuro
    private static final Color DORADO      = new Color(232, 184, 75); //dorado
    private static final Color FONDO       = new Color(18, 16, 27);   //fondo casi negro
    private static final Color TEXTO       = new Color(61, 43, 0);    //texto oscuro
    private static final Color TEXTO_CLARO = new Color(217, 210, 232);//texto claro
    private static final Color BLANCO      = Color.WHITE;
    private Usuario usuario;
    private Carrito carrito;
    private final Inventario inventario;
    private final GestorUsuarios gestorUsuarios;
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel paneles = new JPanel(tarjetas);

    // componentes de la interfaz
    private JTable tablaCatalogo;
    private DefaultTableModel modeloCatalogo;
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;
    private JTextField campoCantidad;
    private JLabel labelSubtotal;
    private JLabel labelIVA;
    private JLabel labelTotal;
    private JButton botonIniciarSesion;

    public VentanaUsuario() {
        this(null);
    }

    public VentanaUsuario(Usuario usuario) {
        this.usuario = usuario;
        this.carrito = usuario == null ? new Carrito() : usuario.getCarrito();
        this.inventario = new Inventario();
        this.gestorUsuarios = new GestorUsuarios();

        setTitle("Tienda");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(paneles);
        paneles.add(crearPanelLogin(), "login");
        mostrarCatalogo();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JPanel crearPanelLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(FONDO);
        JPanel formulario = new JPanel(new BorderLayout(8, 12));
        formulario.setBackground(FONDO);
        formulario.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel titulo = new JLabel("Iniciar sesión", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(DORADO);
        JTextField campoCorreo = new JTextField(20);
        JPasswordField campoContrasenia = new JPasswordField(20);
        JLabel labelCorreo = new JLabel("Correo:");
        JLabel labelContrasenia = new JLabel("Contraseña:");
        labelCorreo.setForeground(TEXTO_CLARO);
        labelContrasenia.setForeground(TEXTO_CLARO);
        JPanel campos = new JPanel(new GridLayout(2, 2, 8, 8));
        campos.setBackground(FONDO);
        campos.add(labelCorreo);
        campos.add(campoCorreo);
        campos.add(labelContrasenia);
        campos.add(campoContrasenia);

        JButton iniciar = new JButton("Iniciar sesión");
        JButton crearCuenta = new JButton("Crear cuenta");
        JButton continuarComoInvitado = new JButton("Continuar como invitado");
        estilizarBoton(iniciar, DORADO, TEXTO);
        estilizarBoton(crearCuenta, AZUL_MARINO, BLANCO);
        estilizarBoton(continuarComoInvitado, AZUL_MARINO, BLANCO);
        JPanel botones = new JPanel(new GridLayout(1, 3, 8, 8));
        botones.setBackground(FONDO);
        botones.add(iniciar);
        botones.add(crearCuenta);
        botones.add(continuarComoInvitado);
        formulario.add(titulo, BorderLayout.NORTH);
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);
        panel.add(formulario);

        iniciar.addActionListener(e -> {
            Usuario autenticado = gestorUsuarios.autenticar(campoCorreo.getText().trim(),
                    new String(campoContrasenia.getPassword()));
            if (autenticado == null) {
                JOptionPane.showMessageDialog(this, "Correo o contraseña incorrectos.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                return;
            }
            usuario = autenticado;
            mostrarCatalogo();
        });
        crearCuenta.addActionListener(e -> mostrarRegistro(campoCorreo, campoContrasenia));
        continuarComoInvitado.addActionListener(e -> {
            getRootPane().setDefaultButton(null);
            tarjetas.show(paneles, "catalogo");
        });
        botonIniciarSesion = iniciar;
        getRootPane().setDefaultButton(iniciar);
        return panel;
    }

    private void mostrarRegistro(JTextField campoCorreo, JPasswordField campoContrasenia) {
        JTextField nombre = new JTextField();
        JTextField apellido = new JTextField();
        JTextField rut = new JTextField();
        ((AbstractDocument) rut.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass bypass, int offset, String texto, AttributeSet atributos)
                    throws BadLocationException {
                replace(bypass, offset, 0, texto, atributos);
            }

            @Override
            public void replace(FilterBypass bypass, int offset, int longitud, String texto,
                    AttributeSet atributos) throws BadLocationException {
                if (texto == null || texto.chars().allMatch(caracter -> caracter >= '0' && caracter <= '9')) {
                    bypass.replace(offset, longitud, texto, atributos);
                }
            }
        });
        JTextField correo = new JTextField();
        JPasswordField contrasenia = new JPasswordField();
        Object[] campos = {"Nombre:", nombre, "Apellido:", apellido, "RUT:", rut, "Correo:", correo,
                "Contraseña:", contrasenia};
        int resultado = JOptionPane.showConfirmDialog(this, campos, "Crear cuenta",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        boolean creada = gestorUsuarios.registrar(nombre.getText().trim(), apellido.getText().trim(),
                rut.getText().trim(), correo.getText().trim(), new String(contrasenia.getPassword()));
        if (creada) {
            campoCorreo.setText(correo.getText().trim());
            campoContrasenia.setText("");
            JOptionPane.showMessageDialog(this, "Cuenta creada. Ahora puedes iniciar sesión.");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Completa todos los campos, usa un correo con formato usuario@dominio.com y verifica que esté disponible.",
                    "No se pudo crear la cuenta", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarCatalogo() {
        setTitle(usuario == null ? "Catálogo - Invitado" : "Catálogo - " + usuario.getNombre());
        JPanel catalogo = new JPanel(new BorderLayout(12, 12));
        catalogo.setBackground(FONDO);
        catalogo.add(crearPanelBusqueda(), BorderLayout.NORTH);
        catalogo.add(crearPanelCatalogo(), BorderLayout.CENTER);
        catalogo.add(crearPanelCarrito(), BorderLayout.SOUTH);
        paneles.add(catalogo, "catalogo");
        tarjetas.show(paneles, "catalogo");
        getRootPane().setDefaultButton(null);
        actualizarVista();
        paneles.revalidate();
        paneles.repaint();
    }

    // construye la barra superior azul con el título y el buscador
    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AZUL_MARINO);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titulo = new JLabel("CATÁLOGO");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(BLANCO);
        panel.add(titulo, BorderLayout.WEST);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        busqueda.setOpaque(false);

        JLabel labelBuscar = new JLabel("Buscar:");
        labelBuscar.setForeground(BLANCO);

        JTextField campoBuscar = new JTextField(15);
        campoBuscar.setBackground(new Color(28, 24, 43));
        campoBuscar.setForeground(TEXTO_CLARO);
        campoBuscar.setCaretColor(TEXTO_CLARO);        JButton btnBuscar = new JButton("Buscar");
        JButton btnRefrescar = new JButton("Ver todo");
        JButton btnActualizar = new JButton("Actualizar");

        btnBuscar.addActionListener(e -> {
            String texto = campoBuscar.getText().trim();
            if (!texto.isEmpty()) {
                cargarProductosEnTabla(inventario.buscarPorNombre(texto));
            }
        });
        btnRefrescar.addActionListener(e -> actualizarVista());
        btnActualizar.addActionListener(e -> {
            inventario.recargarDesdeArchivo();
            actualizarVista();
        });

        busqueda.add(labelBuscar);
        busqueda.add(campoBuscar);
        busqueda.add(btnBuscar);
        busqueda.add(btnRefrescar);
        busqueda.add(btnActualizar);

        if (usuario == null) {
            JButton btnIniciarSesion = new JButton("Iniciar sesión");
            estilizarBoton(btnIniciarSesion, DORADO, TEXTO);
            btnIniciarSesion.addActionListener(e -> {
                tarjetas.show(paneles, "login");
                getRootPane().setDefaultButton(botonIniciarSesion);
            });
            busqueda.add(btnIniciarSesion);
        } else {
            JLabel saludo = new JLabel("Hola, " + usuario.getNombre());
            saludo.setForeground(BLANCO);
            busqueda.add(saludo);
        }

        panel.add(busqueda, BorderLayout.EAST);
        return panel;
    }

    // arma la tabla del catalogo en el centro de la ventana
    private JPanel crearPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        String[] columnas = {"ID", "Producto", "Precio", "Stock"};
        modeloCatalogo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tablaCatalogo = new JTable(modeloCatalogo);
        tablaCatalogo.setRowHeight(28);
        tablaCatalogo.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaCatalogo.getTableHeader().setBackground(AZUL_MARINO);
        tablaCatalogo.getTableHeader().setForeground(BLANCO);
        tablaCatalogo.setSelectionBackground(DORADO);
        tablaCatalogo.setSelectionForeground(TEXTO);
        tablaCatalogo.setGridColor(new Color(60, 52, 80));
        tablaCatalogo.setBackground(new Color(28, 24, 43));
        tablaCatalogo.setForeground(TEXTO_CLARO);

        JScrollPane scrollCatalogo = new JScrollPane(tablaCatalogo);
        scrollCatalogo.getViewport().setBackground(new Color(28, 24, 43));
        panel.add(scrollCatalogo, BorderLayout.CENTER);
        return panel;
    }

    // arma el panel de abajo con el carrito y los botones
    private JPanel crearPanelCarrito() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));

        String[] columnas = {"Producto", "Cantidad", "Subtotal"};
        modeloCarrito = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setRowHeight(24);
        tablaCarrito.getTableHeader().setBackground(AZUL_MARINO);
        tablaCarrito.getTableHeader().setForeground(BLANCO);
        tablaCarrito.setBackground(new Color(28, 24, 43));
        tablaCarrito.setForeground(TEXTO_CLARO);
        JScrollPane scrollCarrito = new JScrollPane(tablaCarrito);
        scrollCarrito.getViewport().setBackground(new Color(28, 24, 43));
        scrollCarrito.setPreferredSize(new Dimension(0, 140));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controles.setBackground(FONDO);

        JLabel labelCantidad = new JLabel("Cantidad:");
        labelCantidad.setForeground(TEXTO_CLARO);
        controles.add(labelCantidad);
        campoCantidad = new JTextField("1", 4);
        campoCantidad.setBackground(new Color(28, 24, 43));
        campoCantidad.setForeground(TEXTO_CLARO);
        campoCantidad.setCaretColor(TEXTO_CLARO);        controles.add(campoCantidad);

        JButton btnAgregar = new JButton("Agregar al carrito");
        estilizarBoton(btnAgregar, DORADO, TEXTO);
        btnAgregar.addActionListener(e -> agregarAlCarrito());
        controles.add(btnAgregar);

        JButton btnComprar = new JButton("Comprar");
        estilizarBoton(btnComprar, AZUL_MARINO, BLANCO);
        btnComprar.addActionListener(e -> comprarProductos());
        controles.add(btnComprar);

        labelSubtotal = new JLabel("Subtotal: $0");
        labelSubtotal.setForeground(TEXTO_CLARO);
        controles.add(labelSubtotal);

        labelIVA = new JLabel("IVA (19%): $0");
        labelIVA.setForeground(TEXTO_CLARO);
        controles.add(labelIVA);

        labelTotal = new JLabel("TOTAL: $0");
        labelTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        labelTotal.setForeground(DORADO);
        controles.add(labelTotal);

        panel.add(controles, BorderLayout.NORTH);
        panel.add(scrollCarrito, BorderLayout.CENTER);
        return panel;
    }

    // le da el mismo estilo a los botones para no repetir codigo
    private void estilizarBoton(JButton boton, Color fondo, Color texto) {
        boton.setBackground(fondo);
        boton.setForeground(texto);
        boton.setFocusPainted(false);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    // llena la tabla con la lista de productos recibida
    private void cargarProductosEnTabla(java.util.List<Producto> productos) {
        modeloCatalogo.setRowCount(0);
        for (Producto p : productos) {
            modeloCatalogo.addRow(new Object[]{
                    p.getId(), p.getNombre(), "$" + p.getPrecio(), p.getStock()
            });
        }
    }

    private void agregarAlCarrito() {
        // toma el producto seleccionado en la tabla en vez de un id escrito a mano
        int fila = tablaCatalogo.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla.",
                    "Falta selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) modeloCatalogo.getValueAt(fila, 0);
        Producto producto = inventario.buscarProducto(id);

        int cantidad;
        try {
            cantidad = Integer.parseInt(campoCantidad.getText().trim());
            if (cantidad <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número mayor a 0.", "Cantidad inválida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cantidad > producto.getStock()) {
            JOptionPane.showMessageDialog(this,
                    "La cantidad ingresada es mayor a la que existe. Solo hay " + producto.getStock() + " unidad(es) disponibles de "
                            + producto.getNombre() + ".",
                    "Cantidad inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // cuenta cuantas unidades se lograron agregar de verdad, por si el stock no alcanza
        int agregadas = 0;
        for (int i = 0; i < cantidad; i++) {
            if (!carrito.agregarProducto(producto)) {
                break;
            }
            agregadas++;
        }

        if (agregadas == 0) {
            JOptionPane.showMessageDialog(this, "No hay stock suficiente para ese producto.", "Stock insuficiente",
                    JOptionPane.WARNING_MESSAGE);
        }

        campoCantidad.setText("1");
        actualizarVista();
    }

    private void comprarProductos() {
        if (carrito.getProductos().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.", "Compra",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rutCliente = usuario == null ? "" : usuario.getRut();

        // extrae los datos
        double subtotal = carrito.calcularSubtotalCarrito();
        double iva = carrito.calcularIVA();
        double total = carrito.calcularTotal();

        // construye el detalle 
        StringBuilder detalles = new StringBuilder();
        for (Producto p : carrito.getProductos()) {
            int cant = carrito.getCantidad(p.getId());
            detalles.append(cant).append("x ").append(p.getNombre()).append(" | ");
        }

        // crea el gestor y genera una nueva venta 
        GestorVentas gestorVentas = new GestorVentas();
        String numeroTicket = usuario == null ? "" : usuario.getRut();
        java.util.Date fechaActual = new java.util.Date(); 
        String nombreCompleto = usuario == null ? "Invitado" : usuario.getNombre() + " " + usuario.getApellido();

        Venta nuevaVenta = new Venta(
            numeroTicket,
            fechaActual,
            rutCliente,
            nombreCompleto,
            usuario == null ? "" : usuario.getCorreo(),
            detalles.toString(),
            subtotal,
            iva,
            total
        );

        // procesa la compra en el sistema 
        boolean compraExitosa = carrito.finalizarCompra();

        if (!compraExitosa) {
            JOptionPane.showMessageDialog(this, "No hay stock suficiente para completar la compra.", "Compra fallida",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // guarda la venta y el inventario actualizados
        // guarda la venta y el inventario actualizados 
        try {
            if (usuario == null) {
                numeroTicket = gestorVentas.guardarVentaInvitado(nuevaVenta);
            } else {
                gestorVentas.guardarVenta(nuevaVenta);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar la venta: " + e.getMessage(),
                    "Error al guardar la venta", JOptionPane.ERROR_MESSAGE);
            return;
        }
        inventario.guardarEnArchivo();
        
        JOptionPane.showMessageDialog(this, "Tu ticket de venta es el número: " + numeroTicket,
                "Ticket de venta",
            JOptionPane.INFORMATION_MESSAGE);
        actualizarVista();
    }

    public void actualizarVista() {
        // vuelve a llenar la tabla del catalogo con lo que hay en inventario
        cargarProductosEnTabla(inventario.getListaProductos());

        // vuelve a llenar la tabla del carrito
        modeloCarrito.setRowCount(0);
        for (Producto p : carrito.getProductos()) {
            modeloCarrito.addRow(new Object[]{
                    p.getNombre(),
                    carrito.getCantidad(p.getId()),
                    "$" + carrito.calcularSubtotal(p.getId())
            });
        }

        labelSubtotal.setText("Subtotal: $" + carrito.calcularSubtotalCarrito());
        labelIVA.setText("IVA (19%): $" + carrito.calcularIVA());
        labelTotal.setText("TOTAL: $" + carrito.calcularTotal());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaUsuario().setVisible(true));
    }
}