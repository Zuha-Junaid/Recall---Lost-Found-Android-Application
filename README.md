RECALL – Lost & Found Application 📍

RECALL is a community-driven Lost & Found Android application designed to help users report, search, and recover lost items efficiently through a structured digital platform.

Built as part of a Mobile Application Development (MAD) project, the application focuses on real-time communication, secure authentication, and scalable cloud-based architecture using Firebase.

🚀 Features

WALKTHROUGH: https://drive.google.com/file/d/1GvTMs-rXmWNwPulIn9EfSjwWZQkUa0b4/view?usp=sharing

👤 User Management

Email & password authentication via Firebase Auth

Secure user profiles with display picture support

Persistent login sessions

📦 Item Listings

Create Lost or Found item reports

Upload up to 4 images per listing

Add location, category, description, and timestamp

Edit, update, or resolve listings

🔎 Search & Filtering

Keyword-based search system

Filter by category, item type, and location

Token-based search optimization for Firestore queries

💬 Real-Time Messaging

One-to-one chat between listing owner and claimant

Live message updates using Firestore listeners

Conversation threads linked to specific listings

🔔 Notifications

Firebase Cloud Messaging (FCM) integration

Alerts for new messages, claims, and listing updates

🗂 Listing Management

Mark items as Open / Resolved / Closed

Archive resolved listings while keeping searchable history

🏗 Tech Stack

Layer	Technology

Platform	Android (API 24+)

Language	Java (JDK 11)

Architecture	MVVM

Backend	Firebase (Auth, Firestore, Storage, FCM)

UI	Material Design 3

Build Tool	Gradle

🧠 Architecture Overview

The application follows MVVM (Model–View–ViewModel) architecture:

View (UI Layer): Activities & Fragments handle UI rendering

ViewModel (Logic Layer): Manages UI state and business logic

Repository (Data Layer): Abstracts Firebase operations

All data is stored and synchronized using Google Firebase Cloud Firestore, enabling real-time updates without a custom backend.

🗄 Database Structure (Firestore)

👤 users

uid

displayName

email

photoUrl

createdAt

fcmToken

reportCount

📦 listings

listingId

type (LOST / FOUND)

title

category

description

location (GeoPoint)

imageUrls[]

ownerId

status (OPEN / RESOLVED / CLOSED)

createdAt

tokens[]

💬 conversations

conversationId

listingId

participants[]

lastMessage

lastMessageAt

unreadCount

📨 messages (subcollection)

messageId

senderId

text

imageUrl

sentAt

readBy[]

🔐 Security Model

Firebase Authentication for secure login

Firestore Security Rules enforce:

Users can only modify their own data

Only participants can access conversations

Listings are editable only by owners

Firebase Storage rules restrict unauthorized file access

All communication encrypted via TLS

📱 Supported Devices

Minimum SDK: Android 7.0 (API 24)

Target SDK: Android 14 (API 34)

Orientation: Portrait (primary), Landscape supported

⚙️ Setup Instructions

1. Clone Repository

git clone https://github.com/your-username/recall-lost-found.git

2. Firebase Setup

Create a Firebase project

Enable:

Authentication (Email/Password)

Firestore Database

Storage

Cloud Messaging

Download google-services.json

Place it inside /app directory

3. Run Project

Open in Android Studio (Flamingo or later)

Sync Gradle

Build & Run on emulator/device (API 24+)

📊 Testing

Unit Testing: ViewModels & Repositories (Mockito)

Integration Testing: Firebase Emulator Suite

Manual Testing: Verified across multiple Android devices (API 24–34)

🚧 Known Limitations

No advanced full-text search engine (uses token-based search)

No multilingual support (English only)

Manual moderation required for abuse handling

Limited offline map functionality

🔮 Future Enhancements

AI-powered item matching (image + text similarity)

Geo-fenced notifications for nearby lost items

Multi-language support (Urdu + others)

Admin dashboard for moderation

Dark mode support

Advanced search using Elasticsearch / Algolia

👩‍💻 Author

Zuha Junaid

Supervisor: Sir M. Arsalan
📜 License

This project is developed for academic purposes (MAD Project).
All rights reserved unless explicitly stated otherwise.

⭐ Acknowledgement

Special thanks to the faculty and peers who provided guidance and feedback throughout the development process.
