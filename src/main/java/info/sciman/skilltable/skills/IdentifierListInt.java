package info.sciman.skilltable.skills;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.util.Identifier;

public interface IdentifierListInt extends ComponentV3 {
    int getValue(Identifier id);
    void setValue(Identifier id, int val);
}
