package io.github.togar2.pvp.feature.explosion;

import io.github.togar2.pvp.entity.explosion.CrystalEntity;
import io.github.togar2.pvp.entity.explosion.TntEntity;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.item.ItemDamageFeature;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.*;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class VanillaExplosionFeature implements ExplosionFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaExplosionFeature> DEFINED = new DefinedFeature<>(
			FeatureType.EXPLOSION, VanillaExplosionFeature::new,
			FeatureType.ITEM_DAMAGE, FeatureType.ENCHANTMENT
	);
	
	private final FeatureConfiguration configuration;
	
	private ItemDamageFeature itemDamageFeature;
	private VanillaExplosionSupplier explosionSupplier;
	
	public VanillaExplosionFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void initDependencies() {
		this.itemDamageFeature = configuration.get(FeatureType.ITEM_DAMAGE);
		this.explosionSupplier = new VanillaExplosionSupplier(this, configuration.get(FeatureType.ENCHANTMENT));
	}
	
	@Override
	public VanillaExplosionSupplier getExplosionSupplier() {
		return explosionSupplier;
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerUseItemOnBlockEvent.class, event -> {
			ItemStack stack = event.getItemStack();
			Instance instance = event.getInstance();
			Point position = event.getPosition();
			Player player = event.getPlayer();
			
			if (stack.material() != Material.FLINT_AND_STEEL && stack.material() != Material.FIRE_CHARGE) return;
			Block block = instance.getBlock(position);
			if (!block.compare(Block.TNT)) return;
			
			primeExplosive(instance, position, player, 80);
			instance.setBlock(position, Block.AIR);
			
			if (player.getGameMode() != GameMode.CREATIVE) {
				if (stack.material() == Material.FLINT_AND_STEEL) {
					itemDamageFeature.damageEquipment(player, event.getHand() == Player.Hand.MAIN
							? EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, 1);
				} else {
					player.setItemInHand(event.getHand(), stack.consume(1));
				}
			}
		});
		
		node.addListener(PlayerUseItemOnBlockEvent.class, event -> {
			if (event.getItemStack().material() != Material.END_CRYSTAL) return;
			Instance instance = event.getInstance();
			Block block = instance.getBlock(event.getPosition());
			if (!block.compare(Block.OBSIDIAN) && !block.compare(Block.BEDROCK)) return;
			
			Point above = event.getPosition().add(0, 1, 0);
			if (!instance.getBlock(above).isAir()) return;
			
			BoundingBox checkIntersect = new BoundingBox(1, 2, 1);
			for (Entity entity : instance.getNearbyEntities(above, 3)) {
				if (entity.getBoundingBox().intersectBox(above.sub(entity.getPosition()), checkIntersect)) return;
			}
			
			CrystalEntity entity = new CrystalEntity();
			entity.setInstance(instance, above.add(0.5, 0, 0.5));
			
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
				event.getPlayer().setItemInHand(event.getHand(), event.getItemStack().consume(1));
		});
		
		node.addListener(PlayerBlockInteractEvent.class, event -> {
			Instance instance = event.getInstance();
			Block block = instance.getBlock(event.getBlockPosition());
			Player player = event.getPlayer();
			if (!block.compare(Block.RESPAWN_ANCHOR)) return;
			
			// Exit if offhand has glowstone but current hand is main
			if (event.getHand() == Player.Hand.MAIN
					&& player.getItemInMainHand().material() != Material.GLOWSTONE
					&& player.getItemInOffHand().material() == Material.GLOWSTONE)
				return;
			
			ItemStack stack = player.getItemInHand(event.getHand());
			int charges = Integer.parseInt(block.getProperty("charges"));
			if (stack.material() == Material.GLOWSTONE && charges < 4) {
				instance.setBlock(event.getBlockPosition(),
						block.withProperty("charges", String.valueOf(charges + 1)));
				ViewUtil.packetGroup(player).playSound(Sound.sound(
						SoundEvent.BLOCK_RESPAWN_ANCHOR_CHARGE, Sound.Source.BLOCK,
						1.0f, 1.0f
				), event.getBlockPosition().add( 0.5, 0.5, 0.5));
				
				if (player.getGameMode() != GameMode.CREATIVE)
					player.setItemInHand(event.getHand(), player.getItemInHand(event.getHand()).consume(1));
				
				event.setBlockingItemUse(true);
				return;
			}
			
			if (charges == 0) return;
			
			if (instance.getExplosionSupplier() != null
					&& MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType()).respawnAnchorWorks()) {
				instance.setBlock(event.getBlockPosition(), Block.AIR);
				instance.explode(
						(float) (event.getBlockPosition().x() + 0.5),
						(float) (event.getBlockPosition().y() + 0.5),
						(float) (event.getBlockPosition().z() + 0.5),
						5.0f,
						CompoundBinaryTag.builder()
								.putBoolean("fire", true)
								.putBoolean("anchor", true)
								.build()
				);
			}
			
			event.setBlockingItemUse(true);
		});
	}
	
	@Override
	public void primeExplosive(Instance instance, Point blockPosition, @Nullable LivingEntity cause, int fuse) {
		TntEntity entity = new TntEntity(cause);
		entity.setFuse(fuse);
		entity.setInstance(instance, blockPosition.add(0.5, 0, 0.5));
		entity.getViewersAsAudience().playSound(Sound.sound(
				SoundEvent.ENTITY_TNT_PRIMED, Sound.Source.BLOCK,
				1.0f, 1.0f
		), entity);
	}
}
