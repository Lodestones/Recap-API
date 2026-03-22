package gg.lode.recap.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class RecapEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public void call() {
        org.bukkit.Bukkit.getPluginManager().callEvent(this);
    }
}
