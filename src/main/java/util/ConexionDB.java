package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Punto unico de acceso a la base de datos speedfast_db.
 *
 * Lee la URL, el usuario y la clave desde el archivo db.properties (carpeta
 * resources). Cada llamada a getConexion() entrega una conexion nueva, que el
 * DAO cierra con try-with-resources al terminar cada operacion.
 */
public final class ConexionDB {

    private static final String ARCHIVO_CONFIG = "/db.properties";

    /* Valores por defecto si no existe db.properties */
    private static final String URL_DEFECTO = "jdbc:mysql://localhost:3306/speedfast_db";
    private static final String USUARIO_DEFECTO = "root";
    private static final String CLAVE_DEFECTO = "";

    private static final String URL;
    private static final String USUARIO;
    private static final String CLAVE;

    /* Se carga la configuracion una sola vez, al usar la clase por primera vez */
    static {
        Properties config = new Properties();
        try (InputStream entrada = ConexionDB.class.getResourceAsStream(ARCHIVO_CONFIG)) {
            if (entrada != null) {
                config.load(entrada);
            } else {
                System.err.println("[ConexionDB] No se encontro db.properties, se usan valores por defecto.");
            }
        } catch (IOException e) {
            System.err.println("[ConexionDB] No se pudo leer db.properties: " + e.getMessage());
        }
        // Una propiedad de sistema (-Ddb.url=...) tiene prioridad sobre el archivo
        URL = System.getProperty("db.url", config.getProperty("db.url", URL_DEFECTO));
        USUARIO = System.getProperty("db.usuario", config.getProperty("db.usuario", USUARIO_DEFECTO));
        CLAVE = System.getProperty("db.clave", config.getProperty("db.clave", CLAVE_DEFECTO));
    }

    /** Clase utilitaria: no se instancia. */
    private ConexionDB() {
    }

    /**
     * Abre una conexion nueva con la base de datos.
     *
     * @return conexion abierta; quien la recibe es responsable de cerrarla
     * @throws SQLException si el servidor no responde o las credenciales son incorrectas
     */
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    /**
     * Verifica que la base de datos este disponible (Paso 1 de la actividad).
     *
     * @throws SQLException con el detalle del problema si la conexion falla
     */
    public static void probarConexion() throws SQLException {
        try (Connection conexion = getConexion()) {
            if (!conexion.isValid(3)) {
                throw new SQLException("La conexion se abrio pero no responde.");
            }
        }
    }

    /** @return la URL configurada, util para los mensajes de error */
    public static String getUrl() {
        return URL;
    }
}
