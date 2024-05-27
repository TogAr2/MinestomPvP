package io.github.togar2.pvp.feature.food;

import io.github.togar2.pvp.events.PlayerRegenerateEvent;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.provider.DifficultyProvider;
import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.Difficulty;

public class VanillaRegenerationFeature implements RegenerationFeature, RegistrableFeature {
	public static final Tag<Integer> STARVATION_TICKS = Tag.Integer("starvationTicks");
	
	private final ExhaustionFeature exhaustionFeature;
	private final DifficultyProvider difficultyFeature;
	private final CombatVersion version;
	
	public VanillaRegenerationFeature(ExhaustionFeature exhaustionFeature,
	                                  DifficultyProvider difficultyFeature,
	                                  CombatVersion version) {
		this.exhaustionFeature = exhaustionFeature;
		this.difficultyFeature = difficultyFeature;
		this.version = version;
	}
	
	@Override
	public void initPlayer(Player player, boolean firstInit) {
		player.setTag(STARVATION_TICKS, 0);
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerTickEvent.class, event -> onTick(event.getPlayer()));
	}
	
	protected void onTick(Player player) {
		if (!player.getGameMode().canTakeDamage()) return;
		Difficulty difficulty = difficultyFeature.getValue(player);
		
		int food = player.getFood();
		float health = player.getHealth();
		int starvationTicks = player.getTag(STARVATION_TICKS);
		
		if (version.modern() && player.getFoodSaturation() > 0 && health > 0
				&& health < player.getMaxHealth() && food >= 20) {
			starvationTicks++;
			if (starvationTicks >= 10) {
				float amount = Math.min(player.getFoodSaturation(), 6);
				regenerate(player, amount / 6, amount);
				starvationTicks = 0;
			}
		} else if (food >= 18 && health > 0
				&& health < player.getMaxHealth()) {
			starvationTicks++;
			if (starvationTicks >= 80) {
				regenerate(player, 1, version.legacy() ? 3 : 6);
				starvationTicks = 0;
			}
		} else if (food <= 0) {
			starvationTicks++;
			if (starvationTicks >= 80) {
				if (health > 10 || difficulty == Difficulty.HARD
						|| ((health > 1) && (difficulty == Difficulty.NORMAL))) {
					player.damage(DamageType.STARVE, 1);
				}
				
				starvationTicks = 0;
			}
		} else {
			starvationTicks = 0;
		}
		
		player.setTag(STARVATION_TICKS, starvationTicks);
	}
	
	@Override
	public void regenerate(Player player, float health, float exhaustion) {
		PlayerRegenerateEvent event = new PlayerRegenerateEvent(player, health, exhaustion);
		EventDispatcher.callCancellable(event, () -> {
			player.setHealth(player.getHealth() + event.getAmount());
			exhaustionFeature.addExhaustion(player, event.getExhaustion());
		});
	}
}
