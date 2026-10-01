package dao;

import modelo.Repartidor;
import util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla repartidores.
 * Todas las sentencias usan PreparedStatement y los recursos se cierran con
 * try-with-resources.
 */
public class RepartidorDAO implements CrudDAO<Repartidor> {

    private static final String SQL_INSERT = "INSERT INTO repartidores (nombre) VALUES (?)";
    private static final String SQL_SELECT = "SELECT id, nombre FROM repartidores ORDER BY id";
    private static final String SQL_UPDATE = "UPDATE repartidores SET nombre = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM repartidores WHERE id = ?";

    @Override
    public void create(Repartidor repartidor) throws DAOException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, repartidor.getNombre());
            ps.executeUpdate();

            // Se recupera el id AUTO_INCREMENT asignado por MySQL
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    repartidor.setId(claves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw DAOException.desde("registrar el repartidor", e);
        }
    }

    @Override
    public List<Repartidor> readAll() throws DAOException {
        List<Repartidor> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_SELECT);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Repartidor(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            throw DAOException.desde("listar los repartidores", e);
        }
        return lista;
    }

    @Override
    public boolean update(Repartidor repartidor) throws DAOException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, repartidor.getNombre());
            ps.setInt(2, repartidor.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.desde("actualizar el repartidor", e);
        }
    }

    @Override
    public boolean delete(int id) throws DAOException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            if (DAOException.esViolacionIntegridad(e)) {
                // La clave foranea de entregas impide borrar al repartidor
                throw new DAOException("No se puede eliminar el repartidor porque tiene entregas registradas.\n"
                        + "Elimine o reasigne primero esas entregas.", e);
            }
            throw DAOException.desde("eliminar el repartidor", e);
        }
    }
}
