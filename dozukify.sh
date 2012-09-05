#!/bin/bash

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
   domain="www.dozuki.com"
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
   sed -i "" -e "s/view\.ui\.TopicsActivity/view.ui.TopicsActivityTmp/" ./AndroidManifest.xml
   sed -i "" -e "s/dozuki\.ui\.SiteListActivity/view.ui.TopicsActivity/" ./AndroidManifest.xml
   sed -i "" -e "s/view\.ui\.TopicsActivityTmp/dozuki.ui.SiteListActivity/" ./AndroidManifest.xml

   # Update the theme
   sed -i "" -e "s/Theme\.iFixit/Theme.Dozuki/" ./AndroidManifest.xml
fi

cd -
