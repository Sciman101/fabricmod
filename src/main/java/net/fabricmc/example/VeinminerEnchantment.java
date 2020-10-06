package net.fabricmc.example;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;

public class VeinminerEnchantment extends Enchantment {

    protected VeinminerEnchantment() {
        // Setup enchantment properties
        super(Rarity.RARE, EnchantmentTarget.DIGGER, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        return level * 10;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    /**
     * Couldn't make them work together, so they'll be incompatible
     * @param other
     * @return
     */
    @Override
    public boolean canAccept(Enchantment other) {
        return super.canAccept(other) && other != Enchantments.FORTUNE && other != Enchantments.SILK_TOUCH;
    }

}
