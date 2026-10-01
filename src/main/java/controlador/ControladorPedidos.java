package controlador;

import dao.DAOException;
import dao.PedidoDAO;
import modelo.EstadoPedido;
import modelo.Pedido;
import modelo.TipoPedido;

import java.util.List;

/**
 * Coordina las acciones de la vista de pedidos con PedidoDAO.
 */
public class ControladorPedidos {

    private final PedidoDAO dao = new PedidoDAO();

    public void registrar(Pedido pedido) throws DAOException {
        dao.create(pedido);
    }

    public List<Pedido> listar() throws DAOException {
        return dao.readAll();
    }

    /**
     * Lista pedidos con filtros opcionales (null = sin filtro).
     */
    public List<Pedido> listar(EstadoPedido estado, TipoPedido tipo) throws DAOException {
        return dao.readByFiltro(estado, tipo);
    }

    public void actualizar(Pedido pedido) throws DAOException {
        if (!dao.update(pedido)) {
            throw registroInexistente(pedido.getId());
        }
    }

    /**
     * Marca un pedido como ENTREGADO (se ofrece al registrar su entrega).
     */
    public void marcarEntregado(int idPedido) throws DAOException {
        if (!dao.updateEstado(idPedido, EstadoPedido.ENTREGADO)) {
            throw registroInexistente(idPedido);
        }
    }

    public void eliminar(int id) throws DAOException {
        if (!dao.delete(id)) {
            throw registroInexistente(id);
        }
    }

    private DAOException registroInexistente(int id) {
        return new DAOException("El pedido " + id + " ya no existe en la base de datos.\n"
                + "Puede haber sido eliminado desde otra sesion.", null);
    }
}
