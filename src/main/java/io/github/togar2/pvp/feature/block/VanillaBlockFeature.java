package io.github.togar2.pvp.feature.block;

import java.util.concurrent.ThreadLocalRandom;

import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.enums.Tool;
import io.github.togar2.pvp.events.DamageBlockEvent;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.cooldown.ItemCooldownFeature;
import io.github.togar2.pvp.feature.item.ItemDamageFeature;
import io.github.togar2.pvp.utils.CombatVersion;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.entity.metadata.projectile.AbstractArrowMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.BlocksAttacks;
import net.minestom.server.registry.*;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.item.component.BlocksAttacks.*;

/**
 * Vanilla implementation of {@link BlockFeature}
 */
public class VanillaBlockFeature implements BlockFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaBlockFeature> DEFINED = new DefinedFeature<>(
			FeatureType.BLOCK, VanillaBlockFeature::new,
			FeatureType.ITEM_DAMAGE, FeatureType.ITEM_COOLDOWN, FeatureType.VERSION
	);

	private final FeatureConfiguration configuration;

	private ItemDamageFeature itemDamageFeature;
	private ItemCooldownFeature itemCooldownFeature;
	private CombatVersion version;

	private static final Tag<@NotNull Long> STARTED_BLOCKING = Tag.Transient("started_blocking");
	private static final int DEFAULT_DISABLE_COOLDOWN_TICKS = 100;

	public VanillaBlockFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void initDependencies() {
		this.itemDamageFeature = configuration.get(FeatureType.ITEM_DAMAGE);
		this.itemCooldownFeature = configuration.get(FeatureType.ITEM_COOLDOWN);
		this.version = configuration.get(FeatureType.VERSION);
	}

	@Override
	public void init(EventNode<@NotNull EntityInstanceEvent> node) {
		EventNode<@NotNull PlayerBeginItemUseEvent> blockNode = EventNode.event(
				"block",
				EventFilter.from(PlayerBeginItemUseEvent.class, Player.class, PlayerBeginItemUseEvent::getPlayer),
				event -> event.getPlayer().getItemInHand(event.getHand()).get(DataComponents.BLOCKS_ATTACKS) != null
		);

		blockNode.addListener(PlayerBeginItemUseEvent.class, event ->
				event.getPlayer().setTag(STARTED_BLOCKING, System.currentTimeMillis()));

		node.addChild(blockNode);
	}
	
	protected boolean isPiercing(Damage damage) {
		Entity source = damage.getSource();
		if (source != null && source.getEntityMeta() instanceof AbstractArrowMeta) {
			return ((AbstractArrowMeta) source.getEntityMeta()).getPiercingLevel() > 0;
		}
		
		return false;
	}
	
	@Override
	public boolean isDamageBlocked(LivingEntity entity, Damage damage) {
		if (damage.getAmount() <= 0) return false;
		DamageTypeInfo info = DamageTypeInfo.of(damage.getType());

		if (!(entity.getEntityMeta() instanceof LivingEntityMeta meta) || !meta.isHandActive()) return false;

		ItemStack itemStack = entity.getItemInHand(meta.getActiveHand());
		if (damagePassesThroughBlocking(itemStack, damage)) return false;

		BlocksAttacks blocksAttacks = entity.getItemInHand(meta.getActiveHand()).get(DataComponents.BLOCKS_ATTACKS);
		if (blocksAttacks == null) return false;

		Long startedBlocking = entity.getTag(STARTED_BLOCKING);
		if (startedBlocking == null ||
				startedBlocking + (long) (blocksAttacks.blockDelaySeconds() * 1000) > System.currentTimeMillis())
			return false;

		// If damage doesn't bypass armor, no piercing, and blocking is active
		if (!info.bypassesArmor() && !isPiercing(damage) && !info.unblockable()) {
			if (version.legacy()) return true;

			double angle = 0;
			Entity attacker = damage.getAttacker();
			if (attacker != null) angle = resolveAngle(attacker, entity);

			return resolveBlockedDamage(blocksAttacks, damage, angle) > 0;
		}

		return false;
	}

	protected float resolveBlockedDamage(BlocksAttacks blocksAttacks, Damage damage, double angle) {
		float blockedDamage = 0;

		for (DamageReduction reduction : blocksAttacks.damageReductions()) {
			blockedDamage += resolveDamageReduction(reduction, damage, angle);
		}

		return Math.clamp(blockedDamage, 0.0f, damage.getAmount());
	}
	
	@Override
	public boolean applyBlock(LivingEntity entity, Damage damage) {
		float amount = damage.getAmount();

		LivingEntityMeta meta = (LivingEntityMeta) entity.getEntityMeta();
		ItemStack itemStack = entity.getItemInHand(meta.getActiveHand());
		BlocksAttacks blocksAttacks = itemStack.get(DataComponents.BLOCKS_ATTACKS);

		float blockedDamage = 0;
		double angle = 0;
		Entity attacker = damage.getAttacker();

		if (attacker != null) angle = resolveAngle(attacker, entity);
		if (blocksAttacks != null) blockedDamage = resolveBlockedDamage(blocksAttacks, damage, angle);

		float resultingDamage = version.legacy() ? Math.max(0, (amount + 1) * 0.5f) : Math.clamp(amount - blockedDamage, 0, amount);
		
		DamageBlockEvent damageBlockEvent = new DamageBlockEvent(entity, amount, resultingDamage, false);
		EventDispatcher.call(damageBlockEvent);
		if (damageBlockEvent.isCancelled()) return false;
		damage.setAmount(damageBlockEvent.getResultingDamage());

		float threshold = 1;
		float itemDamageBase = 0;
		float itemDamageFactor = 1;

		if (blocksAttacks != null) {
			ItemDamageFunction itemDamage = blocksAttacks.itemDamage();
			threshold = itemDamage.threshold();
			itemDamageBase = itemDamage.base();
			itemDamageFactor = itemDamage.factor();
		}

		if (amount >= threshold) {
			int shieldDamage = (int) Math.floor(itemDamageBase + itemDamageFactor * amount);
			PlayerHand hand = meta.getActiveHand();
			itemDamageFeature.damageEquipment(
					entity,
					hand == PlayerHand.MAIN ?
							EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND,
					shieldDamage
			);
			
			if (entity.getItemInHand(meta.getActiveHand()).isAir()) {
				((LivingEntityMeta) entity.getEntityMeta()).setHandActive(false);

				if (blocksAttacks != null && blocksAttacks.disableSound() != null) {
					entity.getViewersAsAudience().playSound(Sound.sound(
							blocksAttacks.disableSound(), Sound.Source.PLAYER, 1.0f,
							0.8f + ThreadLocalRandom.current().nextFloat(0.4f)
					));
				}
			}
		}
		
		// Take blocking hit (knockback and disabling)
		DamageTypeInfo info = DamageTypeInfo.of(damage.getType());
		if (!info.projectile() && damage.getAttacker() instanceof LivingEntity)
			takeBlockingHit(entity, (LivingEntity) damage.getAttacker(), damageBlockEvent.knockbackAttacker());
		
		return resultingDamage == 0;
	}

	protected float resolveDamageReduction(@NotNull DamageReduction reduction, Damage damage, double angle) {
		if (angle > (Math.PI / 180.0) * reduction.horizontalBlockingAngle())
			return 0.0f;
		else {
			float amount = damage.getAmount();
			return reduction.type() != null && !reduction.type().contains(damage.getType())
					? 0.0f
					: Math.clamp(reduction.base() + reduction.factor() * amount, 0.0f, amount);
		}
	}

	protected double resolveAngle(Entity source, Entity entity) {
		Vec entityPos = entity.getPosition().asVec();
		Vec sourcePos = source.getPosition().asVec();

		Vec lookDir = entity.getPosition().direction().withY(0).normalize();
		Vec toSourceDir = sourcePos.sub(entityPos).withY(0).normalize();

		double dot = Math.max(-1.0, Math.min(1.0, lookDir.dot(toSourceDir)));
		return Math.acos(dot);
	}
	
	protected void takeBlockingHit(LivingEntity entity, LivingEntity attacker, boolean applyKnockback) {
		if (applyKnockback) {
			Pos entityPos = entity.getPosition();
			Pos attackerPos = attacker.getPosition();
			attacker.takeKnockback(0.5F,
					attackerPos.x() - entityPos.x(),
					attackerPos.z() - entityPos.z()
			);
		}

		if (!(entity instanceof Player player)) return;

		LivingEntityMeta meta = (LivingEntityMeta) player.getEntityMeta();
		ItemStack itemStack = player.getItemInHand(meta.getActiveHand());
		BlocksAttacks blocksAttacks = itemStack.get(DataComponents.BLOCKS_ATTACKS);
		Tool tool = Tool.fromMaterial(attacker.getItemInMainHand().material());

		if (blocksAttacks != null && blocksAttacks.blockSound() != null) {
			Pos pos = player.getPosition();
			ViewUtil.viewersAndSelf(player).playSound(
					Sound.sound(blocksAttacks.blockSound(), Sound.Source.PLAYER, 1.0f,
							0.8f + ThreadLocalRandom.current().nextFloat(0.4f)),
					pos.x(), pos.y(), pos.z());
		}

		if (tool != null && tool.isAxe()) {
			disableBlocking(player, itemStack);
		}
	}
	
	protected void disableBlocking(@NotNull Player player, @NotNull ItemStack itemStack) {
		BlocksAttacks blocksAttacks = itemStack.get(DataComponents.BLOCKS_ATTACKS);
		if (blocksAttacks == null) return;

		int ticks = (int) (DEFAULT_DISABLE_COOLDOWN_TICKS * blocksAttacks.disableCooldownScale());
		itemCooldownFeature.setCooldown(player, itemStack, ticks);

		// Shield disable status
		player.triggerStatus((byte) 30);
		player.triggerStatus((byte) 29);

		if (blocksAttacks.disableSound() != null) {
			Pos pos = player.getPosition();
			ViewUtil.viewersAndSelf(player).playSound(
					Sound.sound(blocksAttacks.disableSound(), Sound.Source.PLAYER, 0.8f,
							0.8f + ThreadLocalRandom.current().nextFloat(0.4f)),
					pos.x(), pos.y(), pos.z());
		}

		PlayerHand hand = player.getPlayerMeta().getActiveHand();
		player.refreshActiveHand(false, hand == PlayerHand.OFF, false);
	}

	protected boolean damagePassesThroughBlocking(@NotNull ItemStack itemStack, @NotNull Damage damage) {
		BlocksAttacks blocksAttacks = itemStack.get(DataComponents.BLOCKS_ATTACKS);
		if (blocksAttacks == null) return false;

		@Nullable RegistryTag<@NotNull DamageType> bypassedBy = blocksAttacks.bypassedBy();
		if (bypassedBy != null) return bypassedBy.contains(damage.getType());
        else return false;
	}
}
