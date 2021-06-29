# MinestomPvP

[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)
[![license](https://img.shields.io/github/license/Bloepiloepi/MinestomPvP.svg?style=flat-square)](LICENSE)

MinestomPvP is an extension for Minestom.
It tries to mimic vanilla PvP as good as possible.
Currently, almost all vanilla PvP features are supported except for a few.

## Table of Contents

- [Future Plans](#plans)
- [Usage](#usage)
- [Events](#events)
- [Contributing](#contributing)

## Plans

- **Applying 1.17 changes**
- Bow and arrows
- Totem of undying
- Splash potions
- Proper death messages

## Usage

To integrate this extension in your minestom server, you may have to tweak a little bit to make sure everything works correctly.
You can get an `EventNode` with all PvP related events listening using `PvpExtension.events()`.
You can add this node as a child to any other node and the pvp will work in the scope.

When applying damage to an entity, use `CustomDamageType` instead of `DamageType` (except if you the default ones: `GRAVITY`, `ON_FIRE` and `VOID`).
If you have your own damage type, also extend `CustomDamageType` instead of `DamageType`.

## Events

This extension provides several events:

- `DamageBlockEvent`: cancellable, called when an entity blocks damage using a shield.
- `FinalDamageEvent`: cancellable, called when the final damage calculation (including armor and effects) is completed. This event should be used instead of `EntityDamageEvent`, unless you want to detect how much damage was originally dealt.

## Contributing

You can contribute in multiple ways. 
If you have an issue or have a great idea, you can open an issue.
You may also open a new pull request if you have made something for this project and you think it will fit in well.
