# Setup Guide, Home Assistant:

## Setup Overview:
1. Install the app variants you want to use (see installation options below). **IMPORTANT: You MUST install the Shield variant** as it is the main app which houses the accessibility service and configuration for all variants.
2. Enable the `Shield TV Control` app accessibility service and configure it: Settings → Device Preferences → Accessibility → Shield TV Control → Configuration
3. In the Configuration screen, enter your Home Assistant base URL, long-lived access token, and the package name for the app you want to return to when pressing the Netflix button (e.g., `com.netflix.ninja`)
4. Create a new Home Assistant automation to subscribe to the `nvidia_shield_tv_request` event type and perform the desired actions. You can use the *Choose* action and filter conditionally with the *Value Template*: `{{ trigger.event.data.command == "SELECT_TV_INPUT" and trigger.event.data.value == "shield"}}`
The default `event.data.value` is the name of the build variant. i.e. one of:
`shield, speaker, nintendo_switch, pc, ps4, ps5, xboxone, gameconsole`

**Note:** Configuration is now done dynamically through the Shield TV Control accessibility settings menu (Device Preferences → Accessibility → Shield TV Control → Configuration). You no longer need to modify gradle.properties or create token files!

## Installation Options:

### Option 1: Download and Install APKs via Browser
1. Download the pre-built APKs for the variants you want to use (**Shield variant is required**)
2. On your Nvidia Shield, use a browser to download the APK files
3. Install each APK by opening the downloaded files
4. You may need to enable "Unknown sources" in Settings → Security & restrictions

### Option 2: Sideload via ADB
1. Enable developer options on the Nvidia Shield:
   - Settings → Device Preferences → About → Build (click 8 times)
   - Settings → Device Preferences → Developer options → Enable Network debugging
2. Connect to the Shield via ADB: `adb connect <shield_ip>:<port>`
3. Install the APKs (**Shield variant is required**): `adb install path/to/shield-variant.apk`
4. Install additional variants as needed: `adb install path/to/other-variant.apk`
5. Optionally disable developer options when complete

## Configuration

1. Enable the `Shield TV Control` app accessibility service and configure it: Settings → Device Preferences → Accessibility → Shield TV Control → Configuration

![Finding Accessibility Menu](./screenshots/configuration-1-menu-finding-accessibility.jpg?raw=true "Finding Accessibility Menu")
![Finding App in Accessibility Menu](./screenshots/configuration-2-menu-finding-app.jpg?raw=true "Finding App Accessibility in Accessibility Menu")
![Shield Accessibility Options](./screenshots/configuration-3-menu-accessibility-options.jpg?raw=true "Shield Accessibility Options")

3. In the Configuration screen, enter your Home Assistant base URL, long-lived access token, and the package name for the app you want to return to when pressing the Netflix button (e.g., `com.netflix.ninja`) You'll likely want to set this to `org.jellyfin.androidtv`

![Configuration Screen](./screenshots/configuration-4-configuration-screen.jpg?raw=true "Configuration Screen")

## Home Assistant Setup

Note:
This assumes you already have the TV or device you wish to control already integrated with Home Assistant. In the example of the LG TV, that I use, it was as easy as adding the LG WebOS TV integration and following the setup steps.

1. Create a new automation to subscribe to the `nvidia_shield_tv_request` event type.
 
2. For each trigger value, hook up the relevant action to be performed. i.e. Change the TV's input.
 
The default `event.data.value` is the name of the build variant. i.e. one of:
`shield, speaker, nintendo_switch, pc, ps4, ps5, xboxone, gameconsole`

Here is an example of my automation configuration yaml file:
```yaml

alias: Nvidia Shield Input Source Controller
description: ""
trigger:
  - platform: event
    event_type: nvidia_shield_tv_request
    event_data: {}
condition: []
action:
  - choose:
      - conditions:
          - condition: template
            value_template: >-
              {{ trigger.event.data.command == "SELECT_TV_INPUT" and
              trigger.event.data.value == "ps4"}}
        sequence:
          - service: media_player.select_source
            target:
              device_id: 82320ac14909b682c69011be87520f89
            data:
              source: Playstation 4
      - conditions:
          - condition: template
            value_template: >-
              {{ trigger.event.data.command == "SELECT_TV_INPUT" and
              trigger.event.data.value == "shield"}}
        sequence:
          - service: media_player.select_source
            target:
              device_id: 82320ac14909b682c69011be87520f89
            data:
              source: Nvidia Shield
      - conditions:
          - condition: template
            value_template: >-
              {{ trigger.event.data.command == "SELECT_TV_INPUT" and
              trigger.event.data.value == "nintendo_switch"}}
        sequence:
          - service: media_player.select_source
            target:
              device_id: 82320ac14909b682c69011be87520f89
            data:
              source: Nintendo Switch
mode: single
```


### Additional Debugging

You can use the Developer tools to help debug the event prior to setting up the automation.

Under Developer Tools → Events → Listen to events, subscribe to the `nvidia_shield_tv_request` and when launching the Android apps, you should see events being fired.
If this is not the case, likely you should pursue some further debugging on the Android app side with logcat.


Feel free to reach out to me if you have any questions, I haven't really productionized this as I don't feel confident with these steps that it's really a truly consumer-friendly experience, but I believe something along these lines would be the future if all the TV companies could get their shit together and decide on a network protocol for all the TVs and devices to communicate. If that were to happen, this device could do this without any of these shenanigans.


