# Device acceptance checklist

Compilation is not device verification. Record device models and Android versions for each check.

## Local game
- Drag and tap moves, both board orientations.
- Illegal drop and drop outside board leave the position unchanged.
- Pawn promotion: all four choices, including narrow screens.
- Check, mate, stalemate, castling and en passant.
- Bot cancellation when leaving game or loading a position.
- Rotate device, background process and restore saved game.

## Bluetooth: two phones required
- Request permissions; deny and retry.
- Bluetooth disabled, then enabled.
- Pair via system settings; host on A, connect from B.
- Nicknames correct; host white, guest black.
- Moves accepted only from correct side; identical positions on both phones.
- Guest move rejected without corrupting state.
- Chat UTF-8, emojis, message length limit and rate limit.
- Resignation confirmation propagates result.
- Radio disabled / peer closes app: board locks, useful error is shown.
- Cancel accept/connect closes sockets.

## Known gaps
- ChessClock is implemented and unit-tested in source, not yet wired to a mode.
- Automatic reconnection and persistent multiplayer sessions are not implemented.
- Avatar transfer and common theme/profile rendering are incomplete.
- No claim of full release readiness until this checklist is executed.
