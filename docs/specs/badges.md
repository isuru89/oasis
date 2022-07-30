# Badges

After points, badges are the most important game element. And Oasis has a rich support
for awarding badges for different rules.

* Badges must be pre-defined before the gameplay.
* Some badges may be defined only to award it manually by Admin or Curator.
* A badge can have sub badges.
    * Each sub badge is identified using a unique name within the main badge.
* Some badges can be restricted only to award a minimum of N times in a lifetime of a player.
* A badge can cause to award more points.


## Badge Awards

A badge can be awarded using below ways.

* An event is occurred for the first time.
    * e.g. First time login, first purchase, etc.
* Based on a simple condition satisfying within an event.
    * Based on multiple conditions with different thresholds, sub-badges can be awarded
* Streaks: Consecutively satisfy a condition
    * Can be combined with time constraints. e.g. should have the streak within last N days/hours.
* A condition is satisfied N times within a time constraint.
    * Could be non-consecutive as well.
    * Can only be used within fixed time periods. (daily/weekly/monthly)
* Earn T points within a single time unit (hour/day/week)
* Daily accumulation is higher than a threshold (T) for,
  * N consecutive days
  * N separate days
* When points scored from a particular point rule.
* Completing a milestone level.
* Manually.

## Specification
| Field                                 | Description                                                                                                     | Required |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------| --- |
| [`selector`](common-spec.md#selector) | Specify events and conditions to filter out for processing                                                      | yes |
| [`kind`](#kind)                       | Type of badge                                                                                                   | yes |
| [`consecutive`](#consecutive)         | (boolean) if the badge is one of streak related one, should the streak be considered as consecutive or separate | yes |
| [`aggregatorExtractor`](#flags)       | Custom set of features to be applied for milestone definition                                                   | default: `null` |
| [`conditions`](#conditions)           | Set of conditions to conditional sub badges                                                                     | default: `null` |
| [`streaks`](#streaks)                 | Set of streak definitions for sub badges                                                                        | default: `null` |
| [`threshold`](#threshold)             | Threshold value for non-streak badges                                                                           | default: `null` |
| [`thresholds`](#thresholds)           | Set of thresholds for periodic sub badges                                                                       | default: `null` |
| [`retainTime`](#retainTime)           | Maximum retention time for out of order events                                                                  | default: `null` |
| [`timeRange`](#timeRange)             | Time range for time constrained badges                                                                          | default: `null` |
| [`period`](#period)                   | Time period for periodic badges                                                                                 | default: `null` |
| [`rewards`](#rewards)                 | Rewards for a badge optionally with points                                                                      | default: `null` |

### kind
Specify the nature of badge. Can only be one of below types.
  * `firstEvent`: Rewards a badge for the very first event of a player.
  * `conditional`: Badges based on conditions
  * `streak`: Satisfy event condition consecutively
  * `timeBoundedStreak`: Satisfy event condition consecutively within a specific time range
  * `periodicOccurrences`: Total number of event occurrences within a time range exceeds a threshold
  * `periodicOccurrencesStreak`: Consecutive occurrences of `periodicOccurrences`
  * `periodicAccumulation`: Accumulation of a dimension within a time range exceeds a threshold
  * `periodicAccumulationStreak`: Consecutive occurrences of `periodicAccumulation`

### consecutive
If the badge kind is one of below types, this parameter will define the consecutive of such streak.
 * streak
 * timeBoundedStreak
 * periodicOccurrencesStreak
 * periodicAccumulationStreak

When specified as `true`, then all matching events must be satisfied consecutively. 
When specified as `false`, there can be mis-matched events in between matched events.

For any other kind of badge, this parameter will be ignored.

### aggregatorExtractor
This parameter accepts an expression to extract aggregator value from each event so that
ultimately sums up to award a badge. This field is applicable only for badge kinds, `periodicOccurrences*` or `periodicAccumulation*`,
See examples for more details.

### conditions
Set of conditions for awarding sub-badges. A different condition can be specified for each sub-badge.

### streaks
Set of streak definitions for streak badges. Each streak can award a sub badge based on the streak value.

### thresholds
Set of thresholds for non-streak periodic badges. Multiple thresholds can be specified for sub-badges.

### threshold
Single threshold value for any `periodic**Streak` badges. 

### retainTime
Maximum retention time for all the out-of-order events.

### timeRange
Time range for `timeBoundedStreak` badge kind.

### period
Time duration for aggregating value based on given `aggregatorExtractor`.

### rewards
Specify reward when `firstEvent` badge kind. 
For rewarding other kind of badges, please refer under
[conditions](#conditions), [streaks](#streaks) and [thresholds](#thresholds)

## Time Constraints

* Time will consider always only to the past. Any events with future timestamps will be discarded.
* Badges can be targeted to award for last X time-units relative to the current time.
    * within last N days/hours
* Badges can consider fixed time periods.
    * within monthly (from 1st day of the month to the last day of month)
    * within weekly (from Monday to Sunday)
    * within daily (from morning to midnight)
    * within hourly (from T:00:00 to T:59:59)
  
## Examples

A comprehensive set of examples for badges can be viewed from [this directory](elements/badges/src/test/resources).

* First event badge
```yaml
  - id: BDG00001
    name: Initial-Registration
    description: Awards a badge when a user first registered within the application.
    type: core:badge
    spec:
      kind: firstEvent
      selector:
        matchEvent: app.user.registered
```

* Sub badges based on conditions
```yaml
  - id: BDG-C0001
    name: Question-Quality
    description: Awards badges based on how many scores got for the question
    type: core:badge
    spec:
      kind: conditional
      selector:
        matchEvent: question.scored
      conditions:
        - priority: 1
          condition: "e.score >= 100"
          rewards:
            badge:
              rank: 30
        - priority: 2
          condition: "e.score >= 25"
          rewards:
            badge:
              rank: 20
        - priority: 3
          condition: "e.score >= 10"
          rewards:
            badge:
              rank: 10
```

* Badges based on continuous streaks
```yaml
  - id: BDG-S0001
    name: Question-Score-Streak
    description: Awards badges when a question is up voted consecutively
    type: core:badge
    spec:
      kind: streak
      selector:
        matchEvent: question.voted
      condition: "e.upvote == true"  # if condition become falsy, then streak will break.
      streaks:
        - streak: 10
          rewards:
            badge:
              rank: 30
        - streak: 50
          rewards:
            badge:
              rank: 20
        - streak: 100
          rewards:
            badge:
              rank: 10
```

* Badges based on periodic aggregations
```yaml
  - id: BDG-P0001
    name: Daily-Reputations
    description: Accumulates user reputation daily and awards badges if user scores 50+ reputations on a day
    type: core:badge
    spec:
      kind: periodicAccumulation
      selector:
        matchEvent: reputation.changed
      period:
        duration: 1
        unit: days
      aggregatorExtractor:
        expression: e.reputations
      thresholds:
        - value: 50        # 50 reputations
          rewards:
            badge:
              rank: 10
        - value: 100         # 100 reputations
          rewards:
            badge:
              rank: 20
        - value: 200       # 200 reputations
          rewards:
            badge:
              rank: 30
```

* Badges based on periodic streaks
```yaml
  - id: BDG-PS0001
    name: Daily-Reputations
    description: Accumulates user reputation and awards badges if user scores 200+ reputation for minimum 5 consecutive days.
    type: core:badge
    spec:
      kind: periodicAccumulationStreak
      selector:
        matchEvent: reputation.changed
      period:
        duration: 1
        unit: daily
      threshold: 200
      consecutive: true
      aggregatorExtractor:
        expression: e.reputations
      streaks:
        - streak: 5         # 5 consecutive days
          rewards:
            badge:
              rank: 10
        - streak: 7         # 7 consecutive days
          rewards:
            badge:
              rank: 20
        - streak: 10        # 10 consecutive days
          rewards:
            badge:
              rank: 30
```

* Badges which awards points
```yaml
  - id: BDG-S0002
    name: Loyality-Customer
    description: Awards badges and points when a customer buys $500 or more worth items consecutively in each weekend.
    type: core:badge
    spec:
      kind: streak
      selector:
        matchEvent: order.accepted
        acceptsWithin:
          anyOf:
            - type: weekly
              when: "SATURDAY,SUNDAY"
      condition: e.total >= 500
      pointId: star.points
      streaks:
        - streak: 3
          rewards:
            badge:
              rank: 10
            points:
              id: star.points
              amount: 100
        - streak: 5
          rewards:
            badge:
              rank: 20
            points:
              id: star.points
              amount: 250
        - streak: 10
          rewards:
            badge:
              rank: 30
            points:
              id: star.points
              amount: 500
```