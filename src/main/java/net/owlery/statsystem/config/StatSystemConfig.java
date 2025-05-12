package net.owlery.statsystem.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class StatSystemConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue BASE_XP_COST;
    public static final ForgeConfigSpec.IntValue XP_INCREMENT;
    public static final ForgeConfigSpec.IntValue LEVEL_CAP;

    static {
        BUILDER.push("Stat System Configuration");

        BASE_XP_COST = BUILDER
            .comment("Base XP cost for buying a stat point")
            .defineInRange("baseXpCost", 100, 1, Integer.MAX_VALUE);

        XP_INCREMENT = BUILDER
            .comment("XP cost increment per purchased point")
            .defineInRange("xpIncrement", 50, 0, Integer.MAX_VALUE);

        LEVEL_CAP = BUILDER
            .comment("Maximum level for each stat")
            .defineInRange("levelCap", 100, 1, Integer.MAX_VALUE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "statsystem-common.toml");
    }
} 