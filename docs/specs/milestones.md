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



| Field | Description | Required |
| --- | --- | --- |
| [`selector`](common-spec.md#selector) | Specify events and conditions to filter out for processing | yes |
| [`valueExtractor`](#valueExtractor) | Function/expression to extract the value from the event for accumulation. | yes |
| [`levels`](#levels) | List of milestone levels. At least one level must be specified | yes |
| [`flags`](#flags) | Custom set of features to be applied for milestone definition | default: `null` |

### selector
[See here](common-spec.md#selector) for more details

### valueExtractor
Function/expression to extract the value from the event for accumulation. The expression must always
evaluate to a number, otherwise it will fail.

Currently, this accepts `expression` as a text.
```yaml
valueExtractor:
  expression: "e.value"
```

If you want to make count the filtered out event, just simply specify `1` in amount field.
```yaml
valueExtractor:
  amount: 1
```

### levels
Levels of this milestones. Each level has two mandatory field to specify.

* `level`: level number.
* `milestone`: Threshold value to achieve this level. Must always be a number.

**_Notes_**: 
1. At least one level must be specified in every milestone. Otherwise, it will fail.
2. `level` field must be a number and cannot have duplicates.


### flags
There are two supported flags by milestones.

  * `TRACK_PENALTIES`: when specified, the negative values will be stored separately as a breakdown of the milestone stats. 
The relevant application can choose to decide what to do with positive and negative accumulation.
  * `SKIP_NEGATIVE_VALUES`: totally ignores the negative values being accumulated into the milestone.

## Examples

More comprehensive set of examples can be found in this [file](elements/milestones/src/test/resources/milestones.yml)

* Accumulate a single point id
```yaml
  - id: MILE-00010
    name: Total-Reputations
    description: Provides ranking through accumulated reputations
    type: core:milestone
    spec:
      selector:
        matchPointIds:
          anyOf:
            - stackoverflow.reputation
      flags:
        - TRACK_PENALTIES         # negative values will store separately along with milestone
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
  - id: MILE-00020
    name: Star-Points
    description: Allow tiers for customers based on star points accumulated
    type: core:milestone
    spec:
      selector:
        matchPointIds:
          anyOf:
            - star.points
            - coupan.points
      flags:
        - SKIP_NEGATIVE_VALUES      # prevents reducing milestone scores due to negative values
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
  - id: MILE-00030
    name: Milestone-with-Event-Count
    description: This is a milestone counting events based on a criteria.
    type: core:milestone
    spec:
      selector:
        matchEvent: stackoverflow.question.answered
        filter:
          expression: e.answeredAt - e.askedAt <= 60 * 60 * 5
      valueExtractor:
        amount: 1       # by setting 1, we simulate the count behaviour
      levels:
        - level: 1
          milestone: 5
        - level: 2
          milestone: 10
        - level: 3
          milestone: 15
```