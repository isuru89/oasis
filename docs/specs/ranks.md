# Ranks (_formerly Attributes_)

Rank represents a variation of a particular type of reward.
For e.g. badge variations in Stackoverflow, such as Gold, Silver and Bronze are ranks.

 * Ranks are associated to a game, and they must be unique within the game.
 * Ranks are identified/referenced by its id in rewards.
 * Ranks cannot be deleted once added to a game.
   * Because, players might have scored the ranks related awards already.
 * Each rank can have a color for the purpose of identification to external parties.

### Example Ranking sets
  * _Medal Rankings_: [ Bronze, Silver, Gold, Platinum ]
  * _Expertise Rankings_: [ Beginner, Intermediate, Expert ]
  * _Military Rankings_: [ Private, Corporal, Sargent, Major ]

## API Specification

| EndPoint                    | Parameters                              | Description                                          |
|-----------------------------|-----------------------------------------|------------------------------------------------------|
| POST `/game/{gameId}/ranks` | * `gameId`: game id to insert this rank | Allows inserting a new rank definition under a game. |
| GET `/game/{gameId}/ranks`  | * `gameId`: game id to fetch            | Fetch all defined ranks under a game.                |

## Specification
| Field       | Description                                                                                  | Required |
|-------------|----------------------------------------------------------------------------------------------|----------|
| `name`      | A shorter name to identify this rank.                                                        | yes      |
| `priority`  | Order of ranking. This number will be sorted and assumes higher number has highest priority. | yes      |
| `colorCode` | Hex color code to represent this ranking as a metadata. Engine does not use this at all.     | no       |

## Examples

 * POST `/game/{gameId}/ranks`
   * Request Body:
```json
{
   "name": "Gold",         
   "priority": 1,           // order of ranking
   "colorCode": "#FFD700"
}
```
  * Response Body:
```json
{
   "id": "1",
   "name": "Gold",          
   "priority": 1,           
   "colorCode": "#FFD700"
}
```