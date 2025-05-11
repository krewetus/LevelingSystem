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
import net.owlery.statsystem.capability.UpgradeStatPacket;
import net.owlery.statsystem.capability.BuyPointPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;

public class StatsMenuScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(StatSystemMod.MOD_ID, "textures/gui/stats_menu.png");
    private static final ResourceLocation UPGRADE_BUTTON = new ResourceLocation(StatSystemMod.MOD_ID, "textures/gui/upgrade_button.png");
    private static final ResourceLocation UPGRADE_BUTTON_DISABLED = new ResourceLocation(StatSystemMod.MOD_ID, "textures/gui/upgrade_button-disabled.png");
    private static final ResourceLocation BUY_POINTS_BUTTON = new ResourceLocation(StatSystemMod.MOD_ID, "textures/gui/buy_button.png");
    private static final ResourceLocation UPGRADE_BUTTON_HOVER = new ResourceLocation(StatSystemMod.MOD_ID, "textures/gui/upgrade_button-hover.png");
    private static final ResourceLocation BUY_POINTS_BUTTON_HOVER = new ResourceLocation(StatSystemMod.MOD_ID, "textures/gui/buy_button-hover.png");
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

    // Stat button hitboxes
    private int healthPlusX1, healthPlusX2, healthPlusY1, healthPlusY2;
    private int strengthPlusX1, strengthPlusX2, strengthPlusY1, strengthPlusY2;
    private int agilityPlusX1, agilityPlusX2, agilityPlusY1, agilityPlusY2;
    private int charismaPlusX1, charismaPlusX2, charismaPlusY1, charismaPlusY2;

    // Buy point button hitbox
    private int buyPointX1, buyPointX2, buyPointY1, buyPointY2;

    private int x;
    private int y;
    private int currentWidth;
    private int currentHeight;
    private net.owlery.statsystem.capability.PlayerStatCapability capability;

    public StatsMenuScreen() {
        super(Component.literal("Stats Menu"));
        this.openTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();
        x = (width - TEXTURE_WIDTH) / 2;
        y = (height - TEXTURE_HEIGHT) / 2;
        currentWidth = TEXTURE_WIDTH;
        currentHeight = TEXTURE_HEIGHT;
        capability = PlayerStatCapabilityProvider.get(minecraft.player).orElse(null);
    }

    private Integer getTitleColor(String title) {
        if (title.equalsIgnoreCase("Dragon Slayer")) return 0xC084FC; // purple
        if (title.equalsIgnoreCase("Adventurer")) return 0x00FF00; // green
        if (title.equalsIgnoreCase("Noob")) return 0xFFFF00; // yellow
        if (title.equalsIgnoreCase("Marathon Runner")) return 0x7EC8E3; // light blue
        return null;
    }
    private Integer getJobColor(String job) {
        if (job.equalsIgnoreCase("None")) return 0xAAAAAA; // gray
        if (job.equalsIgnoreCase("Trader")) return 0x80EF80; // lightgreen
        if (job.equalsIgnoreCase("Hunter")) return 0xFFD700; // dark yellow
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
        int plusButtonSize = 16; // Ensure this is defined for use in hitbox calculations
        if (button == 0 && isInJobValue((int)mouseX, (int)mouseY)) {
            Minecraft.getInstance().setScreen(new JobListScreen(this));
            return true;
        }
        if (button == 0 && isInTitleValue((int)mouseX, (int)mouseY)) {
            Minecraft.getInstance().setScreen(new TitleListScreen(this));
            return true;
        }
        if (button == 0) {
            int buyButtonX = x + currentWidth - 28 - 20;
            int buyButtonY = y + currentHeight - 28 - 20;
            
            // Buy point button
            if (mouseX >= buyButtonX && mouseX < buyButtonX + 28 &&
                mouseY >= buyButtonY && mouseY < buyButtonY + 28) {
                StatSystemMod.NETWORK.sendToServer(new BuyPointPacket());
                return true;
            }
            
            // Stat upgrade buttons (only visible button area, no invisible hitbox)
            if (mouseX >= healthPlusX1 && mouseX < healthPlusX2 && mouseY >= healthPlusY1 && mouseY < healthPlusY2) {
                if (capability != null && capability.getAvailablePoints() > 0 && capability.getHealthStat() < 100) {
                    StatSystemMod.NETWORK.sendToServer(new UpgradeStatPacket("health"));
                    return true;
                }
            }
            if (mouseX >= strengthPlusX1 && mouseX < strengthPlusX2 && mouseY >= strengthPlusY1 && mouseY < strengthPlusY2) {
                if (capability != null && capability.getAvailablePoints() > 0 && capability.getStrengthStat() < 100) {
                    StatSystemMod.NETWORK.sendToServer(new UpgradeStatPacket("strength"));
                    return true;
                }
            }
            if (mouseX >= agilityPlusX1 && mouseX < agilityPlusX2 && mouseY >= agilityPlusY1 && mouseY < agilityPlusY2) {
                if (capability != null && capability.getAvailablePoints() > 0 && capability.getAgilityStat() < 100) {
                    StatSystemMod.NETWORK.sendToServer(new UpgradeStatPacket("agility"));
                    return true;
                }
            }
            if (mouseX >= charismaPlusX1 && mouseX < charismaPlusX2 && mouseY >= charismaPlusY1 && mouseY < charismaPlusY2) {
                if (capability != null && capability.getAvailablePoints() > 0 && capability.getCharismaStat() < 100) {
                    StatSystemMod.NETWORK.sendToServer(new UpgradeStatPacket("charisma"));
                    return true;
                }
            }
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
            // Level and XP (move back to right, but shift 8px left)
            Player player = this.minecraft.player;
            if (player != null) {
                String levelLabel = "LEVEL: ";
                String levelValue = String.valueOf(player.experienceLevel);
                int levelLabelX = x + currentWidth - 20 - this.font.width(levelLabel) - this.font.width(levelValue) - 8 - 8; // shift 8px left
                int levelLabelY = usernameY;
                int levelValueX = levelLabelX + this.font.width(levelLabel) + 4;
                // Draw label
                guiGraphics.drawString(this.font, Component.literal(levelLabel), levelLabelX, levelLabelY, 0xFFFFFF | (alpha << 24));
                // Draw level number (big)
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
                int xpValueX = x + currentWidth - 20 - this.font.width(xpValue) - 8; // shift 8px left
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
            String jobName = cap != null ? cap.getEquippedJob() : "None";
            if (jobName.equals("Jobless")) jobName = "None";
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
                float absorptionHp = player.getAbsorptionAmount();
                String hpLabel = "HP: ";
                String hpCurrent;
                if (absorptionHp > 0) {
                    String base = String.valueOf((int) currentHp);
                    String absorb = "+" + (int)absorptionHp;
                    hpCurrent = base + "§e" + absorb + "§r";
                } else {
                    hpCurrent = String.valueOf((int) currentHp);
                }
                String hpMax = String.valueOf((int) maxHp);
                int hpColor = 0x00FF00; // green
                float hpPercent = currentHp / maxHp;
                float absorptionPercent = absorptionHp > 0 ? Math.min(absorptionHp / maxHp, 1.0f) : 0.0f;
                // Draw label
                guiGraphics.drawString(this.font, Component.literal(hpLabel), hpLabelX, hpLabelY, 0xFFFFFF | (alpha << 24));
                // Draw current HP (red if <30%)
                int hpCounterColor;
                if (hpPercent > 0.6f) hpCounterColor = 0x00FF00; // green
                else if (hpPercent > 0.3f) hpCounterColor = 0xFFFF00; // yellow
                else hpCounterColor = 0xFF5555; // red
                int baseHpWidth = this.font.width(String.valueOf((int) currentHp));
                guiGraphics.drawString(
                    this.font,
                    Component.literal(String.valueOf((int) currentHp)),
                    hpLabelX + this.font.width(hpLabel),
                    hpLabelY,
                    hpCounterColor | (alpha << 24)
                );
                if (absorptionHp > 0) {
                    guiGraphics.drawString(
                        this.font,
                        Component.literal("+" + (int)absorptionHp),
                        hpLabelX + this.font.width(hpLabel) + baseHpWidth,
                        hpLabelY,
                        0xFFFF55 | (alpha << 24) // yellow
                    );
                }
                // Draw slash and max HP (always green)
                String slashMax = " / " + hpMax;
                int hpCurrentWidth = this.font.width(String.valueOf((int) currentHp)) + (absorptionHp > 0 ? this.font.width("+" + (int)absorptionHp) : 0);
                guiGraphics.drawString(this.font, Component.literal(slashMax), hpLabelX + this.font.width(hpLabel) + hpCurrentWidth, hpLabelY, hpColor | (alpha << 24));

                // Draw HP bar below HP text
                int barWidth = currentWidth - 40;
                int barHeight = 8;
                int barX = x + 20;
                int barY = hpLabelY + 14;
                int fillWidth = (int) (barWidth * Mth.clamp(hpPercent, 0.0f, 1.0f));
                int absorptionFillWidth = (int) (barWidth * Mth.clamp((currentHp + absorptionHp) / maxHp, 0.0f, 1.0f));
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
                // Draw absorption bar (light yellow)
                if (absorptionHp > 0 && absorptionFillWidth > fillWidth) {
                    int absorptionColor = 0xFFFF55; // bright yellow
                    guiGraphics.fill(barX + fillWidth, barY, barX + absorptionFillWidth, barY + barHeight, absorptionColor);
                }
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
            // Draw buy-point button in bottom right corner
            int buyButtonSize = 28;
            int buyButtonX = x + currentWidth - buyButtonSize - 20; // Move to right side
            int buyButtonY = y + currentHeight - buyButtonSize - 30; // Move up by 10 pixels
            buyPointX1 = buyButtonX;
            buyPointX2 = buyButtonX + buyButtonSize;
            buyPointY1 = buyButtonY;
            buyPointY2 = buyButtonY + buyButtonSize;
            int xpCost = 100 + 50 * (cap != null ? cap.getPointsPurchased() : 0);
            boolean canBuy = canAffordXP(xpCost);
            drawBigPlusButton(guiGraphics, buyButtonX, buyButtonY, buyButtonSize, canBuy, mouseX, mouseY);
            // Draw XP cost above the button, now split into two lines
            String nextCostLabel = "Next cost:";
            String nextCostValue = "-" + xpCost + " XP";
            int nextCostLabelX = x + currentWidth - this.font.width(nextCostLabel) - 20; // right align label
            int nextCostY = buyButtonY - 28; // move label up a bit for two lines
            guiGraphics.drawString(this.font, Component.literal(nextCostLabel), nextCostLabelX, nextCostY, 0xFFFFFF);
            int nextCostValueX = x + currentWidth - this.font.width(nextCostValue) - 20; // right align value
            int nextCostValueY = nextCostY + 12;
            guiGraphics.drawString(this.font, Component.literal(nextCostValue), nextCostValueX, nextCostValueY, canBuy ? 0x00FF00 : 0x888888);
            // Draw points text: Used points above Points available, both left-aligned
            int usedPointsX = x + 20;
            int usedPointsY = nextCostY;
            int availablePointsX = x + 20;
            int availablePointsY = usedPointsY + 16;
            if (cap != null) {
                String usedPointsLabel = "Used points: " + cap.getUsedPoints();
                String availablePointsLabel = "Points available: " + cap.getAvailablePoints();
                guiGraphics.drawString(this.font, Component.literal(usedPointsLabel), usedPointsX, usedPointsY, 0x888888 | (alpha << 24));
                guiGraphics.drawString(this.font, Component.literal(availablePointsLabel), availablePointsX, availablePointsY, 0xFFFFFF | (alpha << 24));
            }
            // Draw stats below the buy-point button and above the points fields
            int statsY = y + currentHeight - 170; // Move stats up by 50 pixels
            int statLabelX = x + 4; // Move stats section 20px more to the left
            int statValueX = statLabelX + 70; // Move value closer to label
            int statColSpacing = 128; // Move right column further right
            int plusButtonSize = 16;
            if (cap != null) {
                // Find the max width of all stat values for alignment
                int maxStatValueWidth = Math.max(
                    Math.max(
                        Math.max(
                            this.font.width(String.format("%3d", cap.getHealthStat())),
                            this.font.width(String.format("%3d", cap.getStrengthStat()))
                        ),
                        this.font.width(String.format("%3d", cap.getAgilityStat()))
                    ),
                    this.font.width(String.format("%3d", cap.getCharismaStat()))
                );
                // Health (left column, right-aligned, aligned colon)
                String healthLabel = "HEALTH:";
                String healthValue = String.format("%3d", cap.getHealthStat());
                int healthRightX = statLabelX + 98;
                int healthLabelWidth = this.font.width(healthLabel);
                int healthTextX = healthRightX - healthLabelWidth - maxStatValueWidth - 4;
                guiGraphics.drawString(this.font, Component.literal(healthLabel), healthTextX, statsY, 0xFFFFFF | (alpha << 24));
                guiGraphics.drawString(this.font, Component.literal(healthValue), healthTextX + healthLabelWidth + 8 + (maxStatValueWidth - this.font.width(healthValue)), statsY, 0xFFFFFF | (alpha << 24));
                int healthPlusY = statsY + this.font.lineHeight / 2 - plusButtonSize / 2;
                healthPlusX1 = healthRightX + 7;
                healthPlusX2 = healthPlusX1 + plusButtonSize;
                healthPlusY1 = healthPlusY;
                healthPlusY2 = healthPlusY1 + plusButtonSize;
                boolean canUpgradeHealth = cap.getHealthStat() < 100 && cap.getAvailablePoints() > 0;
                drawStatPlusButton(guiGraphics, healthPlusX1, healthPlusY1, plusButtonSize, canUpgradeHealth, mouseX, mouseY);
                // Strength (left column, below health, aligned colon)
                String strengthLabel = "STRENGTH:";
                String strengthValue = String.format("%3d", cap.getStrengthStat());
                int strengthRightX = statLabelX + 98;
                int strengthLabelWidth = this.font.width(strengthLabel);
                int strengthTextX = strengthRightX - strengthLabelWidth - maxStatValueWidth - 4;
                int strengthY = statsY + 20;
                int strengthPlusY = strengthY + this.font.lineHeight / 2 - plusButtonSize / 2;
                guiGraphics.drawString(this.font, Component.literal(strengthLabel), strengthTextX, strengthY, 0xFFFFFF | (alpha << 24));
                guiGraphics.drawString(this.font, Component.literal(strengthValue), strengthTextX + strengthLabelWidth + 8 + (maxStatValueWidth - this.font.width(strengthValue)), strengthY, 0xFFFFFF | (alpha << 24));
                strengthPlusX1 = strengthRightX + 7;
                strengthPlusX2 = strengthPlusX1 + plusButtonSize;
                strengthPlusY1 = strengthPlusY;
                strengthPlusY2 = strengthPlusY1 + plusButtonSize;
                boolean canUpgradeStrength = cap.getStrengthStat() < 100 && cap.getAvailablePoints() > 0;
                drawStatPlusButton(guiGraphics, strengthPlusX1, strengthPlusY1, plusButtonSize, canUpgradeStrength, mouseX, mouseY);
                // Agility (right column, aligned colon)
                String agilityLabel = "AGILITY:";
                String agilityValue = String.format("%3d", cap.getAgilityStat());
                int agilityLabelX = statLabelX + statColSpacing;
                int agilityRightX = agilityLabelX + 98;
                int agilityLabelWidth = this.font.width(agilityLabel);
                int agilityTextX = agilityRightX - agilityLabelWidth - maxStatValueWidth - 4;
                int agilityPlusY = statsY + this.font.lineHeight / 2 - plusButtonSize / 2;
                guiGraphics.drawString(this.font, Component.literal(agilityLabel), agilityTextX, statsY, 0xFFFFFF | (alpha << 24));
                guiGraphics.drawString(this.font, Component.literal(agilityValue), agilityTextX + agilityLabelWidth + 8 + (maxStatValueWidth - this.font.width(agilityValue)), statsY, 0xFFFFFF | (alpha << 24));
                agilityPlusX1 = agilityRightX + 7;
                agilityPlusX2 = agilityPlusX1 + plusButtonSize;
                agilityPlusY1 = agilityPlusY;
                agilityPlusY2 = agilityPlusY1 + plusButtonSize;
                boolean canUpgradeAgility = cap.getAgilityStat() < 100 && cap.getAvailablePoints() > 0;
                drawStatPlusButton(guiGraphics, agilityPlusX1, agilityPlusY1, plusButtonSize, canUpgradeAgility, mouseX, mouseY);
                // Charisma (right column, below agility, aligned colon)
                String charismaLabel = "CHARISMA:";
                String charismaValue = String.format("%3d", cap.getCharismaStat());
                int charismaLabelX = statLabelX + statColSpacing;
                int charismaRightX = charismaLabelX + 98;
                int charismaLabelWidth = this.font.width(charismaLabel);
                int charismaTextX = charismaRightX - charismaLabelWidth - maxStatValueWidth - 4;
                int charismaY = statsY + 20;
                int charismaPlusY = charismaY + this.font.lineHeight / 2 - plusButtonSize / 2;
                guiGraphics.drawString(this.font, Component.literal(charismaLabel), charismaTextX, charismaY, 0xFFFFFF | (alpha << 24));
                guiGraphics.drawString(this.font, Component.literal(charismaValue), charismaTextX + charismaLabelWidth + 8 + (maxStatValueWidth - this.font.width(charismaValue)), charismaY, 0xFFFFFF | (alpha << 24));
                charismaPlusX1 = charismaRightX + 7;
                charismaPlusX2 = charismaPlusX1 + plusButtonSize;
                charismaPlusY1 = charismaPlusY;
                charismaPlusY2 = charismaPlusY1 + plusButtonSize;
                boolean canUpgradeCharisma = cap.getCharismaStat() < 100 && cap.getAvailablePoints() > 0;
                drawStatPlusButton(guiGraphics, charismaPlusX1, charismaPlusY1, plusButtonSize, canUpgradeCharisma, mouseX, mouseY);
            }
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean canAffordStat(net.owlery.statsystem.capability.PlayerStatCapability cap) {
        int used = cap.getUsedPoints();
        int cost = 100;
        for (int i = 0; i < used; i++) cost = (int) (Math.round(cost * 1.3 + 50) / 100.0) * 100;
        Player player = this.minecraft.player;
        return player != null && player.totalExperience >= cost;
    }

    private boolean canAffordXP(int xpCost) {
        Player player = this.minecraft.player;
        return player != null && player.totalExperience >= xpCost;
    }

    private void drawPlusButton(GuiGraphics guiGraphics, int x, int y, int size, boolean enabled, int mouseX, int mouseY) {
        int color = enabled ? 0xFFFFFF : 0x888888;
        guiGraphics.fill(x, y, x + size, y + size, 0xFF000000 | color);
        guiGraphics.drawString(this.font, Component.literal("+"), x + size / 4, y + size / 8, color);
    }

    private void drawBigPlusButton(GuiGraphics guiGraphics, int x, int y, int size, boolean enabled, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        boolean hovered = enabled && mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        ResourceLocation buttonTexture = enabled ? (hovered ? BUY_POINTS_BUTTON_HOVER : BUY_POINTS_BUTTON) : UPGRADE_BUTTON_DISABLED;
        RenderSystem.setShaderTexture(0, buttonTexture);
        guiGraphics.blit(buttonTexture, x, y, 0, 0, size, size, size, size);
    }

    private void drawStatPlusButton(GuiGraphics guiGraphics, int x, int y, int size, boolean enabled, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        boolean hovered = enabled && mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        ResourceLocation buttonTexture = enabled ? (hovered ? UPGRADE_BUTTON_HOVER : UPGRADE_BUTTON) : UPGRADE_BUTTON_DISABLED;
        RenderSystem.setShaderTexture(0, buttonTexture);
        guiGraphics.blit(buttonTexture, x, y, 0, 0, size, size, size, size);
    }
} 