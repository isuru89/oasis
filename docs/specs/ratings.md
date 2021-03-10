# Ratings

A rating indicates a state of a player.

* A rating is a status which a player can be in at any given time.
  * For e.g: Question to answer ratio, Violation density
* A player can belong to one-and-only rating at a given time.
* A rating will be set according to the first satisfying condition of the rule.
  * The ordering of conditions can be controlled through priority field
* If no condition is satisfied, then default rating will be awarded.

 **NOTE**: It is advice to specify conditions to satisfy at least one.
 
* ~~External applications must send events notifying the current state.~~
  * ~~Based on state, user will be awarded points.~~
* ~~The points awarded from state won't be part of Milestones or any other game elements.~~
  * ~~State points are just a net points you awarded for being in that state.~~


## Specification
TBW

## Examples
TBW