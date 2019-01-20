
# Users
 * All users are uniquely identified by their _email_ address.
   * Email cannot be changed at all.
 * _username_ and _nickname_ can be modified at anytime.
 * User has a role within existing team.
 * A user belongs to exactly one team at any given time.
 * Every user is a player by default.

#### Users CRUD
 * Only admin can add/delete users.
 * Before the game starts, admin can pump initial users from the external system(s).
 * By doing so, all users should activate their account by logging once to the system.
   * Activation gives __N__ points to the user to start with. N will be decided by admin.
 * Regardless of the activation status, users will still receive points, badges and all other game elements.
 * New user registration is disabled.
 * Only admin and user him/herself is allowed to modify the user account details.
 * Username, nickname, gender, email, avatar will be publically available to any other user.

#### Reserved Users
 * There are three different reserved users in the system.
   1. Admin: _admin@oasis.com_
   2. Curator: _curator@oasis.com_
   3. Player _player@oasis.com_
 * Purpose is to audit the system with different roles
 * They belong to a predefined team and their teams can never be changed.
 * Their passwords are known only to the deployment team

## Roles
 * There are three main roles (increasing order of previleges)
   * PLAYER
   * CURATOR
   * ADMIN
 * Only one admin can exist in the system.
 * Curators and players can exist within a team scope.
 * TeamScope can have only maximum of 3 curators.
 * At the beginning all users are players.
 * Admin has the previleges to change role of anyone.
 * Admin has the authority to assign curators for a TeamScope.
 * Curators can change,
   * Role of players
   * Create teams
   * Assign team members within assigned TeamScope
 * Curator is still a player with additional previleges.

## Teams
 * Teams can be created by curators or admin scoped to a TeamScope.
 * Each team has a user to represent team itself.
    * It is identified by email, _user@<teamName>.oasis.com_ format.
 * Team name must be globally unique and case sensitive.
 * Once given, team name cannot be changed.
 * Team owner is the curator(s) of the TeamScope.
 * When creating new teams (except default team), at least 2 players must exist initially.
 * Teams cannot be deleted.
 * There is no limit to the number of players in a team.
 * A team belongs to one and only TeamScope ever.

## TeamScope
 * TeamScopes can be created _only_ by admin.
 * TeamScopes cannot be deleted.
 * TeamScope name must be globally unique case sensitively.
 * When created, a _default_ team will be created under it.
   * Its name is: **{teamscope}.default**
 * A new [TeamScope] user will be created under this default team.
 * A TeamScope can have as many as teams.


#### Team User
 * A new _team-user_ will be created for each team, at the time of team creation, except for __default__ team of the TeamScope.
 * Purpose is to represent team level achivements (points/badges/states, etc.) 
 * Name of _team-user_ will be '__user.{team}.oasis__'
   * Hence the name _user_ is reserved.
 * Email format of _team-user_ will be '__user@{team}.oasis.com__'
 * This auto-user will behave exactly as same as other regular players.

#### TeamScope User
 * A new _teamscope-user_ will be created for each TeamScope, at the time of TeamScope creation under the default team.
 * Purpose is to represent TeamScope level achivements (points/badges/states, etc.) 
 * Name of _teamscope-user_ will be '__teamscopedefault.{teamscope}.oasis__'
 * Email format of _teamscope-user_ will be '__teamscopedefault@{teamscope}.oasis.com__'
 * This auto-user will behave exactly as same as other regular players.

