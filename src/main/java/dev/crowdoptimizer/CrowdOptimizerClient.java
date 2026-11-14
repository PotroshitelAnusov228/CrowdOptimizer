package dev.crowdoptimizer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.network.chat.Component;

public final class CrowdOptimizerClient implements ClientModInitializer {
    private static final int SAMPLE_INTERVAL_TICKS = 20;
    private static final int REQUIRED_SAMPLES = 3;

    private enum Tier {
        HIGH("Высокое качество"),
        BALANCED("Сбалансировано"),
        ECONOMY("Экономия ресурсов");

        private final String label;
        Tier(String label) { this.label = label; }
    }

    private Tier tier = Tier.BALANCED;
    private int tickCounter;
    private int belowThresholdSamples;
    private int aboveThresholdSamples;
    private boolean settingsCaptured;
    private double originalEntityDistance;
    private boolean originalEntityShadows;
    private ParticleStatus originalParticles;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(Minecraft client) {
        if (client.level == null) {
            if (settingsCaptured) restoreSettings(client);
            resetState();
            return;
        }
        if (!settingsCaptured) captureSettings(client);
        if (++tickCounter < SAMPLE_INTERVAL_TICKS) return;
        tickCounter = 0;
        updateTier(client, client.getFps());
    }

    private void captureSettings(Minecraft client) {
        originalEntityDistance = client.options.entityDistanceScaling().get();
        originalEntityShadows = client.options.entityShadows().get();
        originalParticles = client.options.particles().get();
        settingsCaptured = true;
    }

    private void updateTier(Minecraft client, int fps) {
        Tier nextTier = tier;
        switch (tier) {
            case HIGH -> {
                belowThresholdSamples = fps < 52 ? belowThresholdSamples + 1 : 0;
                if (belowThresholdSamples >= REQUIRED_SAMPLES) nextTier = Tier.BALANCED;
            }
            case BALANCED -> {
                belowThresholdSamples = fps < 32 ? belowThresholdSamples + 1 : 0;
                aboveThresholdSamples = fps > 62 ? aboveThresholdSamples + 1 : 0;
                if (belowThresholdSamples >= REQUIRED_SAMPLES) nextTier = Tier.ECONOMY;
                else if (aboveThresholdSamples >= REQUIRED_SAMPLES) nextTier = Tier.HIGH;
            }
            case ECONOMY -> {
                aboveThresholdSamples = fps > 42 ? aboveThresholdSamples + 1 : 0;
                if (aboveThresholdSamples >= REQUIRED_SAMPLES) nextTier = Tier.BALANCED;
            }
        }

        if (nextTier != tier) {
            tier = nextTier;
            belowThresholdSamples = 0;
            aboveThresholdSamples = 0;
            applyTier(client);
            client.gui.getChat().addMessage(Component.literal("[CrowdOptimizer] " + tier.label));
        }
    }

    private void applyTier(Minecraft client) {
        switch (tier) {
            case HIGH -> {
                client.options.entityDistanceScaling().set(originalEntityDistance);
                client.options.entityShadows().set(originalEntityShadows);
                client.options.particles().set(originalParticles);
            }
            case BALANCED -> {
                client.options.entityDistanceScaling().set(Math.min(originalEntityDistance, 0.85));
                client.options.entityShadows().set(originalEntityShadows);
                client.options.particles().set(originalParticles);
            }
            case ECONOMY -> {
                client.options.entityDistanceScaling().set(Math.min(originalEntityDistance, 0.65));
                client.options.entityShadows().set(false);
                client.options.particles().set(ParticleStatus.MINIMAL);
            }
        }
    }

    private void restoreSettings(Minecraft client) {
        client.options.entityDistanceScaling().set(originalEntityDistance);
        client.options.entityShadows().set(originalEntityShadows);
        client.options.particles().set(originalParticles);
        settingsCaptured = false;
    }

    private void resetState() {
        tickCounter = 0;
        belowThresholdSamples = 0;
        aboveThresholdSamples = 0;
        tier = Tier.BALANCED;
    }
}
