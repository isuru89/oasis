# Milestones

* All milestones are pre-defined before gameplay.
* Can be defined only by admin.
* Milestones never ends. They are played by players forever.
    * Milestone should be defined in a way which can play forever.
* At least two levels must be there for a milestone.
* A milestone can have only **finite** amount of levels.
* When a user has reached maximum level of a milestone, Oasis consider he/she has completed the milestone.
* Milestone aggregating values must be monotonically increasing.
    * Penalty values (negative values) will be accumulated separately along with positive values.
* Once user achieved a level, it cannot be undone.
  * Due to the penalty values, it might go below the current level. But it does not
  affect to the player's achieved level.
  
## Milestone Creation

* Milestones should be defined only using aggregation functions
    * Sum of points
    * Sum of KPIs
    * Count of events