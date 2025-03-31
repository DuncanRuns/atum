package me.voidxwalker.autoreset.api.seedprovider;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * A waiting screen intended to wait for a seed to become playable with the ability to cancel playing a seed.
 */
public abstract class AtumWaitingScreen extends Screen {
    private final Runnable onCancel;
    private final Runnable onTick;

    protected AtumWaitingScreen(Text title, Runnable onCancel, Runnable onTick) {
        super(title);
        this.onCancel = onCancel;
        this.onTick = onTick;
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
        this.onCancel.run();
    }

    /**
     * If an implementation overrides tick, it needs to run super.tick().
     */
    @Override
    public void tick() {
        onTick.run();
    }
}
