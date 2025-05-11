package net.owlery.statsystem.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.owlery.statsystem.ModJobs;
import net.minecraft.world.entity.player.Player;
import net.owlery.statsystem.capability.PlayerStatCapabilityProvider;
import net.owlery.statsystem.capability.EquipJobTitlePacket;
import net.owlery.statsystem.StatSystemMod;

public class JobListScreen extends Screen {
    private static final ResourceLocation DEFAULT_GUI_TEXTURE = new ResourceLocation("minecraft", "textures/gui/demo_background.png");
    private final Screen parent;
    private int hoveredJobIndex = -1;

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
        
        // Handle clicking outside the list
        if (mouseX < x || mouseX > x + listWidth || mouseY < y || mouseY > y + listHeight) {
            this.onClose();
            return true;
        }

        // Handle left-clicking on a job
        if (button == 0) { // Left click
            int jobIndex = (int) ((mouseY - (y + 24)) / 18);
            if (jobIndex >= 0 && jobIndex < ModJobs.JOBS.length) {
                Player player = this.minecraft.player;
                var cap = player != null ? PlayerStatCapabilityProvider.get(player).orElse(null) : null;
                if (cap != null) {
                    String job = ModJobs.JOBS[jobIndex];
                    if (cap.getUnlockedJobs().contains(job)) {
                        // Send packet to server to equip job
                        StatSystemMod.NETWORK.sendToServer(new EquipJobTitlePacket("job", job));
                    }
                }
                return true;
            }
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
        
        Player player = this.minecraft.player;
        var cap = player != null ? PlayerStatCapabilityProvider.get(player).orElse(null) : null;
        
        // Reset hover state
        hoveredJobIndex = -1;
        
        for (int i = 0; i < ModJobs.JOBS.length; i++) {
            String job = ModJobs.JOBS[i];
            boolean unlocked = cap != null && cap.getUnlockedJobs().contains(job);
            boolean selected = cap != null && job.equals(cap.getEquippedJob());
            
            // Check if mouse is hovering over this job
            int jobY = y + 24 + i * 18;
            if (mouseX >= this.width / 2 - 60 && mouseX <= this.width / 2 + 60 &&
                mouseY >= jobY && mouseY < jobY + 18) {
                hoveredJobIndex = i;
            }
            
            String status;
            if (!unlocked) {
                status = " [✖]";
            } else if (selected) {
                status = " [✔]";
            } else {
                status = "";
            }
            
            int color = unlocked ? 0xFFFFFF : 0x888888;
            Component jobText = Component.literal(job.toUpperCase() + status);
            
            // Add underline if this is the equipped job
            if (selected) {
                jobText = jobText.copy().withStyle(style -> style.withUnderlined(true));
            }
            
            guiGraphics.drawCenteredString(this.font, jobText, this.width / 2, jobY, color);
        }
        
        // Show tooltip for hovered job
        if (hoveredJobIndex != -1 && cap != null) {
            String job = ModJobs.JOBS[hoveredJobIndex];
            boolean unlocked = cap.getUnlockedJobs().contains(job);
            String tooltip = unlocked ? "Left-click to equip" : "Locked";
            guiGraphics.renderTooltip(this.font, Component.literal(tooltip), mouseX, mouseY);
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
} 