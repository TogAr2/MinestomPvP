package io.github.bloepiloepi.pvp.explosion;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entity.PvpPlayer;
import io.github.bloepiloepi.pvp.events.ExplosionEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class PvpExplosionSupplier implements ExplosionSupplier {
	public static final PvpExplosionSupplier INSTANCE = new PvpExplosionSupplier();
	
	private PvpExplosionSupplier() {
	}
	
	@Override
	public Explosion createExplosion(float centerX, float centerY, float centerZ,
	                                 float strength, @Nullable NBTCompound additionalData) {
		return new Explosion(centerX, centerY, centerZ, strength) {
			private final Map<Player, Vec> playerKnockback = new HashMap<>();
			
			@Override
			protected List<Point> prepare(Instance instance) {
				List<Point> blocks = new ArrayList<>();
				ThreadLocalRandom random = ThreadLocalRandom.current();
				
				boolean breakBlocks = true;
				if (additionalData != null && additionalData.contains("breakBlocks"))
					breakBlocks = Objects.requireNonNull(additionalData.getByte("breakBlocks")) == (byte) 1;
				
				if (breakBlocks) {
					for (int x = 0; x < 16; ++x) {
						for (int y = 0; y < 16; ++y) {
							for (int z = 0; z < 16; ++z) {
								if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
									double xLength = (float) x / 15.0F * 2.0F - 1.0F;
									double yLength = (float) y / 15.0F * 2.0F - 1.0F;
									double zLength = (float) z / 15.0F * 2.0F - 1.0F;
									double length = Math.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);
									xLength /= length;
									yLength /= length;
									zLength /= length;
									double centerX = this.getCenterX();
									double centerY = this.getCenterY();
									double centerZ = this.getCenterZ();
									
									float strengthLeft = this.getStrength() * (0.7F + random.nextFloat() * 0.6F);
									for (; strengthLeft > 0.0F; strengthLeft -= 0.225F) {
										Vec position = new Vec(centerX, centerY, centerZ);
										Block block = instance.getBlock(position);
										
										if (!block.isAir()) {
											double explosionResistance = block.registry().explosionResistance();
											strengthLeft -= (explosionResistance + 0.3F) * 0.3F;
											
											if (strengthLeft > 0.0F) {
												Vec blockPosition = position.apply(Vec.Operator.FLOOR);
												if (!blocks.contains(blockPosition)) {
													blocks.add(blockPosition);
												}
											}
										}
										
										centerX += xLength * 0.30000001192092896D;
										centerY += yLength * 0.30000001192092896D;
										centerZ += zLength * 0.30000001192092896D;
									}
								}
							}
						}
					}
				}
				
				// Blocks list may be modified during the event call
				ExplosionEvent explosionEvent = new ExplosionEvent(instance, blocks);
				EventDispatcher.call(explosionEvent);
				if (explosionEvent.isCancelled()) return null;
				
				double strength = this.getStrength() * 2.0F;
				int minX_ = (int) Math.floor(this.getCenterX() - strength - 1.0D);
				int maxX_ = (int) Math.floor(this.getCenterX() + strength + 1.0D);
				int minY_ = (int) Math.floor(this.getCenterY() - strength - 1.0D);
				int maxY_ = (int) Math.floor(this.getCenterY() + strength + 1.0D);
				int minZ_ = (int) Math.floor(this.getCenterZ() - strength - 1.0D);
				int maxZ_ = (int) Math.floor(this.getCenterZ() + strength + 1.0D);
				
				int minX = Math.min(minX_, maxX_);
				int maxX = Math.max(minX_, maxX_);
				int minY = Math.min(minY_, maxY_);
				int maxY = Math.max(minY_, maxY_);
				int minZ = Math.min(minZ_, maxZ_);
				int maxZ = Math.max(minZ_, maxZ_);
				
				BoundingBox explosionBox = new BoundingBox(
						maxX - minX,
						maxY - minY,
						maxZ - minZ
				);
				
				Vec src = new Vec(getCenterX(), getCenterY() - (explosionBox.height() / 2), getCenterZ());
				
				Set<Entity> entities = instance.getEntities().stream()
						.filter(entity -> explosionBox.intersectEntity(src, entity))
						.collect(Collectors.toSet());
				Vec centerPoint = new Vec(this.getCenterX(), this.getCenterY(), this.getCenterZ());
				
				boolean anchor = false;
				if (additionalData != null && additionalData.contains("anchor")) {
					anchor = Boolean.TRUE.equals(additionalData.getBoolean("anchor"));
				}
				
				CustomDamageType damageType = anchor ? CustomDamageType.invalidRespawnPointExplosion()
						: CustomDamageType.explosion(this, getCausingEntity(instance));
				for (Entity entity : entities) {
					double currentStrength = entity.getPosition().distance(centerPoint) / strength;
					if (currentStrength <= 1.0D) {
						double dx = entity.getPosition().x() - this.getCenterX();
						double dy = (entity.getEntityType() == EntityType.TNT ? entity.getPosition().y() :
								entity.getPosition().y() + entity.getEyeHeight()) - this.getCenterY();
						double dz = entity.getPosition().z() - this.getCenterZ();
						double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
						if (distance != 0.0D) {
							dx /= distance;
							dy /= distance;
							dz /= distance;
							double exposure = getExposure(centerPoint, entity);
							currentStrength = (1.0D - currentStrength) * exposure;
							float damage = (float) ((currentStrength * currentStrength + currentStrength)
									/ 2.0D * 7.0D * strength + 1.0D);
							double knockback = currentStrength;
							if (entity instanceof LivingEntity living) {
								if (!living.damage(damageType, damage)) continue;
								knockback = EnchantmentUtils.getExplosionKnockback(living, currentStrength);
							}
							
							Vec knockbackVec = new Vec(
									dx * knockback,
									dy * knockback,
									dz * knockback
							);
							
							int tps = MinecraftServer.TICK_PER_SECOND;
							if (entity instanceof Player player) {
								if (player.getGameMode().canTakeDamage() && !player.isFlying()) {
									playerKnockback.put(player, knockbackVec);
									
									if (player instanceof PvpPlayer custom)
										custom.addVelocity(knockbackVec.mul(tps));
								}
							} else {
								entity.setVelocity(entity.getVelocity().add(knockbackVec.mul(tps)));
							}
						}
					}
				}
				
				return blocks;
			}
			
			@Override
			public void apply(@NotNull Instance instance) {
				List<Point> blocks = prepare(instance);
				if (blocks == null) return; // Event was cancelled
				byte[] records = new byte[3 * blocks.size()];
				for (int i = 0; i < blocks.size(); i++) {
					final var pos = blocks.get(i);
					if (instance.getBlock(pos).compare(Block.TNT)) {
						ExplosionListener.primeTnt(instance, pos, getCausingEntity(instance),
								ThreadLocalRandom.current().nextInt(20) + 10);
					}
					instance.setBlock(pos, Block.AIR);
					final byte x = (byte) (pos.x() - Math.floor(getCenterX()));
					final byte y = (byte) (pos.y() - Math.floor(getCenterY()));
					final byte z = (byte) (pos.z() - Math.floor(getCenterZ()));
					records[i * 3] = x;
					records[i * 3 + 1] = y;
					records[i * 3 + 2] = z;
				}
				
				Chunk chunk = instance.getChunkAt(getCenterX(), getCenterZ());
				if (chunk != null) {
					for (Player player : chunk.getViewers()) {
						Vec knockbackVec = playerKnockback.getOrDefault(player, Vec.ZERO);
						player.sendPacket(new ExplosionPacket(centerX, centerY, centerZ, strength,
								records, (float) knockbackVec.x(), (float) knockbackVec.y(), (float) knockbackVec.z()));
					}
				}
				playerKnockback.clear();
				
				if (additionalData != null && additionalData.contains("fire")) {
					if (Boolean.TRUE.equals(additionalData.getBoolean("fire"))) {
						ThreadLocalRandom random = ThreadLocalRandom.current();
						for (Point point : blocks) {
							if (random.nextInt(3) != 0
									|| !instance.getBlock(point).isAir()
									|| !instance.getBlock(point.sub(0, 1, 0)).isSolid())
								continue;
							
							instance.setBlock(point, Block.FIRE);
						}
					}
				}
				
				postSend(instance, blocks);
			}
			
			private LivingEntity getCausingEntity(Instance instance) {
				LivingEntity causingEntity = null;
				if (additionalData != null && additionalData.contains("causingEntity")) {
					UUID causingUuid = UUID.fromString(Objects.requireNonNull(additionalData.getString("causingEntity")));
					causingEntity = (LivingEntity) instance.getEntities().stream()
							.filter(entity -> entity instanceof LivingEntity
									&& entity.getUuid().equals(causingUuid))
							.findAny().orElse(null);
				}
				
				return causingEntity;
			}
		};
	}
	
	public static double getExposure(Point center, Entity entity) {
		BoundingBox box = entity.getBoundingBox();
		double xStep = 1 / (box.width() * 2 + 1);
		double yStep = 1 / (box.height() * 2 + 1);
		double zStep = 1 / (box.depth() * 2 + 1);
		double g = (1 - Math.floor(1 / xStep) * xStep) / 2;
		double h = (1 - Math.floor(1 / zStep) * zStep) / 2;
		if (xStep < 0 || yStep < 0 || zStep < 0) return 0;
		
		int exposedCount = 0;
		int rayCount = 0;
		double dx = 0;
		while (dx <= 1) {
			double dy = 0;
			while (dy <= 1) {
				double dz = 0;
				while (dz <= 1) {
					double rayX = box.minX() + dx * box.width();
					double rayY = box.minY() + dy * box.height();
					double rayZ = box.minZ() + dz * box.depth();
					Point point = new Vec(rayX + g, rayY, rayZ + h).add(entity.getPosition());
					if (noBlocking(entity.getInstance(), point, center)) exposedCount++;
					rayCount++;
					dz += zStep;
				}
				dy += yStep;
			}
			dx += xStep;
		}
		
		return exposedCount / (double) rayCount;
	}
	
	public static boolean noBlocking(Instance instance, Point start, Point end) {
		return CollisionUtils.isLineOfSightReachingShape(instance, null, start, end, new BoundingBox(1, 1, 1));
	}
}
