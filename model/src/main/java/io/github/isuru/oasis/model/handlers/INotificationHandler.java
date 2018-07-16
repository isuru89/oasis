package io.github.isuru.oasis.model.handlers;

/**
 * @author iweerarathna
 */
public interface INotificationHandler {

    void notificationCreated(Long userId, String message);

}
