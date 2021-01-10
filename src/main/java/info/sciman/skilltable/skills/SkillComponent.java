package info.sciman.skilltable.skills;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SkillComponent implements IdentifierListInt, AutoSyncedComponent {

    private final Entity provider;
    private HashMap<Identifier, Integer> skillLevels;

    public SkillComponent(Entity provider) {
        skillLevels = new HashMap<>();
        this.provider = provider;
        // Setup skill levels
        for (Map.Entry<Identifier,Skill> pair : Skills.getSkillRegistry().entrySet()) {
            skillLevels.put(pair.getKey(),0);
        }
    }

    @Override
    public int getValue(Identifier id) {
        return skillLevels.getOrDefault(id,-1);
    }

    @Override
    public void setValue(Identifier id, int val) {
        if (skillLevels.containsKey(id)) {
            skillLevels.put(id,val);
            Skills.SKILL_LIST.sync(this.provider);
        }
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        for (Map.Entry<Identifier,Integer> pair : skillLevels.entrySet()) {
            skillLevels.put(pair.getKey(),compoundTag.getInt(pair.getKey().toString()));
        }
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.provider; // only sync with the provider itself
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        for (Map.Entry<Identifier,Integer> pair : skillLevels.entrySet()) {
            compoundTag.putInt(pair.getKey().toString(),pair.getValue());
        }
    }
}
