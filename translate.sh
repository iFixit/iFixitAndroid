#!/usr/bin/env bash

lang=$1
tld=`git rev-parse --show-toplevel`
resdir="$tld/App/res"
langdir=$resdir/values-$lang
mkdir $langdir
cp $resdir/values/strings.xml $langdir/
