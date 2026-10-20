# EcoTrack Java and XML practice build

This version follows `CODEX_EcoTrack_6_Hour_Implementation_Plan.md` and the supplied FixIt logic notes. It replaces the previous implementation with **one model, one singleton, one adapter, and two Activities**. The package remains `com.example.ecotrack2`; the working theme and dependencies are preserved.

## Read these five files in order

| File | Responsibility |
| --- | --- |
| `Challenge.java` | One challenge: ID, title, category, description, estimated minutes, impact, status, bookmark |
| `ChallengeManager.java` | Singleton with the full ArrayList, validated updates, completion counts, badge rules, and demo seed data |
| `ChallengeAdapter.java` | RecyclerView rows, click callbacks, original/displayed lists, and `applyFilter()` |
| `MainActivity.java` | Initialize Views, attach toolbar listeners, open the bottom sheet, call manager updates, refresh the adapter |
| `AchievementsActivity.java` | Display manager-derived completion count, percentage, progress bar, and earned badges |

There is no User, Category, Badge, UserProgress, SessionManager, database, or SharedPreferences. This is one person's local practice state. **Challenge status and bookmarks survive screen navigation and rotation while the Android process stays alive. They reset when that process is killed.** UI filter choices are saved in the Activity Bundle; this does not persist challenge data.

## Exact UI files

```text
res/layout/
    activity_main.xml
    activity_achievements.xml
    challenge_row.xml
    bottom_sheet_challenge.xml

res/menu/
    main_navigation.xml
    search_menu.xml
    filter_menu.xml
```

`MainActivity` has two independent Toolbar Views: `searchToolbar` and `filterToolbar`. Each calls `inflateMenu()` with its own menu and uses its own listener. There is no `setSupportActionBar()` call. A Material **BottomNavigationView**, with ID `bottomNavigationView`, loads `main_navigation.xml` in XML and handles the two screen choices through `setOnItemSelectedListener()`.

## Design that fits the competition

The interface uses a warm off-white background, forest-green accent, white cards, consistent 16–20dp spacing, and standard sans-serif fonts. Shapes, state selectors, and vector icons are plain XML; no additional dependencies or image downloads are needed.

- Challenges has a compact live progress card. It hides while the search action is expanded, giving the search results more room.
- Challenge cards show category, title, readable time, impact, a status pill, and the bookmark button. The full row opens details.
- The bottom sheet separates time/impact from the three status choices and keeps Save Status as the primary action.
- Achievements highlights overall progress, the four practice milestones, and earned badge names. The milestone indicators use the same 3/6/10/15 thresholds already defined in the manager.
- BottomNavigationView uses a rounded white background, horizontal icons and labels, and a pale-green selected indicator matching the reference. Its style is in `Widget.EcoTrack.BottomNavigation`. The existing Material 1.14 dependency supports `itemIconGravity="start"` directly, so custom action views are unnecessary. Each Activity selects its own menu item; the listener opens/finishes the other Activity without leaving a stale selection on return.

`Challenge.getEstimatedTimeLabel()` only formats the stored minutes for display: 10080 becomes `7 days`, 60 becomes `1 hour`, and durations such as 90 remain `90 min`. Data storage, the singleton, and filtering keep the original approach. For the six-hour build, implement behavior first and apply the shared styles and backgrounds after the core flow works.

The horizontal category buttons show **All** and the five required categories. They use a RadioGroup so one category is selected at a time. The selected button's text is the category string sent to the adapter.

**All statuses** clears the status restriction and Saved Only while keeping the current search/category. **Saved only** toggles independently of the selected status. **Reset all filters** clears search, category, status, and Saved Only; it is also available on the empty state.

## The FixIt flow

```text
User clicks a row
    -> ChallengeAdapter sends the Challenge through its listener
    -> MainActivity opens BottomSheetDialog
    -> user selects status or bookmarks the challenge
    -> ChallengeManager updates the object by stable ID
    -> adapter.updateList(manager.getAllChallenges())
    -> applyFilter() rebuilds the displayed list
    -> the row and empty-state summary refresh
```

The adapter's listener has `onChallengeClick(Challenge)` and `onBookmarkClick(Challenge)`. The second callback lets the row bookmark button toggle directly while keeping mutations in the Activity/manager. The adapter never changes manager data itself.

`challengesFull` is the source list. `challenges` is what the RecyclerView displays. `applyFilter()` always loops over the full list and combines search, category, status, and bookmark checks with **AND**. This prevents filters from permanently removing rows.

The manager returns a new list container containing the same Challenge objects. Removing or sorting an adapter list cannot remove manager records; manager changes are still visible to both Activities. IDs identify challenges even when filtering changes row positions.

## Bottom sheet

`showChallengeSheet(Challenge)` inflates `bottom_sheet_challenge.xml`, then uses `content.findViewById(...)` to fill title, category, description, estimated minutes, impact, and the selected status. It creates `new BottomSheetDialog(this)` inside an Activity method, matching the FixIt context rule.

The three RadioButtons use the exact labels **Not Started**, **In Progress**, and **Completed**. **Save status** sends the selected label and challenge ID to `updateStatus(...)`. The manager checks that the label is one of these three choices. **Save challenge / Remove from saved** toggles the bookmark. Each successful action refreshes the list, dismisses the sheet, and shows a Snackbar. **Close** dismisses without saving a pending status selection.

## Official values still needed

The 15 titles and five category names follow the supplied EcoTrack requirements. The official Resources folder and its challenge `.txt` details were not supplied.

**Every description, estimated duration, and impact score is a DEMO PLACEHOLDER.** They are isolated in `ChallengeManager.seedChallenges()`. The descriptions begin with `DEMO PLACEHOLDER:` and the UI labels time/impact as demo values. Replace these from the official Resources `.txt` before treating this as a final contest dataset. `estimatedMinutes` is an integer: the current Plastic-Free Week placeholder is 10080 minutes (seven days).

The PDF does not specify badge thresholds. This practice version uses the plan's proposed **3, 6, 10, 15** thresholds: Green Starter, Habit Builder, Planet Protector, Eco Champion. `getEarnedBadges()` derives names from the current completed count. Repeating completion never adds duplicate progress; reducing the count recalculates the earned badges. `getCompletionPercentage()` guards against division by zero and uses integer division, so one out of 15 is **6%**.

## XML previews and formatting

Open a screen or row layout XML file in Android Studio and select **Split** or **Design**. Each screen has `tools:context`, and data-driven fields have `tools:text`. The RecyclerView has `tools:listitem` and `tools:itemCount` for sample rows. The screen roots are visible by default, so no Java visibility changes are needed to preview them. The bottom-sheet XML previews its custom content. The bottom navigation menu is assigned with `app:menu`, so its labels and icons also appear in XML previews.

Java uses one declaration/statement per line, expanded methods and `if/else` blocks, grouped imports, named UI handlers, and comments explaining the ownership/identity decisions. XML places each attribute on its own line. No Streams, reflection, global string registry, or extra architecture is used.

## Build and verify

Use Android Studio's Run button with **Medium_Phone_API_36.0**, or use the existing Gradle wrapper with a working JDK:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Eclipse Adoptium/jdk-21.0.12.8-hotspot'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

- `ChallengeManagerTest`: 15 unique IDs, three challenges per category, default state, all statuses, invalid status labels, bookmarks, exact badge thresholds, duplicate prevention, and readable durations.
- `ChallengeAdapterTest` runs on Android because RecyclerView notifications require Android's Observable implementation. It checks reversible and combined filters, list copying, and refresh without losing filters.
- `EcoTrackFlowTest`: category navigation, filtered ID updates, all status options, reopened sheets, bookmarks/Saved Only, empty state/reset, progress/badges, rotation, and repeated Activity navigation.

The two generated Android Studio example tests are retained separately. App tests restore the singleton's challenge states between cases; they do not use reflection or a test reset API in production code.

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Six-hour practice order

1. Agree on the five class names, method signatures, and XML IDs.
2. Write `Challenge` and `ChallengeManager`; prove the 15 records/default states.
3. Build the row, adapter, two Activity layouts, and navigation.
4. Connect the bottom sheet, all three statuses, and bookmarks.
5. Display progress and badge names from the manager.
6. Add search/status/saved filters using the full/display list pattern.
7. Run the acceptance checks and rehearse the demonstration. Reserve the final 25 minutes for fixes and the APK.

Demo: Waste Reduction -> Plastic-Free Week -> In Progress -> reopen and Completed -> bookmark -> Saved Only -> Achievements -> return to Challenges. Complete three challenges to demonstrate the first proposed badge.
