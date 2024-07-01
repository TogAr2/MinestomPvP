package io.github.togar2.pvp.feature.state;

import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;

import java.util.Objects;

public class VanillaPlayerStateFeature implements PlayerStateFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaPlayerStateFeature> DEFINED = new DefinedFeature<>(
			FeatureType.PLAYER_STATE, configuration -> new VanillaPlayerStateFeature()
	);
	
	public static final Tag<Block> LAST_CLIMBED_BLOCK = Tag.Transient("lastClimbedBlock");
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerTickEvent.class, event -> {
			Player player = event.getPlayer();
			if (player.isOnGround()) player.removeTag(LAST_CLIMBED_BLOCK);
		});
		
		node.addListener(PlayerMoveEvent.class, event -> {
			Player player = event.getPlayer();
			if (isClimbing(player)) {
				player.setTag(LAST_CLIMBED_BLOCK, player.getInstance().getBlock(player.getPosition()));
			}
		});
	}
	
	@Override
	public boolean isClimbing(LivingEntity entity) {
		if (entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) return false;
		
		var tag = MinecraftServer.getTagManager().getTag(net.minestom.server.gamedata.tags.Tag.BasicType.BLOCKS, "minecraft:climbable");
		assert tag != null;
		
		Block block = Objects.requireNonNull(entity.getInstance()).getBlock(entity.getPosition());
		return tag.contains(block.namespace());
	}
	
	@Override
	public Block getLastClimbedBlock(LivingEntity entity) {
		return entity.hasTag(LAST_CLIMBED_BLOCK) ? entity.getTag(LAST_CLIMBED_BLOCK) : Block.AIR;
	}
}
