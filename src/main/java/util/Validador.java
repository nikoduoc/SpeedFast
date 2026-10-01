package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Validaciones reutilizables para los formularios.
 *
 * Todos los metodos lanzan ValidacionException con un mensaje claro cuando el
 * dato no es valido, asi la vista solo debe capturar la excepcion y mostrarla.
 * Se validan los datos ANTES de llamar al controlador y al DAO.
 */
public final class Validador {

    /** Formato de fecha usado en los formularios (ej: 01-10-2026). */
    public static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    /** Formato de hora usado en los formularios (ej: 14:30). */
    public static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    /* Letras (con tildes), espacios, punto, apostrofo y guion */
    private static final String PATRON_NOMBRE = "^[\\p{L} .'-]+$";

    /* Letras, numeros, espacios y signos habituales en una direccion */
    private static final String PATRON_DIRECCION = "^[\\p{L}0-9 #.,/\\-]+$";

    private Validador() {
    }

    /**
     * Valida que un campo de texto no este vacio y respete un largo.
     *
     * @return el texto sin espacios sobrantes
     */
    public static String requerido(String valor, String campo, int min, int max)
            throws ValidacionException {
        String texto = valor == null ? "" : valor.trim().replaceAll("\\s+", " ");
        if (texto.isEmpty()) {
            throw new ValidacionException("El campo \"" + campo + "\" es obligatorio.");
        }
        if (texto.length() < min) {
            throw new ValidacionException("El campo \"" + campo + "\" debe tener al menos " + min + " caracteres.");
        }
        if (texto.length() > max) {
            throw new ValidacionException("El campo \"" + campo + "\" admite como maximo " + max + " caracteres.");
        }
        return texto;
    }

    /**
     * Valida el nombre de un repartidor: obligatorio, 3 a 100 caracteres y solo letras.
     */
    public static String nombre(String valor) throws ValidacionException {
        String texto = requerido(valor, "Nombre", 3, 100);
        if (!texto.matches(PATRON_NOMBRE)) {
            throw new ValidacionException("El nombre solo puede contener letras, espacios, puntos o guiones.");
        }
        return texto;
    }

    /**
     * Valida una direccion: obligatoria, 5 a 100 caracteres y al menos una letra.
     */
    public static String direccion(String valor) throws ValidacionException {
        String texto = requerido(valor, "Direccion", 5, 100);
        if (!texto.matches(PATRON_DIRECCION)) {
            throw new ValidacionException("La direccion contiene caracteres no permitidos.\n"
                    + "Use letras, numeros, espacios y los signos # . , / -");
        }
        if (!texto.matches(".*\\p{L}.*")) {
            throw new ValidacionException("La direccion debe incluir el nombre de la calle.");
        }
        return texto;
    }

    /**
     * Valida que se haya elegido una opcion en un JComboBox.
     */
    public static <T> T seleccion(T valor, String campo) throws ValidacionException {
        if (valor == null) {
            throw new ValidacionException("Debe seleccionar un valor en \"" + campo + "\".");
        }
        return valor;
    }

    /**
     * Convierte el texto de fecha (dd-MM-aaaa) validando que sea una fecha real.
     */
    public static LocalDate fecha(String valor) throws ValidacionException {
        String texto = requerido(valor, "Fecha", 1, 10);
        try {
            return LocalDate.parse(texto, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new ValidacionException("La fecha \"" + texto + "\" no es valida.\nUse el formato dd-MM-aaaa (ej: 01-10-2026).");
        }
    }

    /**
     * Convierte el texto de hora (HH:mm) validando que sea una hora real.
     */
    public static LocalTime hora(String valor) throws ValidacionException {
        String texto = requerido(valor, "Hora", 1, 5);
        try {
            return LocalTime.parse(texto, FORMATO_HORA);
        } catch (DateTimeParseException e) {
            throw new ValidacionException("La hora \"" + texto + "\" no es valida.\nUse el formato HH:mm de 24 horas (ej: 14:30).");
        }
    }

    /**
     * Una entrega registra algo que ya ocurrio: no puede quedar en el futuro.
     */
    public static void noFutura(LocalDate fecha, LocalTime hora) throws ValidacionException {
        if (LocalDateTime.of(fecha, hora).isAfter(LocalDateTime.now())) {
            throw new ValidacionException("La fecha y hora de la entrega no pueden ser posteriores al momento actual.");
        }
    }
}
