package io.github.bloepiloepi.pvp.damage;

import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.entity.Tracker;
import io.github.bloepiloepi.pvp.listeners.DamageListener;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Explosion;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomDamageType extends DamageType {
	public static final CustomDamageType IN_FIRE = (new CustomDamageType("inFire")).setBypassesArmor().setFire();
	public static final CustomDamageType LIGHTNING_BOLT = new CustomDamageType("lightningBolt");
	public static final CustomDamageType ON_FIRE = (new CustomDamageType("onFire")).setBypassesArmor().setFire();
	public static final CustomDamageType LAVA = (new CustomDamageType("lava")).setFire();
	public static final CustomDamageType HOT_FLOOR = (new CustomDamageType("hotFloor")).setFire();
	public static final CustomDamageType IN_WALL = (new CustomDamageType("inWall")).setBypassesArmor();
	public static final CustomDamageType CRAMMING = (new CustomDamageType("cramming")).setBypassesArmor();
	public static final CustomDamageType DROWN = (new CustomDamageType("drown")).setBypassesArmor();
	public static final CustomDamageType STARVE = (new CustomDamageType("starve")).setBypassesArmor().setUnblockable();
	public static final CustomDamageType CACTUS = new CustomDamageType("cactus");
	public static final CustomDamageType FALL = (new CustomDamageType("fall")).setBypassesArmor().setFall();
	public static final CustomDamageType ENDER_PEARL = (new CustomDamageType("fall")).setBypassesArmor().setFall();
	public static final CustomDamageType FLY_INTO_WALL = (new CustomDamageType("flyIntoWall")).setBypassesArmor();
	public static final CustomDamageType OUT_OF_WORLD = (new CustomDamageType("outOfWorld")).setBypassesArmor().setOutOfWorld();
	public static final CustomDamageType GENERIC = (new CustomDamageType("generic")).setBypassesArmor();
	public static final CustomDamageType MAGIC = (new CustomDamageType("magic")).setBypassesArmor().setMagic();
	public static final CustomDamageType WITHER = (new CustomDamageType("wither")).setBypassesArmor();
	public static final CustomDamageType ANVIL = new CustomDamageType("anvil").setDamagesHelmet();
	public static final CustomDamageType FALLING_BLOCK = new CustomDamageType("fallingBlock").setDamagesHelmet();
	public static final CustomDamageType DRAGON_BREATH = (new CustomDamageType("dragonBreath")).setBypassesArmor();
	public static final CustomDamageType DRYOUT = new CustomDamageType("dryout");
	public static final CustomDamageType SWEET_BERRY_BUSH = new CustomDamageType("sweetBerryBush");
	public static final CustomDamageType FREEZE = new CustomDamageType("freeze").setBypassesArmor();
	public static final CustomDamageType FALLING_STALACTITE = new CustomDamageType("fallingStalactite").setDamagesHelmet();
	public static final CustomDamageType STALAGMITE = new CustomDamageType("stalagmite").setBypassesArmor().setFall();
	
	private boolean damagesHelmet;
	private boolean bypassesArmor;
	private boolean outOfWorld;
	private boolean unblockable;
	private float exhaustion = 0.1F;
	private boolean fire;
	private boolean projectile;
	private boolean scaleWithDifficulty;
	private boolean magic;
	private boolean explosive;
	private boolean fall;
	public final String name;
	
	protected CustomDamageType(String name) {
		super(name);
		this.name = name;
	}
	
	public static CustomDamageType sting(LivingEntity attacker) {
		return new CustomEntityDamage("sting", attacker);
	}
	
	public static CustomDamageType mob(LivingEntity attacker) {
		return new CustomEntityDamage("mob", attacker);
	}
	
	public static CustomDamageType indirectMob(Entity projectile, Entity attacker) {
		return new CustomIndirectEntityDamage("mob", attacker, projectile);
	}
	
	public static CustomDamageType player(Player player) {
		return new CustomEntityDamage("player", player);
	}
	
	public static CustomDamageType arrow(Entity arrow, @Nullable Entity shooter) {
		return new CustomIndirectEntityDamage("arrow", arrow, shooter).setProjectile();
	}
	
	public static CustomDamageType trident(Entity trident, @Nullable Entity thrower) {
		return new CustomIndirectEntityDamage("trident", trident, thrower);
	}
	
	public static CustomDamageType fireworks(Entity rocket, @Nullable Entity owner) {
		return new CustomIndirectEntityDamage("fireworks", rocket, owner);
	}
	
	public static CustomDamageType fireball(Entity fireball, @Nullable Entity shooter) {
		return (shooter == null ? new CustomIndirectEntityDamage("onFire", fireball, fireball) : new CustomIndirectEntityDamage("fireball", fireball, shooter)).setFire().setProjectile();
	}
	
	public static CustomDamageType witherSkull(Entity witherSkull, Entity shooter) {
		return new CustomIndirectEntityDamage("witherSkull", witherSkull, shooter).setProjectile();
	}
	
	public static CustomDamageType thrown(Entity thrown, @Nullable Entity thrower) {
		return new CustomIndirectEntityDamage("thrown", thrown, thrower).setProjectile();
	}
	
	public static CustomDamageType indirectMagic(Entity magic, @Nullable Entity owner) {
		return new CustomIndirectEntityDamage("indirectMagic", magic, owner).setBypassesArmor().setMagic();
	}
	
	public static CustomDamageType thorns(Entity wearer) {
		return new CustomEntityDamage("thorns", wearer).setThorns().setMagic();
	}
	
	public static CustomDamageType explosion(@Nullable Explosion explosion, @Nullable LivingEntity causingEntity) {
		return explosion(explosion != null ? causingEntity : null);
	}
	
	public static CustomDamageType explosion(@Nullable LivingEntity causingEntity) {
		return (causingEntity != null ? new CustomEntityDamage("explosion.player", causingEntity) : new CustomDamageType("explosion")).setScaledWithDifficulty().setExplosive();
	}
	
	public static CustomDamageType invalidRespawnPointExplosion() {
		return new InvalidRespawnPointDamageType();
	}
	
	public boolean isProjectile() {
		return this.projectile;
	}
	
	public CustomDamageType setProjectile() {
		this.projectile = true;
		return this;
	}
	
	public boolean isExplosive() {
		return this.explosive;
	}
	
	public CustomDamageType setExplosive() {
		this.explosive = true;
		return this;
	}
	
	public boolean bypassesArmor() {
		return this.bypassesArmor;
	}
	
	public boolean damagesHelmet() {
		return this.damagesHelmet;
	}
	
	public float getExhaustion() {
		return this.exhaustion;
	}
	
	public boolean isOutOfWorld() {
		return this.outOfWorld;
	}
	
	public boolean isUnblockable() {
		return this.unblockable;
	}
	
	@Nullable
	public Entity getDirectEntity() {
		return this.getEntity();
	}
	
	@Nullable
	public Entity getEntity() {
		return null;
	}
	
	protected CustomDamageType setBypassesArmor() {
		this.bypassesArmor = true;
		this.exhaustion = 0.0F;
		return this;
	}
	
	protected CustomDamageType setDamagesHelmet() {
		this.damagesHelmet = true;
		return this;
	}
	
	protected CustomDamageType setOutOfWorld() {
		this.outOfWorld = true;
		return this;
	}
	
	protected CustomDamageType setUnblockable() {
		this.unblockable = true;
		this.exhaustion = 0.0F;
		return this;
	}
	
	protected CustomDamageType setFire() {
		this.fire = true;
		return this;
	}
	
	public @Nullable Component getDeathMessage(@NotNull Player killed) {
		String id = "death.attack." + getIdentifier();
		LivingEntity killer = getKillCredit(killed);
		if (killer == null) {
			return Component.translatable(id, EntityUtils.getName(killed));
		} else {
			return Component.translatable(id + ".player", EntityUtils.getName(killed),
					EntityUtils.getName(killer));
		}
	}
	
	@Override
	public @Nullable Component buildDeathMessage(@NotNull Player killed) {
		return Tracker.combatManager.get(killed.getUuid()).getDeathMessage();
	}
	
	@Override
	public Component buildDeathScreenText(@NotNull Player killed) {
		return buildDeathMessage(killed);
	}
	
	public boolean isFire() {
		return this.fire;
	}
	
	public String getName() {
		return this.name;
	}
	
	public CustomDamageType setScaledWithDifficulty() {
		this.scaleWithDifficulty = true;
		return this;
	}
	
	public boolean isScaledWithDifficulty() {
		return this.scaleWithDifficulty;
	}
	
	public boolean isMagic() {
		return this.magic;
	}
	
	public CustomDamageType setMagic() {
		this.magic = true;
		return this;
	}
	
	public boolean isFall() {
		return this.fall;
	}
	
	public CustomDamageType setFall() {
		this.fall = true;
		return this;
	}
	
	public boolean isCreative() {
		Entity entity = this.getEntity();
		return entity instanceof Player && !((Player) entity).getGameMode().canTakeDamage();
	}
	
	@Nullable
	public Pos getPosition() {
		return null;
	}
	
	@Nullable
	@Override
	public SoundEvent getSound(@NotNull LivingEntity entity) {
		//TODO
		if (this.isFire() && entity instanceof Player) {
			return SoundEvent.ENTITY_PLAYER_HURT_ON_FIRE;
		} else {
			return super.getSound(entity);
		}
	}
	
	@Nullable
	public SoundEvent getDeathSound(@NotNull LivingEntity entity) {
		//TODO
		if (entity instanceof Player) {
			return SoundEvent.ENTITY_PLAYER_DEATH;
		} else {
			return SoundEvent.ENTITY_GENERIC_DEATH;
		}
	}
	
	public static @Nullable LivingEntity getKillCredit(@NotNull Player killed) {
		LivingEntity killer = Tracker.combatManager.get(killed.getUuid()).getKiller();
		if (killer == null) {
			Integer lastDamagedById = killed.getTag(DamageListener.LAST_DAMAGED_BY);
			if (lastDamagedById != null) {
				Entity entity = Entity.getEntity(lastDamagedById);
				if (entity instanceof LivingEntity living) killer = living;
			}
		}
		
		return killer;
	}
}
