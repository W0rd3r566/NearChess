# Material 3 audit notes

The entry screen now uses semantic Material 3 roles, `Scaffold`, `NavigationBar`, `ListItem`, `OutlinedCard`, `FilledTonalButton`, adaptive max width at 600dp, 8dp spacing tokens, Material icons, a local transition, and insets through Scaffold padding. Raw one-off button rows and emoji pseudo-icons were removed.

The interface still needs a full-project audit: existing game/profile/Bluetooth screens use some raw colors and have not yet been migrated to `NearTheme`; the board needs TalkBack semantics and adaptive testing. See the official guide at https://m3.material.io/ and the supplied reference https://github.com/hamen/material-3-skill.
