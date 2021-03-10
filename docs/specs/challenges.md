# Challenges

Challenges can be created while the game is in progress by Curators or Admin to set
temporary challenges for targeted players or teams. Once a challenge is completed by a player,
that person will be rewarded with points.

 * Challenges must have a start time and end time.
 * A challenge can scope to a single user or single team.
 * Winners are being awarded based on First-come basis
 * Challenge can optionally define maximum number of winners allowed.
 * Challenges will be deactivated when the time expired or 
   all the winners are available, whichever comes first.
 * Winners can only be awarded with points and that number of points
    can be decided based on the ranking of each winner.

## Specification
TBW

## Examples

* A challenge which allows only 3 max winners from any player on the game and awards
    * 1st place = 300 points
    * 2nd place = 200 points
    * 3rd place = 100 points
```yaml
  - id: CHG000001
    name: test.challenge.rule
    description: Game scoped challenge when some one scored more than 50
    plugin: core:challenge
    event: event.a
    eventFilter: e.value >= 50
    scope:
      type: GAME
    winnerCount: 3
    startAt: 1583027100000
    expireAt: 1588297500000
    pointId: challenge.points
    pointAwards: 100 * (3 - rank + 1)
```

* A challenge scoped to team identified by id = 2, which accepts out of order winners 
  (in case an event related to a  winner comes up later)
```yaml
  - id: CHG000001
    name: test.challenge.rule
    description: Game scoped challenge when some one scored more than 50
    plugin: core:challenge
    flags:
      - OUT_OF_ORDER_WINNERS
    event: event.a
    eventFilter: e.value >= 50
    scope:
      type: TEAM
      id: 2
    winnerCount: 3
    startAt: 1583027100000
    expireAt: 1588297500000
    pointId: challenge.points
    pointAwards: 100 * (3 - rank + 1)
```