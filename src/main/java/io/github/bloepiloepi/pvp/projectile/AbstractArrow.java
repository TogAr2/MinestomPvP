package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.events.PickupEntityEvent;
import io.github.bloepiloepi.pvp.utils.EffectManager;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.arrow.AbstractArrowMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractArrow extends CustomEntityProjectile {
	private static final double ARROW_BASE_DAMAGE = 2.0;
	
	protected int pickupDelay;
	protected int stuckTime;
	protected PickupMode pickupMode = PickupMode.DISALLOWED;
	protected int ticks;
	private double baseDamage = ARROW_BASE_DAMAGE;
	private int knockback;
	private SoundEvent soundEvent = getDefaultSound();
	
	private final Set<Integer> piercingIgnore = new HashSet<>();
	
	public AbstractArrow(@Nullable Entity shooter, @NotNull EntityType entityType) {
		super(shooter, entityType, false);
		
		if (shooter instanceof Player) {
			pickupMode = ((Player) shooter).isCreative() ? PickupMode.CREATIVE_ONLY : PickupMode.ALLOWED;
		}
	}
	
	@Override
	public void update(long time) {
		if (onGround) {
			stuckTime++;
		} else {
			stuckTime = 0;
		}
		
		if (pickupDelay > 0) {
			pickupDelay--;
		}

        // Pickup
        if (canBePickedUp(null)) {
            instance.getEntityTracker().nearbyEntities(position, 5, EntityTracker.Target.PLAYERS,
                    player -> {
                        if (!player.canPickupItem()) return;

                        // Do not pickup if not visible
                        if (!isViewer(player))
                            return;

                        if (isRemoved() || !canBePickedUp(player))
                            return;

                        if (player.getBoundingBox().expand(1, 0.5f, 1)
                                .intersectEntity(player.getPosition(), this)) {
                            PickupEntityEvent event = new PickupEntityEvent(player, this);
                            EventDispatcher.callCancellable(event, () -> {
                                if (pickup(player)) {
                                    player.sendPacketToViewersAndSelf(new CollectItemPacket(
                                            getEntityId(), player.getEntityId(), 1
                                    ));
                                    remove();
                                }
                            });
                        }
                    });
        }

		//TODO water (also for other projectiles?)
		
		tickRemoval();
	}
	
	protected void tickRemoval() {
		ticks++;
		if (ticks >= 1200) {
			remove();
		}
	}
	
	@Override
	public void onUnstuck() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		setVelocity(getPosition().direction().mul(
				random.nextFloat() * 0.2,
				random.nextFloat() * 0.2,
				random.nextFloat() * 0.2
		).mul(MinecraftServer.TICK_PER_SECOND));
		ticks = 0;
	}
	
	@Override
	public void onHit(@NotNull Entity entity) {
		if (piercingIgnore.contains(entity.getEntityId())) return;
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		double movementSpeed = getVelocity().length() / MinecraftServer.TICK_PER_SECOND;
		int damage = (int) Math.ceil(MathUtils.clamp(
				movementSpeed * baseDamage, 0.0, 2.147483647E9D));
		
		if (getPiercingLevel() > 0) {
			if (piercingIgnore.size() >= getPiercingLevel() + 1) {
				remove();
				return;
			}
			
			piercingIgnore.add(entity.getEntityId());
		}
		
		if (isCritical()) {
			int randomDamage = random.nextInt(damage / 2 + 2);
			damage = (int) Math.min(randomDamage + damage, 2147483647L);
		}
		
		Entity shooter = getShooter();
		DamageType damageType;
		damageType = CustomDamageType.arrow(this, Objects.requireNonNullElse(shooter, this));
		
		if (EntityUtils.damage(entity, damageType, damage)) {
			if (entity.getEntityType() == EntityType.ENDERMAN) return;
			
			if (isOnFire()) {
				EntityUtils.setOnFireForSeconds(entity, 5);
			}
			
			if (entity instanceof LivingEntity living) {
				if (getPiercingLevel() <= 0) {
					living.setArrowCount(living.getArrowCount() + 1);
				}
				
				if (knockback > 0) {
					Vec knockbackVec = getVelocity()
							.mul(1, 0, 1)
							.normalize().mul(knockback * 0.6);
					knockbackVec = knockbackVec.add(0, 0.1, 0)
							.mul(MinecraftServer.TICK_PER_SECOND / 2.0);
					
					if (knockbackVec.lengthSquared() > 0) {
						Vec newVel = living.getVelocity().add(knockbackVec);
						living.setVelocity(newVel);
					}
				}
				
				if (shooter instanceof LivingEntity livingShooter) {
					EnchantmentUtils.onUserDamaged(living, livingShooter);
					EnchantmentUtils.onTargetDamaged(livingShooter, living);
				}
				
				onHurt(living);
				
				if (living != shooter && living instanceof Player
						&& shooter instanceof Player && !isSilent()) {
					EffectManager.sendGameState((Player) shooter,
							ChangeGameStatePacket.Reason.ARROW_HIT_PLAYER, 0.0F);
				}
			}
			
			if (!isSilent()) {
				SoundManager.sendToAround(this, getSound(), Sound.Source.NEUTRAL,
						1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
			}
			
			if (getPiercingLevel() <= 0) {
				remove();
			}
		} else {
			Pos position = getPosition();
			setVelocity(getVelocity().mul(-0.1));
			teleport(position.withYaw(position.yaw() + 180));
			
			if (getVelocity().lengthSquared() < 1.0E-7D) {
				if (pickupMode == PickupMode.ALLOWED) {
					EntityUtils.spawnItemAtLocation(this, getPickupItem(), 0.1);
				}
				
				remove();
			}
		}
	}
	
	@Override
	public void onStuck() {
		if (!isSilent()) {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			SoundManager.sendToAround(this, getSound(), Sound.Source.NEUTRAL,
					1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
		}
		
		pickupDelay = 7;
		setCritical(false);
		setPiercingLevel((byte) 0);
		setSound(SoundEvent.ENTITY_ARROW_HIT);
		piercingIgnore.clear();
	}
	
	public boolean canBePickedUp(@Nullable Player player) {
		if (!((onGround || hasNoGravity()) && pickupDelay <= 0)) {
			return false;
		}
		
		return switch (pickupMode) {
			case ALLOWED -> true;
			case CREATIVE_ONLY -> player == null || player.isCreative();
			default -> false;
		};
	}
	
	public boolean pickup(Player player) {
		return player.isCreative() || player.getInventory().addItemStack(getPickupItem());
	}
	
	protected abstract ItemStack getPickupItem();
	
	protected void onHurt(LivingEntity entity) {
	}
	
	public SoundEvent getSound() {
		return soundEvent;
	}
	
	public void setSound(SoundEvent soundEvent) {
		this.soundEvent = soundEvent;
	}
	
	protected SoundEvent getDefaultSound() {
		return SoundEvent.ENTITY_ARROW_HIT;
	}
	
	public int getKnockback() {
		return knockback;
	}
	
	public void setKnockback(int knockback) {
		this.knockback = knockback;
	}
	
	public double getBaseDamage() {
		return baseDamage;
	}
	
	public void setBaseDamage(double baseDamage) {
		this.baseDamage = baseDamage;
	}
	
	public boolean isCritical() {
		return ((AbstractArrowMeta) getEntityMeta()).isCritical();
	}
	
	public void setCritical(boolean critical) {
		((AbstractArrowMeta) getEntityMeta()).setCritical(critical);
	}
	
	public byte getPiercingLevel() {
		return ((AbstractArrowMeta) getEntityMeta()).getPiercingLevel();
	}
	
	public void setPiercingLevel(byte piercingLevel) {
		((AbstractArrowMeta) getEntityMeta()).setPiercingLevel(piercingLevel);
	}
	
	public boolean isNoClip() {
		return ((AbstractArrowMeta) getEntityMeta()).isNoClip();
	}
	
	public void setNoClip(boolean noClip) {
		((AbstractArrowMeta) getEntityMeta()).setNoClip(noClip);
		super.hasPhysics = !noClip;
		super.noClip = noClip;
	}
	
	public enum PickupMode {
		DISALLOWED,
		ALLOWED,
		CREATIVE_ONLY
	}
}
