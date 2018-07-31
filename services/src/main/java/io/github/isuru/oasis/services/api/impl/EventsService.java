package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.services.api.IEventsService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.RabbitDispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class EventsService extends BaseService implements IEventsService {

    EventsService(IOasisDao dao, IOasisApiService apiService) {
        super(dao, apiService);
    }

    @Override
    public void submitEvent(Map<String, Object> eventData) throws Exception {
        Map<String, Object> event = new HashMap<>(eventData);
        if (!event.containsKey(Constants.FIELD_TEAM)) {
            Long userId = (Long) event.get(Constants.FIELD_USER);
            UserTeam userTeam = getApiService().getProfileService().findCurrentTeamOfUser(userId);
            event.put(Constants.FIELD_TEAM, userTeam.getTeamId());
        }
        RabbitDispatcher.get().dispatch(event);
    }

    @Override
    public void submitEvents(List<Map<String, Object>> events) throws Exception {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (Map<String, Object> event : events) {
            submitEvent(event);
        }
    }
}
