# Setup Guide, Direct Telnet Connection to older LG TVs:

## Setup Overview:
1. Turn on LG IP Control mode.
2. Update `NvidiaShieldTVController/product_variant_config.csv` to reflect the correct input labels to match your setup.
```
[dtv / atv / cadtv / catv / avav1 / component1 / hdmi1 / hdmi2 / hdmi3 / hdmi4]
```
3. Disable the `USE_HOME_ASSISTANT` option and Set the *TV's static IP address* in `NvidiaShieldTVController/app/gradle.properties`
4. Enable developer options on the `Nvidia Shield`
5. Connect ADB to the `Nvidia Shield`
6. Build and install the apps you setup in the `product_variant_config.csv` file. Note: You _must_ include the Shield app as it contains the accessibility service for returning to the `Nvidia Shield`
7. Enable the `Shield TV Control` app accessibility service: Settings → Device Preferences → Accessibility → Shield TV Control
8. Disable accessibility services once everything is setup.

## Detailed Setup Steps:

1. Turn on LG IP Control Mode: 

    To enable IP Control Mode, turn on the LG TV.
    Hold down on the settings button (gear icon) on your remote for over 5 seconds. In the top left-hand corner, the channel information should appear.
    Quickly press the key combination 828[OK] on the remote. If that doesn't work, try navigating to "Network" or "Connections" and pressing 82888. I read that [here](https://github.com/WesSouza/lgtv-ip-control#setting-up-the-tv).
    A dialog should appear titled IP CONTROL SETUP
    Set the Network IP Control option to `ON`
    Below this, you will see the TV IP address. You can use this for step 2.


2. Set TV Input Values:

    Edit the `NvidiaShieldTVController/product_variant_config.csv` configuration values to match your specific inputs. The Nvidia Shield is the main controller, so it contains the accessibility service and must be included. All others are optional.
    If you have an LG TV, primarily this just means entering the correct input values to match the given input for each device. 

    Options are:
        
    ```
    [dtv / atv / cadtv / catv / avav1 / component1 / hdmi1 / hdmi2 / hdmi3 / hdmi4]
    ```

3. Set config values
   Set the  `NvidiaShieldTVController/app/gradle.properties` file to reflect the correct IP address for your TV, and set the boolean `USE_HOME_ASSISTANT` to false.
    
    The IP address of your LG TV should be set to a static value on your router to prevent it from changing.

4. Enable developer mode on the Nvidia Shield TVk

    Open the following menu tree:
    Settings → Device Preferences → About → Build
    Click on `Build` 8 times consecutively. 
    Developer mode should be enabled

    Open the following menu tree:
    
    Settings → Device Preferences → Developer options (at the bottom)
    
    Enable the `Network debugging` option.
    
    Once enabled, the IP address should appear as an option.
 
5. Download and Install Android Studio 

6. From a terminal, connect to the NVIDIA shield with the network debugging IP Address and adb. `adb connect shield_ip:port`
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


Note:
    One of the reasons I made and am open sourcing this project is because I generally distrust any application with accessibility privileges enabled. So this allows any individual to run this setup, but to also audit the code. The accessibility service code can be found in `NvidiaShieldTVController/app/src/shield/java/com/ashbreeze/shield_tv_controller/NavButtonService.kt`.
    This project also allows me to have slightly more intelligent controls between the inputs than a normal shortcut remapper would allow. Also, I ensured that the remote functions entirely normally with this enabled. The Netflix button even works like normal when on the shield. 
    When I first added the remapping accessibility service, it changed how the regular controls worked in strange ways. It took some experimentation to find the combination of flags that would allow everything to function normally.  This from what I could tell has 0 affect, but it's also why I chose to remap just the Netflix button. The other buttons were harder to try to replicate their functionality (e.g. home button)
    Originally, I tried to do multiple buttons, but realized getting their functionality to match perfectly wasn't straight forward. launching Netflix however, was straight forward :)

## Notes / Debugging
You should be able to telnet into the TV and change inputs manually
If you've enabled the IP Control Mode, you should be able to do some simple debugging to test things before getting everything else going. Just telnet into the TV:
```
telnet ${TV_IP_ADDRESS} 9761
```
Then run the following command to select hdmi1
```
INPUT_SELECT hdmi1
```
`NG` means failure, `OK` means success

Other input options:

```
[dtv / atv / cadtv / catv / avav1 / component1 / hdmi1 / hdmi2 / hdmi3 / hdmi4]
```


Feel free to reach out to me if you have any questions, I haven't really productionized this as I don't feel confident with these steps that it's really a truly consumer-friendly experience, but I believe something along these lines would be the future if all the TV companies could get their shit together and decide on a network protocol for all the TVs and devices to communicate. If that were to happen, this device could do this without any of these shenanigans.


