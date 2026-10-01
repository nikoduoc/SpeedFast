import util.ConexionDB;
import vista.VentanaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.SQLException;

/**
 * Punto de entrada de SpeedFast (Semana 8).
 *
 * 1. Verifica la conexion con la base de datos speedfast_db.
 * 2. Si la conexion funciona, abre la ventana principal.
 * 3. Si falla, informa al usuario como resolverlo y termina.
 */
public class Main {

    public static void main(String[] args) {
        usarAparienciaDelSistema();

        try {
            ConexionDB.probarConexion();
            System.out.println("[Main] Conexion exitosa con " + ConexionDB.getUrl());
        } catch (SQLException e) {
            System.err.println("[Main] No se pudo conectar: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "No fue posible conectar con la base de datos speedfast_db.\n\n"
                            + "Revise que:\n"
                            + " - El servidor MySQL este encendido.\n"
                            + " - Se haya ejecutado el script sql/speedfast_db.sql.\n"
                            + " - El usuario y la clave de src/main/resources/db.properties sean correctos.\n\n"
                            + "Detalle: " + e.getMessage(),
                    "Error de conexion", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // La interfaz Swing se crea en el hilo de eventos
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    private static void usarAparienciaDelSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[Main] Se usa la apariencia por defecto de Swing.");
        }
    }
}
