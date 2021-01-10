package info.sciman.skilltable.skills;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import info.sciman.skilltable.SkillTableMod;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Skills implements EntityComponentInitializer {

    // Registry to hold all skills
    private static final HashMap<Identifier,Skill> skillRegistry = new HashMap<>();

    public static final Skill SKILL_SPEED = new Skill(3, new int[]{2,5,10}).addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED,"dfa8594e-c15f-44fc-b1c6-7cbb3afc9c06",0.2, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    public static final Skill SKILL_JUMP = new Skill(3, new int[]{2,5,10});
    public static final Skill SKILL_HASTE = new Skill(2, new int[]{4,7});
    public static final Skill SKILL_VEINMINER = new Skill(2, new int[]{15,30}).addPrerequisite(new Skill.SkillPrerequisite(SKILL_HASTE,2));

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

        register(SkillTableMod.id("veinminer"),SKILL_VEINMINER);
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

    // Try to upgrade a skill for a player, assuming they meet the requirements
    public static boolean tryUpgradeSkill(PlayerEntity player, Skill skill) {
        int playerLevel = getCurrentLevel(player,skill);
        // Make sure we can actually get this
        if (playerLevel < skill.getMaxLevel()) {
            int levelCost = skill.getLevelCost(playerLevel);
            if (player.experienceLevel >= levelCost || player.isCreative()) {
                // Remove levels
                if (!player.isCreative()) player.addExperienceLevels(-levelCost);
                // Increment skill score
                SkillComponent comp = SKILL_LIST.get(player);
                comp.setValue(skill.getIdentifier(), playerLevel+1);

                // Send message
                player.sendMessage(new TranslatableText(skill.getTranslationKey()).append(new TranslatableText("skilltable.misc.on_level_up").append((playerLevel+1)+"!")),false);
                return true;
            }
        }
        return false;
    }

}
