package io.github.isuru.oasis.db.jdbi;

import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.db.IDefinitionDao;
import io.github.isuru.oasis.model.db.IOasisDao;
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
    private static final String DEF_EDIT_DEFINITION = "def/editDefinition";
    private static final String DEF_LIST_DEFINITIONS = "def/listDefinitions";
    private static final String DEF_LIST_GAME_DEFINITIONS = "def/listDefinitionsGame";
    private static final String DEF_ADD_DEFINITION = "def/addDefinition";

    private final IOasisDao dao;

    JdbiDefinitionDao(IOasisDao dao) {
        this.dao = dao;
    }

    @Override
    public DefWrapper readDefinition(long id) throws DbException {
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
    public List<DefWrapper> listDefinitions(int kind) throws DbException {
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
    public List<DefWrapper> listDefinitionsOfGame(long gameId, int kind) throws DbException {
        Map<String, Object> data = new HashMap<>();
        data.put("type", kind);
        data.put("gameId", gameId);

        Iterable<DefWrapper> defWrappers = dao.executeQuery(DEF_LIST_GAME_DEFINITIONS, data, DefWrapper.class);
        List<DefWrapper> wrappers = new LinkedList<>();
        for (DefWrapper wrapper : defWrappers) {
            wrappers.add(wrapper);
        }
        return wrappers;
    }

    @Override
    public long addDefinition(DefWrapper wrapper) throws DbException {
        Map<String, Object> data = new HashMap<>();
        data.put("typeId", wrapper.getKind());
        data.put("name", wrapper.getName());
        data.put("displayName", wrapper.getDisplayName());
        data.put("content", wrapper.getContent());
        data.put("gameId", wrapper.getGameId());
        data.put("parentId", wrapper.getParentId());

        Long id = dao.executeInsert(DEF_ADD_DEFINITION, data, "id");
        if (id == null || id < 0) {
            throw new DbException("Unable to add definition to the game!");
        }
        return id;
    }

    @Override
    public boolean disableDefinition(long id) throws DbException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return dao.executeCommand(DEF_DISABLE_DEFINITION, data) > 0;
    }

    @Override
    public long editDefinition(long id, DefWrapper latest) throws DbException {
        DefWrapper prev = dao.getDefinitionDao().readDefinition(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("typeId", prev.getKind());
        data.put("name", compareWrapper(latest.getName(), prev.getName()));
        data.put("displayName", compareWrapper(latest.getDisplayName(), prev.getDisplayName()));
        data.put("content", compareWrapper(latest.getContent(), prev.getContent()));
        data.put("gameId", compareWrapper(latest.getGameId(), prev.getGameId()));
        data.put("parentId", compareWrapper(latest.getParentId(), prev.getParentId()));

        return dao.executeCommand(DEF_EDIT_DEFINITION, data);
    }

    private String compareWrapper(String latest, String prev) {
        if (latest != null) {
            return latest.equals(prev) ? prev : latest;
        } else {
            return null;
        }
    }

    private Long compareWrapper(Long latest, Long prev) {
        if (latest != null) {
            return latest;
        } else {
            return prev;
        }
    }
}
