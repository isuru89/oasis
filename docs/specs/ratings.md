# Ratings

* A rating is a status which a user can be in at any given time.
  * For e.g: Question to answer ratio, Violation density
* A user can have one-and-only rating at a given time.
* Every rating is being set according to the satisfying condition.
* If no condition is satisfied, then default rating will be awarded.
* NOTE: It is advice to specify conditions to satisfy at least one.
 
* Very similar to milestones but does not accumulate through history.
* External applications must send events notifying the current state.
  * Based on state, user will be awarded points.
* The points awarded from state won't be part of Milestones or any other game elements.
  * State points are just a net points you awarded for being in that state.
