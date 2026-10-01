package util;

/**
 * Error de validacion de un dato ingresado por el usuario en la interfaz.
 * Su mensaje esta pensado para mostrarse directamente en un JOptionPane.
 */
public class ValidacionException extends Exception {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
