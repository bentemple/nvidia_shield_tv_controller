# Setup Guide, Home Assistant:

## Setup Overview:
1. Update the config file `NvidiaShieldTVController/app/gradle.properties` with the correct base url to your Home Assistant instance.
2. Create a long lived token in Home Assistant and save it to: `NvidiaShieldTVController/home_assistant_token`
3. Enable developer options on the `Nvidia Shield` and enable network debugging.
4. Install Android Studio
5. Connect ADB to the `Nvidia Shield` via Network Debugging.
6. Build and install the build variants you want to use. Note: You _must_ include the Shield app as it contains the accessibility service for returning to the `Nvidia Shield`
7. Enable the `Shield TV Control` app accessibility service: Settings → Device Preferences → Accessibility → Shield TV Control
8. Create a new Home Assistant automation to subscribe to the `nvidia_shield_tv_request` event type and perform the desired actions. You can use the *Choose* action and filter conditionally with the *Value Template*: `{{ trigger.event.data.command == "SELECT_TV_INPUT" and trigger.event.data.value == "shield"}}`
The default `event.data.value` is the name of the build variant. i.e. one of:
`shield, speaker, nintendo_switch, pc, ps4, ps5, xboxone, gameconsole`
9. Disable developer options once everything is setup and working.

## Preparing The Shield and Apps:

1. Set config values
   Set the  `NvidiaShieldTVController/app/gradle.properties` file to reflect the correct URL for your HomeAssistant instance. Do *not* include a trailing `/`, and set the boolean `USE_HOME_ASSISTANT` to true.

2. Create a token in Home Assistant for your user, and save it to a new file in the top level of the Android project: `NvidiaShieldTVController/home_assistant_token`
   More details on how to obtain a token can be found [here](https://developers.home-assistant.io/docs/auth_api/#long-lived-access-token)

3. Enable developer mode on the Nvidia Shield TVk

    Open the following menu tree:
    Settings → Device Preferences → About → Build
    Click on `Build` 8 times consecutively. 
    Developer mode should be enabled

    Open the following menu tree:
    
    Settings → Device Preferences → Developer options (at the bottom)
    
    Enable the `Network debugging` option.
    
    Once enabled, the IP address should appear as an option.
 
4. Download and Install Android Studio 

5. From a terminal, connect to the NVIDIA shield with the network debugging IP Address and adb. `adb connect shield_ip:port`
NOTE: You may need to lookup guides on how to get ADB working. As of right now I don't have a config setup for the app, so the IP port is hard-coded into the app. Pull requests are welcome to make configuring the app easier by writing to a config file which would enable me to create pre-built APKs, vastly simplifying this process. I just haven't gotten around to it yet.

6. Run the `NvidiaShieldTVController` project for each *Debug* variant to install

    Open the `NvidiaShieldTVController` project with android studio
    At some point, when Android Studio loads, it hopefully will detect the Nvidia Shield TV. When an option to enable debugging appears, allow your computer.
    Once the project loads, select the variants you want on the TV and run each one:
    
    In the bottom left corner of the Android Studio window is a build variants drawer.
    Select this and using the drop-down you can pick each variant. Likely you'll want to run the Debug variant.
    Release variant requires a signing config file, which you'd have to create. It's something for distributing versions of the app.
    Then, at the top of Android Studio, there should be a green run button.
    Run that. When complete, it should flash on your TV.
    If a different input will be selected for a given variant, when you run it from Android Studio, the TV input should also change.

7. Enable Accessibility service:

    You must enable the accessibility service associated with the Shield variant app for the Netflix button to work and bring you back to the shield when on another input. See `Use` section at the top of the file for more details (Default name: "Nvidia Shield")
    
    Open the following menu tree:
    
    Settings → Device Preferences → Accessibility → Shield TV Control
    
    Use Shield TV Control? OK
    
8. Disable Developer Options when everything is working

    You don't need to leave developer options enabled once you have everything working. To disable them once you have everything working and tested, follow these steps:
    
    Open the following menu tree:

    Settings → Device Preferences → Developer options (at the bottom)
    
    Turn off the Developer Options setting. Leave the developer options menu, and they should no longer be visible at the bottom of the menu. You should be permanently good to go.

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


