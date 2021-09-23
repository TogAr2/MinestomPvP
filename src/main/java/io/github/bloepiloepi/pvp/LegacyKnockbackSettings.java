package io.github.bloepiloepi.pvp;

/**
 * Class which contains settings for legacy knockback.
 * <br><br>
 * For further documentation, see the <a href="https://github.com/kernitus/BukkitOldCombatMechanics/blob/d222286fd84fe983fdbdff79699182837871ab9b/src/main/resources/config.yml#L279">config of BukkitOldCombatMechanics</a>
 */
public class LegacyKnockbackSettings {
	private final double horizontal, vertical;
	private final double verticalLimit;
	private final double extraHorizontal, extraVertical;
	
	public LegacyKnockbackSettings(double horizontal, double vertical, double verticalLimit,
	                               double extraHorizontal, double extraVertical) {
		this.horizontal = horizontal;
		this.vertical = vertical;
		this.verticalLimit = verticalLimit;
		this.extraHorizontal = extraHorizontal;
		this.extraVertical = extraVertical;
	}
	
	public double getHorizontal() {
		return horizontal;
	}
	
	public double getVertical() {
		return vertical;
	}
	
	public double getVerticalLimit() {
		return verticalLimit;
	}
	
	public double getExtraHorizontal() {
		return extraHorizontal;
	}
	
	public double getExtraVertical() {
		return extraVertical;
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
