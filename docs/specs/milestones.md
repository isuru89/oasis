# Milestones

    * All milestones are pre-defined before gameplay.
    * Can be defined only by admin.
    * Milestones never ends. They are played by players forever.
        * Milestone should be defined in a way which can play forever.
    * At least two levels must be there for a milestone.
    * A milestone can have only finite amount of levels.
    * Milestones should be defined only using aggregation functions
        * Sum of points
        * Sum of KPIs
        * Count of events
    * When a user has reached maximum level of a milestone, Oasis consider he/she has completed the milestone.
    * Milestone aggregating values must be monotonically increasing.
        * Penalty values will be accumulated separately along with positive values.