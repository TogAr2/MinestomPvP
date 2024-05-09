package io.github.togar2.pvp.feature;

public final class CombatVersion implements IndependentFeature {
	public static CombatVersion MODERN = new CombatVersion(false);
	public static CombatVersion LEGACY = new CombatVersion(true);
	
	private final boolean legacy;
	
	CombatVersion(boolean legacy) {
		this.legacy = legacy;
	}
	
	public boolean modern() {
		return !legacy;
	}
	
	public boolean legacy() {
		return legacy;
	}
	
	public static CombatVersion fromLegacy(boolean legacy) {
		return legacy ? LEGACY : MODERN;
	}
	
	@Override
	public String toString() {
		return "CombatVersion[legacy=" + legacy + "]";
	}
}
