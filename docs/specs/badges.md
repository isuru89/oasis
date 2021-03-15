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
              attribute: 30
        - priority: 2
          condition: "e.score >= 25"
          rewards:
            badge:
              attribute: 20
        - priority: 3
          condition: "e.score >= 10"
          rewards:
            badge:
              attribute: 10
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
              attribute: 30
        - streak: 50
          rewards:
            badge:
              attribute: 20
        - streak: 100
          rewards:
            badge:
              attribute: 10
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
              attribute: 10
        - value: 100         # 100 reputations
          rewards:
            badge:
              attribute: 20
        - value: 200       # 200 reputations
          rewards:
            badge:
              attribute: 30
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
              attribute: 10
        - streak: 7         # 7 consecutive days
          rewards:
            badge:
              attribute: 20
        - streak: 10        # 10 consecutive days
          rewards:
            badge:
              attribute: 30
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
          - type: weekly
            when: "SATURDAY,SUNDAY"
      condition: e.total >= 500
      pointId: star.points
      streaks:
        - streak: 3
          rewards:
            badge:
              attribute: 10
            points:
              id: star.points
              amount: 100
        - streak: 5
          rewards:
            badge:
              attribute: 20
            points:
              id: star.points
              amount: 250
        - streak: 10
          rewards:
            badge:
              attribute: 30
            points:
              id: star.points
              amount: 500
```