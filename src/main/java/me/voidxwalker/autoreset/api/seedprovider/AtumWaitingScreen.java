package me.voidxwalker.autoreset.api.seedprovider;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public abstract class AtumWaitingScreen extends Screen {
    private final CompletableFuture<String> seedFuture;

    protected AtumWaitingScreen(Text title, CompletableFuture<String> seedFuture) {
        super(title);
        this.seedFuture = seedFuture;
    }

    @SuppressWarnings("unused")
    protected final void cancelWorldCreation() {
        this.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public final void onClose() {
        this.seedFuture.cancel(true);
        super.onClose();
    }

    /**
     * Executed when the seed future has an exception.
     */
    @SuppressWarnings("unused")
    public void onFail(Throwable ex) {
    }
}
