package info.sciman.skilltable.skills;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashSet;

public class Skill {

    private int maxLevel;
    private int[] levelCosts;
    private Identifier identifier;
    private HashSet<SkillPrerequisite> prerequisites; // Skills we need to get this

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
