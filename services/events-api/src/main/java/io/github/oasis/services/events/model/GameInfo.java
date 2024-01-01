package io.github.oasis.services.events.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class GameInfo {

    public static final String ID = "id";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String STATUS = "status";

    private JsonObject ref;

    public static GameInfo create(int id, JsonObject other) {
        return new GameInfo(new JsonObject()
                .mergeIn(other)
                .put(ID, id));
    }

    public GameInfo(JsonObject ref) {
        this.ref = ref;
    }

    public JsonObject toJson() {
        return ref;
    }

    public long getId() {
        return ref.getLong(ID);
    }

    public long getStartAt() {
        return ref.getLong(START_TIME);
    }

    public long getEndAt() {
        return ref.getLong(END_TIME);
    }

    @Override
    public String toString() {
        return "GameInfo{" +
                "ref=" + ref +
                '}';
    }
}
