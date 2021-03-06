package info.sciman.skilltable;

import info.sciman.skilltable.screen.SkillScreenHandler;
import info.sciman.skilltable.skills.Skill;
import info.sciman.skilltable.skills.Skills;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SkillTableMod implements ModInitializer {

	public static final String MOD_ID = "skilltable";
	public static final Identifier SKILL_TABLE_ID = id("skill_table");

	// Networking identifiers
	public static final Identifier REQUEST_SKILL_UPGRADE_PACKET_ID = id("upgrade_skill");

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

		// Setup skill upgrade request handler
		ServerPlayNetworking.registerGlobalReceiver(REQUEST_SKILL_UPGRADE_PACKET_ID, (server,playerEntity,handler,buf,responseSender) -> {
			Skill skill = Skills.get(buf.readIdentifier());
			Skills.tryUpgradeSkill(playerEntity,skill);
		});
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID,path);
	}

}
