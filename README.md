## Application Architecture

### PetAdoptionApplication.kt
The **Application class** that extends Android's `Application`. It serves as the entry point for the entire Android application
lifecycle. This class is instantiated before any other component (activities, services, etc.) and lives for the entire lifetime
of the app process. It's typically used for global initialization tasks and dependency injection setup.


### PetAdoptionApp.kt
A **Composable function** that represents the main UI entry point of the app. This is where the Compose UI tree begins, 
handling the app's state management (login status, user role) and rendering the main scaffold structure. 
While `PetAdoptionApplication` handles the Android framework layer, `PetAdoptionApp` handles the UI layer using Jetpack Compose.
