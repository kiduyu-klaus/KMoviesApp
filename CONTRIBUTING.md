# Contributing to KMovies App

Thank you for considering contributing to KMovies App! This document provides guidelines and instructions for contributing.

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 8 or higher
- Android SDK with API level 34
- Git for version control

### Setting Up Development Environment
1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/KMoviesApp.git`
3. Open the project in Android Studio
4. Create `local.properties` file with your SDK path
5. Sync Gradle and build the project

## Development Guidelines

### Code Style
- Follow standard Java coding conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Keep methods focused and concise
- Use proper indentation (4 spaces)

### Project Structure
- Place model classes in `models` package
- Put UI-related code in appropriate activity/fragment
- Keep scraping logic in `scraper` package
- Use `utils` for helper functions
- Create adapters in `adapters` package

### Commit Messages
- Use clear, descriptive commit messages
- Start with a verb (Add, Fix, Update, Remove)
- Keep the first line under 50 characters
- Add detailed description if needed

Example:
```
Add search functionality to MainActivity

- Implement search bar in toolbar
- Add SearchTask for async searching
- Update adapter to display search results
```

### Branch Naming
- `feature/feature-name` for new features
- `bugfix/bug-description` for bug fixes
- `improvement/description` for enhancements

## Making Changes

### Before Starting
1. Create a new branch from `main`
2. Ensure your local repository is up to date
3. Check existing issues and pull requests

### Development Process
1. Make your changes in the new branch
2. Test thoroughly on Android TV emulator/device
3. Update documentation if needed
4. Commit changes with clear messages
5. Push to your fork
6. Create a pull request

### Testing
- Test on Android TV emulator
- Verify all navigation works with D-pad
- Check video playback functionality
- Test error handling scenarios
- Ensure no crashes or ANR issues

## Pull Request Process

### Creating a Pull Request
1. Ensure your code builds successfully
2. Update README.md if you've added features
3. Describe your changes clearly in PR description
4. Reference any related issues
5. Request review from maintainers

### PR Requirements
- Code must build without errors
- Follow project coding standards
- Include appropriate comments
- Test all changes thoroughly
- No merge conflicts with main branch

## Reporting Issues

### Bug Reports
Include:
- Clear description of the bug
- Steps to reproduce
- Expected vs actual behavior
- Android version and device info
- Screenshots if applicable
- Logs/error messages

### Feature Requests
Include:
- Clear description of the feature
- Use case and benefits
- Possible implementation approach
- Any relevant examples

## Code Review Process

### What We Look For
- Code quality and readability
- Proper error handling
- Performance considerations
- UI/UX improvements
- Documentation updates

### Response Time
- Initial review within 3-5 days
- Follow-up on requested changes
- Merge after approval from maintainers

## Areas for Contribution

### High Priority
- Search functionality implementation
- Favorites/watchlist feature
- Continue watching tracking
- Subtitle support
- Quality selection

### Medium Priority
- UI/UX improvements
- Performance optimization
- Better error messages
- Loading animations
- Caching improvements

### Low Priority
- Additional themes
- Settings page
- About page
- Statistics tracking

## Questions?

Feel free to open an issue with the `question` label if you need help or clarification.

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

Thank you for contributing to KMovies App!
