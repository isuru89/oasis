package io.github.oasis.core.external.storage;

import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;

import java.util.List;

public interface ElementDao {

    ElementDef addNewElement(int gameId, ElementDef elementDef);
    ElementDef updateElement(int gameId, String id, SimpleElementDefinition elementDef);
    ElementDef deleteElement(int gameId, String id);
    ElementDef readElement(int gameId, String id);
    ElementDef readElementWithoutData(int gameId, String id);
    List<ElementDef> readElementsByType(int gameId, String type);
    List<ElementDef> readElementsByGameId(int gameId);

}
