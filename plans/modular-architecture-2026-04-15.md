# LifeOS Modular Architecture Plan
Date: 2026-04-15

## Core Approach

| Aspect | Decision |
|--------|----------|
| Loading | Compile-time (Android limitation) - all modules in app, user enables/disables |
| Onboarding | Only fresh installs; all modules unchecked by default |
| Module Manager | Always visible on home screen (even for upgraded users) |
| Existing users | All modules pre-enabled, no change in behavior |
| Data | Disabling = hide module + option to delete data |
| Source | Fetch module list from GitHub |

## Implementation Phases

### Phase 1: Core Infrastructure
- Create `ModuleConfig.kt` - module metadata model
- Create `InstalledModules.kt` - DataStore for enabled module IDs
- Update `ModuleRegistry` - add `getEnabledModules()`

### Phase 2: Onboarding (Fresh Installs Only)
- Create `ModuleOnboardingScreen.kt` - full-screen overlay
- Add `FIRST_LAUNCH` flag detection via DataStore
- Fetch module list from GitHub on launch

### Phase 3: Module Manager UI
- Create `ModuleManagerScreen.kt` - toggle enable/disable, remove/delete data
- Add "Manage Modules" card to HomeScreen

### Phase 4: Home Screen & Navigation
- Filter modules by `InstalledModules.getInstalled()`
- Add `module_manager` route to navigation

### Phase 5: GitHub Setup
- Create `module_installs/modules.json`
- Add module icons

### Phase 6: Backup Integration
- Include `installed_module_ids` in backup/restore

## File Changes

| File | Action |
|------|--------|
| `modules/lifeos_core/.../ModuleConfig.kt` | Create |
| `modules/lifeos_core/.../LifeOSModule.kt` | Modify |
| `app/.../InstalledModules.kt` | Create |
| `app/.../ui/home/HomeScreen.kt` | Modify |
| `app/.../ui/modules/ModuleManagerScreen.kt` | Create |
| `app/.../ui/onboarding/ModuleOnboardingScreen.kt` | Create |
| `app/.../MainActivity.kt` | Modify |
| `app/.../backup/BackupManager.kt` | Modify |
| `module_installs/modules.json` | Create |
| `module_installs/icons/*.png` | Create |

## Data Flow

```
App Launch
    |
    ├── First launch? ──Yes──> ModuleOnboardingScreen ──> Store selected IDs
    |                                              |
    |                                              v
    |                                         HomeScreen
    |                                              |
    └──No (upgrade)───────────────────────────────|
                                                 v
                                          ModuleManagerScreen
                                          (always accessible)
```

## GitHub Module List Format

`module_installs/modules.json`:
```json
{
  "version": "1.0",
  "modules": [
    {
      "id": "mealtracker",
      "name": "Meal Tracker",
      "description": "Track meals, calories, macros, and weight goals",
      "version": "1.0",
      "icon": "Restaurant",
      "isBuiltIn": true
    }
  ]
}
```
