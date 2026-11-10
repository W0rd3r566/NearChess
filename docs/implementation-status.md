# NearChess — implementation status

This document separates implemented code from planned integration so the README does not overclaim.

## Implemented

- Pure Kotlin chess rules and tests.
- Compose board with click and drag input.
- Local bot with shallow search.
- Local profile nickname and image picker.
- Local Fischer clock screen.
- RFCOMM transport, framed protocol, host-side move validation, chat and reactions.
- Material 3 entry screen and five board palettes.

## Integration still required

- Use `ChessClock` inside `NearbyActivity` and send clock snapshots in `STATE` frames.
- Persist a multiplayer session and resume it after process death.
- Make reconnect handshake compare session id, sequence, FEN hash and clocks.
- Send a small avatar identifier or approved compressed profile image with `HELLO`; never trust arbitrary received files.
- Add TalkBack semantics to every board square and announce legal moves/check.
- Add animation state around the board without delaying protocol messages.
- Add PGN parser, game archive and training puzzles.

## Protocol invariants

- Host is authoritative for legal move order.
- Every frame has a version, type, sequence and bounded UTF-8 payload.
- Duplicate or stale move sequences are rejected.
- A disconnect must not silently continue a timed game.
- Chat is never interpreted as a command.
