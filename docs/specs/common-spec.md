# Common Specification

## selector

Selector field matches the incoming event and filter out events which should be
send for processing by the respective rule.

This field is mandatory for every rule.


```yaml
selector:
  matchEvent: <string>    # a single event id to match for
  
  matchEvents:            # Only one 'anyOf' or 'patterns' must be specified at a time
    anyOf:                # matches of any of events specified under
      - <string>
      - ...
    patterns:             # matches events by matching at least one regex
      - <regex-pattern>
      - ...
  
  filter:
    expression: <string>  # dynamic expression which evaluates against each event
  
  acceptsWithin:          # details more below
    - type: <string>
      from: <string>
      to: <string>
```

### acceptsWithin
This field will determine how events should be matched based on its timestamp.
This is suitable, if you want to filter event based on time or season. This
should be specified using `type` field followed by relevant fields for each type.

There are in-built several types supported by the framework. Such as,

  * `seasonal`: Allows you to specify range of months or day-of-month. The allowed format is `MM-DD`.
  * `time`: Allows you to specify a range of time-of-day. The allowed format is `HH:mm:ss`.
  * `weekly`: Allows you to specify weekdays.
  * `custom`: Allows you to specify a custom predicate to evaluate with given event timestamp

**Note**: 
1. When multiple time filters has specified under `acceptsWithin`, the engine will evaluate
for **_at least one match_** to allow the event for execution.
2. When evaluating ranges, the `from` field will be inclusive, while `to` field will be exclusive.

#### Examples
* Filter for the month of December. You can match across months or even consecutive years.
```yaml
acceptsWithin:
  - type: seasonal
    from: "12-01"
    to: "01-01"     # to field is exclusive
```

* Filter out winter season
```yaml
acceptsWithin:
  - type: seasonal
    from: "12-01"
    to: "03-01"
```

* Filter out Dinner time (assuming its 6pm to 11pm)
```yaml
acceptsWithin:
  - type: time
    from: "18:00"
    to: "23:00"
```

* Filter our mid-night time (usually 11pm - 5am on the following day)
```yaml
acceptsWithin:
  - type: time
    from: "23:00"
    to: "05:00"
```

* Filter out Weekend days. Should specify days in a comma separated string on `when` property.
```yaml
acceptsWithin:
  - type: weekly
    when: "Saturday,Sunday"
```

* A custom filter which filters last week of every month. The expression must be written in Jdk-11 compatible way.
```yaml
acceptsWithin:
  - type: custom
    expression: |
        YearMonth currMonth = YearMonth.of(ts.getYear(), ts.getMonth());
        ts.getDayOfMonth() >= currMonth.lengthOfMonth() - 7
```