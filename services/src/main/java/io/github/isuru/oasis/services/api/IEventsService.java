package io.github.isuru.oasis.services.api;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IEventsService {

    void submitEvent(Map<String, Object> eventData) throws Exception;

    void submitEvents(List<Map<String, Object>> events) throws Exception;

}
