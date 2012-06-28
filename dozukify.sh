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

# Update logo and app name
sed -i "" -e "s/_ifixit/_$siteName/" ./AndroidManifest.xml

# Update intent-filter for GuideView
sed -i "" -e "s/www\.ifixit\.com/$siteName.dozuki.com/" ./AndroidManifest.xml

# Change the default activity to the site list for Dozuki
if [ "$siteName" = "dozuki" ]
then
   sed -i "" -e "s/TopicsActivity/TopicsActivityTmp/" ./AndroidManifest.xml
   sed -i "" -e "s/SiteListActivity/TopicsActivity/" ./AndroidManifest.xml
   sed -i "" -e "s/TopicsActivityTmp/SiteListActivity/" ./AndroidManifest.xml

   # Update the theme
   sed -i "" -e "s/Theme\.iFixit/Theme.Dozuki/" ./AndroidManifest.xml
fi

cd -
