[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Build Status](https://github.com/isuru89/oasis/workflows/Oasis-ci-test/badge.svg)
[![Known Vulnerabilities](https://snyk.io/test/github/isuru89/oasis/badge.svg)](https://snyk.io/test/github/isuru89/oasis)
[![coverage](https://codecov.io/gh/isuru89/oasis/branch/master/graph/badge.svg)](https://codecov.io/gh/isuru89/oasis)


_This project is still under development_

# OASIS
Open-source Gamification framework based on Redis.

Oasis, is an event-driven gamification framework having ability to define the game rules for events
coming from your applications. This is inspired from Stackoverflow badge system, and extended into
supporting many other game elements, such as, points, badges, leaderboards,
milestones, challenges, and ratings.

## Features:
* Different types of customizable [gamification elements](#game-elements).
* Near real-time status updates
* Embeddable game engine
* Users can play in teams
* Modular design, so its easier to extend to the specific needs
* Out of order event support

## Contents
* [Architecture of Oasis](#architecture-of-oasis)
* [Running Modes](#running-modes)
    * [Embedding Engine](#embedding-engine)
    * [Engine as a service](#engine-as-a-service)
* [Quick Demo](#quick-demo)
* [Concepts](#concepts)
    * [Participants](#participants)
    * [Game Elements](#game-elements)
* [Why Oasis?](#why-oasis)
* [Contributing](#contributing)
* [Roadmap](#roadmap)
* [Kudos!](#kudos)
* [License](#license)

## Architecture of Oasis
![Oasis Architecture](/docs/images/oasis-arch.png?raw=true "Oasis Architecture")

* **Events API**: The api can be exposed to public networks where event-sources can publish events to the framework.
  This authorize the event sources and game model before accepting any events.
* **Admin/Stats API**: This api is for manipulating [game model](#gamification-model) (or in other word admin-related operations) and
  querying statistics. Through this API, model entities can be created/updated/removed and those
  changes will be reflected in engine too. This API is only for internal purpose.
* **Engine**: This is the heart of the framework which create rewards by evaluating received events based on the defined
  rules in the system.

As of initial version, Redis will act as the database for engine operations considering the performance.


## Running Modes

**Note**: The important thing about Redis.

> Redis must be configured in [Replication mode](https://redis.io/topics/replication) 
to not losing data. Because all earned game rewards and current processing states are being stored in the Redis.
That means primary database for the engine is the Redis. If you want to store those rewards 
in a more durable database like SQL, MongoDB or any NoSQL solution, you can explicitly
intercept through engine by providing a custom signal subscription instance.

### Embedding Engine

One of the important thing about Oasis is that you can embed the game engine
inside your application. In this way, you have to pump all your events and getting
data/statistics out of Redis.

Its very simple.

```java
public static void main(String[] args) throws Exception {
    // load the engine configuration (we use typesafe configs here)
    var oasisConfigs = OasisConfigs.defaultConfigs();
    
    // initialize the engine database: redis
    var dbPool = RedisDb.create(oasisConfigs);
    dbPool.init();

    // required for storing individual events for some game element rules.
    // for now you can use the same Redis instance to store events.
    var eventStore = new RedisEventLoader(dbPool, oasisConfigs);

    // add elements as you desired for your application
    EngineContext context = EngineContext.builder()
            .withConfigs(oasisConfigs)
            .withDb(dbPool)
            .withEventStore(eventStore)
            .withSignalSubscription(subscription)  // OPTIONAL: if you want to tap into produced rewards.
            .installModule(RatingsModuleFactory.class)
            .installModule(PointsModuleFactory.class)
            .installModule(MilestonesModuleFactory.class)
            .installModule(ChallengesModuleFactory.class)
            .installModule(BadgesModuleFactory.class)
            .build();
    
    var engine = new OasisEngine(context);
    engine.start(); 
    
    // first notify game create and start events to game engine
    engine.createGame(gameId);
    
    // let's parse the game rules from yaml file
    var gameDef = GameParserYaml.fromFile(gameDefFile);
    engine.startGame(gameId, gameDef);
    
    // now you can submit events
    engine.submit(event1);
    engine.submit(event2);
    ...
        
        
    // later, if you think the game is over, you can signal it to the engine
    // so it will accepts no more events for that game and clean up anything related to the game.
    engine.stopGame(gameId);
}

```

**Note:** Once you start the engine, it will keep running until the application goes down.

Check the methods of `OasisEngine` class how you can submit your events/rules/game commands.

### Engine as a service

This is a full deployment with all the components as shown in [Architecture](#architecture-of-oasis).
This provides out-of-the-box components which can be used by your applications.

For testing purpose, a docker compose setup has been provided to up and running locally.
[See Quick Demo](#quick-demo) section to test that out.

Kubernetes and AWS solution are still pending.

## Quick Demo

You can run a simulation in your local computer using docker-compose.

Prerequisites:
  * Docker compose must be installed ([refer official site](https://docs.docker.com/compose/install/))
  * JDK 11 or higher must be installed

Steps:
  * Clone this repository
  * Run `sh build.sh` from the project root directory to build the project and create docker images
    * Once the script is completed, you will see the components are up and running with the help of docker-compose
  * Run the simulation using script inside _simulations_ directory. 
    * Change directory to `cd simulations`
    * Then execute, `sh run-simulation.sh`
  * Open the api documentation from here 
    [http://localhost:8010/api/swagger-ui/index.html?configUrl=/api/v3/api-docs/swagger-config](http://localhost:8010/api/swagger-ui/index.html?configUrl=/api/v3/api-docs/swagger-config)

**Notes**:
  1. In a restart, the data stored through admin api will be lost because by default
it is configured to use in-memory H2 database. MySQL connector is shipped with by default,
     and you can change jdbc configs in buildscripts/stats-api.config file, 
     if you want to use it for more durable data.
   2. Currently, multiple engines are not supported through docker compose. because engine has
been assigned a static id.
  3. Redis and RabbitMQ data will be stored across restarts through bounded volumes in `.tmpdata` 
folder. If you want to clear all the data, delete the contents of _redis_ and _rabbit_ folders.

## Concepts

This section will introduce you to the concepts around the Oasis framework.

## Rules of Oasis

Below rules are fundamental assumptions when building Oasis.

* **Rule-1**: There can be many Games running at the same time.
* **Rule-2**: A Player can play in many Games at once.
* **Rule-3**: A Player can play a Game _only_ by association with one Team.
  Assigning to multiple Teams within a single Game is not allowed.
* **Rule-4**: A Player may change his/her Team, or leave the Game completely while the game is in progress.
* **Rule-5**: An Event-Source and Game has a many-to-many relationship,
  so that a Game can have multiple sources while a source may push events to multiple games.
* **Rule-6**: A game should have at least one element rule defined. 
* **Rule-7**: And those rules can be added/removed to a game while it is running. 
  Modification to an existing rule may affect to its old data and its solely dependent on the rule's implementation.
  Framework does not guarantee a smooth migration to existing data in case of a rule modification.

## Participants
It is very important to understand supporting model before going down to the details.
There are four major participants in the system.

### Entities
#### Game:
A game is a boundary of all game elements. Manipulation of games can only be done by an admin thorugh Stats API.


#### Player:
A player is an entity who associate with events and get rewarded from elements.
Players can register to the system only by Admins and they will be uniquely identified by email address.
Also Players needs to associate with a game through a team. That means every player must be a member of team.


If an event cannot be correlate to a existing player in the system, that event will be discarded and will
not be processed by engine. So, it is important to register all applicable users to the system before such
event is arrived. Still you can add players while the game is running.

#### Team:
A team is formed by grouping several players together within a game.
A team name must be unique across all games (i.e. system).

Purpose of a team is to associate players to a game, plus,
provide leaderboards based on awards generated from game rules.
Sometimes it is not fair someone to compete with whole players of a game.


#### Event Source:
An external/internal party which generated actual events which can be evaluated by engine.
These event sources must be first registered with the system and download the private key.
Only an Admin can register event sources and map those to games.
And after that, private key must be used to sign the payload of events being dispatched.

Once received those events to Events API, it will check for the integrity of messages by comparing
the signature and will allow further processing.


## Game Elements

All these game elements except ranks can be defined in yaml files and register to the engine.
Ranks must be defined along with a game through admin-api.

### [Ranks](docs/specs/ranks.md)
Each game should define set of ranks so that can reward a variation.
For e.g. ranks equivalent in Stackoverflow are gold, silver and bronze.

### [Points](docs/specs/points.md)

One of the core element type in Oasis. The points indicate a measurement about a user against an event.
Users can accumulate points over the time as rules defined by the admin or curator.
Sometimes, points can be negative, hence called penalties.

### [Badges](docs/specs/badges.md)

A badge is a collectible achievement by a user based on correlating one or several
events. Every badge can associate with a rank.

There are several kinds of badges supported by Oasis.

* An event has occurred for the first time (eg: [Stackoverflow Altruist badge](https://stackoverflow.com/help/badges/222/altruist) )
* An event satisfies a certain criteria (eg: [Stackoverflow Popular Question](https://stackoverflow.com/help/badges/26/popular-question) )
    * For different thresholds can award different sub-badges
    * (eg: [Stackoverflow Famous question](https://stackoverflow.com/help/badges/28/famous-question) )
* Streaks
    * Satisfies a condition for N consecutive times. (e.g.: [Stackoverflow Enthusiast](https://stackoverflow.com/help/badges/71/enthusiast) )
    * Satisfies a condition for N consecutive times within T time-unit.
    * Satisfies a condition for N times within T time-unit. (e.g.: [Stackoverflow Curious badge](https://stackoverflow.com/help/badges/4127/curious) )
* Earn K points within a single time-unit (daily/weekly/monthly)
    * E.g.: [Stackoverflow Mortarboard badge](https://stackoverflow.com/help/badges/144/mortarboard)
* Daily accumulation of an event field is higher than a threshold (T) for,
    * N consecutive days. (eg: Earn 50 daily reputation for 10 consecutive days)
    * N separate days (eg: Earn 200 daily reputation for 50 consecutive days)

### [Milestones](docs/specs/milestones.md)

Milestone can be created to accumulate points over the lifecycle of a game.
It indicates the progress gained by a user. Milestones are always being based on the points
scored by a user.

### [Challenges](docs/specs/challenges.md)

Challenge can be created by a curator at any time of game lifecycle
to motivate a user towards a very short term goal. 

See more detailed information about challenges from [here]()

### [Ratings](docs/specs/ratings.md)

Ratings indicate the current state of a user at a particular time. 
Based on the events, player's  status will be calculated.

### [Leaderboards](docs/specs/leaderboard.md)
Oasis provides leaderboards based on points scored by users. 
Leaderboards are scoped to game and team levels.

## Why Oasis?

Ultimate objective of the Oasis is to increase the user engagement in applications
through a gamified environment.
Oasis might help your applications/community to increase the productivity
and could help in creating a better and enjoyable experience.

Following gamifiable environments have been identified.
- SDLC: whole software development lifecycle (coding, bug fixing, deployments) using the
  application stack (Code Quality, CI/CD, ALM tools)
- Support Systems: IT helpdesk systems
- Customer Loyalty frameworks
- Q/A sites: Stackoverflow, Reddit like sites
- Social Networking

## Roadmap
* Character driven game playing
* Narrative Play
* Cloud friendly

## Contributing
TBW

## Kudos!

This project could not have existed thanks to these awesome open-source projects.

* [Redis](https://redis.io/)
* [Kafka](https://kafka.apache.org/)
* [Akka](https://akka.io/)
* [Vert.x](https://vertx.io/)
* [Spring-boot](https://spring.io/projects/spring-boot)
* [MVEL](https://github.com/mvel/mvel)

## License

Apache License - version 2.0
