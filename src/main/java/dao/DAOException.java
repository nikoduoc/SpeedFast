package dao;

import java.sql.SQLException;

/**
 * Excepcion de la capa de acceso a datos.
 *
 * Envuelve la SQLException original (que se conserva como causa y se registra
 * en consola) y entrega un mensaje comprensible para el usuario final.
 */
public class DAOException extends Exception {

    public DAOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /**
     * Traduce una SQLException a un mensaje claro segun su codigo SQLState.
     *
     * @param operacion descripcion de lo que se intentaba hacer (ej: "registrar el pedido")
     * @param e         excepcion original del driver JDBC
     * @return excepcion lista para lanzar hacia el controlador y la vista
     */
    public static DAOException desde(String operacion, SQLException e) {
        System.err.println("[DAO] Error al " + operacion + " -> SQLState " + e.getSQLState()
                + ", codigo " + e.getErrorCode() + ": " + e.getMessage());

        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String mensaje;
        if (estado.startsWith("08")) {
            mensaje = "No fue posible conectar con la base de datos.\n"
                    + "Verifique que el servidor MySQL este encendido.";
        } else if (estado.startsWith("28")) {
            mensaje = "El usuario o la clave de la base de datos son incorrectos.\n"
                    + "Revise el archivo db.properties.";
        } else if (estado.startsWith("23")) {
            mensaje = "No se pudo " + operacion + " porque el registro esta relacionado con otros datos.";
        } else if (estado.startsWith("22")) {
            mensaje = "No se pudo " + operacion + ": uno de los datos no tiene el formato o largo permitido.";
        } else {
            mensaje = "Ocurrio un error al " + operacion + ".\nDetalle: " + e.getMessage();
        }
        return new DAOException(mensaje, e);
    }

    /**
     * Indica si la causa es una violacion de integridad (clave foranea, unico, etc.).
     */
    public static boolean esViolacionIntegridad(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }
}
