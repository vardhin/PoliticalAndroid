# Political Android App

A modern Android application for political news and gossip with administrative capabilities. This app provides users with the latest political articles, featured content, and category-based browsing, while offering admins a comprehensive dashboard for content management.

## 🚀 Features

### User Features
- **Article Browsing**: Browse latest and featured political articles
- **Category Filtering**: Filter articles by categories (General, Politics, etc.)
- **Article Detail View**: Read full articles with images and summaries
- **Search Functionality**: Search through articles
- **Responsive UI**: Material Design 3 with modern UI components

### Admin Features
- **Admin Dashboard**: Comprehensive content management interface
- **Article Creation**: Create new articles with images, summaries, and categories
- **Article Management**: Edit, update, and delete existing articles
- **User Authentication**: Secure login system with JWT tokens
- **Featured Content**: Mark articles as featured
- **Image Management**: Upload and manage article images

## 🛠️ Tech Stack

### Frontend (Android)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: Jetpack Navigation Compose
- **State Management**: Compose State & ViewModel
- **Networking**: Retrofit2 with OkHttp
- **Image Loading**: Coil
- **Dependency Injection**: Manual DI pattern
- **Data Persistence**: DataStore Preferences
- **Serialization**: Kotlinx Serialization

### Backend (Node.js)
- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: MongoDB with Mongoose ODM
- **Authentication**: JWT (JSON Web Tokens)
- **File Upload**: Multer
- **Security**: Helmet, CORS, Rate Limiting
- **Password Hashing**: bcryptjs
- **Deployment**: Vercel (Serverless)

### Development Tools
- **Build System**: Gradle (Kotlin DSL)
- **IDE**: Android Studio
- **Version Control**: Git
- **Target SDK**: Android 35 (API 35)
- **Minimum SDK**: Android 28 (API 28)

## 📱 App Architecture

```
app/
├── src/main/java/com/example/politicalandroid/
│   ├── data/           # Data models and DTOs
│   ├── network/        # API service interfaces
│   ├── repository/     # Data repository layer
│   ├── ui/
│   │   ├── screens/    # Compose UI screens
│   │   └── theme/      # App theming
│   ├── utils/          # Utility classes
│   └── viewmodel/      # ViewModels for state management
```

## 🔧 Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 8 or later
- Node.js 14+ (for backend)
- MongoDB Atlas account

### Frontend Setup
1. Clone the repository:
```bash
git clone https://github.com/vardhin/PoliticalAndroid.git
cd PoliticalAndroid
```

2. Open the project in Android Studio

3. Update the API base URL in `DashboardRepository.kt` and `ArticleRepository.kt`:
```kotlin
private const val BASE_URL = "your-backend-url/api/"
```

4. Build and run the app

### Backend Setup
1. Navigate to the backend directory and install dependencies:
```bash
cd backend
npm install
```

2. Create a `.env` file with the following variables:
```env
MONGODB_URI=your_mongodb_connection_string
JWT_SECRET=your_jwt_secret
REFRESH_TOKEN_SECRET=your_refresh_token_secret
NODE_ENV=production
```

3. Deploy to Vercel or run locally:
```bash
npm start
```

## 🔐 Authentication

The app uses JWT-based authentication with:
- **Access Tokens**: Short-lived (1 hour)
- **Refresh Tokens**: Long-lived (7 days)
- **Role-based Access**: Admin and Editor roles
- **Secure Storage**: Tokens stored in DataStore

## 📊 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Refresh access token

### Articles
- `GET /api/articles/latest` - Get latest articles
- `GET /api/articles/featured` - Get featured articles
- `GET /api/articles/category/:category` - Get articles by category
- `GET /api/articles/:id` - Get specific article
- `POST /api/articles` - Create new article (Admin)
- `PUT /api/articles/:id` - Update article (Admin)
- `DELETE /api/articles/:id` - Delete article (Admin)

### Media
- `GET /api/image/:articleId` - Get article image

## 🎨 UI Components

- **Material Design 3**: Modern Material You design system
- **Compose Navigation**: Type-safe navigation between screens
- **Lazy Loading**: Efficient list rendering with LazyColumn
- **Image Loading**: Optimized image loading with Coil
- **Form Validation**: Real-time input validation
- **Loading States**: Proper loading and error states
- **Swipe Refresh**: Pull-to-refresh functionality

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Rate Limiting**: API rate limiting for security
- **Input Validation**: Server-side input validation
- **CORS Protection**: Cross-origin request security
- **Password Hashing**: Secure password storage
- **Role-based Access**: Different access levels for users

## 📦 Dependencies

### Key Android Dependencies
```kotlin
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
implementation("androidx.navigation:navigation-compose")
implementation("com.squareup.retrofit2:retrofit")
implementation("io.coil-kt:coil-compose")
implementation("androidx.datastore:datastore-preferences")
```

### Key Backend Dependencies
```json
{
  "express": "^4.18.0",
  "mongoose": "^7.0.0",
  "jsonwebtoken": "^9.0.0",
  "bcryptjs": "^2.4.3",
  "helmet": "^6.0.0",
  "cors": "^2.8.5",
  "multer": "^1.4.5"
}
```

## 🚀 Deployment

### Android App
- Build APK: `./gradlew assembleRelease`
- Generate AAB: `./gradlew bundleRelease`
- Deploy to Google Play Store

### Backend
- Deployed on Vercel with serverless functions
- MongoDB hosted on MongoDB Atlas
- Automatic deployments via Git integration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Contact

- **Developer**: Vardhin
- **GitHub**: [@vardhin](https://github.com/vardhin)

## 🙏 Acknowledgments

- Material Design 3 for the design system
- Jetpack Compose for the modern UI toolkit
- MongoDB for the database solution
- Vercel for serverless deployment