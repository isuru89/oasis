# OASIS
Open-source BPLM Gamification framework

_This project is still under development_

Oasis, is a event-driven gamification framework where you can define the game rules for events
from you applications and let the players play on top of them.
There is no restriction in terms of which application you can use with this framework, as long as
you can generate events, which indicates key performance indicators (KPIs), from them.

In Oasis, the admin can define several gamification elements, such as, Points, Badges, Leaderboards,
Milestones, Challenges, and Races.

Players can accumulate points and badges for the events they correlated,
and will automatically compete in leaderboards, milestones and races.
Top competitors will be awarded more points in turn.
And also, earned points can be used to buy items for your playing hero and upgrade its abilities and compete
with whole other teams.

## Features:
  * Many different customizable gamification elements. (see below)
  * Near real-time status updates
  * Character driven game playing
  * Supports teams and projects
  * Highly customizable theme to the playing environment
  * Embeddable leaderboards and avatar widgets inside official apps
  * Cloud friendly

## Key Gamification Elements in Oasis

### Points
A user can score points based on KPIs in the events. The point rules are pre-defined
before the gameplay. However rules can be added later, when a new application is going to be integrated
or new events are ready to be generated.
Points are the primary element in Oasis and it drives many other elements too.

### Badges
A badge is a collectible achievement by a user based on correlating one or several
events. Badges are again pre-defined before the gameplay. Set of users can form a team
and then teams can compete together.

Here are the some badge rule types you can define in Oasis.
  * An event is occurred for the first time (eg: Stackoverflow Altruist badge https://stackoverflow.com/help/badges/222/altruist)
  * An event satisfies a certain criteria (eg: Stackoverflow Popular Question https://stackoverflow.com/help/badges/26/popular-question)
     * For different thresholds can award different sub-badges
     * (eg: Stackoverflow famous question https://stackoverflow.com/help/badges/28/famous-question)
  * Streaks:
     * Satisfies a condition for N consecutive times. (eg: Stackoverflow https://stackoverflow.com/help/badges/71/enthusiast)
     * Satisfies a condition for N consecutive times within T time-unit.
     * Satisfies a condition for N times within T time-unit. (eg: Stackoverflow curious badge https://stackoverflow.com/help/badges/4127/curious)
  * Earn K points within a single time-unit (daily/weekly/monthly)
     * Eg: Stackoverflow Mortarboard badge https://stackoverflow.com/help/badges/144/mortarboard
  * Daily accumulation of an event field is higher than a threshold (T) for,
     * N consecutive days. (eg: Earn 50 reputation for 10 consecutive days)
     * N separate days (eg: Earn 200 daily reputation for 50 consecutive days)
  * From milestone completion
  * Manually
     * Curators and admin can award badges to players based on non-measurable activities.

### Leaderboards
Admins or curators can define leaderboards based on points.
Rankings of players will be calculated in three scopes.
    1. Team-wise
    2. TeamScope-wise (project-wise)
    3. Global

Rankings can be calculated for a particular time range and it can be specified dynamically
when requesting for a leaderboard.

### Milestones
Milestone can be created to accumulate points forever to make players play the game for longtime.
It indicates the progress gained by a user. Milestones can be broken down to
levels. And completion of each level will award bonus points or badges to the users.

Eg: Total Reputation earned can be defined as a milestone definition and levels
can be defined in such a way,
  * Scoring 10k reputation - Level 1
  * Scoring 50k reputation - Level 2
  * Scoring 100k reputation - Level 3
  * Scoring 500k reputation - Level 4
  * Scoring 1M reputation - Level 5

### Challenges
Challenge can be created by a curator to motivate towards a very short term goal. Usually
a curator can create a challenge for a certain event to come first and once it received,
the user correlated with that event will be considered as a winner. Once a winner is declared,
the challenge is completed. Also a challenge can be expired by time as well.

Or in another way, challenges can be defined to have multiple winners. For eg:
a challenge can be defined to set award points for first 3 winners.

#### Races
Races are point-awarded leaderboards for non-overlapping time windows. At a pre defined time range
(daily, weekly, monthly), leaderboard winners will be awarded set of points. This will
continue in each time window as specified in a race.

#### States
States indicates the current state of a user/team/team-scope. Based on the events, user's
status will be calculated and from that status, some amount of net points will be awarded.
A user can only be in one state at a time.

For eg: someone can define a state (good/bad) based on total good answer ratio. As long as
a user has positive good answer ratio, then that user will have, say 100 points, with him/her.
Once the ratio goes down below a threshold, status will be changed to _'bad'_ and he/she will
loose 100 points he had.

## Why Oasis?

Ultimate objective of the Oasis is to increase the user engagement in working places
through a gamified environment. Oasis might help your organization/community to increase the productivity
and could help in creating a better and enjoyable place to work.

Following gamifiable environments have been identified.
   - SDLC: whole software development lifecycle (coding, bug fixing, deployments) using the
   application stack (Code Quality, CI/CD, ALM tools)
   - Support Systems: IT helpdesk systems
   - Q/A sites: Stackoverflow like sites
   - Social Networking

## Kudos!

This project could not have existed thanks to these awesome open-source projects.

  * [Spring-boot](https://spring.io/projects/spring-boot)
  * [Apache Flink](https://flink.apache.org/)
  * [MVEL](https://github.com/mvel/mvel)

