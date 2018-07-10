package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.rules.BadgeRule;
import io.github.isuru.oasis.model.rules.PointRule;

/**
 * @author iweerarathna
 */
public class NoneOutputHandler implements IOutputHandler {

    private final IPointHandler pointHandler = new NonePointHandler();
    private final IBadgeHandler badgeHandler = new NoneBadgeHandler();
    private final IMilestoneHandler milestoneHandler = new NoneMilestoneHandler();

    @Override
    public IPointHandler getPointsHandler() {
        return pointHandler;
    }

    @Override
    public IBadgeHandler getBadgeHandler() {
        return badgeHandler;
    }

    @Override
    public IMilestoneHandler getMilestoneHandler() {
        return milestoneHandler;
    }

    private static class NoneMilestoneHandler implements IMilestoneHandler {

        @Override
        public void milestoneReached(Long user, int level, Event event, Milestone milestone) {

        }

        @Override
        public void onError(Throwable ex, Event e, Milestone rule) {
            System.out.println("Milestone error!");
        }
    }

    private static class NoneBadgeHandler implements IBadgeHandler {

        @Override
        public void badgeReceived(Long userId, BadgeNotification badgeNotification) {

        }

        @Override
        public void onError(Throwable ex, Event e, BadgeRule rule) {
            System.out.println("Badge error!");
        }
    }

    private static class NonePointHandler implements IPointHandler {

        @Override
        public void pointsScored(Long userId, PointNotification pointNotification) {
            //System.out.println(userId + " -> " + rule.getId() + " #" + points);
        }

        @Override
        public void onError(Throwable ex, Event e, PointRule rule) {
            System.out.println("Error!");
        }
    }
}
