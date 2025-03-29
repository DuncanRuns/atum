package me.voidxwalker.autoreset;

import me.voidxwalker.autoreset.api.seedprovider.AtumWaitingScreen;
import me.voidxwalker.autoreset.api.seedprovider.SeedProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class Atum implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final boolean HAS_WORLDPREVIEW = FabricLoader.getInstance().isModLoaded("worldpreview");

    public static AtumConfig config;
    public static KeyBinding resetKey;

    private static boolean running = false;
    private static boolean shouldReset;

    private static final SeedProvider DEFAULT_SEED_PROVIDER = () -> CompletableFuture.completedFuture(Atum.config.seed);
    private static SeedProvider seedProvider = DEFAULT_SEED_PROVIDER;

    public static CompletableFuture<String> currentSeedFuture = null;

    public static void createNewWorld() {
        running = true;
        shouldReset = false;

        MinecraftClient.getInstance().openScreen(new AtumCreateWorldScreen(null));
    }

    public static boolean isRunning() {
        return running;
    }

    public static void stopRunning() {
        shouldReset = false;
        running = false;
        config.dataPackMismatch = false;
    }

    public static void scheduleReset() {
        if (!(MinecraftClient.getInstance().currentScreen instanceof AtumWaitingScreen)) {
            shouldReset = true;
        }
    }

    public static boolean isResetScheduled() {
        return shouldReset;
    }

    public static boolean shouldReset() {
        return isResetScheduled() && !isBlocking();
    }

    public static boolean isBlocking() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.getOverlay() != null || isLoadingWorld() || client.currentScreen instanceof AtumWaitingScreen;
    }

    public static boolean isInWorld() {
        return MinecraftClient.getInstance().world != null;
    }

    public static boolean isLoadingWorld() {
        return MinecraftClient.getInstance().getServer() != null && MinecraftClient.getInstance().world == null;
    }

    public static boolean inDemoMode() {
        return isRunning() && config.demoMode;
    }

    /**
     * Returns true if the seed is set by Atum and no external seed provider is used, used by chunkcacher.
     */
    public static boolean isSetSeed() {
        return Atum.seedProvider == DEFAULT_SEED_PROVIDER && (config.isSetSeed() || config.demoMode);
    }

    public static SeedProvider getSeedProvider() {
        return seedProvider;
    }

    @SuppressWarnings("unused")
    public static void setSeedProvider(SeedProvider seedProvider) {
        Atum.ensureState(Atum.seedProvider == DEFAULT_SEED_PROVIDER, "Seed provider has already been changed! It is likely that multiple mods are trying to set the seed provider!");
        Atum.ensureState(!Atum.isRunning(), "Seed provider set at an illegal time!");
        Atum.seedProvider = Objects.requireNonNull(seedProvider);
    }

    public static void ensureState(boolean condition, String exceptionMessage) throws IllegalStateException {
        if (!condition) throw new IllegalStateException(exceptionMessage);
    }

    @Override
    public void onInitializeClient() {
        resetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Create New World",
                GLFW.GLFW_KEY_F6,
                "key.categories.atum"
        ));

        Object LOCK = new Object();

        setSeedProvider(new SeedProvider() {
            long nextAvailable = 0;

            @Override
            public CompletableFuture<String> requestSeed() {
                CompletableFuture<String> seedFuture = new CompletableFuture<>();
                new Thread(() -> {
                    synchronized (LOCK) {
                        long current = System.currentTimeMillis();
                        if (current > nextAvailable) {
                            nextAvailable = current + 5000;
                            seedFuture.complete("aaa " + new Random().nextLong());
                        }
                        try {
                            Thread.sleep(5000);
                            seedFuture.complete("aaa " + new Random().nextLong());
                        } catch (Exception e) {
                            seedFuture.completeExceptionally(e);
                        }
                    }
                }).start();
                return seedFuture;
            }

            @Override
            public Optional<AtumWaitingScreen> getWaitingScreen(Runnable cancelFunction) {
                return Optional.of(new AtumWaitingScreen(Text.of("aaaaaa"), cancelFunction) {
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        renderBackground(matrices);
                        drawCenteredText(matrices, textRenderer, title, width / 2, height / 2 - textRenderer.fontHeight, Formatting.WHITE.getColorValue());
                    }

                    @Override
                    public boolean shouldCloseOnEsc() {
                        return true;
                    }
                });
            }
        });
    }
}
