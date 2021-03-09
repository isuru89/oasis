# Badges

After points, badges are the most important element. And Oasis has a rich support
for awarding badges for different rules.

* Badges must be pre-defined before the gameplay.
* Some badges may be defined only to award manually.
  * Only by Admin or Curator.
* A badge can have sub badges.
    * Each sub badge is identified using a unique name within the main badge.
* Some badges can be restricted only to award minimum N times in a lifetime of a player.
* A badge can cause to award bonus points


## Badge Awards

A badge can be awarded using below ways.

* An event is occurred for the first time.
    * e.g. First time login.
* Based on a simple condition satisfying within an event.
    * Based on multiple conditions with different thresholds, sub-badges can be awarded
* Streaks: Continuously satisfy a condition
    * Can be combined with time constraints. e.g. should have the streak within last N days/hours.
* When some condition is satisfied N times within a time constraint.
    * Need not be continuously.
    * Can only be used within fixed time periods. (daily/weekly/monthly)
* Earn T points within a single time unit (day/hour/week)
* Daily accumulation is higher than a threshold (T) for,
  * N consecutive days
  * N separate days
* When points scored from a particular point rule.
* Completing a milestone level.
* Manually.

## Specification
TBW

## Time Constraints

* Time will consider always only to the past.
* Badges can be targeted to award for last X time-units relative to the current time.
    * within last N days/hours
* Badges can consider fixed time periods.
    * within monthly (from 1st day of the month to the last day of month)
    * within weekly (from Monday to Sunday)
    * within daily (from morning to midnight)
  
## Examples

A comprehensive set of examples for badges can be viewed from [this directory](elements/badges/src/test/resources).

* First event badge
```yaml
  - id: BDG00001
    name: Initial-Registration
    description: Awards a badge when a user first registered within the application.
    event: app.user.registered
    kind: firstEvent
```

* Sub badges based on conditions
```yaml
  - id: BDG-C0001
    name: Question-Quality
    description: Awards badges based on how many scores got for the question
    event: question.scored
    kind: conditional
    conditions:
      - priority: 1
        attribute: 30
        condition: "e.score >= 100"
      - priority: 2
        attribute: 20
        condition: "e.score >= 25"
      - priority: 3
        attribute: 10
        condition: "e.score >= 10"
```

* Badges based on continuous streaks
```yaml
  - id: BDG-S0001
    name: Question-Score-Streak
    description: Awards badges when a question is up voted consecutively
    event: question.voted
    kind: streak
    condition: "e.upvote == true"   # if condition become falsy, then streak will break.
    streaks:
      - streak: 10
        attribute: 10
      - streak: 50
        attribute: 20
      - streak: 100
        attribute: 30
```

* Badges based on periodic aggregations
```yaml
  - id: BDG-P0001
    name: Daily-Reputations
    description: Accumulates user reputation daily and awards badges if user scores 50+ reputations on a day
    kind: periodicAccumulation
    event: reputation.changed
    timeUnit: daily
    aggregatorExtractor: e.reputations
    thresholds:
      - value: 50        # 50 reputations
        attribute: 10
      - value: 100         # 100 reputations
        attribute: 20
      - value: 200       # 200 reputations
        attribute: 30
```

* Badges based on periodic streaks
```yaml
  - id: BDG-PS0001
    name: Daily-Reputations
    description: Accumulates user reputation and awards badges if user scores 200+ reputation for minimum 5 consecutive days.
    kind: periodicAccumulationStreak
    event: reputation.changed
    threshold: 200
    timeUnit: daily
    consecutive: true
    aggregatorExtractor: e.reputations
    streaks:
      - streak: 5         # 5 consecutive days
        attribute: 10
      - streak: 7         # 7 consecutive days
        attribute: 20
      - streak: 10        # 10 consecutive days
        attribute: 30
```

* Badges which awards points
```yaml
  - id: BDG00002
    name: test.badge.points.rule
    description: Awards badge and points if value is >= 50 for streaks
    plugin: core:badge
    kind: streak
    event: event.a
    eventFilter: e.value >= 50
    timeUnit: 10
    pointId: star.points
    streaks:
      - streak: 3
        attribute: 10
        pointAwards: 50
      - streak: 5
        attribute: 20
        pointAwards: 100
```