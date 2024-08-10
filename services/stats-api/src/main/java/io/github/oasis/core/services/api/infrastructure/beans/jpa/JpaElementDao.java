package io.github.oasis.core.services.api.infrastructure.beans.jpa;

import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.external.storage.ElementDao;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class JpaElementDao implements ElementDao {


    @Override
    public ElementDef addNewElement(int gameId, ElementDef elementDef) {
        return null;
    }

    @Override
    public ElementDef updateElement(int gameId, String id, SimpleElementDefinition elementDef) {
        return null;
    }

    @Override
    public ElementDef deleteElement(int gameId, String id) {
        return null;
    }

    @Override
    public ElementDef readElement(int gameId, String id) {
        return null;
    }

    @Override
    public ElementDef readElementWithoutData(int gameId, String id) {
        return null;
    }

    @Override
    public List<ElementDef> readElementsByType(int gameId, String type) {
        return null;
    }

    @Override
    public List<ElementDef> readElementsByGameId(int gameId) {
        return null;
    }

}
