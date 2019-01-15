# mcMMOAction

![Action-bar messages](https://i.imgur.com/QYvRTRA.png)

## Description

This lightweight plugin is based on the feature request from [here](https://github.com/mcMMO-Dev/mcMMO/issues/2659).
It forwards some useful mcMMO messages to the action chat. Instead that these messages spam the regular chat,
you'll see them above the item-bar and they will disappear after a shorten time. This plugin has no config. Just drop
the plugin in your plugins folder and start your server. 

This was implemented in an extra plugin, because action bar messages are only implemented in the Spigot API
and not in Bukkit. mcMMO only depends on the Bukkit API and did not want to move to Spigot only API. This changed for
mcMMO 2.1 and so will be included in the main plugin. This plugin still exists for earlier versions.

## Features

* Lightweight
* Notification sound for new messages (except progress)
* Support many mcMMO messages
* Configurable appearance time
* Supports localized messages
* Ignore the messages you don't want to see in the action bar
* Shows progress message if the user gains skill experience
* Good performance - Messages are loaded only once in a fast collection

## Supported messages (by default)

* All skill messages:
    * level-up
    * ability activate and deactivate
    * tool raise and lower
* Too tired message
* Hardcore messages
* Refresh message
* Party level-up
* Combat abilities (i.e. swords bleeding)
* [mcMMOHorses](https://github.com/ZombieStriker/mcMMOHorses/) Support
    * Refresh messages for Infuriate and Super Speed
    * Skills increases for Agility, Swiftness, Vitality and Wrath
    * Notification messages like \*\*Dodge\*\*

## Commands

* /mmoaction - toggle the action-bar for mcMMO messages
* /mmoaction progress - toggles the progress feature for the invoker

## Dependencies

* [ProtocolLib](https://dev.bukkit.org/bukkit-plugins/protocollib)
* [mcMMO](https://dev.bukkit.org/bukkit-plugins/mcmmo)

![progress](https://user-images.githubusercontent.com/6004542/30592754-0c7b1706-9d16-11e7-8136-cccde2296446.png)

## Development builds

Development builds of this project can be acquired at the provided CI (continuous integration) server. It contains the
latest changes from the Source-Code in preparation for the following release. This means they could contain new
features, bug fixes and other changes since the last release.

Nevertheless builds are only tested using a small set of automated and a few manual tests. Therefore they **could**
contain new bugs and are likely to be less stable than released versions.

https://ci.codemc.org/job/Games647/job/mcMMOAction/changes
