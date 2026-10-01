package dao;

import java.util.List;

/**
 * Contrato comun de los DAO del sistema: las cuatro operaciones CRUD.
 * Cada entidad (Repartidor, Pedido, Entrega) tiene su propia implementacion.
 *
 * @param <T> entidad del modelo que administra el DAO
 */
public interface CrudDAO<T> {

    /**
     * Inserta la entidad (INSERT). Al terminar, el objeto queda con el id generado.
     */
    void create(T entidad) throws DAOException;

    /**
     * Obtiene todos los registros de la tabla (SELECT).
     */
    List<T> readAll() throws DAOException;

    /**
     * Actualiza un registro existente segun su id (UPDATE).
     *
     * @return true si se modifico una fila, false si el id ya no existe
     */
    boolean update(T entidad) throws DAOException;

    /**
     * Elimina un registro segun su id (DELETE).
     *
     * @return true si se elimino una fila, false si el id ya no existe
     */
    boolean delete(int id) throws DAOException;
}
