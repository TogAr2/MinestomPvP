# MinestomPvP

[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)
[![license](https://img.shields.io/github/license/Bloepiloepi/MinestomPvP.svg?style=flat-square)](LICENSE)

MinestomPvP is an extension for Minestom.
It tries to mimic vanilla PvP as good as possible.

## Table of Contents

- [Features](#features)
- [Future Plans](#plans)
- [Usage](#usage)
- [Integration](#integration)
- [Events](#events)
- [Customization](#customization)
- [Contributing](#contributing)

## Features

Currently, most vanilla PvP features are supported.

- Attack cooldown
- Damage invulnerability
- Weapons
- Armor
- Food
- Potions
- Totems
- All enchantments possible with the above features (this includes protection, sharpness, knockback, ...)

## Plans

- Bow and arrows
- Lingering potions
- Fall damage
- Proper death messages

## Usage

You can get an `EventNode` with all PvP related events listening using `PvpExtension.events()`.
You can add this node as a child to any other node and the pvp will work in the scope.
Separated features of this extension are also available as static methods in `PvpExtension`.

### Integration

To integrate this extension into your minestom server, you may have to tweak a little bit to make sure everything works correctly.

When applying damage to an entity, use `CustomDamageType` instead of `DamageType` (except if you use the default ones: `GRAVITY`, `ON_FIRE` and `VOID`).
If you have your own damage type, also extend `CustomDamageType` instead of `DamageType`.

Potions are considered food: The Minestom food events are also called for drinkable potions.

### Events

This extension provides several events:

- `DamageBlockEvent`: cancellable, called when an entity blocks damage using a shield.
- `FinalDamageEvent`: cancellable, called when the final damage calculation (including armor and effects) is completed. This event should be used instead of `EntityDamageEvent`, unless you want to detect how much damage was originally dealt.
- `TotemUseEvent`: cancellable, called when a totem prevents an entity from dying.

### Customization

It is possible to add your own features to this extension. For example, you can extend the current enchantment behavior by registering an enchantment using `CustomEnchantments`. This will provide you with a few methods for when the enchantment is used. It is also possible to do the same for potion effects using `CustomPotionEffects`, which will provide you with a few methods for when the effect is applied and removed.

For further customization, it is always possible to use events or, if really necessary, a mixin.

## Contributing

You can contribute in multiple ways. 
If you have an issue or have a great idea, you can open an issue.
You may also open a new pull request if you have made something for this project and you think it will fit in well.
