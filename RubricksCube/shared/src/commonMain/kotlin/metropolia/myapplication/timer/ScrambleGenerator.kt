package metropolia.myapplication.timer

/** Generates a WCA random-state 3x3 scramble off the UI thread. */
expect suspend fun generate3x3Scramble(): String
