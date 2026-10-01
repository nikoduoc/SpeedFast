package controlador;

import dao.DAOException;
import dao.EntregaDAO;
import modelo.Entrega;

import java.util.List;

/**
 * Coordina las acciones de la vista de entregas con EntregaDAO.
 */
public class ControladorEntregas {

    private final EntregaDAO dao = new EntregaDAO();

    public void registrar(Entrega entrega) throws DAOException {
        dao.create(entrega);
    }

    public List<Entrega> listar() throws DAOException {
        return dao.readAll();
    }

    /**
     * Lista entregas filtrando por pedido y/o repartidor (null = sin filtro).
     */
    public List<Entrega> listar(Integer idPedido, Integer idRepartidor) throws DAOException {
        return dao.readFiltrado(idPedido, idRepartidor);
    }

    public void actualizar(Entrega entrega) throws DAOException {
        if (!dao.update(entrega)) {
            throw registroInexistente(entrega.getId());
        }
    }

    public void eliminar(int id) throws DAOException {
        if (!dao.delete(id)) {
            throw registroInexistente(id);
        }
    }

    private DAOException registroInexistente(int id) {
        return new DAOException("La entrega " + id + " ya no existe en la base de datos.\n"
                + "Puede haber sido eliminada desde otra sesion.", null);
    }
}
