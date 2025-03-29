package me.voidxwalker.autoreset.api.seedprovider;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class AtumWaitingScreen extends Screen {
    private final Runnable cancelFunction;

    protected AtumWaitingScreen(Text title, Runnable cancelFunction) {
        super(title);
        this.cancelFunction = cancelFunction;
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
        this.cancelFunction.run();
        super.onClose();
    }
}
