package io.github.togar2.pvp.legacy;

import net.minestom.server.ServerFlag;

/**
 * Class which contains settings for legacy knockback.
 * <br><br>
 * For further documentation, see the <a href="https://github.com/kernitus/BukkitOldCombatMechanics/blob/d222286fd84fe983fdbdff79699182837871ab9b/src/main/resources/config.yml#L279">config of BukkitOldCombatMechanics</a>
 */
public record LegacyKnockbackSettings(double horizontal, double vertical,
                                      double verticalLimit,
                                      double extraHorizontal, double extraVertical) {
	public static final LegacyKnockbackSettings DEFAULT = builder().build();
	
	public LegacyKnockbackSettings(double horizontal, double vertical, double verticalLimit,
	                               double extraHorizontal, double extraVertical) {
		double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
		this.horizontal = horizontal * tps;
		this.vertical = vertical * tps;
		this.verticalLimit = verticalLimit * tps;
		this.extraHorizontal = extraHorizontal * tps;
		this.extraVertical = extraVertical * tps;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private double horizontal = 0.4, vertical = 0.4;
		private double verticalLimit = 0.4;
		private double extraHorizontal = 0.5, extraVertical = 0.1;
		
		public Builder horizontal(double horizontal) {
			this.horizontal = horizontal;
			return this;
		}
		
		public Builder vertical(double vertical) {
			this.vertical = vertical;
			return this;
		}
		
		public Builder verticalLimit(double verticalLimit) {
			this.verticalLimit = verticalLimit;
			return this;
		}
		
		public Builder extraHorizontal(double extraHorizontal) {
			this.extraHorizontal = extraHorizontal;
			return this;
		}
		
		public Builder extraVertical(double extraVertical) {
			this.extraVertical = extraVertical;
			return this;
		}
		
		public LegacyKnockbackSettings build() {
			return new LegacyKnockbackSettings(horizontal, vertical, verticalLimit, extraHorizontal, extraVertical);
		}
	}
}
