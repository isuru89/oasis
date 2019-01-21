# Leaderboards

* Leaderboards are only defined from points.
* Leaderboard definitions can be created by Curators and Admin only.
  * Curators' leaderboards are defined only to their team-scope.
  * Admin's leaderboards are global and visible to all.
* Leaderboards can be defined while the game is playing.
* Leaderboards are calculated on-the-fly, will not be cached. (?)
* Leaderboards are defined within three dimensions.
    * Time range = daily / weekly / monthly / all time / custom ranges
    * Point Rules = set of point rule(s) to get sum of points
    * Scope = team-wise, team-scope-wise, global

## Leaderboard Definition

* Can include a set of point rule ids to calculate sum for.
* Can include a set of point rule ids to exclude from sum calculation.
  * Once exclude set has been defined, any rules other than specified will be taken into sum.
* **Cannot** define include set and exclude set in a single leaderboard definition.
* Order can be defined.
  * Either it must be ascending (asc) or descending (desc).
  * Default is _descending_.
  
## Tie Breakers

* When two users have same number of points, we will be using total event count contributed
 to the total points as tie breaker.
* The lower the count, will be the higher in rank.
* If it is to have same event count, then they will have the **same rank**.
  * For eg: Adam and Lily will get two ranks because their count is different although they have exactly same total points.
  
| User | Points | #Events | Rank |
|---     |---   |--- | --- |
| Jon | 7739 | 121 | 1 |
| **Adam** | 3864 | 78 | **2** |
| **Lily** | 3864 | 91 | **3** |
| Shanon | 2705 | 74 | 4 |
| Gabriel |  921 | 32 |  5 |

* In case, assume that Adam and Lily has the same count, then both of their ranks will be same.
  * Here Lily and Adam both will be ranked #2.
  * And Shanon will still be 4th, not 3rd.

| User | Points | #Events | Rank |
|---     |---   |--- | --- |
| Jon | 7739 | 121 | 1 |
| **Adam** | 3864 | 78 | **2** |
| **Lily** | 3864 | 78 | **2** |
| Shanon | 2705 | 74 | 4 |
| Gabriel |  921 | 32 |  5 |

* When you want to get top 3 from above leaderboard, it will return only based on rank.
  * Therefore, only Jon, Adam and Lily will be returned. 
  * Shanon will be ignored as her rank is 4th.