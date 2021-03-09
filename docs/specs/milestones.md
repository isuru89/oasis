# Milestones

* All milestones are pre-defined before gameplay.
* Can be defined only by Admin.
* Milestones never ends. They are played by players forever.
    * Milestone should be defined in a way which can play forever.
* At least two levels must be there for a milestone.
* A milestone can have only **finite** number of levels.
* When a player has reached maximum level of a milestone, Oasis considers that player has completed the milestone.
* ~~Milestone aggregating values must be monotonically increasing.~~
    * ~~Penalty values (negative values) will be accumulated separately along with positive values.~~
* Once a player achieved/surpassed a level, it cannot be undone.
  * Due to the penalty values, it might go below the current level. But, it does not
  affect to the player's achieved level.
  
## Milestone Creation

* Milestones should be defined only using aggregation functions
    * Sum of point ids
    * Sum of events  
    * Count of events
  
## Specification
TBW

## Examples

More comprehensive set of examples can be found in this [file](elements/milestones/src/test/resources/milestones.yml)

* Accumulate a single point id
```yaml
  - id: MILE-0001
    name: Total-Reputations
    description: Provides ranking through accumulated reputations
    pointIds:
      - stackoverflow.reputation
    levels:
      - level: 1
        milestone: 1000
      - level: 2
        milestone: 5000
      - level: 3
        milestone: 10000
      - level: 4
        milestone: 50000
      - level: 5
        milestone: 100000
```

* Accumulates based on multiple point ids
```yaml
  - name: Star-Points
    description: Allow tiers for customers based on star points accumulated
    pointIds:
      - star.points
      - coupan.points
    levels:
      - level: 1
        milestone: 100
      - level: 2
        milestone: 1000
      - level: 3
        milestone: 10000
```

* Accumulate directly from events based on a condition
```yaml
  - name: Milestone-with-Event-Count
    description: This is a milestone counting events based on a criteria.
    event: stackoverflow.question.answered
    eventFilter: |
      return e.answeredAt - e.askedAt <= 60 * 60 * 5
    valueExtractor: 1
    levels:
      - level: 1
        milestone: 5
      - level: 2
        milestone: 10
      - level: 3
        milestone: 15
```