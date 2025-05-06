# Love Calendar API

[![Kotlin](https://img.shields.io/badge/kotlin-2.1.20-purple.svg)](https://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/ktor-3.1.2-blue.svg)](https://ktor.io)
[![MongoDB](https://img.shields.io/badge/mongodb-5.4.0-yellow.svg)](https://www.mongodb.com)

## 🚀 Overview

A clean and functional backend server powering the Love Calendar mobile application. Built with Ktor and MongoDB, this API handles user authentication, profile management, and calendar data with solid security practices.

## ✨ Features

- **🛡️ Security**
    - Custom SHA256 hashing implementation
    - JWT authentication with access & refresh token mechanism
    - Ktor JWT authentication integration

- **🧩 Architecture**
    - Dependency injection with Koin
    - MongoDB for data persistence
    - Comprehensive exception handling with StatusPages

- **🧪 Testing**
    - 100% unit test coverage* on core functionality
    - Focused unit tests with kotlin.test, mockk and assertk
    - *Excluding boilerplate code: di, module, model, util packages, and Application.kt

- **🔌 API Endpoints**
    - Complete user management flows
    - Secure authentication processes
    - ContentNegotiation for request/response formatting

## 🔧 Tech Stack

- **Framework:** Ktor
- **Database:** MongoDB
- **Authentication:** JWT
- **Dependency Injection:** Koin
- **Testing:** kotlin.test, MockK, AssertK
- **Serialization:** kotlinx.serialization

## 📝 API Documentation

### Authentication

| Endpoint    | Method | Description                                   |
|-------------|--------|-----------------------------------------------|
| `/sign_up`  | POST   | Register a new user                           |
| `/sign_in`  | POST   | Authenticate user and receive tokens          |
| `/sign_out` | POST   | Invalidate current tokens                     |
| `/refresh`  | POST   | Generate new access token using refresh token |

### User Management

| Endpoint | Method | Description               |
|----------|--------|---------------------------|
| `/users` | GET    | Retrieve user information |
| `/users` | PUT    | Update user profile       |
| `/users` | DELETE | Delete user account       |

## 💡 Getting Started

```bash
# Clone the repository
git clone https://github.com/pavel-maiseichyk/love-calendar-api.git

# Navigate to project directory
cd love-calendar-api

# Start MongoDB with Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Set required environment variables
export ISSUER="your-issuer"
export AUDIENCE="your-audience"
export JWT_SECRET="your-secret-key"

# Run the application
./gradlew run

# Run tests
./gradlew test
```

## 🤝 Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request.

## 📄 License

This project is licensed under the MIT License – see the [LICENSE](./LICENSE) file for details.
