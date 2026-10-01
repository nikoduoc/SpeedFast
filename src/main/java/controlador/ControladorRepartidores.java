package controlador;

import dao.DAOException;
import dao.RepartidorDAO;
import modelo.Repartidor;

import java.util.List;

/**
 * Coordina las acciones de la vista de repartidores con su DAO.
 * La vista ya entrega datos validados; el controlador delega en el DAO y
 * convierte los casos sin efecto (id inexistente) en mensajes claros.
 */
public class ControladorRepartidores {

    private final RepartidorDAO dao = new RepartidorDAO();

    public void registrar(Repartidor repartidor) throws DAOException {
        dao.create(repartidor);
    }

    public List<Repartidor> listar() throws DAOException {
        return dao.readAll();
    }

    public void actualizar(Repartidor repartidor) throws DAOException {
        if (!dao.update(repartidor)) {
            throw registroInexistente(repartidor.getId());
        }
    }

    public void eliminar(int id) throws DAOException {
        if (!dao.delete(id)) {
            throw registroInexistente(id);
        }
    }

    private DAOException registroInexistente(int id) {
        return new DAOException("El repartidor " + id + " ya no existe en la base de datos.\n"
                + "Puede haber sido eliminado desde otra sesion.", null);
    }
}
