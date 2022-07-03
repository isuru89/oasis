# Challenges

Challenges can be created while the game is in progress by Curators or Admin to set
temporary targets for specific number of players or teams. 
Once a challenge is completed by a player, that person will be rewarded with points.

 * Challenges must have a start time and end time.
 * A challenge can be scoped to a single user or single team.
 * Winners are being awarded based on First-come basis
 * Challenge can optionally define maximum number of winners allowed.
 * Challenges will be deactivated when the time expired or 
   all the winners are decided, whichever comes first.
    * Unless, `OUT_OF_ORDER_WINNERS` flag is specified under the rule.
 * Winners can only be awarded with points and that number of points
    can be decided based on the ranking of each winner.
 * By default, a player cannot be rewarded twice for the same challenge.
   * Can be modified this behaviour using `REPEATABLE_WINNERS` flag.

## Specification

| Field                           | Description                                                                  | Required      |
|---------------------------------|------------------------------------------------------------------------------|---------------|
| [`selector`](#selector)         | Specify events and conditions to filter out for processing                   | yes           |
| [`scopeTo`](#scopeTo)           | Function/expression to extract the value from the event for accumulation.    | yes           |
| [`flags`](#flags)               | Custom set of features to be applied for milestone definition                | default: `[]` |
| [`startAt`](#startAt/expireAt)  | Start time in epoch milliseconds of the challenge to start processing events | yes           |
| [`expireAt`](#startAt/expireAt) | End epoch millisecond time to stop processing anymore events                 | yes           |
| [`winnerCount`](#winnerCount)   | Maximum count of winners eligible.                                           | default: `-1` |
| [`rewards`](#rewards)           | Rewards for each winner                                                      | yes           |

### selector
Selects events which are eligible for processing under this challenge.
[See here](common-spec.md#selector) for more details

### scopeTo
Defines the scope of this challenge for winners. Currently, this supports two scopes.
 * Game: (*default scope*) the challenge winners are selected only from specified game.
 * Teams: the challenge winners are selected only from the specified team(s).


When `GAME` type is specified, any player from the rule defined game will be eligible for winning.
While, `TEAM` type is specified, multiple teams can also be defined. The team ids can be found via
team API call. If this needs to be defined for a single team, `teamId` parameter should be used.

E.g.
```yaml
    scopeTo:
      type: TEAM
      targetIds:
        - 33213
        - 26940
```

### flags
Challenges support below feature flags.
  * `REPEATABLE_WINNERS`: Allows same players to win a challenge multiple times.
  * `OUT_OF_ORDER_WINNERS`: Winners can still be chosen if events are delayed after the challenge expire time. Due to
     this, some winners might be dethroned if a winner is chosen after expire time.

### startAt/expireAt
Start time and end time of the challenge to be active. Any events fall outside this timestamp range
will be discarded. Timestamp values must be in epoch milliseconds.

Both, `startAt` and `expireAt` are inclusive.

### winnerCount
Maximum number of winners that can be awarded from this challenge over the time.
This must be an integer and setting the value to 0 will effectively disable winners.

A negative value (<0) will effectively accept indefinite number of winners as long as the challenge is active.

### rewards
Challenge winners can only be rewarded with points. 
Points can be fixed amount or an expression.

Specifying `amount` will cause all winners to get fixed amount of points regardless of the winning rank.

Expression must be used when the amount of points to be awarded for each winner should be different.
Expression will be invoked for each winner with its rank.
Expression must return an integer or decimal number which represents the points for winner.
Expression will get two variables, namely,
  * `rank`: winner's rank as number (1,2,3, etc...)
  * `rule`: challenge rule definition. Can access various rule parameters if required.

```yaml
    rewards:
      points:
        id: pointId       # point id
        amount: number    # fixed amount of points for all winners
        expression: string  # points rewarded can be manipulated based on winner rank
```

**Warn**: Specifying both `amount` and `expression` will cause rule validation to be failed.

## Examples

* A challenge which allows only 3 max winners from any player on the game and awards
    * 1st place = 300 points
    * 2nd place = 200 points
    * 3rd place = 100 points
```yaml
  - id: CHG000001
    name: test.challenge.rule
    description: Game scoped challenge when some one scored more than 50
    type: core:challenge
    spec:
      selector:
        matchEvent: user.scored
        filter:
          expression: e.value >= 50
      scopeTo:
        type: GAME
      startAt: 1583027100000
      expireAt: 1588297500000
      winnerCount: 3
      rewards:
        points:
          id: challenge.points
          expression: 100 * (4 - rank)
```

* A challenge winners only scoped to the team identified by id = 2, which accepts out of order winners 
  (in case an event related to a  winner comes up later)
```yaml
  - id: TEAM_SCOPED_MULTI_WINNER_NO_REPEAT
    name: test.challenge.rule
    description: Game scoped challenge when some one scored more than 50
    type: core:challenge
    spec:
      selector:
        matchEvent: user.scored
        filter:
          expression: e.value >= 50
      scopeTo:
        type: TEAM
        targetId: 2
      startAt: 0
      expireAt: 200
      winnerCount: 3
      rewards:
        points:
          id: challenge.points
          expression: 100 * (4 - rank)
```