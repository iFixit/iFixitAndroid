
White label your Dozuki App
==============

Here are the links to get you started.

Authentication / Login Classes:
https://github.com/iFixit/iFixitAndroid/blob/master/App/src/com/dozuki/ifixit/App.java#L337

https://github.com/iFixit/iFixitAndroid/tree/master/App/src/com/dozuki/ifixit/ui/auth
https://github.com/iFixit/iFixitAndroid/blob/master/App/src/com/dozuki/ifixit/ui/auth/LoginFragment.java

https://github.com/iFixit/iFixitAndroid/tree/master/App/src/com/dozuki/ifixit/model/auth
https://github.com/iFixit/iFixitAndroid/blob/master/App/src/com/dozuki/ifixit/model/auth/Authenticator.java


Site Customization
--------------

We use gradle for our build process, so you have to start first with getting
your dev environment set up.

https://github.com/iFixit/iFixitAndroid#installation


Then add the new site to the `build.gradle` file.

https://github.com/iFixit/iFixitAndroid/blob/master/App/build.gradle#L39


To build and install the APK, use:

   gradle assemble<SITENAME>Release install<SITENAME>Release


You can look through the build.gradle script to see what we do, but the gist of
it is that we merge the App/sites/{site-name} dir with the main res dir as well
as merging the manifests so all the customizations you make in your site
directory override the defaults.


You have to request an AppID from apps@dozuki.com or by contacting your dozuki
representative. 


Instructions
--------------

* Add your sites information to this file - https://github.com/iFixit/iFixitAndroid/blob/master/App/src/com/dozuki/ifixit/model/dozuki/Site.java#L235
   * You can find the necessary information on your dozuki site from https://<sitename>.dozuki.com/api/2.0/info
* Create a new folder in the sites/ dir
   * https://github.com/iFixit/iFixitAndroid/tree/master/App/sites/accustream
* Add a manifest for your new site, which is merged during the build process
   * Example - https://github.com/iFixit/iFixitAndroid/blob/master/App/sites/accustream/AndroidManifest.xml
* Add your app icon to the site-res directory.  NOTE - it MUST be named `icon.png`, this is due to gradle-android oddities.
   * You’ll also need a notification icon named ic_notification_icon.png
* Theming and customizing the app is done through https://github.com/iFixit/iFixitAndroid/blob/master/App/sites/accustream/res/values/themes.xml and https://github.com/iFixit/iFixitAndroid/blob/master/App/sites/accustream/res/values/styles.xml.
   * Note: If you theme your app, you must add your themes app name to Site.java (https://github.com/iFixit/iFixitAndroid/blob/master/App/src/com/dozuki/ifixit/model/dozuki/Site.java#L141)
* These are the assets you need to recolor if you want to change the hover/highlight state colors.  (https://github.com/iFixit/iFixitAndroid/tree/master/App/sites/accustream/res/drawable-hdpi)
   * Here are some tools out there that can help you generate these system assets.
      * http://android-arsenal.com/
      * http://romannurik.github.io/AndroidAssetStudio/
      * http://android-holo-colors.com/
      * https://github.com/jeromevdl/android-holo-colors-idea-plugin (which is new, I haven’t yet tried it, but looks very promising!)


Gotchas
--------------

All the activity:name attributes must be com.dozuki.ifixit, but the
android:authority fields must be com.dozuki.{SITENAME}.  It’s an unfortunate side
effect of building multiple apps out of the same code base.  We wrestled with
this for a long time when we were first adding this feature.




Kestore Generation


    keytool -genkey -dname "cn={Site}, ou={Site}, o={Site}, c=US" -v -keystore {site} -alias {site}_keystore -keyalg RSA -keysize 2048 -validity 36500

