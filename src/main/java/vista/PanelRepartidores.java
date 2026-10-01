package vista;

import controlador.ControladorRepartidores;
import modelo.Repartidor;
import util.Validador;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulario de gestion de repartidores: registrar, editar, eliminar y listar.
 */
public class PanelRepartidores extends PanelBase {

    private final ControladorRepartidores controlador = new ControladorRepartidores();

    private final JTextField txtId = new JTextField(6);
    private final JTextField txtNombre = new JTextField(28);

    private final DefaultTableModel modeloTabla = crearModeloTabla("ID", "Nombre");
    private final JTable tabla = crearTabla(modeloTabla);

    /* Copia de lo mostrado en la tabla, para recuperar el objeto seleccionado */
    private List<Repartidor> repartidores = new ArrayList<>();

    public PanelRepartidores() {
        super(new BorderLayout(8, 8));
        construir();
        refrescar();
    }

    private void construir() {
        txtId.setEditable(false);
        txtId.setToolTipText("Lo asigna la base de datos");
        txtNombre.setToolTipText("Entre 3 y 100 caracteres, solo letras");
        txtNombre.addActionListener(e -> {
            if (idSeleccionado(tabla) >= 0) {
                editar();
            } else {
                agregar();
            }
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnAgregar = crearBoton("Agregar", Colores.AGREGAR, e -> agregar());
        JButton btnEditar = crearBoton("Editar", Colores.EDITAR, e -> editar());
        JButton btnEliminar = crearBoton("Eliminar", Colores.ELIMINAR, e -> eliminar());
        botones.add(btnAgregar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(crearBoton("Limpiar", Colores.NEUTRO, e -> limpiar()));

        JPanel formulario = crearFormulario("Datos del repartidor");
        agregarCampo(formulario, 0, "ID:", txtId);
        agregarCampo(formulario, 1, "Nombre:", txtNombre);
        agregarFilaCompleta(formulario, 2, botones);

        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        enlazarBotonesConSeleccion(tabla, btnAgregar, btnEditar, btnEliminar);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        add(formulario, BorderLayout.NORTH);
        add(conScroll(tabla, "Repartidores registrados"), BorderLayout.CENTER);
    }

    /* =====================================================
     * Acciones de los botones (CRUD)
     * ===================================================== */

    private void agregar() {
        boolean ok = ejecutar(() -> {
            String nombre = Validador.nombre(txtNombre.getText());
            Repartidor nuevo = new Repartidor(nombre);
            controlador.registrar(nuevo);
            mostrarExito("Repartidor registrado con el ID " + nuevo.getId() + ".");
        });
        if (ok) {
            limpiar();
            notificarCambio();
        }
    }

    private void editar() {
        int id = idSeleccionado(tabla);
        if (id < 0) {
            mostrarAdvertencia("Seleccione en la tabla el repartidor que desea editar.");
            return;
        }
        boolean ok = ejecutar(() -> {
            String nombre = Validador.nombre(txtNombre.getText());
            controlador.actualizar(new Repartidor(id, nombre));
            mostrarExito("Repartidor " + id + " actualizado.");
        });
        if (ok) {
            notificarCambio();
        }
    }

    private void eliminar() {
        int id = idSeleccionado(tabla);
        if (id < 0) {
            mostrarAdvertencia("Seleccione en la tabla el repartidor que desea eliminar.");
            return;
        }
        if (!confirmar("Desea eliminar al repartidor " + id + " - " + txtNombre.getText() + "?")) {
            return;
        }
        boolean ok = ejecutar(() -> {
            controlador.eliminar(id);
            mostrarExito("Repartidor " + id + " eliminado.");
        });
        if (ok) {
            limpiar();
            notificarCambio();
        }
    }

    private void limpiar() {
        tabla.clearSelection();
        txtId.setText("");
        txtNombre.setText("");
        txtNombre.requestFocusInWindow();
    }

    /** Copia los datos de la fila seleccionada al formulario. */
    private void cargarSeleccion() {
        int id = idSeleccionado(tabla);
        for (Repartidor r : repartidores) {
            if (r.getId() == id) {
                txtId.setText(String.valueOf(r.getId()));
                txtNombre.setText(r.getNombre());
                return;
            }
        }
    }

    @Override
    public void refrescar() {
        int seleccionado = idSeleccionado(tabla);
        ejecutar(() -> {
            repartidores = controlador.listar();
            modeloTabla.setRowCount(0);
            for (Repartidor r : repartidores) {
                modeloTabla.addRow(new Object[]{r.getId(), r.getNombre()});
            }
        });
        reseleccionar(tabla, seleccionado);
    }
}
