# Nvidia Shield TV Controller
This project is what I used to consildate all of my TV remotes down to just the Nvidia Shield TV Remote. I have an `Nvidia Shield TV Pro (2019)` connected to my LG TV via HDMI 1, a `Playstation 4` on HDMI 2, and a `Nintendo Switch` on HDMI 3. With this application (3 applications) installed on my tv, I can select each application `Nvidia Shield TV Pro`, `Playstation 4`, or `Nintendo Switch` and it will automatically switch to the associated input for the given device. I then can press on the Netflix button to return to the shield at any time. The Netflix button works like normal, while the Nvidia Shield is the selected input.

## Use:
Once setup, this will create discrete app icons on the Nvidia Shield to either deliver events to your Home Assistant instance to change your TV's input, or directly deliver input change commands to your LG TV.

There are 2 discreet ways to integrate this app with your TV
1. Home Assistant automation driven by an event to control your TV (or anything)
2. Directly sending Telnet input change command to older LG TVs.

The app maintains an internal state of the current input.

By default the `Netflix` button will issue the command to switch the input back to the  `Nvidia Shield `
Once on the shield input, the `Netflix` button should continue to operate normally.

If at any time you long-press the `Netflix` button for >2 seconds , it will again send the command to return to the Nvidia Shield TV input.

## Setup:

To setup with Home Assistant, please follow the [HomeAssistantSetup.md](./HomeAssistantSetup.md) guide

To setup to directly control your LG TV via telnet (older LG TVs only) follow the [TelnetDirectAccessSetup.md](./TelnetDirectAccessSetup.md) guide

## Screenshots:

![Home Screen Shortcuts](./screenshots/home_screen_shortcuts.webp?raw=true "Home Screen Shortcuts")

![All Apps](./screenshots/all_apps.webp?raw=true "All Apps")


## Support:

I don't offer any guarantees of any kind around this project, but I'm very happy to help as much as possible. If you encounter a problem, or would like help to set this up with something other than an LG TV (e.g. onkyo receiver or Samsung TV) feel free to reach out to open a new issue on GitHub, and I'd be happy to work with you to get this setup and working for whatever devices you have at home.


## Credit:

I loosely referenced several projects when making this, so always nice to see the breadcrumbs.

For the accessibility service, I used this:

https://github.com/shuhaowu/NavButtonRemap

The primary issue I encountered with this was one of the lines in the service_config.xml

`android:accessibilityFeedbackType="feedbackGeneric"`

With this flag enabled, whenever I would do special actions like try to move an app icon (Long press → move), it would display a visual on-screen display of some sort with visible controls, but it also just wouldn't function correctly with the remote. Materially, the icons wouldn't move with the arrow keys like normal. Once I removed this, that issue went away.


For the telnet service, I found this as a starting point:
https://github.com/hkdsun/Android-Telnet-Client
