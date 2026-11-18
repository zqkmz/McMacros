package com.jsmacrosce.jsmacros.client.mcmacros;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import com.jsmacrosce.jsmacros.client.JsMacrosClient;
import com.jsmacrosce.jsmacros.core.service.ServiceManager;
import com.jsmacrosce.jsmacros.core.service.ServiceTrigger;
import com.jsmacrosce.wagyourgui.McTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * The small panel in the top-right corner of the game screen that lists the services that are running,
 * with how long each has been running and its toggle key.
 */
public final class McHud {
    private McHud() {
    }

    private static final int MARGIN = 4;
    private static final int PAD = 5;
    private static final int LINE = 11;

    /** Draws the panel. Hidden with F1, with F3 open, and when no service is running. */
    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.getDebugOverlay().showDebugScreen()) return;

        ServiceManager services = JsMacrosClient.clientCore.services;
        List<String[]> rows = new ArrayList<>();
        for (String name : services.getServices()) {
            ServiceTrigger trigger = services.getTrigger(name);
            if (trigger == null || !trigger.isShowInHud() || !services.isRunning(name)) continue;
            long ms = McServices.runningMillis(name);
            String key = trigger.getToggleKey().isEmpty() ? "" : JsMacrosClient.getKeyText(trigger.getToggleKey()).getString();
            rows.add(new String[]{name, ms < 0 ? "0:00" : McServices.formatDuration(ms), key});
        }
        if (rows.isEmpty()) return;

        Font font = mc.font;
        String title = "McMacros";
        int nameW = 0, timeW = 0, keyW = 0;
        for (String[] r : rows) {
            nameW = Math.max(nameW, font.width(r[0]));
            timeW = Math.max(timeW, font.width(r[1]));
            keyW = Math.max(keyW, r[2].isEmpty() ? 0 : font.width(r[2]) + 6);
        }
        int dot = 6;
        int inner = Math.max(font.width(title), dot + 4 + nameW + 8 + timeW + (keyW > 0 ? 6 + keyW : 0));
        int w = inner + PAD * 2;
        int h = PAD * 2 + LINE + rows.size() * LINE + 2;
        int x2 = g.guiWidth() - MARGIN;
        int x1 = x2 - w;
        int y1 = MARGIN;
        int y2 = y1 + h;

        McTheme.panel(g, x1, y1, x2, y2, McTheme.CUT_LARGE, 0xC0071220, McTheme.LINE);
        int y = y1 + PAD;
        g.drawString(font, title, x1 + PAD, y, McTheme.ACCENT, false);
        String count = String.valueOf(rows.size());
        g.drawString(font, count, x2 - PAD - font.width(count), y, McTheme.TEXT_DIM, false);
        y += LINE + 2;
        for (String[] r : rows) {
            int x = x1 + PAD;
            g.fill(x, y + 1, x + dot, y + 1 + dot, McTheme.SUCCESS);
            x += dot + 4;
            g.drawString(font, r[0], x, y, McTheme.TEXT, false);
            int right = x2 - PAD;
            if (!r[2].isEmpty()) {
                int kw = font.width(r[2]) + 6;
                McTheme.outlineCut(g, right - kw, y - 1, right, y + 9, 1, McTheme.LINE);
                g.drawString(font, r[2], right - kw + 3, y, McTheme.TEXT_DIM, false);
                right -= kw + 6;
            }
            g.drawString(font, r[1], right - font.width(r[1]), y, McTheme.SUCCESS, false);
            y += LINE;
        }
    }
}
