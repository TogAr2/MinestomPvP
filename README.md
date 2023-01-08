# MinestomPvP

[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)
[![license](https://img.shields.io/github/license/TogAr2/MinestomPvP.svg?style=flat-square)](LICENSE)
[![platform](https://img.shields.io/badge/platform-Minestom-ff69b4?style=flat-square)](https://github.com/Minestom/Minestom)
[![](https://jitpack.io/v/TogAr2/MinestomPvP.svg?style=flat-square)](https://jitpack.io/#TogAr2/MinestomPvP)

MinestomPvP is an extension for Minestom.
It tries to mimic vanilla (and pre-1.9) PvP as good as possible, while also focusing on customizability and usability.

But, MinestomPvP does not only provide PvP, it also provides everything around it (e.g., status effects and food).
You can easily choose which features you want to use.

The maven repository is available on [jitpack](https://jitpack.io/#TogAr2/MinestomPvP).

**You might not want to put this extension in your extensions folder, more information at [usage](#usage).**

## Table of Contents

- [Features](#features)
- [Future Plans](#plans)
- [Usage](#usage)
- [Integration](#integration)
- [Events](#events)
- [Customization](#customization)
- [Contributing](#contributing)
- [Credits](#credits)

## Features

Currently, most vanilla PvP features are supported.

- Attack cooldown
- Damage invulnerability
- Weapons
- Armor
- Shields (or sword blocking)
- Food
- Totems
- Bows and crossbows
- Tridents (with riptide or loyalty)
- Fishing rods (only hooking entities or legacy knockback, not fishing)
- Other projectiles (potions, snowballs, eggs, ender pearls)
- All enchantments possible with the above features (this includes protection, sharpness, knockback, ...)
- Fall damage
- End crystals
- TNT
- Respawn anchors (explosion only)

## Plans

- Lingering potions
- Fireworks (for crossbows)
- Projectile collision might need some improvements

## Usage

One way to use this extension is by simply putting the jar inside your servers extensions folder.
This will apply PvP mechanics to your whole server.

But you can also choose to (and this is the preferred option for most servers) use the jar file as a library.
In this case, you can choose where to apply the PvP mechanics and customize them.
The rest of this readme assumes you are using this method.

Before doing anything else, you should call `PvpExtension.init()`. This will make sure everything is registered correctly.
After you've initialized the extension, you can get an `EventNode` with all PvP related events listening using `PvpExtension.events()`.
By adding this node as a child to any other node, you enable pvp in that scope.

Example (adds PvP to the global event handler, so everywhere):
```java
PvpExtension.init();
MinecraftServer.getGlobalEventHandler().addChild(PvpExtension.events());
```

You can customize which features of this extension you want to enable or disable by using `PvPConfig`.
Obtain a builder by using one of the static methods of `PvPConfig`: `#defaultBuilder()` (returns a builder with the default options), `#legacyBuilder()` (returns a builder with the legacy options) or `#emptyBuilder()` (has everything disabled by default). You can add custom settings to it by using the methods of the builder. To create an `EventNode` from your config builder, use `#build().createNode()`.

Example:
```java
eventHandler.addChild(
    PvPConfig.emptyBuilder()
        .potion(PotionConfig.legacyBuilder().drinking(false))
        .build().createNode()
);
```
Which would result in potion effects and splash potions still working, but not drinkable potions.
Everything else not to do with potions would be disabled as well, since you are using `PvPConfig.emptyBuilder()`.

### Legacy PvP

You can get the `EventNode` for legacy PvP using `PvpExtension.legacyEvents()`, and adjust its settings by using the method described above.

To disable attack cooldown for a player and set their attack damage to the legacy value, use `PvpExtension.setLegacyAttack(player, true)`.
To enable the cooldown again and set the attack damage to the new value, use `false` instead of `true`.

#### Knockback

A lot of servers like to customize their 1.8 knockback. It is also possible to do so with this extension. In `EntityKnockbackEvent`, you can set a `LegacyKnockbackSettings` object. It contains information about how the knockback is calculated. A builder is obtainable by using `LegacyKnockbackSettings.builder()`. For more information, check the [config of BukkitOldCombatMechanics](https://github.com/kernitus/BukkitOldCombatMechanics/blob/d222286fd84fe983fdbdff79699182837871ab9b/src/main/resources/config.yml#L279).

### Integration

To integrate this extension into your minestom server, you may have to tweak a little bit to make sure everything works correctly.

When applying damage to an entity, use `CustomDamageType` instead of `DamageType`.
If you have your own damage type, also extend `CustomDamageType` instead of `DamageType`.

Potions and milk buckets are considered food: the Minestom food events are also called for drinkable items.

The extension uses a custom player implementation, if you use one, it is recommended to extend `CustomPlayer`. If you for some reason can't, make sure to implement `PvpPlayer`. The implementation is registered inside `PvpExtension.init()`, so register yours after the call.

To allow explosions, you have to register `PvpExplosionSupplier` to every instance in which they are used.
```
instance.setExplosionSupplier(PvpExplosionSupplier.INSTANCE);
```

### Events

This extension provides several events:

- `DamageBlockEvent`: cancellable, called when an entity blocks damage using a shield. This event can be used to set the remaining damage.
- `EntityKnockbackEvent`: cancellable, called when an entity gets knocked back by another entity. Gets called twice for weapons with the knockback enchantment (once for default damage knockback, once for the extra knockback). This event can be used to set the knockback strength.
- `EntityPreDeathEvent`: cancellable, a form of `EntityDeathEvent` but cancellable and with a damage type. Can be used to cancel the death while still applying after-damage effects, such as attack sounds.
- `EquipmentDamageEvent`: cancellable, called when an item in an equipment slot gets damaged.
- `ExplosionEvent`: cancellable, called when an explosion will take place. Can be used to modify the affected blocks.
- `FinalAttackEvent`: cancellable, called when a player attacks an entity. Can be used to set a few variables like sprint, critical, sweeping, etc.
- `FinalDamageEvent`: cancellable, called when the final damage calculation (including armor and effects) is completed. This event should be used instead of `EntityDamageEvent`, unless you want to detect how much damage was originally dealt.
- `LegacyKnockbackEvent`: cancellable, called when an entity gets knocked back by another entity using legacy pvp. Same applies as for `EntityKnockbackEvent`. This event can be used to change the knockback settings.
- `PickupEntityEvent`: cancellable, called when a player picks up an entity (arrow or trident).
- `PlayerExhaustEvent`: cancellable, called when a players' exhaustion level changes.
- `PlayerRegenerateEvent`: cancellable, called when a player naturally regenerates health.
- `PlayerSpectateEvent`: cancellable, called when a spectator tries to spectate an entity by attacking it.
- `PotionVisibilityEvent`: cancellable, called when an entities potion state (ambient, particle color and invisibility) is updated.
- `ProjectileBlockHitEvent`: called when a projectile hits a block.
- `ProjectileEntityHitEvent`: cancellable, called when a projectile hits an entity.
- `TotemUseEvent`: cancellable, called when a totem prevents an entity from dying.

### Customization

It is possible to add your own features to this extension. For example, you can extend the current enchantment behavior by registering an enchantment using `CustomEnchantments`. This will provide you with a few methods for when the enchantment is used. It is also possible to do the same for potion effects using `CustomPotionEffects`, which will provide you with a few methods for when the effect is applied and removed.

You can use the class `Tool`, which contains all tools and their properties (not all properties are currently included, will change soon).
The same applies for `ToolMaterial` (wood, stone, ...) and `ArmorMaterial`.

## Contributing

You can contribute in multiple ways.
If you have an issue or a great idea, you can open an issue.
You may also open a new pull request if you have made something for this project and you think it will fit in well.

If anything does not integrate with your project, you can also open an issue (or submit a pull request).
I aim towards making this extension as usable as possible!

## Credits

Thanks to [kiip1](https://github.com/kiip1) for testing and finding bugs.

I used [BukkitOldCombatMechanics](https://github.com/kernitus/BukkitOldCombatMechanics) as a resource for recreating legacy pvp.
