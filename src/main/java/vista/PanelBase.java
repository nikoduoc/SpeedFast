package vista;

import dao.DAOException;
import util.ValidacionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.util.function.Predicate;

/**
 * Base comun de los paneles de gestion (repartidores, pedidos y entregas).
 *
 * Reune lo que se repetiria en cada formulario: creacion de tablas no
 * editables, botones, filas de formulario, mensajes con JOptionPane y el
 * manejo centralizado de errores de validacion y de base de datos.
 */
public abstract class PanelBase extends JPanel {

    /** Accion de la vista que puede fallar por validacion o por base de datos. */
    @FunctionalInterface
    protected interface AccionCrud {
        void ejecutar() throws ValidacionException, DAOException;
    }

    /* Aviso a la ventana principal cuando cambian los datos */
    private Runnable alCambiarDatos = () -> { };

    /**
     * @param layout administrador de diseno del panel
     */
    protected PanelBase(LayoutManager layout) {
        super(layout);
    }

    /**
     * Recarga la tabla y los combos del panel desde la base de datos.
     */
    public abstract void refrescar();

    /**
     * La ventana principal registra aqui que hacer cuando cambian los datos,
     * para refrescar los JComboBox de los otros paneles.
     */
    public void setAlCambiarDatos(Runnable alCambiarDatos) {
        this.alCambiarDatos = alCambiarDatos;
    }

    /** Informa que se creo, edito o elimino un registro. */
    protected void notificarCambio() {
        alCambiarDatos.run();
    }

    /* =====================================================
     * Ejecucion de acciones con manejo de errores
     * ===================================================== */

    /**
     * Ejecuta una accion mostrando un mensaje claro si algo falla.
     *
     * @return true si la accion termino sin errores
     */
    protected boolean ejecutar(AccionCrud accion) {
        try {
            accion.ejecutar();
            return true;
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Datos no validos", JOptionPane.WARNING_MESSAGE);
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            System.err.println("[Vista] Error inesperado: " + e);
            JOptionPane.showMessageDialog(this, "Ocurrio un error inesperado: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    protected void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Operacion exitosa", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Atencion", JOptionPane.WARNING_MESSAGE);
    }

    protected boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(this, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /* =====================================================
     * Construccion de componentes
     * ===================================================== */

    /**
     * Crea el modelo de una tabla de solo lectura con las columnas indicadas.
     */
    protected DefaultTableModel crearModeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    /**
     * Crea un JTable de seleccion simple para el modelo dado.
     */
    protected JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.setAutoCreateRowSorter(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));
        return tabla;
    }

    /** Envuelve un componente en un panel con scroll y titulo. */
    protected JScrollPane conScroll(JComponent componente, String titulo) {
        JScrollPane scroll = new JScrollPane(componente);
        scroll.setBorder(BorderFactory.createTitledBorder(titulo));
        return scroll;
    }

    protected JButton crearBoton(String texto, Color color, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setForeground(color);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD));
        boton.addActionListener(accion);
        return boton;
    }

    /** Panel con GridBagLayout para ordenar etiquetas y campos. */
    protected JPanel crearFormulario(String titulo) {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titulo),
                BorderFactory.createEmptyBorder(4, 8, 8, 8)));
        return formulario;
    }

    /**
     * Agrega una fila "Etiqueta: campo" al formulario.
     */
    protected void agregarCampo(JPanel formulario, int fila, String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 4, 4, 8);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        formulario.add(new JLabel(etiqueta), c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(campo, c);
    }

    /**
     * Agrega una fila que ocupa todo el ancho (por ejemplo, la barra de botones).
     */
    protected void agregarFilaCompleta(JPanel formulario, int fila, JComponent componente) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(10, 4, 0, 4);
        c.anchor = GridBagConstraints.WEST;
        formulario.add(componente, c);
    }

    /**
     * Selecciona en un combo el primer elemento que cumple la condicion.
     * Se usa para ubicar un objeto por su id (el id se conserva dentro del objeto).
     */
    protected <T> void seleccionarEnCombo(JComboBox<T> combo, Predicate<T> condicion) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (condicion.test(combo.getItemAt(i))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(-1);
    }

    /** Lee el id (columna 0) de la fila seleccionada, considerando el ordenamiento. */
    protected int idSeleccionado(JTable tabla) {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return -1;
        }
        int fila = tabla.convertRowIndexToModel(filaVista);
        return (Integer) tabla.getModel().getValueAt(fila, 0);
    }

    /** Vuelve a seleccionar la fila con el id dado despues de recargar la tabla. */
    protected void reseleccionar(JTable tabla, int id) {
        if (id < 0) {
            return;
        }
        for (int i = 0; i < tabla.getRowCount(); i++) {
            if ((Integer) tabla.getValueAt(i, 0) == id) {
                tabla.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    /**
     * Habilita los botones segun haya o no una fila seleccionada:
     * sin seleccion solo se puede agregar; con seleccion solo se puede editar o
     * eliminar. Asi se evita crear un registro nuevo cuando la intencion era editar.
     */
    protected void enlazarBotonesConSeleccion(JTable tabla, JButton agregar, JButton... requierenSeleccion) {
        agregar.setToolTipText("Para registrar uno nuevo, primero presione Limpiar");
        Runnable actualizar = () -> {
            boolean haySeleccion = tabla.getSelectedRow() >= 0;
            agregar.setEnabled(!haySeleccion);
            for (JButton boton : requierenSeleccion) {
                boton.setEnabled(haySeleccion);
            }
        };
        tabla.getSelectionModel().addListSelectionListener(e -> actualizar.run());
        actualizar.run();
    }
}
