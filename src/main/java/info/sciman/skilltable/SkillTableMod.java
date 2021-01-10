package info.sciman.skilltable;

import info.sciman.skilltable.screen.SkillScreenHandler;
import info.sciman.skilltable.skills.Skill;
import info.sciman.skilltable.skills.SkillComponent;
import info.sciman.skilltable.skills.Skills;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SkillTableMod implements ModInitializer {

	public static final String MOD_ID = "skilltable";
	public static final Identifier SKILL_TABLE_ID = id("skill_table");

	// Networking identifiers
	public static final Identifier UPGRADE_SKILL_PACKET_ID = id("upgrade_skill");

	public static final ScreenHandlerType<SkillScreenHandler> SKILL_TABLE_SCREEN_HANDLER;

	public static final Block BLOCK_SKILL_TABLE = new SkillTableBlock(FabricBlockSettings.of(Material.WOOD));

	static {
		SKILL_TABLE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(SKILL_TABLE_ID, SkillScreenHandler::new);
	}

	@Override
	public void onInitialize() {
		// Register skill table block
		Registry.register(Registry.BLOCK, SKILL_TABLE_ID, BLOCK_SKILL_TABLE);
		Registry.register(Registry.ITEM, SKILL_TABLE_ID, new BlockItem(BLOCK_SKILL_TABLE, new Item.Settings().group(ItemGroup.DECORATIONS)));
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID,path);
	}

}
