package io.github.bloepiloepi.pvp.damage;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Explosion;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Position;
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
	public static final CustomDamageType FALL = (new CustomDamageType("fall")).setBypassesArmor();
	public static final CustomDamageType FLY_INTO_WALL = (new CustomDamageType("flyIntoWall")).setBypassesArmor();
	public static final CustomDamageType OUT_OF_WORLD = (new CustomDamageType("outOfWorld")).setBypassesArmor().setOutOfWorld();
	public static final CustomDamageType GENERIC = (new CustomDamageType("generic")).setBypassesArmor();
	public static final CustomDamageType MAGIC = (new CustomDamageType("magic")).setBypassesArmor().setUsesMagic();
	public static final CustomDamageType WITHER = (new CustomDamageType("wither")).setBypassesArmor();
	public static final CustomDamageType ANVIL = new CustomDamageType("anvil");
	public static final CustomDamageType FALLING_BLOCK = new CustomDamageType("fallingBlock");
	public static final CustomDamageType DRAGON_BREATH = (new CustomDamageType("dragonBreath")).setBypassesArmor();
	public static final CustomDamageType DRYOUT = new CustomDamageType("dryout");
	public static final CustomDamageType SWEET_BERRY_BUSH = new CustomDamageType("sweetBerryBush");
	private boolean bypassesArmor;
	private boolean outOfWorld;
	private boolean unblockable;
	private float exhaustion = 0.1F;
	private boolean fire;
	private boolean projectile;
	private boolean scaleWithDifficulty;
	private boolean magic;
	private boolean explosive;
	public final String name;
	
	protected CustomDamageType(String name) {
		super("");
		this.name = name;
	}
	
	public static CustomDamageType entity(LivingEntity attacker) {
		return new CustomEntityDamage(attacker);
	}
	
	public static CustomDamageType projectile(Entity projectile, Entity attacker) {
		return (new CustomEntityProjectileDamage(attacker, projectile));
	}
	
	public static CustomDamageType magic(Entity magic, @Nullable Entity attacker) {
		return (new CustomEntityProjectileDamage(attacker, magic)).setBypassesArmor().setUsesMagic();
	}
	
	public static CustomDamageType thorns(Entity attacker) {
		return (new CustomEntityDamage(attacker)).setThorns().setUsesMagic();
	}
	
	public static CustomDamageType explosion(@Nullable Explosion explosion, @Nullable LivingEntity causingEntity) {
		return explosion(explosion != null ? causingEntity : null);
	}
	
	public static CustomDamageType explosion(@Nullable LivingEntity attacker) {
		return attacker != null ? (new CustomEntityDamage(attacker)).setScaledWithDifficulty().setExplosive() : (new CustomDamageType("explosion")).setScaledWithDifficulty().setExplosive();
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
	public Entity getSource() {
		return this.getAttacker();
	}
	
	@Nullable
	public Entity getAttacker() {
		return null;
	}
	
	protected CustomDamageType setBypassesArmor() {
		this.bypassesArmor = true;
		this.exhaustion = 0.0F;
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
	
	//TODO death message
	
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
	
	public CustomDamageType setUsesMagic() {
		this.magic = true;
		return this;
	}
	
	public boolean isSourceCreativePlayer() {
		Entity entity = this.getAttacker();
		return entity instanceof Player && !((Player) entity).getGameMode().canTakeDamage();
	}
	
	@Nullable
	public Position getPosition() {
		return null;
	}
	
	@Nullable
	@Override
	public SoundEvent getSound(@NotNull LivingEntity entity) {
		if (this.isFire() && entity instanceof Player) {
			return SoundEvent.PLAYER_HURT_ON_FIRE;
		} else {
			return super.getSound(entity);
		}
	}
}
