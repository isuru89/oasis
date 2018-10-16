# Race

Races are pre-defined by admin when the gameplay started.

    * Races are always running for continuous fixed time periods.
        * Such as, daily, weekly, monthly.
    * Races are build on top of points. Hence very similar to leaderboards.
    * Races can be defined using various aggregator types
        * Leaderboards are defined only using SUM.
        * Races can be defined using other aggregator functions such as AVG, MIN, MAX, COUNT.
    * Unlike leaderboards, at the end of time period, the top/bottom N will be awarded points.
    * Races can be defined in Global, TeamScope and Team levels.
    * New races can only be defined by Admin

### Use Cases

    * When a user changes his/her team scope within a period of race?
        * User will score only for the team he/she is currently at the time of awarding points.
        * Any other previous scopes will discard.
        * Reason is that, with the aggregation type of MAX or MIN, there is an unfair advantage
          for the user because he/she can score from all scopes falling to that period.

