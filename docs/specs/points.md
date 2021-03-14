# Points

Points is the only core game element shipped with while all other elements has to add explicitly.

 * Points are always accurate no more than up to 2 decimal points
 * Only players can score points
 * Each point is associated with a type
 * The timestamp, team and event source id will be recorded against each point event scored.
 * Once scored, points cannot be deleted or modified
   * To simulate a delete, same amount of penalty points needs to be added against same player manually.
   
## Scoring Methods:
 * By raw events
   * Based on some condition
 * ~~By total points scored per event~~
   * ~~Several types of points can be generated per event.~~
 * When earned a badge
 * When milestone level reached
 * Becoming a winner of a challenge
 * Manually can be awarded by a Curator or Admin

## Tips
* You can combine multiple rules to emit same point type by having same id for `pointId` field.
  This is useful to combine awards based on multiple time ranges.

## Specification


| Field | Description | Required |
| --- | --- | --- |
| [`selector`](common-spec.md#selector) | Specify events and conditions to filter out for processing | yes |
| [`reward`](#reward) | Indicates what kind of points and how many should be rewarded. | yes |
| [`cap`](#cap) | If specified, then this indicated how many maximum points can be earned from this rule for the specified constrained time period. | default: `null` |

### selector
 [See here](common-spec.md#selector) for more details

### reward
 For points, rewards can be either a constant amount or a dynamic expression.

Note: It is mandatory to provide `pointId` with either one of `amount` or `expression`.
If `amount` and `expression` both have been specified, it will throw an error.

```yaml
reward:
  pointId: <string>   # id of the points to be awarded
  amount: <number>    # constant amount of points to award
  expression: <string>  # an expression which evaluates against 
                        # each event to derive the amount to be awarded.
```

### cap
Optional maximum achievable points per unit time. This will be checked
against the points as mentioned in `pointId` in `reward` section. 

```yaml
cap:
  duration: daily|weekly|monthly|quarterly|annually
  limit: <number>   # maximum point that can be earned for the time period 
```

## Examples

More up to date but different examples can be viewed from this [file](../../elements/points/src/test/resources/points.yml)

* Award a constant amount of points upon a particular event received (no condition)
```yaml
version: 1

elements:
  - id: P_ANSWER_ACCEPTED
    name: Answer-Accepted
    description: Awards 15 reputations when an answer has been accepted
    type: core:point
    spec:
      selector:
        matchEvent: stackoverflow.answer.accepted
      reward:
        pointId: stackoverflow.reputation
        amount: 15
```

* Award amount of points based on the field in the event (no condition)
```yaml
  - id: POINT-GSR
    name: General-Spending-Rule
    description: Customer could receive points for order value
    type: core:point
    spec:
      selector:
        matchEvent: order.accepted
      reward:
        pointId: star.points
        expression: "e.total"
```

* Reward is based on set of conditions
```yaml
  - id: P_BIG_PURCHASE
    name: Big-Purchase-Bonus
    description: Customer receives bonus points for passing purchase limit.
    type: core:point
    spec:
      selector:
        matchEvent: order.accepted
      reward:
        pointId: star.points
        expression: |
          if (e.total >= 500) {
            return (e.total - 500) * 10;
          } else if (e.total >= 100) {
            return (e.total - 100) * 3;
          } else if (e.total >= 50) {
            return (e.total - 50) * 2;
          }
          return 0;
```

* Based on any type of events with a filter
```yaml
  - id: PNT00001
    name: Bonus-Half-Century
    description: Awards points for each additional value when total is greater than 50
    type: core:point
    spec:
      selector:
        matchEvents:
          anyOf:
            - event.a
            - event.c
        filter:
          expression: e.value >= 50
      reward:
        expression: e.value - 50
        pointId: bonus.points
```

* Seasonal points. This rule will double the awarded points only in the month of December.
```yaml
  - id: P_SEASONAL_AWARDS
    name: Special-Seasonal-Award
    description: Awards double bonus points for every purchase done on december.
    type: core:point
    spec:
      selector:
        matchEvent: order.accepted
        acceptsWithin:
          - type: seasonal
            from: "12-01"
            to: "12-31"
      reward:
        pointId: star.points
        expression: "e.total * 2"
```