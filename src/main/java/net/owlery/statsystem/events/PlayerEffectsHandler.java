package net.owlery.statsystem.events;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.owlery.statsystem.StatSystemMod;
import net.owlery.statsystem.capability.PlayerStatCapabilityProvider;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.UUID;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

@Mod.EventBusSubscriber(modid = StatSystemMod.MOD_ID)
public class PlayerEffectsHandler {
    private static final UUID MARATHON_RUNNER_SPEED_UUID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    private static final String MARATHON_RUNNER_SPEED_NAME = "marathon_runner_speed_boost";
    
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
                var speedAttribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
                if (speedAttribute == null) return;

                // Remove existing modifier if it exists
                speedAttribute.removeModifier(MARATHON_RUNNER_SPEED_UUID);

                // Check for Marathon Runner title
                if (cap.getEquippedTitle().equals("Marathon Runner")) {
                    // Add 20% speed boost
                    AttributeModifier speedModifier = new AttributeModifier(
                        MARATHON_RUNNER_SPEED_UUID,
                        MARATHON_RUNNER_SPEED_NAME,
                        0.2, // 20% boost
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                    );
                    speedAttribute.addTransientModifier(speedModifier);
                }

                // Handle Miner job Haste effect
                player.removeEffect(MobEffects.DIG_SPEED); // Always remove first
                if (cap.getEquippedJob().equals("Miner")) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, -1, 0, true, false));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
                // Check for Doctor title or job
                if (cap.getEquippedTitle().equals("Doctor") || cap.getEquippedJob().equals("Doctor")) {
                    // Increase healing by 15%
                    float newAmount = event.getAmount() * 1.15f;
                    event.setAmount(newAmount);
                }
            });
        }
    }
} 