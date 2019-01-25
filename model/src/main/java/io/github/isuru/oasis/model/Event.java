package io.github.isuru.oasis.model;

import java.io.Serializable;
import java.util.Map;

public interface Event extends Serializable {

    Map<String, Object> getAllFieldValues();

    void setFieldValue(String fieldName, Object value);

    /**
     * Returns the value for the given field name.
     *
     * @param fieldName field name.
     * @return field value. null if not exist.
     */
    Object getFieldValue(String fieldName);

    /**
     * Returns the event type as string. This will be used
     * to apply rules specifically targetted for the defined event types.
     *
     * @return event type
     */
    String getEventType();

    /**
     * Actual event occurred time. Must be in epoch-milliseconds.
     *
     * @return event occurred timestamp.
     */
    long getTimestamp();

    /**
     * Returns the owner user id of this event. There
     * can only be one user for an event. If there are multiple
     * users, you may split/duplicate this event as required.
     *
     * @return the user id of this event.
     */
    long getUser();

    /**
     * Returns the external reference id for this event.
     * This will be useful to attach this event instance to
     * domain specific record in the user application.
     *
     * @return external reference id.
     */
    String getExternalId();

    /**
     * Return user id indicated by any other field. This is useful
     * when an event is associated with several users, and will be called when
     * framework needs to assign point(s) to this other user as well.
     *
     * @param fieldName user field name.
     * @return other user id.
     */
    Long getUserId(String fieldName);

    /**
     * Returns the current team of the associated user of this event.
     *
     * @return team id.
     */
    Long getTeam();

    /**
     * Returns the scope id of the current team.
     * Operators does not care about this field for its computation.
     *
     * @return the scope id of the current team.
     */
    Long getTeamScope();

    /**
     * Return source id generated this event.
     *
     * @return source id
     */
    Integer getSource();

    /**
     * Returns associated game id of this event.
     *
     * @return game id.
     */
    Integer getGameId();
}
