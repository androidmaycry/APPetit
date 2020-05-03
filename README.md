# APPetit
Final version of the app

### Installation on OSX

**Install Android-SDK**
`brew cask install android-sdk`

**Export ANDROID_HOME**
`export ANDROID_HOME=/usr/local/Caskroom/android-sdk/{android_sdk version number}`

**Install OpenJdk8**
`brew cask install adoptopenjdk/openjdk/adoptopenjdk8` 

**Ensure OpenJdk8 is used**
`export JAVA_HOME=/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home`

**Accept Androind-SDK Licence**
```
cd /usr/local/Caskroom/android-sdk/{android_sdk version number}/tools/bin
yes | sdkmanager --licenses && sdkmanager --update
```

**Get the Google Services Json**
  - Sign in to Firebase, then create your project
  - Add a name for your project and select the appropriate settings, then click on Create project.
  - Click Settings icon, then select Project settings
  - In the `Your apps` card, select the platform for the app you want created.
  - Follow the required steps to add Firebase to your Android app. Here are the steps:
    - Step 1 - Register app
      - Package name : `com.mad.customer`
      - Create local keystore file from command line : `keytool -genkey -v -keystore ~/.android/debug.keystore -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000`
      - Get the local debug certificate fingerprint : `keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore`
    - Step 2 - Download config file `google-services.json`
    - Step 3 - Move `google-services.json` to `{APPetit root folder}/customer/`
    - Step 4 - Edit `{APPetit root folder}/build.gradle`, change `classpath` to `com.google.gms:google-services:4.3.3` (or whatever google tells you)

**Build the app**
```
cd {APPetit root folder}/customer/
gradle build  # apk is now located in {APPetit root folder}/customer/outputs/build/apk/debug and {APPetit root folder}/customer/outputs/build/apk/release
```
