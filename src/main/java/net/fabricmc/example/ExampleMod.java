package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.example.patterns.RingPatternHeal;
import net.fabricmc.example.patterns.RingPatternLaunch;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;
import java.util.Iterator;

public class ExampleMod implements ModInitializer {

	// Veinminer enchantment
	private static Enchantment VEINMINER = Registry.register(
		Registry.ENCHANTMENT,
		new Identifier("sciman","veinminer"),
		new VeinminerEnchantment()
	);

	// Create items
	public static final Item TOOL_WOOD = new WandItem(ToolMaterials.WOOD,new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), WandItem.WandType.DEFAULT);
	public static final Item TOOL_STONE = new WandItem(ToolMaterials.STONE,new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), WandItem.WandType.DEFAULT);
	public static final Item TOOL_GOLD = new WandItem(ToolMaterials.GOLD,new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), WandItem.WandType.DEFAULT);
	public static final Item TOOL_DIAMOND = new WandItem(ToolMaterials.DIAMOND,new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), WandItem.WandType.DEFAULT);
	// Jeweled tools yield stronger effects
	public static final Item TOOL_GOLD_JEWELED = new WandItem(ToolMaterials.GOLD,new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), WandItem.WandType.LUCKY);
	public static final Item TOOL_DIAMOND_JEWELED = new WandItem(ToolMaterials.DIAMOND,new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), WandItem.WandType.LUCKY);
	// Tainted tools yield the opposite effect
	public static final Item TOOL_GOLD_TAINTED = new WandItem(ToolMaterials.GOLD,new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), WandItem.WandType.TAINTED);
	public static final Item TOOL_DIAMOND_TAINTED = new WandItem(ToolMaterials.DIAMOND,new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), WandItem.WandType.TAINTED);

	@Override
	public void onInitialize() {

		// Register items
		Registry.register(Registry.ITEM, new Identifier("sciman","tool_wood"), TOOL_WOOD);
		Registry.register(Registry.ITEM, new Identifier("sciman","tool_stone"), TOOL_STONE);
		Registry.register(Registry.ITEM, new Identifier("sciman","tool_gold"), TOOL_GOLD);
		Registry.register(Registry.ITEM, new Identifier("sciman","tool_diamond"), TOOL_DIAMOND);
		Registry.register(Registry.ITEM, new Identifier("sciman","tool_gold_jeweled"), TOOL_GOLD_JEWELED);
		Registry.register(Registry.ITEM, new Identifier("sciman","tool_diamond_jeweled"), TOOL_DIAMOND_JEWELED);
		Registry.register(Registry.ITEM, new Identifier("sciman","tool_gold_tainted"), TOOL_GOLD_TAINTED);
		Registry.register(Registry.ITEM, new Identifier("sciman","tool_diamond_tainted"), TOOL_DIAMOND_TAINTED);

		// Register ring patterns
		WandItem.registerPattern(new RingPatternHeal());
		WandItem.registerPattern(new RingPatternLaunch());

		// Register block break event for veinminer
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
			if (player == null || player.isSneaking()) return;

			// Find the tool the player is using
			ItemStack tool = player.inventory.getMainHandStack();

			// Can we actually break this block, with this tool?
			if (state.isToolRequired() && !tool.isEffectiveOn(state)) {
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

						for (int x=-1;x<2;x++) {
							for (int y=-1;y<2;y++) {
								for (int z = -1; z < 2; z++) {
									BlockPos p = pos.add(x,y,z);
									if (world.getBlockState(p).getBlock() == block && !blocksToDestroy.contains(p)) {newBlocksToConsider.add(p);}
								}
							}
						}

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
					world.breakBlock(b,true,player);

					if (!player.abilities.creativeMode) {
						// Damage tool
						if (!world.isClient) {
							tool.damage(1, player, (ent) -> {
								player.sendToolBreakStatus(Hand.MAIN_HAND);
							});
							// If the tool breaks...
							if (tool.getCount() == 0) break;
						}
					}
				}
			}
		});

	}

}
