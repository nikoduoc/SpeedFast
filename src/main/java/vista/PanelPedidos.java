package vista;

import controlador.ControladorPedidos;
import modelo.EstadoPedido;
import modelo.Pedido;
import modelo.TipoPedido;
import util.Validador;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulario de gestion de pedidos: registrar, editar, eliminar y listar con
 * filtros opcionales por estado y por tipo.
 */
public class PanelPedidos extends PanelBase {

    private static final String TODOS = "TODOS";

    private final ControladorPedidos controlador = new ControladorPedidos();

    private final JTextField txtId = new JTextField(6);
    private final JTextField txtDireccion = new JTextField(28);
    private final JComboBox<TipoPedido> cmbTipo = new JComboBox<>(TipoPedido.values());
    private final JComboBox<EstadoPedido> cmbEstado = new JComboBox<>(EstadoPedido.values());

    /* Filtros del listado */
    private final JComboBox<Object> cmbFiltroEstado = new JComboBox<>();
    private final JComboBox<Object> cmbFiltroTipo = new JComboBox<>();
    private final JLabel lblTotal = new JLabel();

    private final DefaultTableModel modeloTabla = crearModeloTabla("ID", "Direccion", "Tipo", "Estado");
    private final JTable tabla = crearTabla(modeloTabla);

    private List<Pedido> pedidos = new ArrayList<>();

    public PanelPedidos() {
        super(new BorderLayout(8, 8));
        construir();
        refrescar();
    }

    private void construir() {
        txtId.setEditable(false);
        txtId.setToolTipText("Lo asigna la base de datos");
        txtDireccion.setToolTipText("Entre 5 y 100 caracteres (ej: Maipu 88, Los Andes)");
        cmbTipo.setSelectedIndex(-1);
        cmbEstado.setSelectedItem(EstadoPedido.PENDIENTE);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnAgregar = crearBoton("Agregar", Colores.AGREGAR, e -> agregar());
        JButton btnEditar = crearBoton("Editar", Colores.EDITAR, e -> editar());
        JButton btnEliminar = crearBoton("Eliminar", Colores.ELIMINAR, e -> eliminar());
        botones.add(btnAgregar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(crearBoton("Limpiar", Colores.NEUTRO, e -> limpiar()));

        JPanel formulario = crearFormulario("Datos del pedido");
        agregarCampo(formulario, 0, "ID:", txtId);
        agregarCampo(formulario, 1, "Direccion:", txtDireccion);
        agregarCampo(formulario, 2, "Tipo:", cmbTipo);
        agregarCampo(formulario, 3, "Estado:", cmbEstado);
        agregarFilaCompleta(formulario, 4, botones);

        /* Filtros opcionales: "TODOS" equivale a no filtrar */
        cmbFiltroEstado.addItem(TODOS);
        for (EstadoPedido estado : EstadoPedido.values()) {
            cmbFiltroEstado.addItem(estado);
        }
        cmbFiltroTipo.addItem(TODOS);
        for (TipoPedido tipo : TipoPedido.values()) {
            cmbFiltroTipo.addItem(tipo);
        }
        cmbFiltroEstado.addActionListener(e -> refrescar());
        cmbFiltroTipo.addActionListener(e -> refrescar());

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filtros.add(new JLabel("Filtrar por estado:"));
        filtros.add(cmbFiltroEstado);
        filtros.add(new JLabel("  tipo:"));
        filtros.add(cmbFiltroTipo);
        filtros.add(crearBoton("Quitar filtros", Colores.NEUTRO, e -> quitarFiltros()));
        filtros.add(lblTotal);

        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        enlazarBotonesConSeleccion(tabla, btnAgregar, btnEditar, btnEliminar);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        JPanel listado = new JPanel(new BorderLayout(4, 4));
        listado.add(filtros, BorderLayout.NORTH);
        listado.add(conScroll(tabla, "Pedidos"), BorderLayout.CENTER);

        add(formulario, BorderLayout.NORTH);
        add(listado, BorderLayout.CENTER);
    }

    /* =====================================================
     * Acciones de los botones (CRUD)
     * ===================================================== */

    private void agregar() {
        boolean ok = ejecutar(() -> {
            Pedido nuevo = new Pedido(
                    Validador.direccion(txtDireccion.getText()),
                    Validador.seleccion((TipoPedido) cmbTipo.getSelectedItem(), "Tipo"),
                    Validador.seleccion((EstadoPedido) cmbEstado.getSelectedItem(), "Estado"));
            controlador.registrar(nuevo);
            mostrarExito("Pedido registrado con el ID " + nuevo.getId() + ".");
        });
        if (ok) {
            limpiar();
            notificarCambio();
        }
    }

    private void editar() {
        int id = idSeleccionado(tabla);
        if (id < 0) {
            mostrarAdvertencia("Seleccione en la tabla el pedido que desea editar.");
            return;
        }
        boolean ok = ejecutar(() -> {
            Pedido pedido = new Pedido(id,
                    Validador.direccion(txtDireccion.getText()),
                    Validador.seleccion((TipoPedido) cmbTipo.getSelectedItem(), "Tipo"),
                    Validador.seleccion((EstadoPedido) cmbEstado.getSelectedItem(), "Estado"));
            controlador.actualizar(pedido);
            mostrarExito("Pedido " + id + " actualizado.");
        });
        if (ok) {
            notificarCambio();
        }
    }

    private void eliminar() {
        int id = idSeleccionado(tabla);
        if (id < 0) {
            mostrarAdvertencia("Seleccione en la tabla el pedido que desea eliminar.");
            return;
        }
        if (!confirmar("Desea eliminar el pedido " + id + " - " + txtDireccion.getText() + "?")) {
            return;
        }
        boolean ok = ejecutar(() -> {
            controlador.eliminar(id);
            mostrarExito("Pedido " + id + " eliminado.");
        });
        if (ok) {
            limpiar();
            notificarCambio();
        }
    }

    private void limpiar() {
        tabla.clearSelection();
        txtId.setText("");
        txtDireccion.setText("");
        cmbTipo.setSelectedIndex(-1);
        cmbEstado.setSelectedItem(EstadoPedido.PENDIENTE);
        txtDireccion.requestFocusInWindow();
    }

    private void quitarFiltros() {
        cmbFiltroEstado.setSelectedIndex(0);
        cmbFiltroTipo.setSelectedIndex(0);
    }

    private void cargarSeleccion() {
        int id = idSeleccionado(tabla);
        for (Pedido p : pedidos) {
            if (p.getId() == id) {
                txtId.setText(String.valueOf(p.getId()));
                txtDireccion.setText(p.getDireccion());
                cmbTipo.setSelectedItem(p.getTipo());
                cmbEstado.setSelectedItem(p.getEstado());
                return;
            }
        }
    }

    @Override
    public void refrescar() {
        int seleccionado = idSeleccionado(tabla);
        Object estado = cmbFiltroEstado.getSelectedItem();
        Object tipo = cmbFiltroTipo.getSelectedItem();
        ejecutar(() -> {
            pedidos = controlador.listar(
                    estado instanceof EstadoPedido ? (EstadoPedido) estado : null,
                    tipo instanceof TipoPedido ? (TipoPedido) tipo : null);
            modeloTabla.setRowCount(0);
            for (Pedido p : pedidos) {
                modeloTabla.addRow(new Object[]{p.getId(), p.getDireccion(), p.getTipo(), p.getEstado()});
            }
            lblTotal.setText("   " + pedidos.size() + " pedido(s)");
        });
        reseleccionar(tabla, seleccionado);
    }
}
