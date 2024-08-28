# MinestomPvP

[![license](https://img.shields.io/github/license/TogAr2/MinestomPvP.svg?style=for-the-badge)](LICENSE)
[![platform](https://img.shields.io/badge/platform-Minestom-ff69b4?style=for-the-badge)](https://github.com/Minestom/Minestom)

MinestomPvP is an extension for Minestom.
It tries to mimic vanilla (modern **and** pre-1.9) PvP as good as possible, while also focusing on customizability and usability.

But, MinestomPvP does not only provide PvP, it also provides everything around it (e.g., status effects and food).
You can easily pick which features you want to use.

The maven repository is available on [jitpack](https://jitpack.io/#TogAr2/MinestomPvP).

> MinestomPvP has been rewritten, see [Important changes](#important-changes)

## Table of Contents

- [Important changes](#important-changes)
- [Features](#features)
- [Future Plans](#plans)
- [Usage](#usage)
- [Customization](#customization)
- [Legacy PvP](#legacy-pvp)
- [Integration](#integration)
- [Registries](#registries)
- [Events](#events)
- [Custom combat features](#custom-combat-features)
- [Contributing](#contributing)
- [Credits](#credits)

## Important changes

MinestomPvP has recently been rewritten. Most features are now independent of each other and can be used separately.

Major changes include:
- `PvpExtension` is now `MinestomPvP`
- Configs have been removed in favor of a feature based configuration system
- Registries are no longer prefixed by `Custom` but by `Combat`
- `PvpPlayer` is now `CombatPlayer`, `CustomPlayer` is now `CombatPlayerImpl`

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
- Support for (some) water mechanics (e.g. slowing projectiles down)
- 1.21 features (the library is already 1.21 compatible, just doesn't support its combat features)
- Rework of the tool & armor registry to allow for customization

## Usage

Before doing anything else, you should call `MinestomPvP.init()`. This will make sure everything is registered correctly.

After you've initialized the library, you can start using combat features.
For the most basic setup you can use the following:
```java
MinestomPvP.init();

CombatFeatureSet modernVanilla = CombatFeatures.modernVanilla();
MinecraftServer.getGlobalEventHandler().addChild(modernVanilla.createNode());
```

This will give you a full vanilla experience without any customization.

Every combat feature has a `createNode()` method, which returns an `EventNode` with all listeners of the feature attached.
This event node can be added to another event node to enable the feature within that scope.
In the example above, it is being added to the global event handler, which means the feature will work everywhere.

The combat feature used in this example is a `CombatFeatureSet`.
This is essentially a container for a list of combat features.
There are two feature sets already defined by MinestomPvP:
- Full modern combat, `CombatFeatures.modernVanilla()`
- Full legacy (pre-1.9) combat, `CombatFeatures.legacyVanilla()`

### Customization

The `CombatFeatures` class contains a field for every individual combat feature which has been defined by MinestomPvP itself.
For example, you can add fall damage to your instance like so:

```java
Instance instance;

CombatFeatureSet featureSet = CombatFeatures.empty()
        .version(CombatVersion.MODERN)
        .add(CombatFeatures.VANILLA_FALL)
        .add(CombatFeatures.VANILLA_PLAYER_STATE)
        .build();
instance.eventNode().addChild(featureSet.createNode());
```

As you can see, `CombatFeatures.empty()` provides you with a builder-like structure (`CombatConfiguration`) to which features can be added.

This combat configuration also contains convenience methods:
- `version(CombatVersion)` to set a combat version, which is used by some vanilla features to adjust values which are different across versions
- `difficulty(DifficultyProvider)` to set a difficulty provider, which is used by some vanilla features containing behavior which is different depending on the difficulty

In the example above, a `PLAYER_STATE` feature is added alongside the `FALL` feature, because the fall feature depends on it.
`CombatConfiguration` takes care of handling these dependencies for you. The order in which the features are added does not matter.
It is also possible to leave out the `PLAYER_STATE` feature: a `NO_OP` feature will then be used, which in this case will always signal to the fall feature that the player is not climbing.

Upon calling `CombatConfiguration#build()`, the combat configuration resolves all these dependencies and creates a `CombatFeatureSet` in which all the features are instantiated.

> Features defined inside the `CombatFeatures` class are not yet instantiated, but are a `DefinedFeature`.
> The `CombatConfiguration` will instantiate the features for you, which will turn them into `CombatFeature` instances.
> An instantiated feature always knows its dependencies.

### Legacy PvP

Earlier minecraft versions (pre-1.9) used a different PvP system, which to this day is still preferred by some. *Legacy* is the term used to describe this type of PvP throughout the library.
You can get the `CombatFeatureSet` for legacy PvP using `CombatFeatures.legacyVanilla()`.

To disable attack cooldown for a player, use `MinestomPvP.setLegacyAttack(player, true)`.
To enable the cooldown again, use `false` instead of `true`.

#### Knockback

A lot of servers like to customize their 1.8 knockback. It is also possible to do so with this extension. In `EntityKnockbackEvent`, you can set a `LegacyKnockbackSettings` object. It contains information about how the knockback is calculated. A builder is obtainable by using `LegacyKnockbackSettings.builder()`. For more information, check the [config of BukkitOldCombatMechanics](https://github.com/kernitus/BukkitOldCombatMechanics/blob/d222286fd84fe983fdbdff79699182837871ab9b/src/main/resources/config.yml#L279).

### Integration

To integrate this extension into your minestom server, you may have to tweak a little bit to make sure everything works correctly.

The extension uses a custom player implementation, if you use one, it is recommended to extend `CombatPlayerImpl`. If you for some reason can't, make sure to implement `CombatPlayer` in a similar fashion to `CombatPlayerImpl`.
The implementation of MinestomPvP is registered inside `MinestomPvP.init()`, so register yours after initializing the library.

To allow explosions, you have to register an explosion supplier to every instance in which they are used.
Implementations of `ExplosionFeature` might provide an explosion supplier.

```java
CombatFeatureSet featureSet;
Instance instance;

instance.setExplosionSupplier(featureSet.get(FeatureType.EXPLOSION).getExplosionSupplier());
```

Keep in mind that the explosion supplier can be different depending on the explosion feature,
so always register the one from the explosion feature which is active in the instance.

### Registries

MinestomPvP has several registries, which you can also register to in order to create custom behavior:
- `CombatEnchantments`: a registry of enchantment behaviors, used by `EnchantmentFeature`
- `CombatPotionEffects`: a registry of potion effect behaviors, used by `EffectFeature`
- `CombatPotionTypes`: a registry of potion types and which effects they contain, used by `EffectFeature`

You can use the static `#register(...)` method in those classes to add custom entries.

You can also use the class `Tool`, which contains all tools and their properties (not all properties are currently included, will change soon).
The same applies to `ToolMaterial` (wood, stone, ...) and `ArmorMaterial`.

### Events

The library provides several events:

- `AnchorChargeEvent`: cancellable, called when a player charges a respawn anchor.
- `AnchorExplodeEvent`: cancellable, called when a player clicks on a respawn anchor to explode it.
- `CrystalPlaceEvent`: cancellable, called when a player places an end crystal.
- `DamageBlockEvent`: cancellable, called when an entity blocks damage using a shield. This event can be used to set the remaining damage.
- `EntityKnockbackEvent`: cancellable, called when an entity gets knocked back by another entity. Gets called twice for weapons with the knockback enchantment (once for default damage knockback, once for the extra knockback). This event can be used to set the knockback strength.
- `EntityPreDeathEvent`: cancellable, a form of `EntityDeathEvent` but cancellable and with a damage type. Can be used to cancel the death while still applying after-damage effects, such as attack sounds.
- `EquipmentDamageEvent`: cancellable, called when an item in an equipment slot gets damaged.
- `ExplosionEvent`: cancellable, called when an explosion will take place. Can be used to modify the affected blocks.
- `ExplosivePrimeEvent`: cancellable, called when a tnt gets ignited, either by a player or by a nearby explosion.
- `FinalAttackEvent`: cancellable, called when a player attacks an entity. Can be used to set a few variables like sprint, critical, sweeping, etc.
- `FinalDamageEvent`: cancellable, called when the final damage calculation (including armor and effects) is completed. This event should be used instead of `EntityDamageEvent`, unless you want to detect how much damage was originally dealt.
- `FishingBobberRetrieveEvent`: cancellable, called when a player retrieves a fishing bobber.
- `LegacyKnockbackEvent`: cancellable, called when an entity gets knocked back by another entity using legacy pvp. Same applies as for `EntityKnockbackEvent`. This event can be used to change the knockback settings.
- `PrepareAttackEvent`: cancellable, called before calculations for a given melee attack are done. Can be used to cancel the attack from happening in known situations where attacks shouldn't occur - ie; in a lobby/waiting phase.
- `PickupEntityEvent`: cancellable, called when a player picks up an entity (arrow or trident).
- `PlayerExhaustEvent`: cancellable, called when a players' exhaustion level changes.
- `PlayerRegenerateEvent`: cancellable, called when a player naturally regenerates health.
- `PlayerSpectateEvent`: cancellable, called when a spectator tries to spectate an entity by attacking it.
- `PotionVisibilityEvent`: cancellable, called when an entities potion state (ambient, particle color and invisibility) is updated.
- `TotemUseEvent`: cancellable, called when a totem prevents an entity from dying.

### Custom combat features

It is possible to create your own combat features, which can extend an existing one or be completely independent.
Below is an explanation followed by an example.

In order to be compatible with the library, your combat features must implement `CombatFeature`.
It is also possible to implement `RegistrableFeature` instead, which will provide you with a `createNode()` method.
In this case, you must also implement `RegistrableFeature#init(EventNode)`, which attaches all the listeners to the given event node.

After this, you must create a `FeatureType` for your custom feature.
If you are implementing an existing feature, use existing feature types in the `FeatureType` class.
Otherwise, you can create your own using `FeatureType.of(String, F)`.
The first argument will be the name, the second the `NO_OP` feature which will be used when no implementation is present.
It is recommended to create an interface for your custom feature type which extends `CombatFeature` (or `RegistrableFeature`).
This way, you can easily specify methods to expose to other features.

Lastly, it is needed to create a `DefinedFeature` instance for your custom implementation.
This defined feature defines an implementation of your feature type, and it can be used to add your implementation to a combat configuration.

Example of a custom feature type, with 1 method which can be used by other features:

```java
interface MyCustomFeature extends CombatFeature {
	MyCustomFeature NO_OP = new MyCustomFeature() {};
	
	FeatureType TYPE = FeatureType.of("MY_CUSTOM", NO_OP);
	
	boolean isItWorking();
}
```

Example of an implementation of this custom feature type, which listens for events and implements the method:

```java
class MyCustomFeatureImpl implements MyCustomFeature, RegistrableFeature {
    public static final DefinedFeature<MyCustomFeatureImpl> DEFINED = new DefinedFeature<>(
            MyCustomFeature.TYPE, configuration -> new MyCustomFeatureImpl()
    );

    @Override
    public void init(EventNode<PlayerInstanceEvent> node) {
        node.addListener(PlayerChatEvent.class, event -> {
            // Do something...
        });
    }

    @Override
    public boolean isItWorking() {
        return true;
    }
}
```

Now you can use your own feature:

```java
MinecraftServer.getGlobalEventHandler().addChild(
        CombatFeatures.single(MyCustomFeatureImpl.DEFINED)
);
```

As you can see, it is also possible to use `CombatFeatures.single(DefinedFeature)` to instantiate a single feature without dependencies.

#### Depending on other features

Say, you want to access a players fall distance in your own feature. You can do this by depending on `FallFeature`.

```java
class MyCustomFeatureImpl implements MyCustomFeature {
    public static final DefinedFeature<MyCustomFeatureImpl> DEFINED = new DefinedFeature<>(
            MyCustomFeature.TYPE, configuration -> new MyCustomFeatureImpl(configuration),
            FeatureType.FALL
    );

    private final FeatureConfiguration configuration;
    private FallFeature fallFeature;

    public MyCustomFeatureImpl(FeatureConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void initDependencies() {
        this.fallFeature = configuration.get(FeatureType.FALL);
    }

    @Override
    public boolean isItWorking() {
        Player player; // Some player
        return fallFeature.getFallDistance(player) > 3;
    }
}
```

Note that the `FeatureConfiguration` which can be used to get the `FallFeature` from is only ready when `initDependencies()` is called.
This is due to complications with recursive dependencies between features.

#### Player init

You might want to initialize certain things for a player upon joining or whenever they get reset (respawn).
This is possible by using the player init of a defined feature. Its constructor can also take a `DefinedFeature.PlayerInit`.
This is a class whose `init(Player player, boolean firstInit)` method will be called upon a player join or reset.

You can for example use this player init to set tags on a player. The vanilla implementation of `FallFeature` uses it to set the fall distance tag on the player to 0.

There are two criteria to use the player init:
- The logic does not depend on other features and as such can be defined once for every feature implementation, and not for every instance of this implementation.
- The logic is required to be ran for the feature to work. If this is done inside the feature itself it might be out of scope (e.g. player join event is not called on an instance, so the feature might miss it).

## Contributing

You can contribute in multiple ways.
If you have an issue or a great idea, you can open an issue.
You may also open a new pull request if you have made something for this project and you think it will fit in well.

If anything does not integrate with your project, you can also open an issue (or submit a pull request).
I aim towards making this extension as usable as possible!

## Credits

Thanks to [kiip1](https://github.com/kiip1) for testing and finding bugs.

I used [BukkitOldCombatMechanics](https://github.com/kernitus/BukkitOldCombatMechanics) as a resource for recreating legacy pvp.
