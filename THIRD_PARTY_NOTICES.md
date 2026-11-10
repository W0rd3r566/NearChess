# Chess figure assets

The build packages the 36 original SVG files from lichess-org/lila revision
87e973ad65489975caf55c440537ea317f40a242, public/piece/{cburnett,chessnut,spatial}.
No Unicode-derived substitutes or lossy conversion are used. AndroidSVG renders SVG paths as vector drawing commands.

- Cburnett: Colin M. L. Burnett, GPL version 2 or later.
- Chessnut: Alexis Luengas, Apache License 2.0. https://github.com/LexLuengas/chessnut-pieces
- Spatial: Maurizio Monge, MIT. https://github.com/maurimo/chess-art

Upstream attribution table: https://github.com/lichess-org/lila/blob/87e973ad65489975caf55c440537ea317f40a242/COPYING.md

The build downloads upstream notices into APK assets/licenses. SVGs are fetched at build time, never at runtime: installed games remain offline. Full vendoring of licenses and SVGs into the source repository is still pending; do not claim a fully offline build or release licensing completion.
AndroidSVG: Apache 2.0, https://github.com/BigBadaboom/androidsvg.
