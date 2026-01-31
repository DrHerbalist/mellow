package com.example.mellow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class RenderDistanceConfig {

    public int startDistance = 2;
    public int endDistance = 7;
    public int firstIncreaseDelaySeconds = 120;
    public int stepDelaySeconds = 60;
    public int stepSize = 2;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("mellow.json")
                    .toFile();

    public static RenderDistanceConfig load() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();

            if (!CONFIG_FILE.exists()) {
                RenderDistanceConfig config = new RenderDistanceConfig();
                save(config);
                return config;
            }

            RenderDistanceConfig config =
                    GSON.fromJson(new FileReader(CONFIG_FILE), RenderDistanceConfig.class);

            if (config == null) {
                config = new RenderDistanceConfig();
                save(config);
            }

            return config;
        } catch (Exception e) {
            e.printStackTrace();
            return new RenderDistanceConfig();
        }
    }

    public static void save(RenderDistanceConfig config) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
