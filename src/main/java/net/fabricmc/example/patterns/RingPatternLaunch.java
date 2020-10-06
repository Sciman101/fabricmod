package net.fabricmc.example.patterns;

import net.fabricmc.example.WandItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.List;

public class RingPatternLaunch extends RingPattern {
    // Launch all mobs in a radius

    @Override
    public boolean comparePattern(Block[] pattern) {
        // All poppy
        return pattern[0] == Blocks.LILY_OF_THE_VALLEY && pattern[1] == Blocks.OXEYE_DAISY && pattern[2] == Blocks.LILY_OF_THE_VALLEY;
    }

    @Override
    public void triggerEffect(World world, BlockPos center, ItemStack wand, WandItem.WandType type, PlayerEntity player) {
        // Calculate bounding box for entity bounds
        Box bounds = new Box(center.add(-2,0,-2),center.add(2,4,2));
        List<Entity> entities = world.getOtherEntities(null,bounds);
        if (entities.size() > 0) {
            float velocityY = 1.0f / entities.size();

            if (type == WandItem.WandType.LUCKY) velocityY *= 2;
            if (type == WandItem.WandType.TAINTED) velocityY *= -1;

            for (Entity e : entities) {
                e.addVelocity(0, velocityY, 0);
                if (e instanceof LivingEntity) {
                    if (type == WandItem.WandType.LUCKY) {
                        ((LivingEntity) e).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, (int)(60 * velocityY)));
                    }
                }
            }
            // Particle effects
            if (world.isClient) {
                for (Vec3i offset : WandItem.OFFSETS) {
                    BlockPos pos = center.add(offset);
                    for (int i = 0; i < 5; i++) {
                        world.addParticle(ParticleTypes.SMOKE, (double) pos.getX() + .5, (double) pos.getY() + .5, (double) pos.getZ() + .5, 0.0D, world.getRandom().nextDouble() * 2, 0.0D);
                    }
                }
            }
        }
    }
}
