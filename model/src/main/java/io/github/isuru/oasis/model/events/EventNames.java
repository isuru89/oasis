package io.github.isuru.oasis.model.events;

/**
 * @author iweerarathna
 */
public final class EventNames {

    public static final String TERMINATE_GAME = "__OASIS_TERMINATE__";

    public static final String EVENT_COMPENSATE_POINTS = "__OASIS_COMPENSATE_POINTS__";
    public static final String EVENT_AWARD_BADGE = "__OASIS_MANUAL_BADGE__";
    public static final String EVENT_SHOP_ITEM_SHARE = "__OASIS_ITEM_SHARE__";

    public static final String START_CHALLENGE = "__OASIS_CHALLENGE_START__";
    public static final String CHALLENGE_TIMEOUT = "__OASIS_CHALLENGE_TIMEOUT__";
    public static final String CHALLENGE_COMPLETED = "__OASIS_CHALLENGE_COMPLETED__";


    public static final String POINT_RULE_COMPENSATION_NAME = EVENT_COMPENSATE_POINTS.toLowerCase();
    public static final String POINT_RULE_MILESTONE_BONUS_NAME = "__oasis_milestone_bonus__";
    public static final String POINT_RULE_BADGE_BONUS_NAME = "__oasis_badge_bonus__";


}
