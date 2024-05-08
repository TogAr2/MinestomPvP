package io.github.togar2.pvp.feature;

import io.github.togar2.pvp.feature.armor.ArmorFeatureImpl;
import io.github.togar2.pvp.feature.attack.AttackFeatureImpl;
import io.github.togar2.pvp.feature.attack.CriticalFeatureImpl;
import io.github.togar2.pvp.feature.attack.SweepingFeatureImpl;
import io.github.togar2.pvp.feature.block.BlockFeatureImpl;
import io.github.togar2.pvp.feature.cooldown.CooldownFeatureImpl;
import io.github.togar2.pvp.feature.damage.DamageFeatureImpl;
import io.github.togar2.pvp.feature.fall.FallFeatureImpl;
import io.github.togar2.pvp.feature.food.ExhaustionFeatureImpl;
import io.github.togar2.pvp.feature.food.FoodFeatureImpl;
import io.github.togar2.pvp.feature.food.RegenerationFeatureImpl;
import io.github.togar2.pvp.feature.knockback.KnockbackFeatureImpl;
import io.github.togar2.pvp.feature.provider.ProviderForEntity;
import io.github.togar2.pvp.feature.totem.TotemFeatureImpl;
import io.github.togar2.pvp.feature.tracking.DeathMessageFeatureImpl;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;

public class CombatFeatureSet implements RegistrableFeature {
	public static final CombatFeatureSet DEFAULT_2 = new CombatConfiguration()
			.add(AttackFeatureImpl.class)
			.add(DamageFeatureImpl.class)
			.add(FallFeatureImpl.class)
			.add(FoodFeatureImpl.class)
			.add(ExhaustionFeatureImpl.class)
			.add(RegenerationFeatureImpl.class)
			.add(DeathMessageFeatureImpl.class)
			.add(CooldownFeatureImpl.class)
			.add(ProviderForEntity.DIFFICULTY)
			.add(CriticalFeatureImpl.class)
			.add(SweepingFeatureImpl.class)
			.add(KnockbackFeatureImpl.class)
			.add(ArmorFeatureImpl.class)
			.add(TotemFeatureImpl.class)
			.add(BlockFeatureImpl.class)
			.add(CombatVersion.MODERN)
			.build();
	
//	public static final CombatFeatureSet DEFAULT = new CombatFeatureSet(
//			new AttackFeatureImpl(new CooldownFeatureImpl(), new ExhaustionFeatureImpl(ProviderForEntity.DIFFICULTY, false), new CriticalFeatureImpl(false), new SweepingFeatureImpl(new KnockbackFeatureImpl(false)), new KnockbackFeatureImpl(false), false),
//			new DamageFeatureImpl(ProviderForEntity.DIFFICULTY, new BlockFeatureImpl(false), new ArmorFeatureImpl(false), new TotemFeatureImpl(), new ExhaustionFeatureImpl(ProviderForEntity.DIFFICULTY, false), new KnockbackFeatureImpl(false), new DeathMessageFeatureImpl(), false),
//			new FallFeatureImpl(),
//			new FoodFeatureImpl(),
//			new ExhaustionFeatureImpl(ProviderForEntity.DIFFICULTY, false),
//			new RegenerationFeatureImpl(new ExhaustionFeatureImpl(ProviderForEntity.DIFFICULTY, false), ProviderForEntity.DIFFICULTY, false),
//			new DeathMessageFeatureImpl()
//	);
	
	private final CombatFeature[] features;
	
	public CombatFeatureSet(CombatFeature... features) {
		this.features = features;
	}
	
	@Override
	public void init(EventNode<Event> node) {
		for (CombatFeature feature : features) {
			if (!(feature instanceof RegistrableFeature registrable)) continue;
			EventNode<Event> currentNode = EventNode.all(feature.getClass().getTypeName());
			registrable.init(currentNode);
			node.addChild(currentNode);
		}
	}
}
