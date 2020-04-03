# Nvidia Shield TV Controller
This project is what I used to consildate all of my TV remotes down to just the Nvidia Shield TV Remote. I have an `Nvidia Shield TV Pro (2019)` connected to my LG tv via HDMI 1, a `Playstation 4` on HDMI 2, and a `Nintendo Switch` on HDMI 3. With this application (3 applications) installed on my tv, I can select each application `Nvidia Shield TV Pro`, `Playstation 4`, or `Nintendo Switch` and it will automatically switch to the associated input for the given device. I then can press on the netflix button to return to the shield at any time. The netflix button works like normal while the Nvidia Shield is the selected input.

## Use:
Once setup, will create icons to whatever inputs you'd like to control on your tv (or app icons to perform any action and run anything. Should be relatively easily extensible.

Currently setup so that pressing the `netflix` button when the shield input isn't selected, will select the shield input.
When the Nvidia Shield input is selected pressing the `netflix` button will perform the normal action (default launches netflix).

If for some reason you become stuck and pressing the netflix button doesn't take you back (e.g. internal state got messed up) just press and hold on the `netflix` button for 2 seconds or more and then release and it will take you back to the Nvidia Shield input.

## Screenshots:


## Setup:

Configuration:

1. Turn on LG IP Control mode: 

    To enable IP Control Mode, turn on the LG tv.
    Hold down on the settings button (gear icon) on your remote for over 5 seconds. In the top left-hand corner the channel information should appear.
    Quickly press the key combination 828[OK] on the remote. (if this doesn't work try 82888[OK], I read that somewhere, no idea if it works. Just 828 worked for me.)
    A dialog should appear titled IP CONTROL SETUP
    Set the Network IP Control option to `ON`
    Below this you will see the TV ip address. You can use this for step 2.

2. Set config values:

    Edit the `app/build.gradle` configuration values to match your specific inputs. the Nvidia Shield is the main controller, so it contains the accessibility service and must be included. All others are optional.
    If you have an LG tv, primarily this just means entering the correct input values to match the given input for each device. 

    Options are:
        
    ```
    [dtv / atv / cadtv / catv / avav1 / component1 / hdmi1 / hdmi2 / hdmi3]
    ```

    You'll also want to enter the ip address of your lg tv. This should probably be set to static on your router to prevent it from changing.

3. Enable developer mode on the Nvidia Shield TV:

    Open the following menu tree:
    Settings -> Device Preferences -> About -> Build
    Click on `Build` 8 times consecutively. 
    Developer mode should be enabled

    Open the following menu tree:
    
    Settings -> Device Preferences -> Developer options (at the bottom)
    
    Enable the `Network debugging` option.
    
    Once enabled the ip address should appear as an option.
 
4. Download and Install Android Studio 

5. Run the AndroidTVController project for each *Debug* variant to install

    Open the AndroidTVController project with android studio
    At some point when Android Studio loads it hopefully will detect the Nvidia Shield TV. When an option to enable debugging appears, allow your computer.
    Once the project loads, select the variants you want on the tv and run each one:
    
    In the bottom left corner of the Android Studio window is a build variants drawer.
    Select this and using the drop down you can pick each variant. Likely you'll want to run the Debug variant.
    Release variant requires a signing config file which you'd have to create. It's something for distributing versions of the app.
    Then at the top of Android Studio there should be a green run buttom
    Run that. When complete, it should flash on your tv.
    If a different input will be selected for a given variant, when you run it from Android Studio, the tv input should also change.

6. Enable Accessibility service:

    You must enable the accessibility service associated with the Shield variant app for the netflix button to work and bring you back to the shield when on another input. See `Use` section at the top of the file for more details (Default name: "Nvidia Shield")
    
    Open the following menu tree:
    
    Settings -> Device Preferences -> Accessibility -> Nvidia Shield
    
    Use Nvidia Shield? OK
    
7. Disable Developer Options when everything is working

    You don't need to leave developer options enabled once you have everything working. To disable them once you have everything working and tested, follow these steps:
    
    Open the following menu tree:

    Settings -> Device Preferences -> Developer options (at the bottom)
    
    Turn off the Developer Options setting. Leave the developer options menu and they should no longer be visible at the bottom of the menu. You should be permanently good to go.


Note:
    One of the reasons I made and am open sourcing this project is because I generally distrust any application with accessibility privilages enabled. So this allows any individual to run this setup but to also audit the code. The accessibility service code can be found in {NvidiaShieldTVController/app/src/shield/java/com/ashbreeze/shield_tv_controller/NavButtonService.kt}.
    This project also allows me to have slightly more intelligent controls between the inputs than a normal shortcut remapper would allow. Also I ensured that the remote functions entirely normally with this enabled. The netflix button even works like normal when on the shield. 
    When I first added the remapping accessibility service it changed how the regular controls worked in strange ways. This from what I could tell has 0 affect, but it's also why I chose to remap just the netflix button. The other buttons were harder to try to replicate their functionality (e.g. home button)
    Originally I tried to do multiple buttons but realized getting their functionality to match perfectly wasn't straight forward. launching netflix however, was straight forward :)

Another thing to mention about secruity:
    I highly recommend you fully trust your network security before doing this, since the television is now open to being logged into by anyone on your local network. Who knows what vulnerabilities that could expose.


## Notes / Debugging
You should be able to telnet into the TV and change inputs manually
If you've enabled the IP Control Mode, you should be able to do some simple debugging to test things before getting everything else going. Just telnet into the tv:
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
[dtv / atv / cadtv / catv / avav1 / component1 / hdmi1 / hdmi2 / hdmi3]
```


Feel free to reach out to me if you have any questions, I haven't really productionized this as I don't feel confident with these steps that it's really a truly consumer friendly experience, but I believe something along these lines would be the future if all the tv companies could get their shit together and decide on a network protocoal for all the tvs and devices to communicate. If that were to happen this device could do this without any of these shenanigans.

