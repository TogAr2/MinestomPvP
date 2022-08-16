package io.github.bloepiloepi.pvp.config;

public class PvPConfigBuilder {
	private AttackConfig attack;
	private DamageConfig damage;
	private ExplosionConfig explosion;
	private ArmorToolConfig armorTool;
	private FoodConfig food;
	private PotionConfig potion;
	private ProjectileConfig projectile;
	private SwordBlockingConfig swordBlocking;
	
	PvPConfigBuilder() {
	}
	
	/**
	 * Sets everything to the default settings for modern pvp (enables everything).
	 *
	 * @return this
	 */
	public PvPConfigBuilder defaultOptions() {
		attack = AttackConfig.DEFAULT;
		damage = DamageConfig.DEFAULT;
		explosion = ExplosionConfig.DEFAULT;
		armorTool = ArmorToolConfig.DEFAULT;
		food = FoodConfig.DEFAULT;
		potion = PotionConfig.DEFAULT;
		projectile = ProjectileConfig.DEFAULT;
		swordBlocking = null;
		return this;
	}
	
	/**
	 * Sets everything to the default settings for legacy pvp (enables everything).
	 *
	 * @return this
	 */
	public PvPConfigBuilder legacyOptions() {
		attack = AttackConfig.LEGACY;
		damage = DamageConfig.LEGACY;
		explosion = ExplosionConfig.DEFAULT;
		armorTool = ArmorToolConfig.LEGACY;
		food = FoodConfig.LEGACY;
		potion = PotionConfig.LEGACY;
		projectile = ProjectileConfig.LEGACY;
		swordBlocking = SwordBlockingConfig.LEGACY;
		return this;
	}
	
	public void attack(AttackConfig attack) {
		this.attack = attack;
	}
	
	public void damage(DamageConfig damage) {
		this.damage = damage;
	}
	
	public void explosion(ExplosionConfig explosion) {
		this.explosion = explosion;
	}
	
	public void armorTool(ArmorToolConfig armorTool) {
		this.armorTool = armorTool;
	}
	
	public void food(FoodConfig food) {
		this.food = food;
	}
	
	public void potion(PotionConfig potion) {
		this.potion = potion;
	}
	
	public void projectile(ProjectileConfig projectile) {
		this.projectile = projectile;
	}
	
	public void swordBlocking(SwordBlockingConfig swordBlocking) {
		this.swordBlocking = swordBlocking;
	}
	
	public PvPConfig build() {
		return new PvPConfig(
				attack, damage, explosion, armorTool,
				food, potion, projectile, swordBlocking
		);
	}
}
