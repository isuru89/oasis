package io.github.isuru.oasis.model.handlers;

import java.io.Serializable;

public interface IRatingsHandler extends Serializable {

    void handleRatingChange(RatingNotification ratingNotification);

}
