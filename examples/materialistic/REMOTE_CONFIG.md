# Remote configuration

The app uses allow remote configuration to setup the internal Snowplow Android Tracker.

In the app you can specify the url of the remote config file or the url of json-server used to setup the tracker.

The app polls the backend regularly every 10 seconds to check for updates.

To setup in the app the url where to find the config file:
`Menu -> Settings -> Snowplow -> Config URL -> [Write here the complete url]`

## Structure of the config file

This is the structure of a demo json config file:

```
{
	"version": 0,
	"endpoint": <collector url>,
	"requestSecurity": "https",
	"httpMethod": "post",
	"namespace": "My-Namespace",
	"appId": "My-AppId",

	"base64": false,
	"applicationContext": true,
	"mobileContext": true,

	"geoLocationContext": false,

	"applicationCrash": true,
	"trackerDiagnostic": true,

	"screenviewEvents": true,
	"screenContext": true,

	"installTracking": true,

	"lifecycleEvents": true,
	"sessionContext": true,
	"foregroundTimeout": 1800,
	"backgroundTimeout": 1800
}
```

### Mandatory fields
- **version** [int]: it has to be updated for each config change in order to be updated in the app. The app checks the version number to decide whether update the tracker settings.
- **endpoint** [string]: collector endpoint (as it would be written in the tracker settings).
- **requestSecurity** [enum]: It can be "http" or "https".
- **httpMethod** [enum]: It can be "get" or "post".
- **namespace** [string]: Custom namespace.
- **appId** [string]: Custom app ID.

### Optional fields
- **userId** [string]: Custom user ID if needed, it can be omitted (useful for debugging - it should be set only on app).
- **base64** [boolean]: Whether the payload is base64 encoded.
- **applicationContext** [boolean]: Enable tracking of application fields.
- **mobileContext** [boolean]: Enable tracking of mobile fields.
- **geoLocationContext** [boolean]: Enable tracking of geo-location fields.
- **applicationCrash** [boolean]: Enable auto-tracking of crashes in the app.
- **trackerDiagnostic** [boolean]: Enable auto-tracking of internal errors in the tracker.
- **screenviewEvents** [boolean]: Enable auto-tracking of screenview events.
- **screenContext** [boolean]: Enable tracking of screen context added to all the events.
- **installTracking** [boolean]: Let the tracker send an event when the app runs for the first time after the installation.
- **lifecycleEvents** [boolean]: Enable auto-tracking of lifecycle events (background and foreground events).
- **sessionContext** [boolean]: Enable auto-tracking of session events and timeout as specified below.
- **foregroundTimeout** [int]: Seconds before to timeout the session when the app is in foreground state.
- **backgroundTimeout** [int]: Seconds before to timeout the session when the app is in background state.


## Provide config file through a JSON-Server

You can update the app through a JSON-Server able to provide the config file. The advantage of this solution is that you can update the JSON config file locally and it will take care to send to the app the updated configuration.

To launch the JSON Server locally:

1. Install NPM: `npm install -g npx`
2. Create a `db.json` file for the JSON Server
```
{
  "config": {
	"version": 1,
	"endpoint": "com-snplow-eng-aws-dev1.collector.snplow.net",
	"requestSecurity": "https",
	"httpMethod": "post",
	"namespace": "My-Namespace",
	"appId": "My-AppId",

	"userId": "Me",

	"applicationContext": true,
	"mobileContext": true,

	"geoLocationContext": false,

	"applicationCrash": true,
	"trackerDiagnostic": false,

	"screenviewEvents": true,
	"screenContext": true,

	"installTracking": false,

	"lifecycleEvents": false,
	"sessionContext": true,
	"foregroundTimeout": 30,
	"backgroundTimeout": 30
  }
}
```
3. Launch the JSON-Server: `npx json-server --watch db.json`
4. Test it works in the browser on: `http://localhost:3000/config`
5. To expose it publicly and to the app in your device you can use `ngrok` (require installation) launching simply:  `ngrok http 3000`

To setup in the app the url where to find the config file:
`Menu -> Settings -> Snowplow -> Config URL -> <ngrok-url>/config`

The app shows a quick toast message everytime the config is successfully updated.

### How to use JSON-Server and Snowplow Micro together

The basic configuration for ngrok doesn't allow to have multiple instances running together. For this reason it would be hard to test the app getting the config file from a local JSON-Server and sending events to a local Snowplow Micro.
This problem can be solved updating the ngrok configuration.

Once ngrok is registered with the proper `authtoken`:
1. Edit `ngrok.yml` in `~/.ngrok2/`
2. Just below the `authtoken: <your token>` entry add:
```
tunnels:
  mini:
    addr: 2000
    proto: http
  node:
    addr: 4002
    proto: http
  apache:
    addr: 80
    proto: http
  micro:
    addr: 9090
    proto: http
  jsonserver:
    addr: 3000
    proto: http
```
3. Now you can launch multiple services: `ngrok start jsonserver micro` (or whichever service you prefer).

