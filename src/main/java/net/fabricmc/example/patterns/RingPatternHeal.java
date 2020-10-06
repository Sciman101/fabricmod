package net.fabricmc.example.patterns;

import net.fabricmc.example.WandItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class RingPatternHeal extends RingPattern {
    // Heals all mobs in a radius

    @Override
    public boolean comparePattern(Block[] pattern) {
        // All poppy
        return pattern[0] == Blocks.POPPY && pattern[1] == Blocks.POPPY && pattern[2] == Blocks.POPPY;
    }

    @Override
    public void triggerEffect(World world, BlockPos center, ItemStack wand, WandItem.WandType type, PlayerEntity player) {

        Box bounds = new Box(center.add(-2,0,-2),center.add(2,4,2));
        List<Entity> entities = world.getOtherEntities(null,bounds,(ent) -> {
            return ent instanceof LivingEntity;
        });

        if (entities.size() > 0) {

            // Healing decreases as more entities are added
            float healAmount = 5f / entities.size();

            for (Entity e : entities) {

                LivingEntity le = (LivingEntity) e;

                // Trigger opposite effect, do damage
                if (type == WandItem.WandType.TAINTED) {
                    le.damage(DamageSource.MAGIC,healAmount);
                }else{

                    // Trigger amplified effect and add absorption
                    if (type == WandItem.WandType.LUCKY) {
                        le.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,120,3));
                    }
                    // Heal
                    le.heal(healAmount);
                }

                // Add particle effects
                if (world.isClient) {
                    world.addParticle(type == WandItem.WandType.TAINTED ? ParticleTypes.FLAME : ParticleTypes.HEART, e.getX(), e.getBodyY(1.0), e.getZ(), 0.0D, 5.0D, 0.0D);
                }
            }

        }
    }
}
