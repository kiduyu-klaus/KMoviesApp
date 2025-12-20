# GitHub Actions Workflow Guide

This document explains how the automated build and release workflow works for KMovies App.

## Workflow Overview

The `gradle-release.yml` workflow automatically builds the Android application and creates releases when triggered. It consists of three main jobs: **build**, **release**, and **notify**.

## Trigger Events

The workflow is triggered automatically on the following events:

### Push Events
The workflow runs when code is pushed to specific branches:
- **main branch**: Builds APK for production-ready code
- **develop branch**: Builds APK for development code

### Tag Events
When you create a version tag (e.g., `v1.0.0`), the workflow:
- Builds both debug and release APKs
- Creates a GitHub Release
- Attaches APK files to the release

### Pull Request Events
The workflow runs on pull requests to the main branch to verify that the code builds successfully before merging.

### Manual Trigger
You can manually trigger the workflow from the GitHub Actions tab using the "Run workflow" button.

## Jobs Breakdown

### 1. Build Job

This job compiles the Android application and generates APK files.

**Steps:**
1. **Checkout code**: Retrieves the latest code from the repository
2. **Set up JDK 17**: Installs Java Development Kit required for building
3. **Grant execute permission**: Makes the Gradle wrapper executable
4. **Cache Gradle packages**: Speeds up builds by caching dependencies
5. **Build Debug APK**: Compiles the debug version with debugging information
6. **Build Release APK**: Compiles the optimized release version (unsigned)
7. **Upload APKs**: Saves APK files as artifacts for download
8. **Get APK Info**: Displays the size of generated APK files

**Outputs:**
- Debug APK: `app-debug.apk`
- Release APK: `app-release-unsigned.apk`

**Artifact Retention:** APK files are stored for 30 days and can be downloaded from the Actions tab.

### 2. Release Job

This job creates a GitHub Release when a version tag is pushed.

**Conditions:**
- Only runs when a tag starting with `v` is pushed (e.g., `v1.0.0`, `v2.1.3`)
- Depends on successful completion of the build job

**Steps:**
1. **Download APKs**: Retrieves the built APK files from artifacts
2. **Get version**: Extracts version number from the tag name
3. **Rename APKs**: Adds version number to APK filenames
4. **Create Release**: Creates a GitHub Release with release notes and attached APKs

**Release Contents:**
- Release title: "KMovies App v1.0.0"
- Automated release notes with installation instructions
- Debug APK attachment
- Release APK attachment (unsigned)

### 3. Notify Job

This job provides build status feedback.

**Conditions:**
- Always runs after the build job completes
- Reports success or failure status

## How to Use

### Automatic Build on Push

Simply push your code to the main or develop branch:

```bash
git add .
git commit -m "Your commit message"
git push origin main
```

The workflow will automatically build the APK. You can download it from the Actions tab under "Artifacts".

### Creating a Release

To create a new release with automatic APK generation:

1. **Tag your commit** with a version number:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **Wait for the workflow** to complete (usually 5-10 minutes)

3. **Check the Releases page** where you'll find:
   - Release notes
   - Debug APK
   - Release APK (unsigned)

### Manual Workflow Trigger

1. Go to the **Actions** tab in your GitHub repository
2. Select **"Android CI/CD - Build and Release APK"** workflow
3. Click **"Run workflow"** button
4. Select the branch to build from
5. Click **"Run workflow"** to start

## Downloading Built APKs

### From Actions Tab

1. Navigate to the **Actions** tab in your repository
2. Click on the workflow run you want
3. Scroll down to the **Artifacts** section
4. Download either:
   - `kmovies-debug-apk` - Debug version
   - `kmovies-release-apk` - Release version

### From Releases Page

1. Navigate to the **Releases** section in your repository
2. Find the release version you want
3. Download the APK file from the **Assets** section

## APK Types Explained

### Debug APK
- **Purpose**: Testing and development
- **Size**: Larger (includes debugging information)
- **Performance**: Not optimized
- **Use case**: Development, testing, debugging

### Release APK (Unsigned)
- **Purpose**: Production deployment
- **Size**: Smaller (optimized and minified)
- **Performance**: Optimized for production
- **Note**: Unsigned - needs signing for Play Store

## Signing Release APKs

The workflow generates unsigned release APKs. To sign them for production:

### Option 1: Sign Locally

```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore your-keystore.jks \
  app-release-unsigned.apk your-key-alias
```

### Option 2: Configure GitHub Secrets

Add signing configuration to the workflow by storing your keystore as a GitHub secret:

1. Go to **Settings > Secrets and variables > Actions**
2. Add the following secrets:
   - `KEYSTORE_FILE` (base64 encoded keystore)
   - `KEYSTORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`

Then update the workflow to sign the APK automatically.

## Troubleshooting

### Build Fails

**Check the logs:**
1. Go to Actions tab
2. Click on the failed workflow run
3. Expand the failed step to see error details

**Common issues:**
- Gradle sync errors: Check `build.gradle` files
- Missing dependencies: Verify all dependencies are available
- Compilation errors: Fix Java/XML errors in the code

### Release Not Created

**Verify:**
- Tag format starts with `v` (e.g., `v1.0.0`)
- Build job completed successfully
- GitHub token has proper permissions

### APK Not Attached to Release

**Check:**
- Build job produced APK files
- Download artifact step succeeded
- File paths in release step are correct

## Workflow Customization

### Change Trigger Branches

Edit the `on.push.branches` section:

```yaml
on:
  push:
    branches: [ main, develop, feature/* ]
```

### Modify APK Retention

Change the `retention-days` value:

```yaml
- name: Upload Debug APK
  uses: actions/upload-artifact@v3
  with:
    retention-days: 60  # Keep for 60 days
```

### Add Signing Configuration

Add a signing step before the build:

```yaml
- name: Sign Release APK
  run: |
    echo "${{ secrets.KEYSTORE_FILE }}" | base64 -d > keystore.jks
    ./gradlew assembleRelease \
      -Pandroid.injected.signing.store.file=keystore.jks \
      -Pandroid.injected.signing.store.password=${{ secrets.KEYSTORE_PASSWORD }} \
      -Pandroid.injected.signing.key.alias=${{ secrets.KEY_ALIAS }} \
      -Pandroid.injected.signing.key.password=${{ secrets.KEY_PASSWORD }}
```

## Best Practices

1. **Use semantic versioning** for tags: `v1.0.0`, `v1.1.0`, `v2.0.0`
2. **Test locally** before pushing to ensure builds work
3. **Review workflow logs** to catch issues early
4. **Keep dependencies updated** to avoid build failures
5. **Sign release APKs** before distributing to users

## Monitoring Builds

### Build Status Badge

Add a build status badge to your README:

```markdown
![Build Status](https://github.com/kiduyu-klaus/KMoviesApp/actions/workflows/gradle-release.yml/badge.svg)
```

### Email Notifications

GitHub automatically sends email notifications for workflow failures if you have notifications enabled in your GitHub settings.

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android Gradle Plugin Guide](https://developer.android.com/studio/build)
- [Signing Android Apps](https://developer.android.com/studio/publish/app-signing)

## Support

If you encounter issues with the workflow, please open an issue in the repository with:
- Workflow run link
- Error logs
- Steps to reproduce
