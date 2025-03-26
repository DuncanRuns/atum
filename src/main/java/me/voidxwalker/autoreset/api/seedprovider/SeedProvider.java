package me.voidxwalker.autoreset.api.seedprovider;

import java.util.concurrent.CompletableFuture;

public interface SeedProvider {
    /**
     * Requests a seed from the seed provider. Can be completed immediately in the implemented method to skip any
     * waiting screens.
     */
    CompletableFuture<String> requestSeed();

    /**
     * Determines whether a set seed should be present in logs, LevelLoadingScreen, and DebugHUD.
     */
    default boolean shouldShowSeed() {
        return true;
    }

    /**
     * Gets the waiting screen.
     * The waiting screen will automatically close when the seed future is completed, but implementations may also cancel with cancelWorldCreation.
     * The implemented waiting screen can also override shouldCloseOnEsc(), returning true to allow cancelling with the 'escape' key.
     */
    default AtumWaitingScreen getWaitingScreen(CompletableFuture<String> seedFuture) {
        throw new IllegalStateException("No waiting screen available!");
    }
}
