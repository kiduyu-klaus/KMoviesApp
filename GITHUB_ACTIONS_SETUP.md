# GitHub Actions CI/CD Setup Guide

## Overview
This guide will help you set up the GitHub Actions workflow for automatic APK building and releases.

## Files Created
The following files have been created for the CI/CD workflow:
- `.github/workflows/gradle-release.yml` - Main workflow configuration
- `.github/WORKFLOW_GUIDE.md` - Detailed workflow documentation
- `WORKFLOW_README.md` - Quick start guide
- `gradlew` - Gradle wrapper script (Linux/Mac)
- `gradlew.bat` - Gradle wrapper script (Windows)

## Setup Instructions

### Method 1: Push from Local Machine (Recommended)

1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/kiduyu-klaus/KMoviesApp.git
   cd KMoviesApp
   ```

2. Pull the latest changes:
   ```bash
   git pull origin main
   ```

3. Push the workflow files:
   ```bash
   git push origin main
   ```

### Method 2: Create Workflow Directly on GitHub

1. Go to your repository: https://github.com/kiduyu-klaus/KMoviesApp
2. Click on the **Actions** tab
3. Click **"New workflow"**
4. Click **"set up a workflow yourself"**
5. Name the file: `gradle-release.yml`
6. Copy the workflow content from the file in this repository
7. Click **"Commit changes"**

### Method 3: Upload via GitHub Web Interface

1. Go to your repository on GitHub
2. Navigate to the main page
3. Click **"Add file" > "Create new file"**
4. Type `.github/workflows/gradle-release.yml` as the filename
5. Paste the workflow content
6. Commit the file

## What the Workflow Does

### Automatic Builds
- Triggers on push to `main` or `develop` branches
- Triggers on pull requests to `main`
- Can be manually triggered from Actions tab

### APK Generation
- Builds debug APK with debugging symbols
- Builds release APK optimized for production
- Uploads APKs as downloadable artifacts

### Automatic Releases
- Creates GitHub releases when you push a version tag (e.g., `v1.0.0`)
- Attaches debug and release APKs to the release
- Generates release notes automatically

## Usage

### Trigger a Build
Simply push your code:
```bash
git add .
git commit -m "Your changes"
git push origin main
```

### Create a Release
Tag your commit and push:
```bash
git tag v1.0.0
git push origin v1.0.0
```

### Download APKs
- Go to **Actions** tab > Select a workflow run > Download from **Artifacts**
- Or go to **Releases** page to download from a release

## Troubleshooting

### Permission Error
If you get a permission error when pushing the workflow:
- The GitHub App doesn't have workflow permissions
- Use one of the alternative methods above
- Or grant workflow permissions in repository settings

### Build Fails
- Check the Actions tab for detailed error logs
- Ensure all dependencies are correct in build.gradle
- Verify the code builds locally first

### No APK Generated
- Wait for the workflow to complete (usually 5-10 minutes)
- Check if the build step succeeded
- Look for errors in the workflow logs

## Next Steps

1. Set up the workflow using one of the methods above
2. Push code to trigger your first build
3. Create a version tag to generate your first release
4. Download and test the APK on your Android TV device

## Support

For detailed documentation, see:
- `.github/WORKFLOW_GUIDE.md` - Complete workflow documentation
- `WORKFLOW_README.md` - Quick reference guide

For issues, check the Actions tab in your GitHub repository for build logs and error messages.
