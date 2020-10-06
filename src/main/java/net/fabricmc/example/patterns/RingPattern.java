package net.fabricmc.example.patterns;

import net.fabricmc.example.WandItem;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class RingPattern {

    public abstract boolean comparePattern(Block[] pattern);
    public abstract void triggerEffect(World world, BlockPos center, ItemStack wand, WandItem.WandType type, PlayerEntity player);

}
