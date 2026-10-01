package dao;

import modelo.Entrega;
import util.ConexionDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla entregas.
 * Las consultas usan JOIN con pedidos y repartidores para mostrar en la tabla
 * la direccion y el nombre, ademas de los id.
 */
public class EntregaDAO implements CrudDAO<Entrega> {

    private static final String SQL_INSERT =
            "INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT =
            "SELECT e.id, e.id_pedido, e.id_repartidor, e.fecha, e.hora, "
                    + "p.direccion, r.nombre "
                    + "FROM entregas e "
                    + "JOIN pedidos p ON p.id = e.id_pedido "
                    + "JOIN repartidores r ON r.id = e.id_repartidor";

    private static final String SQL_UPDATE =
            "UPDATE entregas SET id_pedido = ?, id_repartidor = ?, fecha = ?, hora = ? WHERE id = ?";

    private static final String SQL_DELETE = "DELETE FROM entregas WHERE id = ?";

    @Override
    public void create(Entrega entrega) throws DAOException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            cargarParametros(ps, entrega);
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    entrega.setId(claves.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (DAOException.esViolacionIntegridad(e)) {
                throw new DAOException("No se pudo registrar la entrega: el pedido o el repartidor ya no existen.\n"
                        + "Actualice las listas e intente nuevamente.", e);
            }
            throw DAOException.desde("registrar la entrega", e);
        }
    }

    @Override
    public List<Entrega> readAll() throws DAOException {
        return readFiltrado(null, null);
    }

    /**
     * Lista las entregas de un pedido.
     */
    public List<Entrega> readByPedido(int idPedido) throws DAOException {
        return readFiltrado(idPedido, null);
    }

    /**
     * Lista las entregas realizadas por un repartidor.
     */
    public List<Entrega> readByRepartidor(int idRepartidor) throws DAOException {
        return readFiltrado(null, idRepartidor);
    }

    /**
     * Lista entregas con filtros opcionales por pedido y/o repartidor.
     *
     * @param idPedido     id del pedido, o null para no filtrar
     * @param idRepartidor id del repartidor, o null para no filtrar
     */
    public List<Entrega> readFiltrado(Integer idPedido, Integer idRepartidor) throws DAOException {
        StringBuilder sql = new StringBuilder(SQL_SELECT).append(" WHERE 1 = 1");
        List<Integer> parametros = new ArrayList<>();
        if (idPedido != null) {
            sql.append(" AND e.id_pedido = ?");
            parametros.add(idPedido);
        }
        if (idRepartidor != null) {
            sql.append(" AND e.id_repartidor = ?");
            parametros.add(idRepartidor);
        }
        sql.append(" ORDER BY e.fecha DESC, e.hora DESC, e.id DESC");

        List<Entrega> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setInt(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw DAOException.desde("listar las entregas", e);
        }
        return lista;
    }

    @Override
    public boolean update(Entrega entrega) throws DAOException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE)) {

            cargarParametros(ps, entrega);
            ps.setInt(5, entrega.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            if (DAOException.esViolacionIntegridad(e)) {
                throw new DAOException("No se pudo actualizar la entrega: el pedido o el repartidor ya no existen.", e);
            }
            throw DAOException.desde("actualizar la entrega", e);
        }
    }

    @Override
    public boolean delete(int id) throws DAOException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.desde("eliminar la entrega", e);
        }
    }

    /**
     * Asigna los 4 primeros parametros comunes a INSERT y UPDATE.
     */
    private void cargarParametros(PreparedStatement ps, Entrega entrega) throws SQLException {
        ps.setInt(1, entrega.getIdPedido());
        ps.setInt(2, entrega.getIdRepartidor());
        ps.setDate(3, Date.valueOf(entrega.getFecha()));
        ps.setTime(4, Time.valueOf(entrega.getHora()));
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Entrega.
     */
    private Entrega mapear(ResultSet rs) throws SQLException {
        Date fecha = rs.getDate("fecha");
        Time hora = rs.getTime("hora");
        Entrega entrega = new Entrega(
                rs.getInt("id"),
                rs.getInt("id_pedido"),
                rs.getInt("id_repartidor"),
                fecha == null ? null : fecha.toLocalDate(),
                hora == null ? null : hora.toLocalTime());
        entrega.setDireccionPedido(rs.getString("direccion"));
        entrega.setNombreRepartidor(rs.getString("nombre"));
        return entrega;
    }
}
