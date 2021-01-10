package info.sciman.skilltable.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import info.sciman.skilltable.SkillTableMod;
import info.sciman.skilltable.skills.Skill;
import info.sciman.skilltable.skills.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class SkillScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("skilltable", "textures/gui/container/skill_table.png");

    private ButtonWidget[] skillButtons = new ButtonWidget[3];

    private boolean isScrolling;
    private float scrollPos;
    private int skillIndexOffset;

    public SkillScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        for (int i=0;i<3;i++) {
            skillButtons[i] = this.addButton(new SkillButtonWidget(x+43, y+16 + i * 18, i, LiteralText.EMPTY, button -> {
                if (button instanceof SkillButtonWidget) {
                    // Try and purchase skill upgrade. If we're successful, update the UI
                    int skillIndex = ((SkillButtonWidget) button).index+skillIndexOffset;
                    PlayerEntity player = playerInventory.player;

                    PacketByteBuf buf = PacketByteBufs.create();
                    // Write relevant data
                    buf.writeIdentifier(((SkillScreenHandler)getScreenHandler()).playerQualifiedSkills.get(skillIndex).getIdentifier()); // Identify the skill
                    // Send the packet
                    ClientPlayNetworking.send(SkillTableMod.REQUEST_SKILL_UPGRADE_PACKET_ID,buf);

                    // Refresh
                    updateOffset();
                }
            }));
        }
        updateOffset();
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1,1,1,1);
        client.getTextureManager().bindTexture(TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        // Draw the main background
        drawTexture(matrices,x,y,0,0,backgroundWidth,backgroundHeight);
    }

    private void renderScrollBar(MatrixStack matrices) {
        int xx = 161;
        int yy = 16;
        if (canScroll()) {
            yy += scrollPos * (55 - 17);
            drawTexture(matrices, x + xx, y + yy, this.getZOffset(), 0, 166, 6, 17, 256, 256);
        }else{
            drawTexture(matrices, x + xx, y + yy, this.getZOffset(), 6, 166, 6, 17, 256, 256);
        }
    }

    private boolean canScroll() {
        return ((SkillScreenHandler)handler).playerQualifiedSkills.size() > 3;
    }

    // Used to scroll list
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.isScrolling = false;
        if (canScroll()) {
            int i = (this.width - this.backgroundWidth) / 2;
            int j = (this.height - this.backgroundHeight) / 2;
            if (mouseX > (double)(i + 161) && mouseX < (double)(i + 160 + 6) && mouseY > (double)(j + 16) && mouseY <= (double)(j + 15 + 57 + 1)) {
                this.isScrolling = true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);

    }
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isScrolling) {
            // Get top and bottom of scroll region
            int sTop = y+16;
            int sBot = sTop+57;
            scrollPos = ((float)mouseY - (float)sTop - 7.5F) / ((float)(sBot - sTop) - 17.0F);
            scrollPos = MathHelper.clamp(scrollPos,0,1);
            updateOffset();
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (canScroll()) {
            scrollPos = (float) MathHelper.clamp(scrollPos - amount * 0.1, 0, 1);
            updateOffset();
            return true;
        }return false;
    }
    private void updateOffset() {
        List<Skill> qualifiedSkills = ((SkillScreenHandler)handler).playerQualifiedSkills;
        skillIndexOffset = Math.round(scrollPos * (qualifiedSkills.size() - 3));

        for (int i=0;i<3;i++) {
            int index = skillIndexOffset + i;
            if (index >= qualifiedSkills.size()) {
                skillButtons[i].active = false;
            }else{
                Skill skill = qualifiedSkills.get(index);
                int level = Skills.SKILL_LIST.get(playerInventory.player).getValue(skill.getIdentifier());

                MutableText buttontext = new TranslatableText(skill.getTranslationKey()).append(" ");
                buttontext.append(new TranslatableText(skill.getMaxLevel() == level ? "skilltable.misc.level_max" : ("enchantment.level." + (level+1))));

                skillButtons[i].setMessage(buttontext);
                skillButtons[i].active = !skill.isPlayerMaxLevel(playerInventory.player);
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices,mouseX,mouseY,delta);

        this.client.getTextureManager().bindTexture(TEXTURE);
        renderScrollBar(matrices);

        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }


    @Environment(EnvType.CLIENT)
    class SkillButtonWidget extends ButtonWidget {
        final int index;

        public SkillButtonWidget(int i, int j, int k, Text text, ButtonWidget.PressAction pressAction) {
            super(i, j, 118, 18, text, pressAction);
            this.index = k;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
            if (this.hovered) {
                Skill skill = ((SkillScreenHandler) handler).playerQualifiedSkills.get(this.index+skillIndexOffset);
                int level = Skills.SKILL_LIST.get(playerInventory.player).getValue(skill.getIdentifier());

                // Generate tooltip text
                List<Text> lines = new ArrayList<>();
                lines.add(new TranslatableText(skill.getTooltipTranslationKey()).formatted(Formatting.ITALIC));

                if (level < skill.getMaxLevel()) {
                    lines.add(new TranslatableText("skilltable.misc.level_current").append(new LiteralText(level+"/"+skill.getMaxLevel()).formatted(Formatting.BOLD)));
                    lines.add(new TranslatableText("skilltable.misc.upgrade_cost").append(skill.getLevelCost(level) + "").formatted(Formatting.GREEN));
                }else{
                    lines.add(new TranslatableText("skilltable.misc.level_max").formatted(Formatting.BOLD));
                }

                SkillScreen.this.renderTooltip(matrices, lines, mouseX, mouseY);
            }
        }
    }
}
