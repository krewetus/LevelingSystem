package net.owlery.statsystem.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.owlery.statsystem.StatSystemMod;
import net.minecraft.client.Minecraft;

public class StatsMenuScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.of(StatSystemMod.MOD_ID + ":textures/gui/stats_menu.png", ':');
    private static final int TEXTURE_WIDTH = 220;
    private static final int TEXTURE_HEIGHT = 384;
    private static final int TEXTURE_U = 0;
    private static final int TEXTURE_V = 0;
    
    // Animation properties
    private float animationProgress = 0.0f;
    private static final float ANIMATION_SPEED = 5.0f;
    private static final int INITIAL_WIDTH = 220;
    private static final int INITIAL_HEIGHT = 3;
    private static final int FINAL_WIDTH = TEXTURE_WIDTH;
    private static final int FINAL_HEIGHT = TEXTURE_HEIGHT;
    private long openTime;
    private boolean isAnimating = true;

    public StatsMenuScreen() {
        super(Component.empty());
        this.openTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_SEMICOLON) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        
        if (isAnimating) {
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - openTime) / 1000.0f;
            animationProgress = Mth.clamp(deltaTime * ANIMATION_SPEED, 0.0f, 1.0f);
            
            if (animationProgress >= 1.0f) {
                isAnimating = false;
            }
        }
        
        int currentWidth = INITIAL_WIDTH;
        int currentHeight = (int) Mth.lerp(animationProgress, INITIAL_HEIGHT, FINAL_HEIGHT);
        
        int x = (this.width - currentWidth) / 2;
        int y = (this.height - currentHeight) / 2;
        
        guiGraphics.blit(TEXTURE, 
            x, y, 
            TEXTURE_U, TEXTURE_V, 
            currentWidth, currentHeight,
            TEXTURE_WIDTH, TEXTURE_HEIGHT);

        if (animationProgress > 0.5f) {
            float textAlpha = Mth.clamp((animationProgress - 0.5f) * 2.0f, 0.0f, 1.0f);
            int alpha = (int) (textAlpha * 255);
            
            // Fixed Y positions from the top of the menu
            int statusY = y + 50;      // Center between the two blue lines
            int usernameY = y + 75;    // Below STATUS
            
            // Draw "STATUS" with larger text, centered
            Component statusText = Component.literal("STATUS")
                .setStyle(Style.EMPTY
                    .withFont(new ResourceLocation("minecraft", "uniform"))
                    .withBold(true)
                    .withColor(0xFFFFFF));
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(this.width / 2, statusY, 0);
            guiGraphics.pose().scale(2.0f, 2.0f, 1.0f);
            int textWidth = this.font.width(statusText);
            guiGraphics.pose().translate(-textWidth / 2, -this.font.lineHeight / 2, 0);
            guiGraphics.drawString(this.font, statusText, 0, 0, 0xFFFFFF | (alpha << 24));
            guiGraphics.pose().popPose();

            // Draw username below STATUS, left-aligned with a margin
            guiGraphics.drawString(this.font,
                Component.literal("Username: " + this.minecraft.getUser().getName()),
                x + 20,
                usernameY,
                0xFFFFFF | (alpha << 24));
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }
} 