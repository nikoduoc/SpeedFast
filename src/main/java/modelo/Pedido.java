package modelo;

/**
 * Representa un pedido de SpeedFast (tabla pedidos).
 */
public class Pedido {

    private int id;
    private String direccion;
    private TipoPedido tipo;
    private EstadoPedido estado;

    /**
     * Constructor para un pedido nuevo, aun sin id asignado por la base de datos.
     */
    public Pedido(String direccion, TipoPedido tipo, EstadoPedido estado) {
        this(0, direccion, tipo, estado);
    }

    /**
     * Constructor para un pedido ya almacenado.
     *
     * @param id        identificador generado por la base de datos
     * @param direccion direccion de despacho
     * @param tipo      tipo de pedido
     * @param estado    estado actual del pedido
     */
    public Pedido(int id, String direccion, TipoPedido tipo, EstadoPedido estado) {
        this.id = id;
        this.direccion = direccion;
        this.tipo = tipo;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public TipoPedido getTipo() {
        return tipo;
    }

    public void setTipo(TipoPedido tipo) {
        this.tipo = tipo;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    /**
     * Texto legible para los JComboBox: "id - direccion".
     */
    @Override
    public String toString() {
        return id + " - " + direccion;
    }
}
