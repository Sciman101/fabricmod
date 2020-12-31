package info.sciman.skilltable;

import info.sciman.skilltable.screen.SkillScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

@Environment(EnvType.CLIENT)
public class SkillTableClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(SkillTableMod.SKILL_TABLE_SCREEN_HANDLER, SkillScreen::new);
    }
}
