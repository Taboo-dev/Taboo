package xyz.chalky.taboo.events;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.chalky.taboo.central.Taboo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventManager implements IEventManager {

    private final ArrayList<EventListener> listeners = new ArrayList<>();
    private final Logger LOGGER = LoggerFactory.getLogger(EventManager.class);

    public void init() {
        registerEvents();
    }

    private void registerEvents() {
        register(new ReadyHandler());
        register(new InteractionsListener());
        register(new CommandsListener());
        register(new MessageListener());
        register(new JoinLeaveListener());
        register(new GuildJoinListener());
        register(new GuildEvents());
        register(new VoiceListener());
        register(new MusicEvents());
        register(Taboo.getInstance().getEventWaiter());
    }

    @Override
    public void register(@NotNull Object listener) {
        if (listener instanceof EventListener eventListener) {
            listeners.add(eventListener);
        }
    }

    @Override
    public void unregister(@NotNull Object listener) {
        if (listener instanceof EventListener eventListener) {
            listeners.remove(eventListener);
        }
    }

    @Override
    public void handle(@NotNull GenericEvent event) {
        for (var listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                LOGGER.error("Error while handling event", e);
            }
        }
    }

    @NotNull
    @Override
    public List<Object> getRegisteredListeners() {
        return Collections.singletonList(listeners);
    }

}
