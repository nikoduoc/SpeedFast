package vista;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Ventana principal de SpeedFast.
 *
 * Agrupa los tres formularios en pestanas. Cuando un panel modifica datos,
 * la ventana refresca todos los paneles para que tablas y JComboBox queden
 * sincronizados con la base de datos.
 */
public class VentanaPrincipal extends JFrame {

    private final PanelRepartidores panelRepartidores = new PanelRepartidores();
    private final PanelPedidos panelPedidos = new PanelPedidos();
    private final PanelEntregas panelEntregas = new PanelEntregas();
    private final JTabbedPane pestanas = new JTabbedPane();

    public VentanaPrincipal() {
        super("SpeedFast - Gestion de pedidos y entregas");
        construir();
    }

    private void construir() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(860, 620));
        setLayout(new BorderLayout());

        add(crearEncabezado(), BorderLayout.NORTH);

        pestanas.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pestanas.addTab("Repartidores", panelRepartidores);
        pestanas.addTab("Pedidos", panelPedidos);
        pestanas.addTab("Entregas", panelEntregas);
        add(pestanas, BorderLayout.CENTER);

        // Cualquier cambio en un panel actualiza los demas (combos y tablas)
        Runnable refrescarTodo = this::refrescarTodo;
        panelRepartidores.setAlCambiarDatos(refrescarTodo);
        panelPedidos.setAlCambiarDatos(refrescarTodo);
        panelEntregas.setAlCambiarDatos(refrescarTodo);

        // Al cambiar de pestana se leen los datos mas recientes
        pestanas.addChangeListener(e -> {
            PanelBase actual = (PanelBase) pestanas.getSelectedComponent();
            if (actual != null) {
                actual.refrescar();
            }
        });

        pack();
        setSize(960, 680);
        setLocationRelativeTo(null);
    }

    private JPanel crearEncabezado() {
        JLabel titulo = new JLabel("SpeedFast");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 24f));
        titulo.setForeground(Colores.ACENTO);

        JLabel subtitulo = new JLabel("Gestion de repartidores, pedidos y entregas - base de datos speedfast_db");
        subtitulo.setForeground(Color.WHITE);

        JPanel encabezado = new JPanel(new GridLayout(2, 1));
        encabezado.setBackground(Colores.ENCABEZADO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        encabezado.add(titulo);
        encabezado.add(subtitulo);
        return encabezado;
    }

    /** Recarga los datos de los tres paneles. */
    private void refrescarTodo() {
        panelRepartidores.refrescar();
        panelPedidos.refrescar();
        panelEntregas.refrescar();
    }
}
