package info.sciman.skilltable.skills;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import info.sciman.skilltable.SkillTableMod;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public final class Skills implements EntityComponentInitializer {

    // Registry to hold all skills
    private static final HashMap<Identifier,Skill> skillRegistry = new HashMap<>();

    public static final Skill SKILL_SPEED = new Skill(3, new int[]{2,5,10});
    public static final Skill SKILL_JUMP = new Skill(3, new int[]{2,5,10});
    public static final Skill SKILL_HASTE = new Skill(2, new int[]{4,7});
    public static final Skill SKILL_TEST = new Skill(2, new int[]{2,5});
    public static final Skill SKILL_VEINMINER = new Skill(2, new int[]{15,30}).addPrerequisite(new Skill.SkillPrerequisite(SKILL_HASTE,2));
    public static final Skill SKILL_DOUBLE_JUMP = new Skill(1, new int[]{20}).addPrerequisite(new Skill.SkillPrerequisite(SKILL_JUMP,3));

    // Get array of all skills
    public static HashMap<Identifier,Skill> getSkillRegistry() {
        return skillRegistry;
    }

    public static Skill get(Identifier id) {
        return skillRegistry.getOrDefault(id,null);
    }

    // Get a list of all skills a player is qualified for
    public static List<Skill> getQualifiedSkills(PlayerEntity player) {
        List<Skill> qualifiedSkills = new ArrayList<>();
        for (Skill skill : skillRegistry.values()) {
            if (skill.playerSatisfiesPrerequisites(player)) {
                qualifiedSkills.add(skill);
            }else{
                System.out.println("Player not qualified for " + skill.getTranslationKey());
            }
        }
        return qualifiedSkills;
    }

    public static void initialize() {
        System.out.println("Registering all skills");
        register(SkillTableMod.id("speed"),SKILL_SPEED);
        register(SkillTableMod.id("jump"),SKILL_JUMP);
        register(SkillTableMod.id("haste"),SKILL_HASTE);
        register(SkillTableMod.id("test"),SKILL_TEST);

        register(SkillTableMod.id("veinminer"),SKILL_VEINMINER);
        register(SkillTableMod.id("double_jump"),SKILL_DOUBLE_JUMP);
    }

    public static void register(Identifier id, Skill skill) {
        skillRegistry.put(id,skill);
        skill.setIdentifier(id);
    }


    //-----------------

    // Skill component
    public static final ComponentKey<SkillComponent> SKILL_LIST = ComponentRegistryV3.INSTANCE.getOrCreate(SkillTableMod.id("speed_boost"), SkillComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // Run initialize first so we don't create an empty skill component
        initialize();
        registry.registerForPlayers(SKILL_LIST,e -> new SkillComponent(e), RespawnCopyStrategy.ALWAYS_COPY);
    }

    public static int getCurrentLevel(PlayerEntity player, Skill skill) {
        return SKILL_LIST.get(player).getValue(skill.getIdentifier());
    }
    public static void upgradeSkill(PlayerEntity player, Skill skill) {
        SkillComponent comp = SKILL_LIST.get(player);
        int desiredLevel = comp.getValue(skill.getIdentifier()) + 1;
        comp.setValue(skill.getIdentifier(), desiredLevel);
    }

}
