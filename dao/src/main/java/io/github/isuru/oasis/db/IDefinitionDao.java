package io.github.isuru.oasis.db;

import io.github.isuru.oasis.model.defs.DefWrapper;

import java.util.List;

/**
 * @author iweerarathna
 */
public interface IDefinitionDao {

    DefWrapper readDefinition(long id) throws Exception;

    List<DefWrapper> listDefinitions(int kind) throws Exception;

    void addDefinition(DefWrapper wrapper) throws Exception;

    boolean disableDefinition(long id) throws Exception;

    void editDefinition(long id, DefWrapper wrapper) throws Exception;

}
