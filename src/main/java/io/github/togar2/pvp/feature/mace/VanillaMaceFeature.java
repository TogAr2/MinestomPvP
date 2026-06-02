package io.github.togar2.pvp.feature.mace;

import io.github.togar2.pvp.events.FinalAttackEvent;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.fall.FallFeature;
import io.github.togar2.pvp.feature.fall.VanillaFallFeature;
import io.github.togar2.pvp.feature.item.ItemDamageFeature;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public class VanillaMaceFeature implements MaceFeature, RegistrableFeature {

    public static final DefinedFeature<VanillaMaceFeature> DEFINED = new DefinedFeature<>(
            FeatureType.MACE, VanillaMaceFeature::new,
            FeatureType.FALL, FeatureType.ATTACK_COOLDOWN
    );

    private final FeatureConfiguration featureConfiguration;

    private FallFeature fallFeature;
    private ItemDamageFeature itemDamageFeature;

    public VanillaMaceFeature(FeatureConfiguration featureConfiguration) {
        this.featureConfiguration = featureConfiguration;
    }

    @Override
    public void initDependencies() {
        this.fallFeature = featureConfiguration.get(FeatureType.FALL);
        this.itemDamageFeature = featureConfiguration.get(FeatureType.ITEM_DAMAGE);
    }

    // TODO add support for mace enchantments
    // TODO add particles

    @Override
    public void init(EventNode<@NotNull EntityInstanceEvent> node) {
        EventNode<@NotNull FinalAttackEvent> maceNode = EventNode.event(
                "mace",
                EventFilter.from(FinalAttackEvent.class, Entity.class, FinalAttackEvent::getEntity),
                event -> event.getEntity() instanceof LivingEntity entity && entity.getItemInMainHand().material() == Material.MACE
        );

        maceNode.addListener(FinalAttackEvent.class, event -> {
           LivingEntity entity = (LivingEntity) event.getEntity();
           Entity target = event.getTarget();
           itemDamageFeature.damageEquipment(entity, EquipmentSlot.MAIN_HAND, 1);

           if (canSmashAttack(entity)) {
               event.setAttackSounds(false);
               entity.setTag(VanillaFallFeature.EXTRA_FALL_PARTICLES, true); // TODO Doesn't look right in-game

               float amount = event.getBaseDamage();
               event.setBaseDamage(amount + calculateSmashDamageBonus(entity));

               Vec currentVel = entity.getVelocity();
               Vec newVel = currentVel.withY(0.01);
               entity.setVelocity(newVel);
               entity.sendPacketToViewersAndSelf(new EntityVelocityPacket(entity.getEntityId(), newVel));

               playMaceSmashFX(entity, target);
               areaKnockback(entity, target);
           }
        });

        node.addChild(maceNode);
    }

    protected boolean canSmashAttack(@NotNull LivingEntity entity) {
        return fallFeature.getFallDistance(entity) > 1.5 && !entity.isFlyingWithElytra();
    }

    protected float calculateSmashDamageBonus(@NotNull LivingEntity entity) {
         float fallDistance = (float) fallFeature.getFallDistance(entity);
         float damage;
         if (fallDistance <= 3.0f)
             damage = 4.0f * fallDistance;
         else if (fallDistance <= 8.0f)
             damage = 12.0f + 2.0f * (fallDistance - 3.0f);
         else
             damage = 22.0f + fallDistance - 8.0f;
         return damage;
    }

    protected void playMaceSmashFX(@NotNull LivingEntity entity, @NotNull Entity target) {
        SoundEvent soundEvent;
        if (target.isOnGround() && fallFeature.getFallDistance(entity) <= 5.0)
            soundEvent = SoundEvent.ITEM_MACE_SMASH_GROUND;
        else if (target.isOnGround() && fallFeature.getFallDistance(entity) > 5.0)
            soundEvent = SoundEvent.ITEM_MACE_SMASH_GROUND_HEAVY;
        else
            soundEvent = SoundEvent.ITEM_MACE_SMASH_AIR;

        Pos pos = entity.getPosition();
        ViewUtil.viewersAndSelf(entity).playSound(
                Sound.sound()
                        .type(soundEvent)
                        .source(Sound.Source.PLAYER)
                        .build(),
                pos.x(), pos.y(), pos.z()
        );

        // TODO doesn't work
        Pos impactPos = target.getPosition();
        WorldEventPacket smashPacket = new WorldEventPacket(2013, impactPos, 750, false);
        ViewUtil.packetGroup(entity).sendGroupedPacket(smashPacket);
    }

    protected void areaKnockback(@NotNull LivingEntity entity, @NotNull Entity initialTarget) {
        initialTarget.getInstance().getEntityTracker().nearbyEntities(
                initialTarget.getPosition(), 3.5, EntityTracker.Target.ENTITIES, nearby -> {
                    if (nearby != initialTarget && nearby != entity) {
                        Vec direction = nearby.getPosition().sub(initialTarget.getPosition()).asVec();

                        // Risk of division by 0 without this
                        if (direction.lengthSquared() == 0) return;

                        double knockbackPower = getKnockbackPower(entity, nearby);
                        Vec knockbackVector = direction.normalize().mul(knockbackPower);

                        if (knockbackPower > 0.0) {
                            nearby.setVelocity(nearby.getVelocity().add(knockbackVector.x(), 0.7f, knockbackVector.z()));
                        }
                    }
                });
    }

    protected double getKnockbackPower(@NotNull LivingEntity entity, @NotNull Entity target) {
        int fallMultiplier = fallFeature.getFallDistance(entity) > 5.0 ? 2 : 1;
        double knockbackResistance = target instanceof LivingEntity livingTarget ? livingTarget.getAttributeValue(Attribute.KNOCKBACK_RESISTANCE) : 0;
        return (3.5 - target.getDistance(entity)) * 0.7 * fallMultiplier * (1 - knockbackResistance);
    }
}
