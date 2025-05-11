package net.owlery.statsystem.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.owlery.statsystem.StatSystemMod;
import net.minecraft.client.Minecraft;
import net.owlery.statsystem.client.JobListScreen;
import net.owlery.statsystem.client.TitleListScreen;
import net.owlery.statsystem.ModTitles;
import net.owlery.statsystem.ModJobs;
import net.minecraft.world.entity.player.Player;

public class StatsMenuScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.of(StatSystemMod.MOD_ID + ":textures/gui/stats_menu.png", ':');
    private static final int TEXTURE_WIDTH = 280;
    private static final int TEXTURE_HEIGHT = 384;
    private static final int TEXTURE_U = 0;
    private static final int TEXTURE_V = 0;
    
    private static final int DEFAULT_TITLE_INDEX = 0;
    private static final int DEFAULT_JOB_INDEX = 0;

    // Animation properties
    private float animationProgress = 0.0f;
    private static final float ANIMATION_SPEED = 5.0f;
    private static final int INITIAL_WIDTH = 280;
    private static final int INITIAL_HEIGHT = 3;
    private static final int FINAL_WIDTH = TEXTURE_WIDTH;
    private static final int FINAL_HEIGHT = TEXTURE_HEIGHT;
    private long openTime;
    private boolean isAnimating = true;

    // Tooltip and click logic
    private long jobLabelHoverStart = -1;
    private boolean showJobTooltip = false;
    private int jobLabelX1, jobLabelX2, jobLabelY1, jobLabelY2;
    private long titleLabelHoverStart = -1;
    private boolean showTitleTooltip = false;
    private int titleLabelX1, titleLabelX2, titleLabelY1, titleLabelY2;
    private static final int TOOLTIP_DELAY_MS = 500;

    public StatsMenuScreen() {
        super(Component.empty());
        this.openTime = System.currentTimeMillis();
    }

    private Integer getTitleColor(String title) {
        if (title.equalsIgnoreCase("Dragon Slayer")) return 0xC084FC; // purple
        if (title.equalsIgnoreCase("Adventurer")) return 0x00FF00; // green
        if (title.equalsIgnoreCase("Noob")) return 0xFFFF00; // yellow
        return null;
    }
    private Integer getJobColor(String job) {
        if (job.equalsIgnoreCase("Jobless")) return 0xAAAAAA; // gray
        return null;
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInJobLabel((int)mouseX, (int)mouseY)) {
            Minecraft.getInstance().setScreen(new JobListScreen(this));
            return true;
        }
        if (button == 0 && isInTitleLabel((int)mouseX, (int)mouseY)) {
            Minecraft.getInstance().setScreen(new TitleListScreen(this));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isInJobLabel(int mouseX, int mouseY) {
        return mouseX >= jobLabelX1 && mouseX <= jobLabelX2 && mouseY >= jobLabelY1 && mouseY <= jobLabelY2;
    }
    private boolean isInTitleLabel(int mouseX, int mouseY) {
        return mouseX >= titleLabelX1 && mouseX <= titleLabelX2 && mouseY >= titleLabelY1 && mouseY <= titleLabelY2;
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
        guiGraphics.blit(TEXTURE, x, y, TEXTURE_U, TEXTURE_V, currentWidth, currentHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        if (animationProgress > 0.5f) {
            float textAlpha = Mth.clamp((animationProgress - 0.5f) * 2.0f, 0.0f, 1.0f);
            int alpha = (int) (textAlpha * 255);
            int statusY = y + 33;
            int usernameY = y + 75;
            int titleY = usernameY + 20;
            int jobY = titleY + 20;
            int hpY = jobY + 20;
            // STATUS
            Component statusText = Component.literal("STATUS").setStyle(Style.EMPTY.withFont(new ResourceLocation("minecraft", "uniform")).withBold(true).withColor(0xFFFFFF));
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(this.width / 2, statusY, 0);
            guiGraphics.pose().scale(2.0f, 2.0f, 1.0f);
            int textWidth = this.font.width(statusText);
            guiGraphics.pose().translate(-textWidth / 2, -this.font.lineHeight / 2, 0);
            guiGraphics.drawString(this.font, statusText, 0, 0, 0xFFFFFF | (alpha << 24));
            guiGraphics.pose().popPose();
            // Username
            String usernameLabel = "USERNAME: ";
            String usernameValue = this.minecraft.getUser().getName();
            guiGraphics.drawString(this.font, Component.literal(usernameLabel + usernameValue).plainCopy().withStyle(s -> s.withColor(0xFFFFFF)), x + 20, usernameY, 0xFFFFFF | (alpha << 24));
            // Title label and value
            String titleLabel = "TITLE: ";
            String titleName = ModTitles.TITLES[DEFAULT_TITLE_INDEX];
            int titleLabelX = x + 20;
            int titleLabelY = titleY;
            int titleLabelWidth = this.font.width(titleLabel);
            Integer titleColor = getTitleColor(titleName);
            if (titleColor == null) titleColor = 0xFFFFFF;
            guiGraphics.drawString(this.font, Component.literal(titleLabel).plainCopy().withStyle(s -> s.withColor(0xFFFFFF)), titleLabelX, titleLabelY, 0xFFFFFF | (alpha << 24));
            guiGraphics.drawString(this.font, Component.literal(titleName.toUpperCase()), titleLabelX + titleLabelWidth, titleLabelY, titleColor | (alpha << 24));
            // Save label area for hover/click
            titleLabelX1 = titleLabelX;
            titleLabelX2 = titleLabelX + titleLabelWidth;
            titleLabelY1 = titleLabelY;
            titleLabelY2 = titleLabelY + this.font.lineHeight;
            // Job label and value
            String jobLabel = "JOB: ";
            String jobName = ModJobs.JOBS[DEFAULT_JOB_INDEX];
            Integer jobColor = getJobColor(jobName);
            if (jobColor == null) jobColor = 0xFFFFFF;
            int jobLabelX = x + 20;
            int jobLabelY = jobY;
            int jobLabelWidth = this.font.width(jobLabel);
            guiGraphics.drawString(this.font, Component.literal(jobLabel).plainCopy().withStyle(s -> s.withColor(0xFFFFFF)), jobLabelX, jobLabelY, 0xFFFFFF | (alpha << 24));
            guiGraphics.drawString(this.font, Component.literal(jobName.toUpperCase()), jobLabelX + jobLabelWidth, jobLabelY, jobColor | (alpha << 24));
            jobLabelX1 = jobLabelX;
            jobLabelX2 = jobLabelX + jobLabelWidth;
            jobLabelY1 = jobLabelY;
            jobLabelY2 = jobLabelY + this.font.lineHeight;
            // HP line
            Player player = this.minecraft.player;
            if (player != null) {
                int hpLabelX = x + 20;
                int hpLabelY = hpY;
                float currentHp = player.getHealth();
                float maxHp = player.getMaxHealth();
                String hpLabel = "HP: ";
                String hpCurrent = String.valueOf((int) currentHp);
                String hpMax = String.valueOf((int) maxHp);
                int hpColor = 0x00FF00; // green
                // Draw label
                guiGraphics.drawString(this.font, Component.literal(hpLabel), hpLabelX, hpLabelY, 0xFFFFFF | (alpha << 24));
                // Draw current HP (red if <30%)
                guiGraphics.drawString(
                    this.font,
                    Component.literal(hpCurrent),
                    hpLabelX + this.font.width(hpLabel),
                    hpLabelY,
                    ((currentHp / maxHp) < 0.3f ? 0xFF5555 : hpColor) | (alpha << 24)
                );
                // Draw slash and max HP (always green)
                String slashMax = " / " + hpMax;
                guiGraphics.drawString(this.font, Component.literal(slashMax), hpLabelX + this.font.width(hpLabel) + this.font.width(hpCurrent), hpLabelY, hpColor | (alpha << 24));
            }
            // Tooltip logic for job
            if (isInJobLabel(mouseX, mouseY)) {
                if (jobLabelHoverStart == -1) jobLabelHoverStart = System.currentTimeMillis();
                else if (System.currentTimeMillis() - jobLabelHoverStart > TOOLTIP_DELAY_MS) showJobTooltip = true;
            } else {
                jobLabelHoverStart = -1;
                showJobTooltip = false;
            }
            if (showJobTooltip) {
                guiGraphics.renderTooltip(this.font, Component.literal("Click me to see all possible jobs"), mouseX, mouseY + 12);
            }
            // Tooltip logic for title
            if (isInTitleLabel(mouseX, mouseY)) {
                if (titleLabelHoverStart == -1) titleLabelHoverStart = System.currentTimeMillis();
                else if (System.currentTimeMillis() - titleLabelHoverStart > TOOLTIP_DELAY_MS) showTitleTooltip = true;
            } else {
                titleLabelHoverStart = -1;
                showTitleTooltip = false;
            }
            if (showTitleTooltip) {
                guiGraphics.renderTooltip(this.font, Component.literal("Click me to see all possible titles"), mouseX, mouseY + 12);
            }
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
} 