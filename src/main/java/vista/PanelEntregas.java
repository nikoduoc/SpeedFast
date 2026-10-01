package vista;

import controlador.ControladorEntregas;
import controlador.ControladorPedidos;
import controlador.ControladorRepartidores;
import modelo.Entrega;
import modelo.EstadoPedido;
import modelo.Pedido;
import modelo.Repartidor;
import util.ValidacionException;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulario de gestion de entregas.
 *
 * El pedido y el repartidor se eligen en JComboBox cargados desde la base de
 * datos: el combo muestra "id - direccion" o "id - nombre", pero guarda el
 * objeto completo, por lo que el id se obtiene sin tener que leer el texto.
 */
public class PanelEntregas extends PanelBase {

    private static final String TODOS = "TODOS";

    private final ControladorEntregas controlador = new ControladorEntregas();
    private final ControladorPedidos controladorPedidos = new ControladorPedidos();
    private final ControladorRepartidores controladorRepartidores = new ControladorRepartidores();

    private final JTextField txtId = new JTextField(6);
    private final JComboBox<Pedido> cmbPedido = new JComboBox<>();
    private final JComboBox<Repartidor> cmbRepartidor = new JComboBox<>();
    private final JTextField txtFecha = new JTextField(10);
    private final JTextField txtHora = new JTextField(5);

    /* Filtros del listado (por pedido o por repartidor) */
    private final JComboBox<Object> cmbFiltroPedido = new JComboBox<>();
    private final JComboBox<Object> cmbFiltroRepartidor = new JComboBox<>();
    private final JLabel lblTotal = new JLabel();

    private final DefaultTableModel modeloTabla =
            crearModeloTabla("ID", "Pedido", "Repartidor", "Fecha", "Hora");
    private final JTable tabla = crearTabla(modeloTabla);

    private List<Entrega> entregas = new ArrayList<>();

    /* Evita que los filtros recarguen la tabla mientras se rellenan los combos */
    private boolean cargandoCombos;

    public PanelEntregas() {
        super(new BorderLayout(8, 8));
        construir();
        refrescar();
        limpiar();
    }

    private void construir() {
        txtId.setEditable(false);
        txtId.setToolTipText("Lo asigna la base de datos");
        txtFecha.setToolTipText("Formato dd-MM-aaaa (ej: 01-10-2026)");
        txtHora.setToolTipText("Formato HH:mm de 24 horas (ej: 14:30)");

        JPanel fechaHora = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        fechaHora.add(txtFecha);
        fechaHora.add(new JLabel(" (dd-MM-aaaa)    Hora:"));
        fechaHora.add(txtHora);
        fechaHora.add(new JLabel(" (HH:mm)  "));
        fechaHora.add(crearBoton("Ahora", Colores.NEUTRO, e -> ponerFechaHoraActual()));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnAgregar = crearBoton("Registrar entrega", Colores.AGREGAR, e -> agregar());
        JButton btnEditar = crearBoton("Editar", Colores.EDITAR, e -> editar());
        JButton btnEliminar = crearBoton("Eliminar", Colores.ELIMINAR, e -> eliminar());
        botones.add(btnAgregar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(crearBoton("Limpiar", Colores.NEUTRO, e -> limpiar()));

        JPanel formulario = crearFormulario("Datos de la entrega");
        agregarCampo(formulario, 0, "ID:", txtId);
        agregarCampo(formulario, 1, "Pedido:", cmbPedido);
        agregarCampo(formulario, 2, "Repartidor:", cmbRepartidor);
        agregarCampo(formulario, 3, "Fecha:", fechaHora);
        agregarFilaCompleta(formulario, 4, botones);

        cmbFiltroPedido.addActionListener(e -> {
            if (!cargandoCombos) {
                cargarTabla();
            }
        });
        cmbFiltroRepartidor.addActionListener(e -> {
            if (!cargandoCombos) {
                cargarTabla();
            }
        });

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filtros.add(new JLabel("Filtrar por pedido:"));
        filtros.add(cmbFiltroPedido);
        filtros.add(new JLabel("  repartidor:"));
        filtros.add(cmbFiltroRepartidor);
        filtros.add(crearBoton("Quitar filtros", Colores.NEUTRO, e -> quitarFiltros()));
        filtros.add(lblTotal);

        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(3).setMaxWidth(110);
        tabla.getColumnModel().getColumn(4).setMaxWidth(80);
        enlazarBotonesConSeleccion(tabla, btnAgregar, btnEditar, btnEliminar);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        JPanel listado = new JPanel(new BorderLayout(4, 4));
        listado.add(filtros, BorderLayout.NORTH);
        listado.add(conScroll(tabla, "Entregas registradas"), BorderLayout.CENTER);

        add(formulario, BorderLayout.NORTH);
        add(listado, BorderLayout.CENTER);
    }

    /* =====================================================
     * Acciones de los botones (CRUD)
     * ===================================================== */

    private void agregar() {
        Pedido pedido = (Pedido) cmbPedido.getSelectedItem();
        boolean ok = ejecutar(() -> {
            Entrega nueva = leerFormulario(0);
            controlador.registrar(nueva);
            mostrarExito("Entrega registrada con el ID " + nueva.getId() + ".");
        });
        if (!ok) {
            return;
        }
        ofrecerMarcarEntregado(pedido);
        limpiar();
        notificarCambio();
    }

    private void editar() {
        int id = idSeleccionado(tabla);
        if (id < 0) {
            mostrarAdvertencia("Seleccione en la tabla la entrega que desea editar.");
            return;
        }
        boolean ok = ejecutar(() -> {
            controlador.actualizar(leerFormulario(id));
            mostrarExito("Entrega " + id + " actualizada.");
        });
        if (ok) {
            notificarCambio();
        }
    }

    private void eliminar() {
        int id = idSeleccionado(tabla);
        if (id < 0) {
            mostrarAdvertencia("Seleccione en la tabla la entrega que desea eliminar.");
            return;
        }
        if (!confirmar("Desea eliminar la entrega " + id + "?")) {
            return;
        }
        boolean ok = ejecutar(() -> {
            controlador.eliminar(id);
            mostrarExito("Entrega " + id + " eliminada.");
        });
        if (ok) {
            limpiar();
            notificarCambio();
        }
    }

    /**
     * Valida el formulario y construye la entrega.
     *
     * @param id 0 para una entrega nueva, o el id de la entrega a editar
     */
    private Entrega leerFormulario(int id) throws ValidacionException {
        if (cmbPedido.getItemCount() == 0) {
            throw new ValidacionException("No hay pedidos registrados. Registre un pedido primero.");
        }
        if (cmbRepartidor.getItemCount() == 0) {
            throw new ValidacionException("No hay repartidores registrados. Registre un repartidor primero.");
        }
        Pedido pedido = Validador.seleccion((Pedido) cmbPedido.getSelectedItem(), "Pedido");
        Repartidor repartidor = Validador.seleccion((Repartidor) cmbRepartidor.getSelectedItem(), "Repartidor");
        LocalDate fecha = Validador.fecha(txtFecha.getText());
        LocalTime hora = Validador.hora(txtHora.getText());
        Validador.noFutura(fecha, hora);
        return new Entrega(id, pedido.getId(), repartidor.getId(), fecha, hora);
    }

    /**
     * Si el pedido entregado aun no figura como ENTREGADO, ofrece actualizarlo.
     */
    private void ofrecerMarcarEntregado(Pedido pedido) {
        if (pedido == null || pedido.getEstado() == EstadoPedido.ENTREGADO) {
            return;
        }
        if (confirmar("El pedido " + pedido.getId() + " esta en estado " + pedido.getEstado()
                + ".\nDesea marcarlo como ENTREGADO?")) {
            ejecutar(() -> controladorPedidos.marcarEntregado(pedido.getId()));
        }
    }

    private void ponerFechaHoraActual() {
        txtFecha.setText(LocalDate.now().format(Validador.FORMATO_FECHA));
        txtHora.setText(LocalTime.now().format(Validador.FORMATO_HORA));
    }

    private void limpiar() {
        tabla.clearSelection();
        txtId.setText("");
        cmbPedido.setSelectedIndex(-1);
        cmbRepartidor.setSelectedIndex(-1);
        ponerFechaHoraActual();
    }

    private void quitarFiltros() {
        cargandoCombos = true;
        cmbFiltroPedido.setSelectedIndex(0);
        cmbFiltroRepartidor.setSelectedIndex(0);
        cargandoCombos = false;
        cargarTabla();
    }

    /** Copia la entrega seleccionada al formulario, ubicando los combos por id. */
    private void cargarSeleccion() {
        int id = idSeleccionado(tabla);
        for (Entrega en : entregas) {
            if (en.getId() == id) {
                txtId.setText(String.valueOf(en.getId()));
                seleccionarEnCombo(cmbPedido, p -> p.getId() == en.getIdPedido());
                seleccionarEnCombo(cmbRepartidor, r -> r.getId() == en.getIdRepartidor());
                txtFecha.setText(en.getFecha() == null ? "" : en.getFecha().format(Validador.FORMATO_FECHA));
                txtHora.setText(en.getHora() == null ? "" : en.getHora().format(Validador.FORMATO_HORA));
                return;
            }
        }
    }

    /* =====================================================
     * Carga de datos
     * ===================================================== */

    /**
     * Recarga los JComboBox desde la base de datos (conservando lo elegido)
     * y luego la tabla. Se llama cada vez que cambia cualquier entidad.
     */
    @Override
    public void refrescar() {
        ejecutar(() -> {
            List<Pedido> pedidos = controladorPedidos.listar();
            List<Repartidor> repartidores = controladorRepartidores.listar();
            cargandoCombos = true;
            try {
                recargarCombo(cmbPedido, pedidos);
                recargarCombo(cmbRepartidor, repartidores);
                recargarFiltro(cmbFiltroPedido, pedidos);
                recargarFiltro(cmbFiltroRepartidor, repartidores);
            } finally {
                cargandoCombos = false;
            }
        });
        cargarTabla();
    }

    /** Rellena un combo del formulario manteniendo seleccionado el mismo id. */
    private <T> void recargarCombo(JComboBox<T> combo, List<T> datos) {
        Object anterior = combo.getSelectedItem();
        combo.removeAllItems();
        for (T dato : datos) {
            combo.addItem(dato);
        }
        if (anterior == null) {
            combo.setSelectedIndex(-1);
        } else {
            seleccionarEnCombo(combo, d -> mismoId(d, anterior));
        }
    }

    /** Rellena un combo de filtro: primero "TODOS" y luego los objetos. */
    private void recargarFiltro(JComboBox<Object> combo, List<?> datos) {
        Object anterior = combo.getSelectedItem();
        combo.removeAllItems();
        combo.addItem(TODOS);
        for (Object dato : datos) {
            combo.addItem(dato);
        }
        combo.setSelectedIndex(0);
        if (anterior != null && !TODOS.equals(anterior)) {
            seleccionarEnCombo(combo, d -> mismoId(d, anterior));
            if (combo.getSelectedIndex() < 0) {
                combo.setSelectedIndex(0);
            }
        }
    }

    /** Compara dos pedidos o dos repartidores por su id. */
    private static boolean mismoId(Object a, Object b) {
        if (a instanceof Pedido && b instanceof Pedido) {
            return ((Pedido) a).getId() == ((Pedido) b).getId();
        }
        if (a instanceof Repartidor && b instanceof Repartidor) {
            return ((Repartidor) a).getId() == ((Repartidor) b).getId();
        }
        return false;
    }

    /** Carga la tabla aplicando los filtros elegidos. */
    private void cargarTabla() {
        int seleccionado = idSeleccionado(tabla);
        Object filtroPedido = cmbFiltroPedido.getSelectedItem();
        Object filtroRepartidor = cmbFiltroRepartidor.getSelectedItem();
        ejecutar(() -> {
            entregas = controlador.listar(
                    filtroPedido instanceof Pedido ? ((Pedido) filtroPedido).getId() : null,
                    filtroRepartidor instanceof Repartidor ? ((Repartidor) filtroRepartidor).getId() : null);
            modeloTabla.setRowCount(0);
            for (Entrega en : entregas) {
                modeloTabla.addRow(new Object[]{
                        en.getId(),
                        en.getIdPedido() + " - " + en.getDireccionPedido(),
                        en.getIdRepartidor() + " - " + en.getNombreRepartidor(),
                        en.getFecha() == null ? "" : en.getFecha().format(Validador.FORMATO_FECHA),
                        en.getHora() == null ? "" : en.getHora().format(Validador.FORMATO_HORA)});
            }
            lblTotal.setText("   " + entregas.size() + " entrega(s)");
        });
        reseleccionar(tabla, seleccionado);
    }
}
