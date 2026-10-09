# Sahayak AI (सहायक AI) 🇮🇳
### AI-Powered Government Scheme & Citizen Welfare Assistant

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![Google Gemini](https://img.shields.io/badge/AI-Google%20Gemini%20API-4285F4?logo=google&logoColor=white)](https://aistudio.google.com/)
[![Supabase](https://img.shields.io/badge/Backend-Supabase%20PostgreSQL-3ECF8E?logo=supabase&logoColor=white)](https://supabase.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Native%20Android%20MVC-blue)](#tech-stack--architecture)

---

## 📖 Overview

**Sahayak AI** is a native Android application designed to bridge the gap between Indian citizens and the hundreds of welfare and developmental schemes offered by Central and State Governments. By pairing **user demographic profiling** with a deterministic **rule-based eligibility-ranking engine** and **multilingual Google Gemini AI assistance**, Sahayak AI helps students, women, farmers, and job seekers discover schemes they qualify for, understand complex eligibility guidelines, and track their application lifecycle.

---

## 🎯 Problem Statement & Proposed Solution

### The Challenge
India has hundreds of government welfare and financial assistance programs across education, agriculture, health, entrepreneurship, and social security. However, citizens often face severe hurdles:
* **Information Fragmentation:** Schemes are scattered across dozens of different ministry portals and state websites.
* **Complex Bureaucratic Jargon:** Eligibility criteria and document requirements are often difficult to comprehend.
* **Missed Opportunities:** Eligible candidates miss critical deadlines because they are unaware that a scheme exists for their demographic.

### The Sahayak AI Solution
1. **Personalized Discovery:** Compares a citizen's age, gender, state, education, category, and household income against scheme databases to provide personalized match percentages.
2. **Interactive AI Guidance:** Enables citizens to ask conversational questions about schemes in simple Hindi, English, Hinglish, or regional languages.
3. **In-App Guidelines & Document Checklists:** Provides clear step-by-step procedures, mandatory document checklists, and application status tracking in one unified interface.

---

## 🌟 Key Features (Actually Implemented)

### 1. 🔐 User Authentication & Secure Session Management
* **Email & Password Authentication:** Powered by Supabase Auth (`/auth/v1/signup`, `/auth/v1/token`).
* **Session Persistence:** Secure local caching of session tokens, refresh tokens, and user credentials via `SharedPreferences`.
* **Password Recovery:** Direct password reset email flow via Supabase Auth recovery endpoints.

### 2. 👤 Citizen Demographic Profiling
* Multi-attribute profile builder collecting:
  * **Basic Details:** Name, Age, Gender.
  * **Location:** State & District selection.
  * **Socio-Economic Factors:** Annual Household Income brackets (e.g., *Below ₹1 Lakh*, *₹1–3 Lakh*, *₹3–5 Lakh*, *Above ₹8 Lakh*), Social Category (General, OBC, SC, ST, EWS).
  * **Occupation & Education:** Education level (School, Undergraduate, Postgraduate, ITI/Diploma), Current Course, Occupation, and Student status.
* Used as the foundational input for the eligibility matching engine.

### 3. 🎯 Rule-Based Scheme Match & Ranking Engine
* Automatic evaluation of all registered government schemes against the citizen's profile.
* Multi-factor scoring (Income, State/Jurisdiction, Age Limits, Gender restrictions, Caste/Category, Education).
* **Strict Income Disqualification:** Automatically prevents high-income users from being recommended poverty-targeted or low-income threshold schemes.
* Categorization of schemes into **"Likely Eligible" (80%+)**, **"Possibly Eligible" (50–79%)**, **"Needs Verification" (25–49%)**, and **"Not Eligible"**.

### 4. 🤖 Multilingual AI Chat Assistant (Google Gemini)
* Real-time conversational scheme assistant powered by the Google Gemini API.
* **Dynamic Context Injection:** Automatically injects the user's demographic profile, matching schemes, and conversation history into the Gemini system prompt.
* **Automatic Multi-Model Resilience:** Built-in model rotation (`gemini-2.5-flash-lite` ➔ `gemini-2.5-flash` ➔ `gemini-1.5-flash`) with automatic fallback to prevent downtime during peak server loads.
* **Multi-Session Chat History:** Create new conversations, switch between previous chats, and delete unwanted chat sessions (with cascade cleanup in PostgreSQL).

### 5. 📋 Scheme Details, Guidelines & Document Checklist
* Detailed breakdown of benefits, eligibility criteria, and step-by-step application instructions.
* **Native In-App Guidelines Modal:** Complete offline-friendly modal dialog with eligibility rules, mandatory document checklist, and important instructions with a clean **Back** navigation action.
* **Direct Official Portal Integration:** One-tap navigation to official government portal links with smart search fallbacks.
* **Deadline Tracking:** Real-time badge indicators for scheme deadlines (e.g., *🟢 Ongoing*, *🟡 15 days left*, *🔴 Expiring soon*, *❌ Expired*).

### 6. 📌 Saved Schemes & Multi-State Application Tracker
* **Bookmark & Saved Schemes:** Save schemes to review later with instant cloud synchronization.
* **Application Lifecycle Tracker:** Track application progress across 5 distinct stages with a visual progress bar:
  1. *Not Started (0%)*
  2. *Preparing Documents (25%)*
  3. *Application Started (50%)*
  4. *Submitted (75%)*
  5. *Completed (100%)*
* **Unlimited Status Updates:** Built with PostgreSQL composite unique key upserts (`on_conflict=user_id,scheme_id`) and automatic `PATCH` fallback for editing application milestones.

### 7. 🔔 In-App Notifications & Alerts
* Smart notifications for new scheme matches, upcoming deadlines, and profile completion reminders.
* Match-aware alert handling: Clicking an alert evaluates the citizen's current profile and opens the exact matching scheme.

---

## 🛠️ Tech Stack & Architecture

### Mobile Client (Android)
* **Language:** Java 8+
* **Framework:** Native Android SDK (Min SDK 24, Target SDK 34)
* **UI & Layouts:** Vanilla Android XML layouts, Material Design Components (`MaterialCardView`, `ChipGroup`, `BottomNavigationView`, `TextInputLayout`, `RecyclerView`)
* **Networking & HTTP:** OkHttp 4.12.0
* **Data Serialization:** `org.json` (Native lightweight JSON parsing)

### Backend & Cloud Infrastructure
* **Database & Auth:** Supabase (PostgreSQL 15 with Row Level Security - RLS)
* **REST API:** Supabase PostgREST Endpoints (stateless HTTPS API over OkHttp)
* **Artificial Intelligence:** Google Gemini API (`generativelanguage.googleapis.com`)
* **Build System:** Gradle (Kotlin DSL / Groovy) with BuildConfig injection

### Architectural Pattern
The project follows a modular **Model-View-Controller (MVC) / Helper-Driven** Android architecture:
* **Models (`com.sahayakai.app.models`):** Immutable Java domain entities (`Scheme`, `User`, `ApplicationModel`, `ChatMessage`, `ChatSession`, `NotificationModel`).
* **Activities & Adapters (`com.sahayakai.app.activities / adapters`):** View controllers managing UI lifecycle, animations, and user interactions.
* **Helpers (`com.sahayakai.app.helpers`):** Specialized singleton and utility services handling business logic, network communication, and AI prompt engineering (`SupabaseHelper`, `GeminiHelper`, `SchemeMatchHelper`, `SchemeDatabaseHelper`).

---

## ⚙️ How the Eligibility Matching Logic Works

The matching algorithm (`SchemeMatchHelper.java`) compares the citizen's profile fields with the scheme's criteria:

```
+-------------------------------------------------------------+
|                     User Profile Attributes                 |
| (Age, Gender, State, Category, Income, Education, Status)   |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                 Scheme Eligibility Rules Evaluation         |
+-------------------------------------------------------------+
| 1. Income Check     -> Disqualifies if user income > max    |
| 2. State Check      -> Matches "All India" or user State    |
| 3. Gender Check     -> Matches "All" or user Gender         |
| 4. Category Check   -> Matches "All" or user Category       |
| 5. Age Limits Check -> Verifies min_age <= user_age <= max  |
| 6. Education Check  -> Matches required study level         |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                     Match Percentage Scoring                |
|       Match % = (Matched Criteria / Evaluated Criteria)     |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                    Ranked Scheme Recommendation             |
|   🟢 Likely Eligible (80%+)  |  🟡 Possibly Eligible (50-79%)|
+-------------------------------------------------------------+
```

---

## 📱 App Screens & Navigation Flow

```
[ Splash Screen (MainActivity) ]
             |
    +--------+--------+
    |                 |
[ Login ] <====> [ Signup ]
    |
    v
[ Profile Setup (If incomplete) ]
    |
    v
[ Home Screen (HomeActivity) ]
    ├── 🌟 Recommended Schemes List
    ├── 🎓 Quick Filter Chips (Scholarships, Women, Students, Employment)
    ├── 🔔 Notifications Activity (NotificationsActivity)
    ├── 💬 AI Assistant (ChatActivity <--> ChatHistoryActivity)
    ├── 📋 Scheme Details (SchemeDetailsActivity)
    │     ├── 📜 In-App Guidelines Modal
    │     ├── 🌐 Official Portal Web Link
    │     ├── 💾 Save / Bookmark Toggle
    │     └── 📊 Application Status Tracker Dialog
    ├── 🔖 Saved Schemes Screen (SavedSchemesActivity)
    └── 👤 Profile Management (ProfileActivity)
```

---

## 📂 Project Structure

```text
Sahayak-AI/
├── app/
│   ├── build.gradle                   # App-level dependencies & BuildConfig fields
│   ├── proguard-rules.pro             # Proguard configuration
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml    # App permissions & activity declarations
│           ├── java/com/sahayakai/app/
│           │   ├── SahayakAIApp.java  # Application entry point & initialization
│           │   ├── activities/        # UI Activities (10 screens)
│           │   │   ├── MainActivity.java
│           │   │   ├── LoginActivity.java
│           │   │   ├── SignupActivity.java
│           │   │   ├── ProfileActivity.java
│           │   │   ├── HomeActivity.java
│           │   │   ├── ChatActivity.java
│           │   │   ├── ChatHistoryActivity.java
│           │   │   ├── SchemeDetailsActivity.java
│           │   │   ├── SavedSchemesActivity.java
│           │   │   └── NotificationsActivity.java
│           │   ├── adapters/          # RecyclerView Adapters
│           │   │   ├── SchemeAdapter.java
│           │   │   ├── ChatAdapter.java
│           │   │   ├── ChatHistoryAdapter.java
│           │   │   ├── SavedSchemeAdapter.java
│           │   │   └── NotificationAdapter.java
│           │   ├── helpers/           # Business Logic & API Services
│           │   │   ├── GeminiHelper.java
│           │   │   ├── SupabaseHelper.java
│           │   │   ├── SchemeMatchHelper.java
│           │   │   └── SchemeDatabaseHelper.java
│           │   └── models/            # Data Models
│           │       ├── User.java
│           │       ├── Scheme.java
│           │       ├── ApplicationModel.java
│           │       ├── ChatMessage.java
│           │       ├── ChatSession.java
│           │       └── NotificationModel.java
│           └── res/                   # Layouts, Drawables, Styles, Colors, Menus
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle                       # Top-level Gradle configuration
├── settings.gradle                    # Project module settings
├── gradle.properties.example          # Safe template for credentials
├── .gitignore                         # Git exclusion rules (secrets & build caches)
└── README.md                          # Project documentation
```

---

## 🚀 Setup & Installation Instructions

### Prerequisites
* **Android Studio:** Hedgehog (2023.1.1) or newer
* **Java Development Kit (JDK):** Version 17 (or bundled Android Studio OpenJDK)
* **Android SDK:** Platform API 34, Build-Tools 34.0.0
* **Device / Emulator:** Android 7.0 (Nougat, API Level 24) or higher

### Step 1: Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/Sahayak-AI.git
cd Sahayak-AI
```

### Step 2: Configure Environment Credentials
1. In the project root folder, create a new file named `gradle.properties` (or copy from `gradle.properties.example`):
   ```bash
   cp gradle.properties.example gradle.properties
   ```
2. Open `gradle.properties` and provide your credentials:
   ```properties
   # Google Gemini API Key (from https://aistudio.google.com/)
   GEMINI_API_KEY=your_gemini_api_key_here

   # Supabase Project Credentials (from Supabase Dashboard -> Settings -> API)
   SUPABASE_URL=https://your-project-id.supabase.co
   SUPABASE_ANON_KEY=your_supabase_anon_public_key_here
   ```

### Step 3: Set Up Supabase Database Tables
Open your **Supabase Dashboard** ➔ **SQL Editor**, create a new query, and run the schema definition to initialize the required tables:
* `profiles` (User demographic profiles)
* `schemes` (Public government schemes repository)
* `chat_sessions` & `chat_messages` (Conversational chat records)
* `saved_schemes` (User bookmarked schemes)
* `applications` (Application progress tracking)
* `notifications` (In-app alerts)

*(Row Level Security policies should allow users to access their own data via `auth.uid() = user_id` and allow public read access on `schemes`.)*

### Step 4: Build & Run in Android Studio
1. Open Android Studio and select **File ➔ Open ➔ Sahayak-AI folder**.
2. Allow Gradle to sync dependencies.
3. Select an Android device or emulator (API 24+) and click **Run (Shift + F10)**.

---

## 🔮 Future Enhancements

* 🎙️ **Voice-Based Chatbot (Speech-to-Text & Text-to-Speech):** Allow rural and non-literate citizens to speak their questions in local dialects and hear scheme explanations aloud.
* 🌐 **Full App UI Localization:** In-app language toggle translating static UI buttons and screens into Hindi, Punjabi, Bengali, Marathi, Tamil, and Telugu.
* 📄 **AI Document Scanner & OCR:** Automatic verification of Aadhaar, Income Certificate, and Caste Certificate to pre-fill eligibility fields and check document validity.
* 🔔 **Push Notifications:** Firebase Cloud Messaging (FCM) integration for background deadline alerts even when the app is closed.

---

## 📄 License

This project is created for educational, academic, and demonstration purposes. Please refer to the repository owner for licensing terms and permissions before commercial or public redistribution.
