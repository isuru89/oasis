# How Feeds Works

## What are Feeds?
Feeds in Oasis are notifications that received by end users about their rewards in playing games. 
Feeds are generated by engine(s) and those will be delivered in near real-time to the end users based on the configured mechanism. 

All feed events will be delivered from engine to feeder component through event stream service.
Therefore, it is strongly recommended to have an implementation for feed stream with your custom event stream implementation.
By default, Kafka provides an implementation.

Feeds can take form of either push notifications or web-socket events depending on the medium used by the user.

### Example Feeds in Oasis
Basically, a game element can generate feeds if necessary, and by default,
all core elements will generate feeds.

* User scored points
* User rewards with a badge
* User achieves a milestone
* User won a challenge
* User's rating has been changed

## Feeds Delivery

The subscriptions will be automatically enabled for all users at the time of registration,
but users can choose themselves to opt out from those subscriptions as necessary.

![Feed Delivery](/docs/images/oasis-feeds.png?raw=true "Feeds Delivery")

As depicted in the image,
  1. Feeds will be generated by engine(s) and push them to the stream manager (default: kafka)
  2. The notification service will read those feed messages, and will hydrate with additional reference information.
  3. And then, those notifications will be delivered via 3rd party messaging systems.

#### Feed Payload
A sample feed will consist of below information.

```json
{
  "version": "1",
  "plugin": "<plugin-id>",
  "type": "<type-of-feed>",
  "eventTimestamp": 1656921284867,
  "scope": {
    "game": {
      "id": 12,
      "name": "Name of the game",
      "description": "Description of the game"
    },
    "source": {
      "id": 9328212,
      "name": "Origin event source of this feed's event"
    },
    "userId": {
      "id": 7084328432473,
      "name": "User name"
    },
    "team": {
      "id": 2241,
      "name": "Team name of the user"
    }
  },
  "data": {
    // ... payload depends on plugin and eventType
  }
  
}
```

| Field         | Type                                              | Description                                                                                                                                           |                                                                                                                             
|---------------|---------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| `version`     | string                                            | Version of this feed event. Default is `1`                                                                                                            |
| `plugin`      | string                                            | Indicates which plugin generated this feed event. Default plguins: `core:point`, `core:badge`, `core:milestone`, `core:challenge`, and `core:rating`. |
| `type`        | string                                            | Type of feed scoped to the plugin. Some plugins may emit different type of feed events. Based on the type, the content of `data` field may vary.      |
| `eventTimestamp` | number                                            | Actual timestamp of this reward achieved. This will contain the timestamp of the caused event to generate the reward in epoch milliseconds.           |
| `scope`       | object | Scope object of this feed.                                                                                                                            |
| `scope.game`  | object<br/>```{"id": number, "name": "{name}"}```                                            | Game id/name this feed belongs to. This field will be there with every feed.                                                                          |
| `scope.user`  | object<br/>```{"id": number, "name": "{name}"}```                                            | User id/name this feed belongs to.                                                                                                                    |
| `scope.team`  | object<br/>```{"id": number, "name": "{name}"}```                                            | Team id/name this user belongs to.                                                                                                                    |
| `scope.source` | object<br/>```{"id": number, "name": "{name}"}```                                            | External source id/name of the event that caused to generated this feed.                                                                              |
| `data`        | object                                            | This field will contain the actual feed data depending on the plugin and its type.                                                                    |

##### Available Feed Types and their data payload by Plugin

| Plugin           | Type              | Data Payload                                                                                                                               |
|------------------|-------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `core:point`     | POINT_SCORED      | { "ruleId": number, "pointsScored": number }                                                                                               |
| `core:badge`     | BADGE_EARNED      | { "ruleId": number, "rank": number }                                                                                                       |
| `core:milestone` | MILESTONE_REACHED | { "ruleId": number, "currentLevel": number, "previousLevel": number }                                                                      |
| `core:challenge` | CHALLENGE_WON     | { "ruleId": number, "position": number }                                                                                                   |
| `core:challenge` | CHALLENGE_OVER    | { "ruleId": number, "reason": "\<string\>" }  <br><br>  The `reason` field can take two forms either `TIME_EXPIRED` or `ALL_WINNERS_FOUND` |
| `core:rating`    | RATING_CHANGED    | { "ruleId": number, "previousRating": number, "currentRating": number }                                                                    |


### Last-mile Delivery

Last mile feed delivery to actual end users will be done by well popular and stable existing 
messaging services, such as, [Amazon SNS](https://aws.amazon.com/sns), [PubNub](https://www.pubnub.com/), [Pusher](https://pusher.com/) etc.
Or you can integrate it with your existing notification infrastructure as you wish.

**Note**: Default notification service will provide the ability to configure which 
messaging service to be used at the deployment time. As a developer,
you need plan ahead in terms of which service to use, since in any case
if you want o switch to another provider. The reason is that, the app
or site the end-users using needs to be updated with new service along
with backend deployments.

## Feeds Subscription
All feeds will be delivered through channel specific to the game.
So, any third-party subscription should be based on the game channel and then filter out team/user events separately.


## Integrate custom messaging service
If you want to integrate the messaging service as one of your preferred service,
you can do it by implementing the `FeedDeliverable` interface. Before that, you need
to add module `oasis-core` as a dependency to your project.

```java
import io.github.oasis.core.external.FeedDeliverable;

public class YourMessagingService implements FeedDeliverable {

    public void init(OasisConfigs configs) throws Exception {
        // initialize your messaging service
        // this will be called once the notification service starts
    }

    public void send(FeedNotification feedEvent) {
        // publish this feed event to your messaging service
        // If any error occurred it should be silently handled by this implementation.
    }

    public void close() {
        // close you opened connections to the service
        // This will be called once when notification service is shutting down.
    }
} 
```

Then specify this implementation class in the configs of `feeder.conf` file.

```
oasis {
  
  messaging {
    "impl": "io.github.oasis.ext.YourMessagingService"
    "configs": {
    
        // ... you can provide any custom configs as you need
        // and read them in the init() method.
        
    }
  }
  
  ... rest of configs
}
```

## Custom Notification Service
If you don't like using a provided notification service, you can stop deploying it. 
And you can directly consume feed entries by hooking into the stream manager.
This allows more freedom to be used by your other internal applications.

