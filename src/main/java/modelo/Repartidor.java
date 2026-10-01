package modelo;

/**
 * Representa a un repartidor de SpeedFast (tabla repartidores).
 */
public class Repartidor {

    private int id;
    private String nombre;

    /**
     * Constructor para un repartidor nuevo, aun sin id asignado por la base de datos.
     *
     * @param nombre nombre completo del repartidor
     */
    public Repartidor(String nombre) {
        this(0, nombre);
    }

    /**
     * Constructor para un repartidor ya almacenado.
     *
     * @param id     identificador generado por la base de datos
     * @param nombre nombre completo del repartidor
     */
    public Repartidor(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Texto legible para los JComboBox: "id - nombre".
     * El objeto completo (con su id) queda guardado en el combo.
     */
    @Override
    public String toString() {
        return id + " - " + nombre;
    }
}
