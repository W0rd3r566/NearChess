package dev.nearchess

// Named-argument adapter used by the current training search.
// Delegates to the validated rules implementation, without changing move semantics.
internal fun Position.play(it: Move, compatibility: Unit = Unit): Position = this.play(it)
