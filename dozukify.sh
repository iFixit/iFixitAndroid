#!/bin/bash

if [ $# -eq 0 ]
then
   echo "Must specify a site name!"
   exit;
fi

siteName=$1

cd App/

sed -i "" -e "s/com.dozuki.ifixit/com.dozuki.$siteName/" `find . -name "*.java"` `find . -name "*.xml"`
mv src/com/dozuki/ifixit src/com/dozuki/$siteName

cd -
