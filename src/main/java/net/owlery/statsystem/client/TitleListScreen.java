package net.owlery.statsystem.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.owlery.statsystem.ModTitles;
import net.minecraft.world.entity.player.Player;
import net.owlery.statsystem.capability.PlayerStatCapabilityProvider;
import net.owlery.statsystem.capability.EquipJobTitlePacket;
import net.owlery.statsystem.StatSystemMod;

public class TitleListScreen extends Screen {
    private static final ResourceLocation DEFAULT_GUI_TEXTURE = new ResourceLocation("minecraft", "textures/gui/demo_background.png");
    private final Screen parent;
    private int hoveredTitleIndex = -1;

    public TitleListScreen(Screen parent) {
        super(Component.literal("All Titles"));
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
        int listHeight = 20 + ModTitles.TITLES.length * 18;
        int x = (this.width - listWidth) / 2;
        int y = (this.height - listHeight) / 2;
        
        // Handle clicking outside the list
        if (mouseX < x || mouseX > x + listWidth || mouseY < y || mouseY > y + listHeight) {
            this.onClose();
            return true;
        }

        // Sort titles: unlocked first, then locked
        Player player = this.minecraft.player;
        var cap = player != null ? PlayerStatCapabilityProvider.get(player).orElse(null) : null;
        String[] sortedTitles = ModTitles.TITLES.clone();
        if (cap != null) {
            java.util.Arrays.sort(sortedTitles, (a, b) -> {
                boolean aUnlocked = cap.getUnlockedTitles().contains(a);
                boolean bUnlocked = cap.getUnlockedTitles().contains(b);
                if (aUnlocked == bUnlocked) return a.compareTo(b);
                return aUnlocked ? -1 : 1;
            });
        }

        // Handle left-clicking on a title
        if (button == 0) { // left click
            int titleIndex = (int) ((mouseY - (y + 24)) / 18);
            if (titleIndex >= 0 && titleIndex < sortedTitles.length) {
                if (cap != null) {
                    String title = sortedTitles[titleIndex];
                    if (cap.getUnlockedTitles().contains(title)) {
                        // Send packet to server to equip title
                        StatSystemMod.NETWORK.sendToServer(new EquipJobTitlePacket("title", title));
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
        int listHeight = 20 + ModTitles.TITLES.length * 18;
        int x = (this.width - listWidth) / 2;
        int y = (this.height - listHeight) / 2;
        guiGraphics.blit(DEFAULT_GUI_TEXTURE, x, y, 0, 0, listWidth, listHeight, 248, 166);
        guiGraphics.drawCenteredString(this.font, Component.literal("All Titles"), this.width / 2, y + 6, 0xFFFFFF);
        
        Player player = this.minecraft.player;
        var cap = player != null ? PlayerStatCapabilityProvider.get(player).orElse(null) : null;
        
        // Reset hover state
        hoveredTitleIndex = -1;
        
        // Sort titles: unlocked first, then locked
        String[] sortedTitles = ModTitles.TITLES.clone();
        if (cap != null) {
            java.util.Arrays.sort(sortedTitles, (a, b) -> {
                boolean aUnlocked = cap.getUnlockedTitles().contains(a);
                boolean bUnlocked = cap.getUnlockedTitles().contains(b);
                if (aUnlocked == bUnlocked) return a.compareTo(b);
                return aUnlocked ? -1 : 1;
            });
        }
        
        for (int i = 0; i < sortedTitles.length; i++) {
            String title = sortedTitles[i];
            boolean unlocked = cap != null && cap.getUnlockedTitles().contains(title);
            boolean selected = cap != null && title.equals(cap.getEquippedTitle());
            
            // Check if mouse is hovering over this title
            int titleY = y + 24 + i * 18;
            if (mouseX >= this.width / 2 - 60 && mouseX <= this.width / 2 + 60 &&
                mouseY >= titleY && mouseY < titleY + 18) {
                hoveredTitleIndex = i;
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
            Component titleText = Component.literal(title.toUpperCase() + status);
            
            // Add underline if this is the equipped title
            if (selected) {
                titleText = titleText.copy().withStyle(style -> style.withUnderlined(true));
            }
            
            guiGraphics.drawCenteredString(this.font, titleText, this.width / 2, titleY, color);
        }
        
        // Show tooltip for hovered title
        if (hoveredTitleIndex != -1 && cap != null) {
            String title = sortedTitles[hoveredTitleIndex];
            boolean unlocked = cap.getUnlockedTitles().contains(title);
            String tooltip = unlocked ? "Left-click to equip" : "Locked";
            guiGraphics.renderTooltip(this.font, Component.literal(tooltip), mouseX, mouseY);
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
} 