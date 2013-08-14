#!/bin/bash

# Features left to move into gradle:
#    Updating intent-filter domain for GuideView

if [ $# -eq 0 ]
then
   echo "Usage: $0 <site name> [--package-change]"
   exit;
fi

siteName=$1

cd App/

if [ "$2" = "--package-change" ]
then
   # Update the package name for all relevant files
   sed -i "" -e "s/com.dozuki.ifixit/com.dozuki.$siteName/" `find ./src -name "*.java"` `find . -name "*.xml"`
   mv src/com/dozuki/ifixit src/com/dozuki/$siteName
fi

# Update the default site
sed -i "" -e "s/SITE_ifixit/SITE_$siteName/" `find ./src -name "MainApplication.java"`

# Update logo and app name
sed -i "" -e "s/_ifixit/_$siteName/" ./AndroidManifest.xml

case "$siteName" in
"dozuki")
   domain="*.dozuki.com"
   ;;
*)
   domain="$siteName.dozuki.com"
   ;;
esac

# Update intent-filter for GuideView
sed -i "" -e "s/www\.ifixit\.com/$domain/" ./AndroidManifest.xml

# Change the default activity to the site list for Dozuki
if [ "$siteName" = "dozuki" ]
then
   sed -i "" -e "s/ui\.topic_view\.TopicActivity/ui.topic_view.TopicActivityTmp/" ./AndroidManifest.xml
   sed -i "" -e "s/ui\.dozuki\.SiteListActivity/ui.topic_view.TopicActivity/" ./AndroidManifest.xml
   sed -i "" -e "s/ui\.topic_view\.TopicActivityTmp/ui.dozuki.SiteListActivity/" ./AndroidManifest.xml

   # Remove comments to enable Dozuki specific code
   sed -i "" -e "/<\!--DOZUKI/d" ./AndroidManifest.xml
   sed -i "" -e "/DOZUKI-->/d" ./AndroidManifest.xml

   # Turn on flags for Dozuki.
   sed -i "" -e "s/UP_NAVIGATION_FINISH_ACTIVITY = false/UP_NAVIGATION_FINISH_ACTIVITY = true/" `find ./src -name "TopicActivity.java"`

   # Update the theme
   sed -i "" -e "s/Theme\.iFixit/Theme.Dozuki/" ./AndroidManifest.xml

   # Update Analytics profile id
   sed -i "" -e "s/30506\-14/30506\-18/" `find ./res -name "analytics.xml"`
fi

cd -
