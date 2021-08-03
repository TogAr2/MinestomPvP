package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.utils.SoundManager;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class ProjectileListener {
	
	public static EventNode<EntityEvent> events() {
		EventNode<EntityEvent> node = EventNode.type("projectile-events", EventFilter.ENTITY);
		
		node.addListener(EventListener.builder(EntityAttackEvent.class).handler(event -> {
			if (!(event.getEntity() instanceof EntityHittableProjectile)) return;
			((EntityHittableProjectile) event.getEntity()).onHit(event.getTarget());
		}).ignoreCancelled(false).build());
		
		node.addListener(EventListener.builder(PlayerUseItemEvent.class).handler(event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			boolean snowball = stack.getMaterial() == Material.SNOWBALL;
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			SoundManager.sendToAround(player,
					snowball ? SoundEvent.SNOWBALL_THROW : SoundEvent.EGG_THROW,
					snowball ? Sound.Source.NEUTRAL : Sound.Source.PLAYER,
					0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));
			
			EntityHittableProjectile projectile = snowball ? new Snowball(player) : new ThrownEgg(player);
			projectile.setItem(stack);
			
			Position position = player.getPosition().clone().add(0D, player.getEyeHeight(), 0D);
			projectile.setInstance(Objects.requireNonNull(player.getInstance()), position);
			
			Vector direction = position.getDirection();
			position = position.clone().add(direction.getX(), direction.getY(), direction.getZ())
					.subtract(0, 0.2, 0); //????????
			
			projectile.shoot(position, 1.5, 1.0);
			
			Vector playerVel = player.getVelocity();
			projectile.setVelocity(projectile.getVelocity().add(playerVel.getX(),
					player.isOnGround() ? 0.0D : playerVel.getY(), playerVel.getZ()));
			
			if (!player.isCreative()) {
				player.setItemInHand(event.getHand(), stack.withAmount(stack.getAmount() - 1));
			}
		}).filter(event -> event.getItemStack().getMaterial() == Material.SNOWBALL
				|| event.getItemStack().getMaterial() == Material.EGG)
				.ignoreCancelled(false).build());
		
		return node;
	}
}
