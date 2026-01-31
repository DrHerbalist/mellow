package com.example.mellow;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class MellowMod implements ClientModInitializer {

    private RenderDistanceConfig config;

    private boolean inWorld = false;
    private int ticksSinceJoin = 0;
    private int ticksSinceLastStep = 0;
    private int currentDistance;

    private int lastAppliedDistance = -1;

    @Override
    public void onInitializeClient() {
        config = RenderDistanceConfig.load();

        currentDistance = config.startDistance;
        applyRenderDistance(currentDistance);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.world == null || mc.player == null) {
                resetState();
                applyRenderDistance(config.startDistance);
                return;
            }

            // Just joined a world
            if (!inWorld) {
                inWorld = true;
                ticksSinceJoin = 0;
                ticksSinceLastStep = 0;

                currentDistance = config.startDistance;
                applyRenderDistance(currentDistance);
                return;
            }

            ticksSinceJoin++;

            if (currentDistance >= config.endDistance) {
                return;
            }

            int firstDelayTicks = Math.max(0, config.firstIncreaseDelaySeconds) * 20;
            int stepDelayTicks = Math.max(1, config.stepDelaySeconds) * 20;

            if (currentDistance == config.startDistance) {
                if (ticksSinceJoin >= firstDelayTicks) {
                    stepUp();
                }
            } else {
                ticksSinceLastStep++;
                if (ticksSinceLastStep >= stepDelayTicks) {
                    stepUp();
                }
            }
        });
    }

    private void stepUp() {
        ticksSinceLastStep = 0;

        int stepSize = Math.max(1, config.stepSize);
        currentDistance = Math.min(currentDistance + stepSize, config.endDistance);

        applyRenderDistance(currentDistance);
    }

    private void resetState() {
        inWorld = false;
        ticksSinceJoin = 0;
        ticksSinceLastStep = 0;
        currentDistance = config.startDistance;
    }


    private void applyRenderDistance(int chunks) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) return;

        if (chunks == lastAppliedDistance) return;
        lastAppliedDistance = chunks;

        client.execute(() -> {
            try {
                client.options.getViewDistance().setValue(chunks);
                client.options.write();

                if (client.worldRenderer != null) {
                    client.worldRenderer.reload();
                }

                System.out.println("[Mellow] Set render distance to " + chunks);
            } catch (Exception e) {
                System.out.println("[Mellow] Failed to apply render distance: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
