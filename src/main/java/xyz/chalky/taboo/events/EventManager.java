package xyz.chalky.taboo.events;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import xyz.chalky.taboo.central.Application;
import xyz.chalky.taboo.central.Taboo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventManager implements IEventManager {

    private final ArrayList<EventListener> listeners = new ArrayList<>();
    private final Logger LOGGER = LoggerFactory.getLogger(EventManager.class);
    private final ApplicationContext context;

    public EventManager() {
        this.context = Application.getInstance().getProvider().getApplicationContext();
    }

    public void init() {
        registerEvents();
    }

    private void registerEvents() {
        register(new ReadyHandler());
        register(new InteractionsListener());
        register(new CommandsListener());
        register(context.getBean(MessageListener.class));
        register(context.getBean(JoinLeaveListener.class));
        register(new GuildJoinListener());
        register(context.getBean(GuildEvents.class));
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
