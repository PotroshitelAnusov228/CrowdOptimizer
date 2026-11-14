package dev.crowdoptimizer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ParticleStatus;

/**
 * Changes only vanilla particle density and entity shadow settings.
 * It deliberately does not modify entity rendering, models, textures,
 * animation ticking, player labels, or name-tag rendering.
 */
public final class CrowdOptimizerClient implements ClientModInitializer {
    private static final int SAMPLE_INTERVAL_TICKS = 20;
    private static final int REQUIRED_SAMPLES = 3;

    private enum Tier {
        HIGH("Высокое качество"),
        BALANCED("Сбалансировано"),
        ECONOMY("Экономия ресурсов");

        private final String label;

        Tier(String label) {
            this.label = label;
        }
    }

    private Tier tier = Tier.BALANCED;
    private int tickCounter;
    private int lowSamples;
    private int highSamples;
    private boolean settingsCaptured;

    private boolean originalEntityShadows;
    private ParticleStatus originalParticles;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(Minecraft client) {
        if (client.level == null) {
            if (settingsCaptured) {
                restoreSettings(client);
            }
            resetState();
            return;
        }

        if (!settingsCaptured) {
            captureSettings(client);
            applyTier(client);
        }

        if (++tickCounter < SAMPLE_INTERVAL_TICKS) {
            return;
        }

        tickCounter = 0;
        updateTier(client, client.getFps());
    }

    private void captureSettings(Minecraft client) {
        originalEntityShadows = client.options.entityShadows().get();
        originalParticles = client.options.particles().get();
        settingsCaptured = true;
    }

    private void updateTier(Minecraft client, int fps) {
        Tier nextTier = tier;

        switch (tier) {
            case HIGH -> {
                lowSamples = fps < 52 ? lowSamples + 1 : 0;
                if (lowSamples >= REQUIRED_SAMPLES) {
                    nextTier = Tier.BALANCED;
                }
            }
            case BALANCED -> {
                lowSamples = fps < 32 ? lowSamples + 1 : 0;
                highSamples = fps > 62 ? highSamples + 1 : 0;

                if (lowSamples >= REQUIRED_SAMPLES) {
                    nextTier = Tier.ECONOMY;
                } else if (highSamples >= REQUIRED_SAMPLES) {
                    nextTier = Tier.HIGH;
                }
            }
            case ECONOMY -> {
                highSamples = fps > 42 ? highSamples + 1 : 0;
                if (highSamples >= REQUIRED_SAMPLES) {
                    nextTier = Tier.BALANCED;
                }
            }
        }

        if (nextTier != tier) {
            tier = nextTier;
            lowSamples = 0;
            highSamples = 0;
            applyTier(client);
            client.gui.getChat().addMessage(
                Component.literal("[CrowdOptimizer] " + tier.label)
            );
        }
    }

    private void applyTier(Minecraft client) {
        switch (tier) {
            case HIGH -> {
                client.options.entityShadows().set(originalEntityShadows);
                client.options.particles().set(originalParticles);
            }
            case BALANCED -> {
                client.options.entityShadows().set(originalEntityShadows);
                client.options.particles().set(
                    originalParticles == ParticleStatus.ALL
                        ? ParticleStatus.DECREASED
                        : originalParticles
                );
            }
            case ECONOMY -> {
                client.options.entityShadows().set(false);
                client.options.particles().set(ParticleStatus.MINIMAL);
            }
        }
    }

    private void restoreSettings(Minecraft client) {
        client.options.entityShadows().set(originalEntityShadows);
        client.options.particles().set(originalParticles);
        settingsCaptured = false;
    }

    private void resetState() {
        tickCounter = 0;
        lowSamples = 0;
        highSamples = 0;
        tier = Tier.BALANCED;
    }
}
