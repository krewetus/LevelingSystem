package net.owlery.statsystem.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.owlery.statsystem.ModJobs;

public class JobListScreen extends Screen {
    private static final ResourceLocation DEFAULT_GUI_TEXTURE = new ResourceLocation("minecraft", "textures/gui/demo_background.png");
    private final Screen parent;

    public JobListScreen(Screen parent) {
        super(Component.literal("All Jobs"));
        this.parent = parent;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listWidth = 120;
        int listHeight = 20 + ModJobs.JOBS.length * 18;
        int x = (this.width - listWidth) / 2;
        int y = (this.height - listHeight) / 2;
        if (mouseX < x || mouseX > x + listWidth || mouseY < y || mouseY > y + listHeight) {
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int listWidth = 120;
        int listHeight = 20 + ModJobs.JOBS.length * 18;
        int x = (this.width - listWidth) / 2;
        int y = (this.height - listHeight) / 2;
        guiGraphics.blit(DEFAULT_GUI_TEXTURE, x, y, 0, 0, listWidth, listHeight, 248, 166);
        guiGraphics.drawCenteredString(this.font, Component.literal("All Jobs"), this.width / 2, y + 6, 0xFFFFFF);
        for (int i = 0; i < ModJobs.JOBS.length; i++) {
            guiGraphics.drawCenteredString(this.font, Component.literal(ModJobs.JOBS[i]), this.width / 2, y + 24 + i * 18, 0xFFFFFF);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
} 