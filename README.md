Official iFixit Android App
===========================

## Version 2.0.0


## Installation

    git clone https://github.com/iFixit/iFixitAndroid.git

### Gradle Config

Add values for `dev_server` and `app_id` to /gradle.properties like so:

    dev_server="www.devserver.com"
    app_id="0123456789ABCDEF0123456789ABCDEF"

### Signing

Add values for `ifixitKeyAlias`, `ifixitStorePassword`, and `ifixitKeyPassword` to /gradle.properties like so:

    ifixitKeyAlias=keyAliasName
    ifixitStorePassword=password
    ifixitKeyPassword=password

Symlink keystores/ifixit.keystore to your keystore like so:

    ln -s ~/path/to/keystore keystores/ifixit.keystore

### Intellij

File -> Import Project...

Select the iFixitAndroid folder that you cloned previously

Create project from existing sources

...

And that's it!


### Eclipse

TODO

## API Documentation

[iFixit API documentation](https://www.ifixit.com/api/1.1/docs)

## Contributing

This app is a native android version of the iFixit website.  We offer native
browsing of our guides as well as integrated web views of our site with Answers
and our Cart.  We've got some fun experimental features we're playing around
with like Voice Command for navigating through guides.

We have a small development team, and don't have the resources to do all of these
ourselves. We'd love help! If any of those problems looks interesting to you, fork our
code and hack away!

Got an awesome feature idea that we don't have an API to support yet? Post the request on
meta.ifixit.com and we'll add it to our to-do list.

## Licensing

>    This source code is licensed under the GPLv3.
>    Any submissions to this project must also be licensed under GPLv3.
>    The contents of this software are subject to the terms of the GNU General Public License (the "License").
>    You may not use this software except in compliance with this License.
>    You can obtain a copy of the license at [http://www.gnu.org/licenses/gpl-3.0.txt](http://www.gnu.org/licenses/gpl-3.0.txt).
>    See the License for the specific language governing permissions and limitations under the License.


## TRADEMARK NOTES

All iFixit trademarks contained herein are NOT licensed for use by any third-parties.
Their inclusion in this open source software is only for their eventual replacement if
you distribute the application.

That is, the trademarks are protected, but the code itself is under an open license. You
can use the trademarks individually, but not for any sort of distribution.

Copyright (c) 2012 iFixit

