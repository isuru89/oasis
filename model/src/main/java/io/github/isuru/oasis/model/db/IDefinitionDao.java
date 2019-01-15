package io.github.isuru.oasis.model.db;

import io.github.isuru.oasis.model.defs.DefWrapper;

import java.util.List;

/**
 * @author iweerarathna
 */
public interface IDefinitionDao {

    DefWrapper readDefinition(long id) throws DbException;

    List<DefWrapper> listDefinitions(int kind) throws DbException;
    List<DefWrapper> listDefinitionsOfGame(long gameId, int kind) throws DbException;

    long addDefinition(DefWrapper wrapper) throws DbException;

    boolean disableDefinition(long id) throws DbException;

    long editDefinition(long id, DefWrapper wrapper) throws DbException;

}
