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
import net.owlery.statsystem.capability.PlayerStatCapabilityProvider;

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
    private int jobValueX1, jobValueX2, jobValueY1, jobValueY2;
    private int titleValueX1, titleValueX2, titleValueY1, titleValueY2;

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
        if (button == 0 && isInJobValue((int)mouseX, (int)mouseY)) {
            Minecraft.getInstance().setScreen(new JobListScreen(this));
            return true;
        }
        if (button == 0 && isInTitleValue((int)mouseX, (int)mouseY)) {
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
    private boolean isInJobValue(int mouseX, int mouseY) {
        return mouseX >= jobValueX1 && mouseX <= jobValueX2 && mouseY >= jobValueY1 && mouseY <= jobValueY2;
    }
    private boolean isInTitleValue(int mouseX, int mouseY) {
        return mouseX >= titleValueX1 && mouseX <= titleValueX2 && mouseY >= titleValueY1 && mouseY <= titleValueY2;
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
            int statusY = y + 34;
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
            // Username (left)
            String usernameLabel = "USERNAME: ";
            String usernameValue = this.minecraft.getUser().getName();
            int usernameX = x + 20;
            guiGraphics.drawString(this.font, Component.literal(usernameLabel + usernameValue).plainCopy().withStyle(s -> s.withColor(0xFFFFFF)), usernameX, usernameY, 0xFFFFFF | (alpha << 24));
            // Level (right, big number)
            Player player = this.minecraft.player;
            if (player != null) {
                String levelLabel = "LEVEL: ";
                String levelValue = String.valueOf(player.experienceLevel);
                int levelLabelX = x + currentWidth - 20 - this.font.width(levelLabel) - this.font.width(levelValue) - 8;
                int levelLabelY = usernameY;
                int levelValueX = levelLabelX + this.font.width(levelLabel) + 4;
                // Draw label
                guiGraphics.drawString(this.font, Component.literal(levelLabel), levelLabelX, levelLabelY, 0xFFFFFF | (alpha << 24));
                // Draw even bigger level number
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(levelValueX, levelLabelY - 6, 0);
                guiGraphics.pose().scale(2.2f, 2.2f, 1.0f);
                guiGraphics.drawString(this.font, Component.literal(levelValue), 0, 0, 0xFFFFFF | (alpha << 24));
                guiGraphics.pose().popPose();
                // XP (below level, right-aligned, lime green)
                int xpTotal = player.totalExperience;
                String xpLabel = "XP: ";
                String xpValue = String.valueOf(xpTotal);
                int xpLabelY = usernameY + 16;
                int xpValueX = x + currentWidth - 20 - this.font.width(xpValue);
                int xpLabelX = xpValueX - this.font.width(xpLabel) - 2;
                guiGraphics.drawString(this.font, Component.literal(xpLabel), xpLabelX, xpLabelY, 0xFFFFFF | (alpha << 24));
                guiGraphics.drawString(this.font, Component.literal(xpValue), xpValueX, xpLabelY, 0x32CD32 | (alpha << 24));
            }
            // Title label and value
            String titleLabel = "TITLE: ";
            var cap = player != null ? PlayerStatCapabilityProvider.get(player).orElse(null) : null;
            String titleName = cap != null ? cap.getEquippedTitle() : "Noob";
            int titleLabelX = x + 20;
            int titleLabelY = titleY;
            int titleLabelWidth = this.font.width(titleLabel);
            Integer titleColor = getTitleColor(titleName);
            if (titleColor == null) titleColor = 0xFFFFFF;
            guiGraphics.drawString(this.font, Component.literal(titleLabel).plainCopy().withStyle(s -> s.withColor(0xFFFFFF)), titleLabelX, titleLabelY, 0xFFFFFF | (alpha << 24));
            // Underline if hovered
            boolean titleHovered = mouseX >= titleLabelX + titleLabelWidth && mouseX <= titleLabelX + titleLabelWidth + this.font.width(titleName.toUpperCase()) && mouseY >= titleLabelY && mouseY <= titleLabelY + this.font.lineHeight;
            Component titleComponent = Component.literal(titleName.toUpperCase());
            if (titleHovered) titleComponent = titleComponent.copy().withStyle(s -> s.withUnderlined(true));
            guiGraphics.drawString(this.font, titleComponent, titleLabelX + titleLabelWidth, titleLabelY, titleColor | (alpha << 24));
            // Save value area for hover/click
            titleValueX1 = titleLabelX + titleLabelWidth;
            titleValueX2 = titleLabelX + titleLabelWidth + this.font.width(titleName.toUpperCase());
            titleValueY1 = titleLabelY;
            titleValueY2 = titleLabelY + this.font.lineHeight;
            // Save label area for hover/click
            titleLabelX1 = titleLabelX;
            titleLabelX2 = titleLabelX + titleLabelWidth;
            titleLabelY1 = titleLabelY;
            titleLabelY2 = titleLabelY + this.font.lineHeight;
            // Job label and value
            String jobLabel = "JOB: ";
            String jobName = cap != null ? cap.getEquippedJob() : "Jobless";
            Integer jobColor = getJobColor(jobName);
            if (jobColor == null) jobColor = 0xFFFFFF;
            int jobLabelX = x + 20;
            int jobLabelY = jobY;
            int jobLabelWidth = this.font.width(jobLabel);
            guiGraphics.drawString(this.font, Component.literal(jobLabel).plainCopy().withStyle(s -> s.withColor(0xFFFFFF)), jobLabelX, jobLabelY, 0xFFFFFF | (alpha << 24));
            // Underline if hovered
            boolean jobHovered = mouseX >= jobLabelX + jobLabelWidth && mouseX <= jobLabelX + jobLabelWidth + this.font.width(jobName.toUpperCase()) && mouseY >= jobLabelY && mouseY <= jobLabelY + this.font.lineHeight;
            Component jobComponent = Component.literal(jobName.toUpperCase());
            if (jobHovered) jobComponent = jobComponent.copy().withStyle(s -> s.withUnderlined(true));
            guiGraphics.drawString(this.font, jobComponent, jobLabelX + jobLabelWidth, jobLabelY, jobColor | (alpha << 24));
            // Save value area for hover/click
            jobValueX1 = jobLabelX + jobLabelWidth;
            jobValueX2 = jobLabelX + jobLabelWidth + this.font.width(jobName.toUpperCase());
            jobValueY1 = jobLabelY;
            jobValueY2 = jobLabelY + this.font.lineHeight;
            // Save label area for hover/click
            jobLabelX1 = jobLabelX;
            jobLabelX2 = jobLabelX + jobLabelWidth;
            jobLabelY1 = jobLabelY;
            jobLabelY2 = jobLabelY + this.font.lineHeight;
            // HP line
            if (player != null) {
                int hpLabelX = x + 20;
                int hpLabelY = hpY;
                float currentHp = player.getHealth();
                float maxHp = player.getMaxHealth();
                String hpLabel = "HP: ";
                String hpCurrent = String.valueOf((int) currentHp);
                String hpMax = String.valueOf((int) maxHp);
                int hpColor = 0x00FF00; // green
                float hpPercent = currentHp / maxHp;
                // Draw label
                guiGraphics.drawString(this.font, Component.literal(hpLabel), hpLabelX, hpLabelY, 0xFFFFFF | (alpha << 24));
                // Draw current HP (red if <30%)
                int hpCounterColor;
                if (hpPercent > 0.6f) hpCounterColor = 0x00FF00; // green
                else if (hpPercent > 0.3f) hpCounterColor = 0xFFFF00; // yellow
                else hpCounterColor = 0xFF5555; // red
                guiGraphics.drawString(
                    this.font,
                    Component.literal(hpCurrent),
                    hpLabelX + this.font.width(hpLabel),
                    hpLabelY,
                    hpCounterColor | (alpha << 24)
                );
                // Draw slash and max HP (always green)
                String slashMax = " / " + hpMax;
                guiGraphics.drawString(this.font, Component.literal(slashMax), hpLabelX + this.font.width(hpLabel) + this.font.width(hpCurrent), hpLabelY, hpColor | (alpha << 24));

                // Draw HP bar below HP text
                int barWidth = currentWidth - 40;
                int barHeight = 8;
                int barX = x + 20;
                int barY = hpLabelY + 14;
                int fillWidth = (int) (barWidth * Mth.clamp(hpPercent, 0.0f, 1.0f));
                int barColor;
                if (hpPercent > 0.6f) barColor = 0x00FF00; // green
                else if (hpPercent > 0.3f) barColor = 0xFFFF00; // yellow
                else barColor = 0xFF5555; // red
                int emptyColor = 0xFFCCCCCC; // light gray as in the reference image
                // Draw shadow (slightly larger, semi-transparent gray)
                guiGraphics.fill(barX - 2, barY - 2, barX + barWidth + 2, barY + barHeight + 2, 0x66000000);
                // Draw empty bar (light gray)
                guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, emptyColor);
                // Draw filled bar
                guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, 0xFF000000 | barColor);
                // Draw big white line below the bar
                int lineMargin = 16;
                int lineWidth = barWidth - lineMargin;
                int lineX = barX + lineMargin / 2;
                int lineY = barY + barHeight + 8;
                guiGraphics.fill(lineX, lineY, lineX + lineWidth, lineY + 3, 0xFFFFFFFF);
            }
            // Tooltip logic for job
            if (isInJobValue(mouseX, mouseY)) {
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
            if (isInTitleValue(mouseX, mouseY)) {
                if (titleLabelHoverStart == -1) titleLabelHoverStart = System.currentTimeMillis();
                else if (System.currentTimeMillis() - titleLabelHoverStart > TOOLTIP_DELAY_MS) showTitleTooltip = true;
            } else {
                titleLabelHoverStart = -1;
                showTitleTooltip = false;
            }
            if (showTitleTooltip) {
                guiGraphics.renderTooltip(this.font, Component.literal("Click me to see all possible titles"), mouseX, mouseY + 12);
            }
            // Draw points fields at the bottom
            int pointsY = y + currentHeight - 40;
            if (cap != null) {
                String usedPointsLabel = "Used points: " + cap.getUsedPoints();
                String availablePointsLabel = "Points available: " + cap.getAvailablePoints();
                int usedPointsX = x + 20;
                int availablePointsX = x + 20;
                guiGraphics.drawString(this.font, Component.literal(usedPointsLabel), usedPointsX, pointsY, 0x888888 | (alpha << 24));
                guiGraphics.drawString(this.font, Component.literal(availablePointsLabel), availablePointsX, pointsY + 16, 0xFFFFFF | (alpha << 24));
            }
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
} 