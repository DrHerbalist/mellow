package com.example.mellow;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

public class MellowMod implements ClientModInitializer {

    private RenderDistanceConfig config;

    private boolean inWorld = false;
    private int ticksSinceJoin = 0;
    private int ticksSinceLastStep = 0;
    private int currentDistance;

    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        config = RenderDistanceConfig.load();

        currentDistance = config.startDistance;
        setRenderDistance(client, currentDistance);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.world == null || mc.player == null) {
                reset(mc);
                return;
            }

            if (!inWorld) {
                inWorld = true;
                ticksSinceJoin = 0;
                ticksSinceLastStep = 0;
                currentDistance = config.startDistance;
                setRenderDistance(mc, currentDistance);
            }

            ticksSinceJoin++;

            if (currentDistance >= config.endDistance) return;

            int firstDelayTicks = config.firstIncreaseDelaySeconds * 20;
            int stepDelayTicks = config.stepDelaySeconds * 20;

            if (currentDistance == config.startDistance) {
                if (ticksSinceJoin >= firstDelayTicks) {
                    stepUp(mc);
                }
            } else {
                ticksSinceLastStep++;
                if (ticksSinceLastStep >= stepDelayTicks) {
                    stepUp(mc);
                }
            }
        });
    }

    private void stepUp(MinecraftClient mc) {
        ticksSinceLastStep = 0;
        currentDistance = Math.min(
                currentDistance + config.stepSize,
                config.endDistance
        );
        setRenderDistance(mc, currentDistance);
    }

    private void reset(MinecraftClient mc) {
        inWorld = false;
        ticksSinceJoin = 0;
        ticksSinceLastStep = 0;
        currentDistance = config.startDistance;
        setRenderDistance(mc, currentDistance);
    }

    private void setRenderDistance(MinecraftClient client, int distance) {
        GameOptions options = client.options;
        if (options.getViewDistance().getValue() != distance) {
            options.getViewDistance().setValue(distance);
            options.write();
        }
    }
}
