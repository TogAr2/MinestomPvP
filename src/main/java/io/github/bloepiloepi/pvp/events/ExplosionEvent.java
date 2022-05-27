package io.github.bloepiloepi.pvp.events;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called when an explosion will take place. Can be used to modify the affected blocks.
 */
public class ExplosionEvent implements InstanceEvent, CancellableEvent {
	private final Instance instance;
	private final List<Point> affectedBlocks;
	
	private boolean cancelled;
	
	public ExplosionEvent(@NotNull Instance instance, @NotNull List<Point> affectedBlocks) {
		this.instance = instance;
		this.affectedBlocks = affectedBlocks;
	}
	
	/**
	 * Gets the blocks affected by this explosion.
	 * The list may be modified.
	 *
	 * @return the list of blocks affected by the explosion
	 */
	public @NotNull List<Point> getAffectedBlocks() {
		return affectedBlocks;
	}
	
	@Override
	public @NotNull Instance getInstance() {
		return instance;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
