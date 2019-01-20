# Race

Races runs periodically and winners will be awarded points.

Races are defined by admin before the game starts.

* Races are always running for continuous fixed time periods.
    * Such as, daily, weekly, monthly.
* Races are build on top of points. Hence very similar to leaderboards.
* Races can be defined using various aggregator types.
    * Leaderboards are defined only using SUM.
    * Races can be defined using other aggregator functions such as AVG, MIN, MAX, COUNT, including SUM as well.
* Unlike leaderboards, at the end of time period, the top/bottom N will be awarded points.
* Races can be defined in Global, TeamScope and Team levels.
* New races can only be defined by Admin

### Use Cases

* When a user changes his/her team scope within a period of race?
    * User will score only for the team he/she is currently at the time of awarding points.
    * So, changing team will have less advantage for the user only for that period of time.
    * Any other previous scopes will discard.
    * Reason is that, with the aggregation type of MAX or MIN, there is an unfair advantage
      for the user because he/she can score from all scopes falling to that period.

* When number of users in a team or team-scope is less than the top N, then do all those players receive points?
    * If there is only one player in the team, then that player won't get any points
    * If number of players less than top N (N > 1), then, only top player will get points.
