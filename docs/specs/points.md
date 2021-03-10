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
TBW

## Examples

More up to date but different examples can be viewed from this [file](elements/points/src/test/resources/points.yml)

* Award a constant amount of points upon a particular event received (no condition)
```yaml
version: 1

points:
  - name: Answer-Accepted
    description: Awards 15 reputations when an answer has been accepted
    event: stackoverflow.answer.accepted
    pointId: stackoverflow.reputation
    award: 15
```

* Award amount of points based on the field in the event (no condition)
```yaml
- name: General-Spending-Rule
  description: Customer could receive points for order value
  event: order.accepted
  pointId: star.points
  award: "e.total"
```

* Based on a event with a condition
```yaml
  - name: Big-Purchase-Bonus
    description: Customer receives bonus points for passing purchase limit
    event: order.accepted
    pointId: star.points
    award: |
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
    plugin: core:point
    events:
      - event.a
      - event.c
    eventFilter: e.value >= 50
    award: e.value - 50
    pointId: bonus.points
```

* Seasonal points. This rule will double the awarded points only in the month of December.
```yaml
  - name: Special-Seasonal-Award
    description: Awards double bonus points for every purchase done on december
    event: order.accepted
    pointId: star.points
    timeRanges:
      - type: seasonal
        from: "12-01"
        to: "12-31"
    award: "e.total * 2"
```