# KMovies App - Android TV Streaming Application

An Android TV application for streaming movies and TV shows with a modern, user-friendly interface.

## Features

### Navigation
- **Home** - Featured and latest content
- **Movies** - Browse all movies
- **TV Shows** - Browse all TV series
- **Top IMDb** - Top-rated content
- **Genre** - Browse by genre
- **Country** - Browse by country
- **Year** - Browse by release year

### Core Functionality
- **Web Scraping** - Automated content extraction using Jsoup
- **Video Streaming** - High-quality playback with ExoPlayer
- **Modern UI** - Clean, responsive interface optimized for Android TV
- **Focus Navigation** - Full D-pad and remote control support
- **Image Loading** - Efficient thumbnail and poster loading with Glide

## Technical Stack

### Languages & Frameworks
- **Java** - Primary programming language
- **Android SDK** - Target API 34, Min API 21

### Key Libraries
- **Jsoup 1.17.1** - HTML parsing and web scraping
- **ExoPlayer 1.2.0** - Video streaming and playback
- **Glide 4.16.0** - Image loading and caching
- **AndroidX Leanback** - Android TV UI components
- **OkHttp 4.12.0** - HTTP client for networking
- **Gson 2.10.1** - JSON parsing

## Architecture

### Package Structure
```
com.klaus.kmoviesapp/
├── models/          # Data models (Movie, Category)
├── scraper/         # Web scraping logic
├── adapters/        # RecyclerView adapters
├── activities/      # Activity classes
├── fragments/       # Fragment classes
└── utils/           # Utility classes
```

### Key Components

#### Web Scraping
- `FMoviesScraper` - Main scraping logic
- `ScraperTask` - AsyncTask for background scraping
- `MovieDetailTask` - Fetch detailed movie information
- `StreamUrlTask` - Extract streaming URLs

#### UI Components
- `MainActivity` - Main navigation and content browsing
- `MovieDetailActivity` - Detailed movie information
- `PlayerActivity` - Video playback with ExoPlayer
- `BrowseActivity` - Category browsing

#### Adapters
- `MovieAdapter` - Display movie cards in grid
- `CategoryAdapter` - Navigation menu items

## Setup & Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 34
- Gradle 8.1.4+

### Build Instructions
1. Clone the repository
2. Open project in Android Studio
3. Sync Gradle dependencies
4. Build and run on Android TV device/emulator

### Configuration
The app is configured to run on Android TV devices with:
- Landscape orientation
- Leanback launcher support
- No touchscreen requirement

## Permissions
- `INTERNET` - Required for content loading and streaming
- `ACCESS_NETWORK_STATE` - Check network connectivity

## Features Implementation

### Content Scraping
The app uses Jsoup to parse HTML from the source website and extract:
- Movie titles and metadata
- Thumbnail and backdrop images
- Ratings and duration
- Genre, country, and director information
- Streaming URLs

### Video Playback
ExoPlayer provides:
- Adaptive streaming support (HLS, DASH)
- Buffering management
- Playback controls
- Error handling
- Multiple format support

### UI/UX
- Card-based layout for content display
- Focus animations for TV navigation
- Loading indicators
- Error handling and user feedback
- Immersive fullscreen playback

## Development Notes

### Web Scraping Strategy
The scraper is designed to be flexible and handle various HTML structures. CSS selectors are used to target specific elements, with fallback selectors for robustness.

### Streaming Implementation
The app extracts streaming URLs from the source website and passes them to ExoPlayer. Support for multiple streaming protocols (HTTP, HLS, DASH) is included.

### Performance Optimization
- Asynchronous loading for all network operations
- Image caching with Glide
- Efficient RecyclerView implementation
- Proper lifecycle management

## Known Limitations
- Streaming URLs depend on source website structure
- Some content may require additional parsing
- Network connectivity required for all features

## Future Enhancements
- Search functionality
- Favorites and watchlist
- Continue watching feature
- Multiple quality options
- Subtitle support
- Offline downloads

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Disclaimer
This application is for educational purposes only. Users are responsible for ensuring they have the right to access and stream content.

## Contributing
Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## Author
Klaus Kiduyu

## Version
1.0.0
