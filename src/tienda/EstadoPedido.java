package tienda;

// estados por los que pasa un pedido, en este orden
public enum EstadoPedido {
    PENDIENTE("Pendiente"),
    PREPARANDO("En preparación"),
    ENVIADO("Despachado"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String texto;

    EstadoPedido(String texto) {
        this.texto = texto;
    }

    // texto para mostrar en pantalla
    public String getTexto() {
        return texto;
    }

    // el estado que viene despues, o null si el pedido ya fue entregado
    public EstadoPedido siguiente() {
        if (this == ENTREGADO || this == CANCELADO) {
            return null;
        }
        return values()[ordinal() + 1];
    }

    public boolean puedeCancelarse() {
        return this == PENDIENTE || this == PREPARANDO;
    }

    // convierte el texto guardado en el csv, si viene vacio es una venta antigua
    public static EstadoPedido desdeTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return ENTREGADO;
        }
        for (EstadoPedido estado : values()) {
            if (estado.name().equalsIgnoreCase(texto.trim())) {
                return estado;
            }
        }
        return ENTREGADO;
    }
}