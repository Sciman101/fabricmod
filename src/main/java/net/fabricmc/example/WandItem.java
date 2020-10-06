package net.fabricmc.example;

import net.fabricmc.example.patterns.RingPattern;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayList;

public class WandItem extends ToolItem {

    private static ArrayList<RingPattern> patterns = new ArrayList<RingPattern>();

    /**
     * Block offsets for the ring
     */
    public static final Vec3i[] OFFSETS = {
        new Vec3i(-1,0,2),
        new Vec3i(0,0,2),
        new Vec3i(1,0,2),
        new Vec3i(2,0,1),
        new Vec3i(2,0,0),
        new Vec3i(2,0,-1),
        new Vec3i(1,0,-2),
        new Vec3i(0,0,-2),
        new Vec3i(-1,0,-2),
        new Vec3i(-2,0,-1),
        new Vec3i(-2,0,0),
        new Vec3i(-2,0,1)
    };

    /**
     * Different wand variants
     */
    public enum WandType {
        DEFAULT,
        LUCKY,
        TAINTED
    }

    /**
     * What type of wand is this?
     */
    private WandType type;

    /**
     * Constructor
     * @param material
     * @param settings
     * @param type What type of wand is this?
     */
    public WandItem(ToolMaterial material, Settings settings, WandType type) {
        super(material, settings);
        this.type = type;
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    /**
     * Called when the wand is used to right click on a block
     * @param context
     * @return
     */
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        // Get position and detect circle around it
        BlockPos center = context.getBlockPos().up();
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();

        // Find a valid block ring
        // The pattern must repeat along each edge
        Block[] blockPattern = new Block[3];
        for (int i=0;i<12;i++) {
            BlockPos p = center.add(OFFSETS[i]);
            Block b = context.getWorld().getBlockState(p).getBlock();
            //We can't have air in the ring
            if (b == Blocks.AIR) {
                return ActionResult.FAIL;
            }
            if (i < 3) {
                // Assign value
                blockPattern[i] = b;
            }else{
                if (blockPattern[i%3] != b) {
                    // We must have a complete pattern
                    return ActionResult.FAIL;
                }
            }
        }

        // Check for a matching pattern
        for (RingPattern pat : patterns) {
            if (pat.comparePattern(blockPattern)) {
                // Success!
                ItemStack stack = context.getStack();
                // Trigger the effect
                pat.triggerEffect(world,center,stack,((WandItem)stack.getItem()).getWandType(),player);

                // Damage a flower?
                double rand = world.getRandom().nextDouble();
                if (rand <= getFlowerDamageChance((WandItem) stack.getItem())) {
                    int index = world.getRandom().nextInt(12);
                    BlockPos flowerPos = center.add(OFFSETS[index]);
                    world.breakBlock(flowerPos,false);
                    if (world.isClient) {
                        for (int i=0;i<10;i++) {
                            world.addParticle(ParticleTypes.ASH,(double)flowerPos.getX()+.5,(double)flowerPos.getY()+.5,(double)flowerPos.getZ()+.5,0,5.0,0);
                        }
                    }
                }

                // Damage the wand
                if (!world.isClient) {
                    stack.damage(1, player, (ent) -> {
                        player.sendToolBreakStatus(player.getActiveHand());
                    });
                }

                // Timeout
                if (stack.getCount() > 0) {
                    stack.setCooldown(30);
                }

                // Return success
                return ActionResult.SUCCESS;
            }
        }
        // Didn't do anything
        return ActionResult.FAIL;
    }

    /**
     * What is the chance that triggering a ring destroys one of the flowers?
     * @param tool
     * @return
     */
    private double getFlowerDamageChance(WandItem tool) {
        if (tool == ExampleMod.TOOL_WOOD) return 0.5D;
        else if (tool == ExampleMod.TOOL_STONE) return 0.2D;
        else if (tool == ExampleMod.TOOL_GOLD) return 0.01D; // Gold has the lowest chance
        else if (tool == ExampleMod.TOOL_DIAMOND) return 0.05D;
        else return 0.0D;
    }

    /**
     * Get the type of wand this is
     * @return
     */
    private WandType getWandType() {
        return type;
    }

    /**
     * Add a new ring pattern
     * @param pat The pattern to add
     */
    public static void registerPattern(RingPattern pat) {
        patterns.add(pat);
    }

}
