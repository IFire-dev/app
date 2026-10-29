# HabitForge: Gamified Habit Tracker with Interactive 2x4 Widget

A gamified Android habit tracking app built with Jetpack Compose, featuring an interactive 2x4 home screen widget with one-tap completion, customizable user accounts with avatar personalization, an XP leveling system, and multiple rich themes including Pure OLED Black.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions incorporate the choices confirmed in Phase 1:
> - **2x4 Widget Experience**: Interactive scrollable/list layout displaying today's habits with direct one-tap completion checkboxes and real-time status updates without needing to open the app.
> - **Gamification & Account**: Checkbox task completion awards instant XP, unlocks levels and titles, and includes a full profile management screen with custom username, title badges, and profile picture (PFP) selection.
> - **Theme System**: Comprehensive theme picker including Pure OLED Black (#000000 high-contrast battery-saving aesthetic), Midnight Emerald, Cyber Neon Amethyst, Sunset Ember, and Dynamic Material You.

---

### 1. Overview & Core Concept

**What It Does**:  
HabitForge transforms daily self-discipline into an engaging RPG-style progression. Users create and manage daily habits, check them off in-app or straight from their Android home screen using a high-density 2x4 interactive widget, and earn XP to level up their custom persona.

**Target Audience & Persona**:  
Productivity enthusiasts, students, and professionals who want quick home-screen friction-free habit logging, deep dark/OLED aesthetics, and satisfying gamified progress tracking.

**Key Value**:  
- **Zero-Friction Widget**: Complete habits in under 1 second right from the phone's home screen via a 2x4 Android AppWidget.
- **Rewarding Feedback**: Visual burst animations, XP flyouts, streak multipliers, and level-ups.
- **Custom Identity**: Profile personalization with custom avatars, titles, and stats.
- **Battery-Friendly Visuals**: True Pure OLED Black theme designed for AMOLED displays alongside multiple curated colorways.

---

### 2. User Experience & Visual Design

#### Key User Flows

1. **Daily Check-Off Flow (App & Widget)**:
   - *In-App*: User opens HabitForge, sees today's progress ring (e.g. 4/6 completed), taps a habit checkbox. The checkbox springs with a tactile bounce, an "+35 XP" badge floats upward, the progress ring animates, and streak counts tick up.
   - *On Widget (2x4)*: User glances at home screen, sees pending habits with streak badges. Tapping the check button on a habit sends an immediate background action, updates Room database, awards XP, and immediately reflects the completed state on the widget RemoteViews.

2. **Habit Creation & Customization**:
   - Tap FAB (+) to open habit creation bottom sheet.
   - Set habit title, description, category (Fitness, Mind, Craft, Health, Routine), target icon, color accent, and base XP reward (e.g., 20 XP for Quick, 40 XP for Deep focus).

3. **Account & Persona Profile Flow**:
   - Tap the top-right profile avatar or bottom nav Profile tab.
   - View current level (e.g. "Level 7 Disciplinarian"), total earned XP, progress to next level, current streak, and completion rate.
   - Edit username, select from a curated gallery of custom avatar icons or upload a custom image (PFP), and unlock profile achievement badges.

4. **Theme Selection Flow**:
   - Access Theme settings to toggle between:
     - **Pure OLED Black**: High-contrast #000000 true black background, deep dark elevated cards (#121212), and electric emerald/amber accents.
     - **Midnight Emerald**: Deep forest slate with luminous mint and neon green accents.
     - **Cyber Neon (Amethyst)**: Deep obsidian with vibrant violet and electric cyan.
     - **Sunset Ember**: Warm twilight base with blazing orange and coral highlights.
     - **Material You Dynamic**: System-adaptive wallpaper colors.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Widget Architecture (Android AppWidgetProvider with RemoteViews & ListView Service)**:
  - *Chosen Approach*: Standard Android AppWidget with `RemoteViewsService` and `RemoteViewsFactory` collection, backed by pending intent broadcast receivers for completion toggles and click actions.
  - *Why*: Provides seamless 2x4 home screen integration across all Android 12+ and 14/15/16 devices, supports scrollable collections, instant click actions, and doesn't rely on experimental or heavy unbundled libraries.
  - *Alternative Considered*: Glance for AppWidgets (added build weight and potential compatibility friction vs. rock-solid canonical RemoteViews collection).

- **Decision 2: Local Persistence Engine (Room Database + Kotlin Flow)**:
  - *Chosen Approach*: Android Room database with reactive `Flow` queries, entity relations for `Habit`, `HabitLog`, and `UserProfile`.
  - *Why*: Habits and home screen widgets require instant, offline-first, sub-millisecond response times. Room with KSP is fully configured and provides type-safe local transactions for streak and XP math.
  - *Data Sync*: When the widget or app modifies a habit, Room updates, which notifies active flows in the UI and sends an AppWidget update broadcast to refresh home screen widgets instantly.

- **Decision 3: Gamification Balance (XP & Leveling Curve)**:
  - *Chosen Approach*: Base XP (25-50 XP) per habit completion, with streak bonus multipliers (+5 XP per day of consecutive streak, capped at 2x). Levels follow quadratic progression `Level = floor(sqrt(XP / 50)) + 1` ensuring early quick rewards and steady long-term milestone achievements.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                      Android Device                         │
│                                                             │
│   ┌─────────────────────┐       ┌───────────────────────┐   │
│   │  Jetpack Compose UI │       │   2x4 AppWidget       │   │
│   │  (HabitForge App)   │       │   (RemoteViews)       │   │
│   └──────────┬──────────┘       └───────────┬───────────┘   │
│              │                              │               │
│              ▼                              ▼               │
│   ┌─────────────────────┐       ┌───────────────────────┐   │
│   │ HabitViewModel &    │       │ HabitWidgetReceiver   │   │
│   │ ProfileViewModel    │       │ & WidgetUpdateService │   │
│   └──────────┬──────────┘       └───────────┬───────────┘   │
│              │                              │               │
│              └──────────────┬───────────────┘               │
│                             │                               │
│                             ▼                               │
│             ┌───────────────────────────────┐               │
│             │     HabitRepository           │               │
│             │  (Habits, Logs, XP & Profile) │               │
│             └───────────────┬───────────────┘               │
│                             │                               │
│                             ▼                               │
│             ┌───────────────────────────────┐               │
│             │     Room Database (SQLite)    │               │
│             │  - HabitEntity                │               │
│             │  - HabitCompletionEntity      │               │
│             │  - UserProfileEntity          │               │
│             └───────────────────────────────┘               │
└─────────────────────────────────────────────────────────────┘
```

#### Core Database Schema
1. **`HabitEntity`**:
   - `id`: Long (PrimaryKey)
   - `title`: String
   - `description`: String
   - `iconName`: String (e.g., "fitness", "water", "book", "code", "meditate")
   - `colorHex`: Long
   - `targetDays`: Int (Bitmask for Mon-Sun or daily)
   - `xpReward`: Int (default 30)
   - `createdAt`: Long
   - `isArchived`: Boolean

2. **`HabitCompletionEntity`**:
   - `id`: Long (PrimaryKey)
   - `habitId`: Long (ForeignKey)
   - `completedDate`: String (ISO date `YYYY-MM-DD`)
   - `completedTimestamp`: Long
   - `xpEarned`: Int

3. **`UserProfileEntity`**:
   - `id`: Int (PrimaryKey = 1)
   - `username`: String (default "Habit Adventurer")
   - `title`: String (e.g. "Level 1 Novice")
   - `avatarId`: String (preset icon id or custom URI)
   - `currentXp`: Int
   - `level`: Int
   - `activeTheme`: String ("oled", "midnight_emerald", "cyber_neon", "sunset_ember", "system")

#### 2x4 Widget Specification
- **Widget dimensions**: Target width 4 cells (~250-320dp), height 2 cells (~110-140dp) defined in `habit_widget_info.xml`.
- **Layout**: Header displaying "Today's Habits" + overall progress percentage + Add/Refresh shortcut; body contains a scrollable list of habit items each featuring category icon, habit title, streak count, and a check button with pending intent triggering `TOGGLE_HABIT` action.
- **Immediate Feedback**: On tap, broadcasts `ACTION_HABIT_TOGGLE`, updates database, queries latest habit states, and triggers `AppWidgetManager.updateAppWidget` to reflect completed checkmarks instantly.
