# GitHub Actions CI/CD Workflow

## Overview

The KMovies App now includes automated CI/CD using GitHub Actions. Every push to the repository automatically builds the APK, and tagged releases create downloadable release packages.

## Quick Start

### Automatic Builds

Every time you push code to `main` or `develop` branches, GitHub Actions will:
1. Build the debug APK
2. Build the release APK (unsigned)
3. Upload both APKs as artifacts

### Creating a Release

To create a new release with downloadable APKs:

```bash
# Tag your commit with a version number
git tag v1.0.0
git push origin v1.0.0
```

Within minutes, a new release will appear on the Releases page with:
- Release notes
- Debug APK (for testing)
- Release APK (unsigned, for production)

## Workflow Features

### ✅ Automated Building
- Builds on every push to main/develop
- Builds on pull requests
- Can be triggered manually

### 📦 APK Generation
- Debug APK with debugging symbols
- Release APK optimized for production
- Automatic artifact upload

### 🚀 Release Creation
- Automatic release creation for version tags
- APK files attached to releases
- Generated release notes

### 📊 Build Status
- Real-time build status in Actions tab
- Artifact downloads available for 30 days
- Build notifications

## Downloading APKs

### From Actions Tab
1. Go to **Actions** tab
2. Click on a workflow run
3. Scroll to **Artifacts** section
4. Download `kmovies-debug-apk` or `kmovies-release-apk`

### From Releases Page
1. Go to **Releases** section
2. Find your version
3. Download APK from **Assets**

## Version Tagging

Use semantic versioning for tags:
- `v1.0.0` - Major release
- `v1.1.0` - Minor update
- `v1.0.1` - Patch/bugfix

Example:
```bash
git tag v1.0.0 -m "Initial release"
git push origin v1.0.0
```

## Workflow Files

- `.github/workflows/gradle-release.yml` - Main workflow configuration
- `.github/WORKFLOW_GUIDE.md` - Detailed workflow documentation

## Build Status Badge

Add this badge to your README to show build status:

```markdown
![Build Status](https://github.com/kiduyu-klaus/KMoviesApp/actions/workflows/gradle-release.yml/badge.svg)
```

## Requirements

The workflow requires:
- GitHub repository with Actions enabled
- No additional secrets needed for basic builds
- Gradle wrapper files in the repository

## Customization

To customize the workflow, edit `.github/workflows/gradle-release.yml`:
- Change trigger branches
- Modify APK retention period
- Add signing configuration
- Customize release notes

## Troubleshooting

### Build Fails
- Check Actions tab for error logs
- Verify all dependencies are correct
- Ensure code compiles locally first

### Release Not Created
- Verify tag format starts with `v`
- Check that build job succeeded
- Ensure you have push permissions

### APK Not Available
- Wait for workflow to complete (5-10 minutes)
- Check Artifacts section in workflow run
- Verify build step succeeded

## Next Steps

1. **Push your code** to trigger the first build
2. **Create a tag** to generate your first release
3. **Download the APK** from the Releases page
4. **Install on Android TV** and test

For detailed documentation, see `.github/WORKFLOW_GUIDE.md`.
