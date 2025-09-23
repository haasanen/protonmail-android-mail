#!/bin/bash

version="latest"
if [[ "$1" == "--version" ]]; then
version="$2"
fi

# rm any existing dirs in /tmp
printf "Removing the dirs from /tmp folder \n\n"
rm -rf /tmp/proton-mail-android/
rm -rf /tmp/mail/

# Clean deps
if [[ "$1" == "--clean" ]]; then
  printf "clean option given, removing all versions of the lib from mvn (local) \n\n"
  rm -rf ~/.m2/repository/me/proton/mail/common/
fi

printf "Extracting the proton-mail-android zip from Downloads... (ensure there's only one!) \n\n"

printf "Downloads content (related to proton): \n\n"
printf "***************************** \n\n"
ls ~/Downloads | grep proton
printf "***************************** \n\n"

unzip -q ~/Downloads/proton-mail-android.zip -d /tmp/


# Install mvn
printf "Installing the lib to mvn locally with version $version \n\n"
mvn install:install-file -Dfile=/tmp/mail/mail-uniffi/android/lib/build/outputs/aar/lib-release.aar -DgroupId=me.proton.mail.common -DartifactId=lib -Dversion="$version" -Dpackaging=aar
