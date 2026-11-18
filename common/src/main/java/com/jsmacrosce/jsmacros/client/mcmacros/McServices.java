package com.jsmacrosce.jsmacros.client.mcmacros;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.jsmacrosce.jsmacros.client.JsMacrosClient;
import com.jsmacrosce.jsmacros.client.api.event.impl.EventKey;
import com.jsmacrosce.jsmacros.client.util.GuiCompat;
import com.jsmacrosce.jsmacros.core.event.BaseEventRegistry;
import com.jsmacrosce.jsmacros.core.service.ServiceManager;
import com.jsmacrosce.jsmacros.core.service.ServiceTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * McMacros' extra service features, which work without any code in the scripts themselves:
 * <ul>
 *     <li>a key per service that starts it when stopped and stops it when running,</li>
 *     <li>starting services again after rejoining a server, if they were running when the connection dropped,</li>
 *     <li>remembering since when each service has been running, for the list screen and the on-screen display.</li>
 * </ul>
 */
public final class McServices {
    private McServices() {
    }

    /** Wait this long after joining before restarting services, so the world and inventory have arrived. */
    private static final int RESUME_DELAY_TICKS = 60;

    private static final Map<String, Long> runningSince = new ConcurrentHashMap<>();
    private static final Set<String> resumeAfterJoin = ConcurrentHashMap.newKeySet();
    private static int resumeCountdown = -1;
    private static int tickCounter = 0;

    /** Set by the service list while it waits for the player to press the new toggle key. */
    public static volatile boolean capturingKey = false;

    /** Registers the key, connection and tick listeners. Call once after the client core is ready. */
    public static void init() {
        BaseEventRegistry registry = JsMacrosClient.clientCore.eventRegistry;
        registry.addListener("Key", event -> {
            onKey((EventKey) event);
            return null;
        });
        registry.addListener("Disconnect", event -> {
            onDisconnect();
            return null;
        });
        registry.addListener("JoinServer", event -> {
            resumeCountdown = RESUME_DELAY_TICKS;
            return null;
        });
    }

    private static ServiceManager services() {
        return JsMacrosClient.clientCore.services;
    }

    private static void onKey(EventKey event) {
        if (event.action != 1 || capturingKey) return;
        Minecraft mc = Minecraft.getInstance();
        if (GuiCompat.screen(mc) != null) return;
        for (String name : services().getServices()) {
            ServiceTrigger trigger = services().getTrigger(name);
            if (trigger == null || trigger.getToggleKey().isEmpty() || !trigger.getToggleKey().equals(event.key)) continue;
            toggle(name);
        }
    }

    /** Starts the service if it is stopped, otherwise stops it, and says which on the action bar. */
    public static void toggle(String name) {
        boolean running = services().isRunning(name);
        if (running) {
            services().stopService(name);
        } else {
            services().startService(name);
        }
        Minecraft mc = Minecraft.getInstance();
        Component msg = running
                ? Component.translatable("mcmacros.toggle.stopped", name).withStyle(ChatFormatting.RED)
                : Component.translatable("mcmacros.toggle.started", name).withStyle(ChatFormatting.GREEN);
        mc.execute(() -> {
            try {
                GuiCompat.hud(mc).setOverlayMessage(msg, false);
            } catch (Throwable ignored) {
            }
        });
    }

    private static void onDisconnect() {
        resumeCountdown = -1;
        resumeAfterJoin.clear();
        for (String name : services().getServices()) {
            ServiceTrigger trigger = services().getTrigger(name);
            if (trigger != null && trigger.isResumeOnJoin() && services().isRunning(name)) {
                resumeAfterJoin.add(name);
            }
        }
    }

    /** Called every client tick. Keeps the running timers current and restarts services after rejoining. */
    public static void tick() {
        if (++tickCounter % 5 == 0) {
            long now = System.currentTimeMillis();
            Set<String> names = services().getServices();
            for (String name : names) {
                if (services().isRunning(name)) {
                    runningSince.putIfAbsent(name, now);
                } else {
                    runningSince.remove(name);
                }
            }
            runningSince.keySet().retainAll(names);
        }
        if (resumeCountdown > 0) {
            resumeCountdown--;
        } else if (resumeCountdown == 0) {
            resumeCountdown = -1;
            List<String> started = new ArrayList<>();
            for (String name : resumeAfterJoin) {
                if (services().getServices().contains(name) && !services().isRunning(name)) {
                    services().startService(name);
                    started.add(name);
                }
            }
            resumeAfterJoin.clear();
            if (!started.isEmpty()) {
                Minecraft mc = Minecraft.getInstance();
                Component msg = Component.translatable("mcmacros.resumed", String.join(", ", started)).withStyle(ChatFormatting.AQUA);
                try {
                    GuiCompat.hud(mc).setOverlayMessage(msg, false);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    /** @return milliseconds the service has been running, or -1 when it is not running */
    public static long runningMillis(String name) {
        Long since = runningSince.get(name);
        return since == null ? -1 : System.currentTimeMillis() - since;
    }

    /** Formats a duration as {@code m:ss} or {@code h:mm:ss}. */
    public static String formatDuration(long millis) {
        long s = Math.max(0, millis / 1000);
        long h = s / 3600, m = (s / 60) % 60, sec = s % 60;
        return h > 0 ? String.format("%d:%02d:%02d", h, m, sec) : String.format("%d:%02d", m, sec);
    }
}
