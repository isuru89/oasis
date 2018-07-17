package io.github.isuru.oasis.db.jdbi;

import io.github.isuru.oasis.db.IDefinitionDao;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.defs.DefWrapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class JdbiDefinitionDao implements IDefinitionDao {

    private static final String DEF_READ_DEFINITION = "def/readDefinition";
    private static final String DEF_DISABLE_DEFINITION = "def/disableDefinition";
    private static final String DEF_LIST_DEFINITIONS = "def/listDefinitions";
    private static final String DEF_ADD_DEFINITION = "def/addDefinition";

    private final IOasisDao dao;

    JdbiDefinitionDao(IOasisDao dao) {
        this.dao = dao;
    }

    @Override
    public DefWrapper readDefinition(long id) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);

        Iterable<DefWrapper> defWrappers = dao.executeQuery(DEF_READ_DEFINITION, data, DefWrapper.class);
        Iterator<DefWrapper> iterator = defWrappers.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    @Override
    public List<DefWrapper> listDefinitions(int kind) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("type", kind);

        Iterable<DefWrapper> defWrappers = dao.executeQuery(DEF_LIST_DEFINITIONS, data, DefWrapper.class);
        List<DefWrapper> wrappers = new LinkedList<>();
        for (DefWrapper wrapper : defWrappers) {
            wrappers.add(wrapper);
        }
        return wrappers;
    }

    @Override
    public void addDefinition(DefWrapper wrapper) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("typeId", wrapper.getKind());
        data.put("name", wrapper.getName());
        data.put("displayName", wrapper.getDisplayName());
        data.put("content", wrapper.getContent());
        data.put("gameId", wrapper.getGameId());
        data.put("parentId", wrapper.getParentId());

        if (dao.executeCommand(DEF_ADD_DEFINITION, data) < 1) {
            throw new Exception("Unable to add definition to the game!");
        }
    }

    @Override
    public boolean disableDefinition(long id) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return dao.executeCommand(DEF_DISABLE_DEFINITION, data) > 0;
    }

    @Override
    public void editDefinition(long id, DefWrapper wrapper) throws Exception {

    }
}
