package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Consumer;

public class ExampleMod implements ModInitializer {

	private static Enchantment VEINMINER = Registry.register(
		Registry.ENCHANTMENT,
		new Identifier("sciman","veinminer"),
		new VeinminerEnchantment()
	);

	@Override
	public void onInitialize() {

		// Register block break event
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
			if (player == null) return;

			// Find the tool the player is using
			ItemStack tool = player.getEquippedStack(EquipmentSlot.MAINHAND);

			// Can we actually break this block, with this tool?
			if (!tool.getItem().isEffectiveOn(state)) {
				return;
			}

			// Get veinminer level
			int level = EnchantmentHelper.getLevel(VEINMINER,tool);
			// Do we have the enchantment?
			if (level > 0) {
				// Generate sets to explore
				HashSet<BlockPos> blocksToDestroy = new HashSet<>();
				HashSet<BlockPos> blocksToConsider = new HashSet<>();
				HashSet<BlockPos> newBlocksToConsider = new HashSet<>();
				blocksToConsider.add(pos);

				// Figure out the block we're going to break
				Block block = state.getBlock();

				// Determine every block we should break
				for (int i=0;i<level+1;i++) {

					// Iterate over considered block
					for (Iterator<BlockPos> iter = blocksToConsider.iterator();iter.hasNext();) {

						BlockPos b = iter.next();

						considerBlock(world,b.north(),block,blocksToDestroy,newBlocksToConsider);
						considerBlock(world,b.south(),block,blocksToDestroy,newBlocksToConsider);
						considerBlock(world,b.east(),block,blocksToDestroy,newBlocksToConsider);
						considerBlock(world,b.west(),block,blocksToDestroy,newBlocksToConsider);
						considerBlock(world,b.up(),block,blocksToDestroy,newBlocksToConsider);
						considerBlock(world,b.down(),block,blocksToDestroy,newBlocksToConsider);

						// Add to remove list and remove from current list
						if (i != 0) {// Skip first iteration since we've already broken that block
							blocksToDestroy.add(b);
						}
					}
					// Swap
					blocksToConsider.clear();
					blocksToConsider.addAll(newBlocksToConsider);
					newBlocksToConsider.clear();
				}

				// Actually break them
				for (BlockPos b : blocksToDestroy) {
					// Destroy block
					world.breakBlock(b,true);
					if (!player.abilities.creativeMode) {
						// Damage tool
						tool.damage(1, player, (ent) -> {
							player.sendToolBreakStatus(player.getActiveHand());
						});
						// If the tool breaks...
						if (tool.getCount() == 0) break;
					}
				}
			}
		});

	}

	/**
	 * Helper function for veinminer
	 * @param world
	 * @param pos
	 * @param block
	 * @param ignoreList
	 * @param considerList
	 */
	private void considerBlock(World world, BlockPos pos, Block block, HashSet<BlockPos> ignoreList, HashSet<BlockPos> considerList) {
		if (world.getBlockState(pos).getBlock() == block && !ignoreList.contains(pos)) {considerList.add(pos);}
	}

}
