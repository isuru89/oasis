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
  
  acceptsWithin:          # more details are shown below
    anyOf:
      - type: <string>
        from: <string>
        to: <string>
      - ...
    allOf:
      - ...
```

**Notes**: 
 1. All expressions must be written in MVEL. See its spec [here](http://mvel.documentnode.com/)
 2. Every criterion must belong to either `anyOf` or `allOf` field to indicate the behaviour
when multiple criteria are specified.

### matchEvent
Matches a single event by its event type. The event type to be matched should be specified.
If you want to match against several event types, see [matchEvents](#matchevents).

### matchEvents
Allows to specify multiply event types, so that event could be filtered when at least one
event type is matched. There are two ways to specify event type values.
 * `anyOf`: event types specified here will be directly matched against event type mentioned in the event.
 * `patterns`: can be specified (optionally) multiple regex expressions to evaluate against event type value in a event.

**Warn**: Either `anyOf` or `patterns` parameter must be specified at once. When both specified, it will throw an error in validation phase.

### filter
Optional conditional expression which will be evaluated against each and every event.
The expression must be written in [MVEL](http://mvel.documentnode.com/) script language.
  * The event and its data can be referenced through `e` variable provided to the script.
  * The rule definition can be referenced through `rule` variable provided to the script.
  * The user related context can be referenced through `ctx` variable.

See examples in each game element to how to write an expression.

### acceptsWithin
This field will determine how events should be matched based on its timestamp.
This is suitable, for example, if you want to filter event based on time or a season. 
The field `type` is mandatory and should be followed by relevant keys for each type which
you can refer below.


There are several types built into the framework. Such as,

  * `seasonal`: Allows you to specify range of months or day-of-month. The allowed format is `MM-DD`.
  * `time`: Allows you to specify a range of time-of-day. The allowed format is `HH:mm:ss`.
  * `weekly`: Allows you to specify weekdays. (`Sunday, Monday, etc...`)
  * `custom`: Allows you to specify a custom predicate to evaluate with given event timestamp

**Note**: 
1. Only one of `allOf` or `anyOf` can be specified. If both exists, it will throw an error.
1. When evaluating ranges, the `from` field will be inclusive, while `to` field will be exclusive.

#### Examples
* Filter for the month of December. You can match across months or even consecutive years.
```yaml
acceptsWithin:
  anyOf:
    - type: seasonal
      from: "12-01"      # from field in inclusive
      to: "01-01"        # to field is exclusive
```

* Filter out winter season
```yaml
acceptsWithin:
  anyOf:
    - type: seasonal
      from: "12-01"
      to: "03-01"
```

* Filter out Dinner time (assuming its 6pm to 11pm)
```yaml
acceptsWithin:
  anyOf:
    - type: time
      from: "18:00"
      to: "23:00"
```

* Filter our midnight time (usually 11pm - 5am on the following day)
```yaml
acceptsWithin:
  anyOf:
    - type: time
      from: "23:00"
      to: "05:00"
```

* Filter out Weekend days. Should specify days in a comma separated string on `when` property.
```yaml
acceptsWithin:
  anyOf:
    - type: weekly
      when: "Saturday,Sunday"
```

* A custom filter which filters last week of every month. The expression must be written in Jdk-11 `java.time.*` compatible way.
  * For the function, you will receive a variable called `ts` which is a [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html) instance in user's timezone.
  * All necessary packages `java.time.*` will have automatically imported to the script. There is no need to write explicit imports here.
  * Function must return a boolean value, whether given time `ts` matches the criteria required.
```yaml
acceptsWithin:
  anyOf:
    - type: custom
      expression: |
        YearMonth currMonth = YearMonth.of(ts.getYear(), ts.getMonth());
        return ts.getDayOfMonth() >= currMonth.lengthOfMonth() - 7
```

* Filter out events falls in December evenings. To match all criteria, we can use `allOf` parameter.
```yaml
acceptsWithin:
  allOf:
    - type: seasonal
      from: "12-01"
      to: "01-01"
      
    - type: time
      from: "06:00"
      to: "23:00"
```