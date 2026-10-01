package dao;

import modelo.EstadoPedido;
import modelo.Pedido;
import modelo.TipoPedido;
import util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla pedidos.
 * Ademas del CRUD permite listar con filtros opcionales por estado y tipo.
 */
public class PedidoDAO implements CrudDAO<Pedido> {

    private static final String SQL_INSERT = "INSERT INTO pedidos (direccion, tipo, estado) VALUES (?, ?, ?)";
    private static final String SQL_SELECT = "SELECT id, direccion, tipo, estado FROM pedidos";
    private static final String SQL_UPDATE = "UPDATE pedidos SET direccion = ?, tipo = ?, estado = ? WHERE id = ?";
    private static final String SQL_UPDATE_ESTADO = "UPDATE pedidos SET estado = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM pedidos WHERE id = ?";

    @Override
    public void create(Pedido pedido) throws DAOException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, pedido.getDireccion());
            ps.setString(2, pedido.getTipo().name());
            ps.setString(3, pedido.getEstado().name());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    pedido.setId(claves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw DAOException.desde("registrar el pedido", e);
        }
    }

    @Override
    public List<Pedido> readAll() throws DAOException {
        return readByFiltro(null, null);
    }

    /**
     * Lista pedidos aplicando filtros opcionales.
     * Los valores siempre viajan como parametros (?), nunca concatenados.
     *
     * @param estado estado a filtrar, o null para no filtrar
     * @param tipo   tipo a filtrar, o null para no filtrar
     */
    public List<Pedido> readByFiltro(EstadoPedido estado, TipoPedido tipo) throws DAOException {
        StringBuilder sql = new StringBuilder(SQL_SELECT).append(" WHERE 1 = 1");
        List<String> parametros = new ArrayList<>();
        if (estado != null) {
            sql.append(" AND estado = ?");
            parametros.add(estado.name());
        }
        if (tipo != null) {
            sql.append(" AND tipo = ?");
            parametros.add(tipo.name());
        }
        sql.append(" ORDER BY id");

        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setString(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw DAOException.desde("listar los pedidos", e);
        }
        return lista;
    }

    @Override
    public boolean update(Pedido pedido) throws DAOException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, pedido.getDireccion());
            ps.setString(2, pedido.getTipo().name());
            ps.setString(3, pedido.getEstado().name());
            ps.setInt(4, pedido.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.desde("actualizar el pedido", e);
        }
    }

    /**
     * Cambia solo el estado de un pedido (se usa al registrar una entrega).
     */
    public boolean updateEstado(int idPedido, EstadoPedido estado) throws DAOException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE_ESTADO)) {

            ps.setString(1, estado.name());
            ps.setInt(2, idPedido);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.desde("cambiar el estado del pedido", e);
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
                throw new DAOException("No se puede eliminar el pedido porque tiene una entrega registrada.\n"
                        + "Elimine primero la entrega asociada.", e);
            }
            throw DAOException.desde("eliminar el pedido", e);
        }
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Pedido.
     */
    private Pedido mapear(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo");
        String estado = rs.getString("estado");
        return new Pedido(
                rs.getInt("id"),
                rs.getString("direccion"),
                tipo == null ? null : TipoPedido.valueOf(tipo),
                estado == null ? null : EstadoPedido.valueOf(estado));
    }
}
