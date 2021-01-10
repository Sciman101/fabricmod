package info.sciman.skilltable.skills;

import com.google.common.collect.Maps;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Skill {

    private int maxLevel;
    private int[] levelCosts;
    private Identifier identifier;
    private HashSet<SkillPrerequisite> prerequisites; // Skills we need to get this
    private final Map<EntityAttribute, EntityAttributeModifier> attributeModifiers = Maps.newHashMap();

    public Skill(int maxLevel, int[] levelCosts) {
        this.maxLevel = maxLevel;
        this.levelCosts = levelCosts;

        if (this.levelCosts.length != this.maxLevel) {
            throw new IndexOutOfBoundsException("Level cost size mismatch!");
        }

        prerequisites = new HashSet<>();
    }

    public void setIdentifier(Identifier id) {
        identifier = id;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Skill addPrerequisite(SkillPrerequisite prerequisite) {
        prerequisites.add(prerequisite); return this;
    }

    public Skill addAttributeModifier(EntityAttribute attribute, String uuid, double amount, EntityAttributeModifier.Operation operation) {
        EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(UUID.fromString(uuid), this::getTranslationKey, amount, operation);
        this.attributeModifiers.put(attribute, entityAttributeModifier);
        return this;
    }

    public String getTranslationKey() {
        return "skill." + identifier.getPath();
    }
    public String getTooltipTranslationKey() {
        return "skill." + identifier.getPath() + ".tooltip";
    }

    // Does this player meet the requirements?
    public boolean playerSatisfiesPrerequisites(PlayerEntity player) {
        if (prerequisites.isEmpty()) return true;
        SkillComponent playerSkillLevels = Skills.SKILL_LIST.get(player);
        for (SkillPrerequisite pre : prerequisites) {
            if (playerSkillLevels.getValue(pre.skill.getIdentifier()) < pre.skillLevel) return false;
        }
        return true;
    }

    // Called when this skill is given to a player
    public void onApplied(PlayerEntity player, AttributeContainer attributes) {
        Iterator modifierIter = this.attributeModifiers.entrySet().iterator();

        while(modifierIter.hasNext()) {
            Map.Entry<EntityAttribute, EntityAttributeModifier> entry = (Map.Entry)modifierIter.next();
            EntityAttributeInstance entityAttributeInstance = attributes.getCustomInstance((EntityAttribute)entry.getKey());
            if (entityAttributeInstance != null) {
                EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier)entry.getValue();
                entityAttributeInstance.removeModifier(entityAttributeModifier);
                entityAttributeInstance.addPersistentModifier(entry.getValue());
            }
        }
    }
    public void onRemoved(PlayerEntity player, AttributeContainer attributes) {
        Iterator var4 = this.attributeModifiers.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<EntityAttribute, EntityAttributeModifier> entry = (Map.Entry)var4.next();
            EntityAttributeInstance entityAttributeInstance = attributes.getCustomInstance((EntityAttribute)entry.getKey());
            if (entityAttributeInstance != null) {
                entityAttributeInstance.removeModifier((EntityAttributeModifier)entry.getValue());
            }
        }
    }

    public boolean isPlayerMaxLevel(PlayerEntity player) {
        SkillComponent playerSkillLevels = Skills.SKILL_LIST.get(player);
        return (playerSkillLevels.getValue(identifier) >= maxLevel);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getLevelCost(int level) {
        return levelCosts[level];
    }


    // A skill prerequisite
    public static class SkillPrerequisite {
        public Skill skill;
        public int skillLevel;
        public SkillPrerequisite(Skill skill, int skillLevel) {
            this.skill = skill;
            this.skillLevel = skillLevel;
        }
    }

}
