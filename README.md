XPrivacy
========

Proof of concept for a privacy manager using the [Xposed framework](http://forum.xda-developers.com/showthread.php?t=1574401)

Module to return no or fake data for any application, simply managed from the *Manage apps* menu.

Permissions
-----------

Currently implemented:

* Browser (bookmarks, searches, etc)
* Calendar
* Call log
* Contacts
* Identification (Android, phone, number, subscriber, SIM)
* Location (coarse/fine)
* Messages (SMS/MMS)
* Voicemail **untested**

Planned:

* Take photo
* Record audio
* Record video
* Incoming/outgoing number
* Default deny/allow toggle
* Batch mode
* Stop app after permission change
* Add permission explanations
* Move permission management entirely to provider

**Tested with CyanogenMod 10**

Reported to work with CyanogenMod 10.1 at least partly.

Installation
------------

* Root your device
* **Make a backup**
* Install the [Xposed framework](http://forum.xda-developers.com/showthread.php?t=1574401)
* Install XPrivacy from [here](http://goo.im/devs/M66B/tools)
* Enable XPrivacy from the Xposed Installer app
* Reboot

Usage
-----

* Select *Manage apps* from the main menu
* Select an app
* Scroll down to disable permissions

To see it in action: try disabling *Identification* for [Android Id Info](https://play.google.com/store/apps/details?id=com.bzgames.androidid)
or try disabling *contacts* for the Contacts app.

Frequently asked questions
--------------------------

* Will you restrict internet access? No, you can use a firewall app, like [AFWall+](https://play.google.com/store/apps/details?id=dev.ukanth.ufirewall)

Developers
----------

To restrict new info:

* Find the package/class/method that exposes the info (look into the Android documentation/sources)
* Create a class that extends [XHook](https://github.com/M66B/XPrivacy/blob/master/src/biz/bokhorst/xprivacy/XHook.java)
* Hook the method in [XPrivacy](https://github.com/M66B/XPrivacy/blob/master/src/biz/bokhorst/xprivacy/XPrivacy.java)
* Update [strings](https://github.com/M66B/XPrivacy/blob/master/res/values/strings.xml) for new permissions
* Figure out how to get a context from the class instance
* Write a before and/or after method to restrict the info (see also existing examples)

License
-------

GNU General Public License version 3

Copyright (c) 2013 [Marcel Bokhorst](http://blog.bokhorst.biz/about/)

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA