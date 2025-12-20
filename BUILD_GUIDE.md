# Build Guide for KMovies App

This guide provides detailed instructions for building and running the KMovies Android TV application.

## System Requirements

The application requires the following development environment to build successfully.

### Required Software
- **Android Studio**: Arctic Fox (2020.3.1) or later recommended
- **JDK**: Java Development Kit 8 or higher
- **Android SDK**: API Level 34 (Android 14)
- **Gradle**: Version 8.1.1 (included in wrapper)
- **Git**: For version control

### Recommended Hardware
- **RAM**: 8GB minimum, 16GB recommended
- **Storage**: 10GB free space for Android SDK and project
- **Processor**: Multi-core processor for faster builds

## Initial Setup

### Step 1: Install Android Studio
Download and install Android Studio from the official website. During installation, ensure the Android SDK is installed with the following components:
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0
- Android Emulator (for testing)

### Step 2: Clone the Repository
Open a terminal and clone the repository to your local machine:
```bash
git clone https://github.com/kiduyu-klaus/KMoviesApp.git
cd KMoviesApp
```

### Step 3: Configure SDK Path
Create a `local.properties` file in the project root directory with your Android SDK path:
```properties
sdk.dir=/path/to/your/android/sdk
```

For different operating systems:
- **Windows**: `C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk`
- **macOS**: `/Users/YourUsername/Library/Android/sdk`
- **Linux**: `/home/YourUsername/Android/Sdk`

## Building the Project

### Using Android Studio

1. **Open Project**: Launch Android Studio and select "Open an Existing Project", then navigate to the KMoviesApp directory.

2. **Sync Gradle**: Android Studio will automatically start syncing Gradle. If not, click "Sync Project with Gradle Files" in the toolbar.

3. **Wait for Dependencies**: The first build may take several minutes as Gradle downloads all required dependencies.

4. **Build APK**: Once syncing is complete, select "Build > Build Bundle(s) / APK(s) > Build APK(s)" from the menu.

5. **Locate APK**: After successful build, the APK will be located at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Using Command Line

For developers who prefer command-line builds:

```bash
# Make gradlew executable (Linux/macOS)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Clean and rebuild
./gradlew clean assembleDebug
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Running the Application

### On Android TV Emulator

1. **Create AVD**: In Android Studio, open AVD Manager (Tools > Device Manager).

2. **Select TV Device**: Click "Create Device" and select a TV device profile (e.g., "Android TV 1080p").

3. **Choose System Image**: Select API Level 34 system image and download if necessary.

4. **Launch Emulator**: Start the emulator and wait for it to boot completely.

5. **Run App**: Click the "Run" button in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

### On Physical Android TV Device

1. **Enable Developer Options**: On your Android TV, go to Settings > About and click "Build" 7 times.

2. **Enable USB Debugging**: In Developer Options, enable "USB debugging" and "Install via USB".

3. **Connect Device**: Connect your Android TV to your computer via USB or network (ADB over network).

4. **Verify Connection**: Check device connection:
   ```bash
   adb devices
   ```

5. **Install APK**: Install the application:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Troubleshooting

### Common Build Issues

**Issue**: Gradle sync fails with "SDK location not found"
**Solution**: Ensure `local.properties` file exists with correct SDK path.

**Issue**: Build fails with "Minimum supported Gradle version"
**Solution**: Update Gradle wrapper to version 8.1.1 or later.

**Issue**: Dependencies download slowly
**Solution**: Configure Gradle to use mirror repositories in `build.gradle`.

**Issue**: Out of memory during build
**Solution**: Increase Gradle heap size in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### Runtime Issues

**Issue**: App crashes on launch
**Solution**: Check logcat for error messages and ensure all permissions are granted.

**Issue**: Videos won't play
**Solution**: Verify internet connection and check if streaming URLs are accessible.

**Issue**: Images not loading
**Solution**: Ensure INTERNET permission is granted and Glide is properly configured.

## Build Variants

The project supports different build variants for various purposes.

### Debug Build
- Includes debugging information
- Not optimized for performance
- Larger APK size
- Used for development and testing

### Release Build
- Optimized and minified code
- Requires signing configuration
- Smaller APK size
- Used for distribution

To create a release build, configure signing in `app/build.gradle`:
```gradle
android {
    signingConfigs {
        release {
            storeFile file("your-keystore.jks")
            storePassword "your-store-password"
            keyAlias "your-key-alias"
            keyPassword "your-key-password"
        }
    }
}
```

## Performance Optimization

For optimal build performance, consider the following configurations in `gradle.properties`:
```properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true
```

## Next Steps

After successfully building the application, refer to the README.md for feature documentation and usage instructions. For contributing to the project, see CONTRIBUTING.md for guidelines and best practices.
