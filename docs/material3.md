# Material 3 implementation notes

Applied the Compose-first Material 3 guidance from:
- https://m3.material.io/
- https://github.com/hamen/material-3-skill

The entry screen now uses a centralized `NearTheme`, dynamic dark color on Android 12+, semantic Material color roles, Material typography, 8dp-based spacing, large accessible touch targets and Material shapes. The logo is original vector-like Canvas artwork: a rook with an N cut-line; no Chess.com artwork is used.

The rest of the app is not yet fully migrated to this theme. A follow-up audit should remove raw colors from existing screens, add dynamic light/dark preference, adaptive layouts and TalkBack semantics to the Canvas board.
