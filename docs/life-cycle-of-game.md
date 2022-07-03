# Life Cycle of a Game

### Starting a Game
 1. Admin defines a new game in the admin API
    1. This will cause to create a game record in admin db
    2. Also, it will publish a game event called `CREATED` so that running game engines will receive it and prepare the workspaces.
 2. Admin creates game rules
    1. Here it will also create necessary element records in admin db
    2. Also, publishes rule events scoped to the game called `RuleCommand.ChangeType.ADD`
    3. Game engines will also read these rule add events and prepares internal state before processing any game events
 3. Admin starts a game
    1. This publishes a game event called `STARTED`
    2. Game engines will now begin listening to the game events topic for that particular game.
 
### Stopping a Game

A `STOPPED` game should be able to restart again at a later time.
This can be used to publish new game rules or remove any existing rules from a game.

 1. Admin stops a game from admin API.
    1. This will mark the game as stopped in admin db
    2. And publishes a game event for all engines to stop processing game events.
    3. As soon as a game engine received this type of event, it will stop processing game event and will close the event consumers.
    4. Game engines will not clear game stats at this point.
    5. After that, it will still keep listening to broadcast channel for any further game updates

### Resuming a Stopped Game

 1. To resume a game, Admin will start an already stopped game from Admin API.
    1. This will mark the game as started in admin db.
    2. Also publishes a game event called `STARTED` to all game engines
    3. Game engines will create new consumers to fetch game events and starts processing


### Remove a Game

A `REMOVED` game will not be able to start again. And this will flush
all the stats and rewards won by the game players. Note that only stopped games
can be removed.

 1. Admin will remove a stopped game
    1. This will mark the game as inactive in admin db
    2. It will publish a game event called `REMOVED` to all game engines
    3. Upon receiving this event, game engines will clear all game related internal states and stats recorded
    4. Event API will ultimately stop accepting events for particular game.


