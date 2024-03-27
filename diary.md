# Note

I have not cloned whole repository. Instead, only the PictureInPictureKotlin directory from github
and imported it in Android Studio.

Out of 3 given tasks, I am starting with task 3 first.

### Task 3: New Feature

1. Requirement: I am required to make the timer persistent across activity navigation in the app.
2. Approach: I will be using SharedPreferences to store the timer value and retrieve it when
   required.
3. Steps:
    1. Since the timer is in MainActivity and the viewmodel for MainActivity is MainViewModel, I
       will
       be making changes in MainViewModel only.
    2. Initially I thought of using MainViewModel methods onCleared() and init() to
       save and restore timer value.
    3. But then I realized that onCleared() is not always called when the activity is destroyed. In
       cases when app is killed from recent apps. So I end up using MainActivity lifecycle methods
       onStart() and onStop() to save and restore timer value.
    4. Next step is to define SharedPreferences in MainViewModel and use it to save and restore
       timer value. But the shared preferences required android context which is not available in
       viewmodel. So two options came to my mind:
        1. Use AndroidViewModel instead of ViewModel.
        2. Inject SharedPreferences in MainViewModel from MainActivity.
    5. I chose the second option as it is more clean and follows the MVVM architecture. But there is
       a problem with this approach. I cannot use constructor injection as MainViewModel is created
       by ViewModelProvider and I cannot pass any arguments to it. So, I need to create a factory to
       inject SharedPreferences in MainViewModel. I went ahead and created MainViewModelFactory.
    6. I have made changes in MainActivity to use MainViewModelFactory to create MainViewModel.
    7. I have created a saveCurrentState() method in MainViewModel to save the timer value in
       SharedPreferences. This method will be called from onStop() method of MainActivity. I need to
       save start-up time, timer value and running flag, whether the
       timer is running or not.
    8. Similarly, I have created a restoreState() method in MainViewModel to restore the timer value
       from SharedPreferences. This will be called from onStart() method of MainActivity.
    9. The idea of restoring the timer value is to calculate the time elapsed if the timer was
       running when the activity was destroyed and start the timer. Similarly, if the timer was not
       running when activity was destroyed, then I will just set the saved timer value. In this
       case the timer will not start.
    10. I have tested the app and it is working as expected. The timer is persistent across activity
        navigation.

### Task 2: Unit tests

1. Requirement: I am required to write unit tests for the MainViewModel class.
2. Approach: I will be using JUnit and Mockito to write android tests.
3. Steps:
    1. I have created MainViewModelTest class in the androidTest directory.
    2. I am thinking of writing couple of tests for the MainViewModel class:
        1. timerDefaultState()
        2. startOrPauseTimer()
        3. resetTimer()
        4. restoreStartState()
        5. restorePauseState()
        6. saveStartState()
        7. savePauseState()
    3. I will be using Mockito library to mock dependencies like SharedPreferences.
    4. I had to add a test helper class for live data observer.
    5. I have used InstantTaskExecutorRule to execute tests synchronously and in main thread.

### Enhancements that can be done:

1. Create a separate class for SharedPreferences and inject it in MainViewModel for better
   separation
   of concerns and testability.
2. Using Kotlin Timer instead of coroutines for better control over timer (start/stop/pause) and
   testability.

> I have not done Task 1 as changing minVersion to 21 means disabling Picture-in-Picture mode for
> devices below API 26. This requires quite a bit of changes in the app and I think it will be a
> overhead for this task. I can do it if required.
   

