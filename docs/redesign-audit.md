# Full redesign audit — baseline 4adb2bf

## Existing architecture

- LaunchActivity: entry dashboard and tabs; routes to Activity-based modes.
- MainActivity: local and bot game state in Compose; uses shared MatchScreen.
- MatchScreen: board-first layout, player initials, read-only position history, Material icons, AlertDialog menu.
- NearbyActivity: ViewModel with host-authoritative MatchSession; separate lobby and game layout; no shared MatchScreen yet.
- TimedActivity: separate clock ViewModel and game layout.
- ProfileActivity: local nickname and sampled JPEG picker; not integrated into player panels.
- NearTheme: dark dynamic palette and original logo; not used consistently in all Activities.
- BoardView: shared original SVG renderer, gestures, move/castle/capture animation and outcome overlay.
- BoardOutcome: mate/pat/insufficient-material effects; no general result state for resignation or timeout. Outcome deduplication is keyed by FEN rather than a stable game ID.
- TrainingBot: negamax with alpha-beta, fixed three-ply search and material-only evaluation. No measured Elo, difficulty selection or per-opponent statistics.
- BluetoothTransport/Wire: RFCOMM and bounded versioned framing; preserve move-validation and transport behavior during UI migration.
- Chess: pure rules module; do not rewrite for UI work.

## Missing screens and real data dependencies

History, achievements and settings are not complete standalone screens. XP and achievements must be derived from persisted completed games; never seed fictional records. Bot difficulty is independent of profile XP.

## Acceptance matrix at audit time

| Screen | Redesign complete | Device verified |
|---|---|---|
| Home | No | No |
| Bot selection | No | No |
| Shared match | Partial | No |
| Bluetooth | No | No |
| Profile | No | No |
| Game archive | No | No |
| Achievements | No | No |
| Settings | No | No |

## Safety gates

Before claiming eight bot levels: implement distinct budgets/evaluation/search features, verify legal moves at every level, tactical regression suite, cancellation and per-level persistent result accounting. Labels such as 400 or 2200 are illustrative, not measured strength. Current shallow engine cannot substantiate these strengths; Stockfish is a candidate subject to GPLv3 distribution obligations and Android ABI integration.

Before claiming visual completion: capture actual home, local, Bluetooth and outcome screens on Android. Two-phone synchronization cannot be verified with one device or compile-only CI.
