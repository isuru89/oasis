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

* Award points based on the fluctuation of user score average 
```yaml
  - id: WITH_THREE_RATINGS
    name: Point-Rating
    description: Awards points based on the value having.
    type: core:rating
    spec:
      selector:
        matchEvent: user.average
      defaultRating: 1
      ratings:
        - priority: 1
          rating: 3
          condition: e.value >= 85
          rewards:
            points:
              id: rating.points
              expression: (3 - previousRating) * 10
        - priority: 2
          rating: 2
          condition: e.value >= 65
          rewards:
            points:
              id: rating.points
              expression: (2 - previousRating) * 10
        - priority: 3
          rating: 1
          condition: e.value >= 50
          rewards:
            points:
              id: rating.points
              expression: (1 - previousRating) * 10
```