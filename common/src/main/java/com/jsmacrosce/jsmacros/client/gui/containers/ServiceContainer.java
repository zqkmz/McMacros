package com.jsmacrosce.jsmacros.client.gui.containers;

import com.jsmacrosce.doclet.DocletIgnore;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import com.jsmacrosce.jsmacros.client.JsMacrosClient;
import com.jsmacrosce.jsmacros.client.gui.overlays.TextOverlay;
import com.jsmacrosce.jsmacros.client.gui.screens.MacroScreen;
import com.jsmacrosce.jsmacros.client.gui.screens.ServiceScreen;
import com.jsmacrosce.jsmacros.client.mcmacros.McServices;
import com.jsmacrosce.jsmacros.core.service.ServiceManager;
import com.jsmacrosce.jsmacros.core.service.ServiceTrigger;
import com.jsmacrosce.wagyourgui.McTheme;
import com.jsmacrosce.wagyourgui.containers.MultiElementContainer;
import com.jsmacrosce.wagyourgui.elements.Button;
import com.jsmacrosce.wagyourgui.overlays.TextPrompt;

import java.io.File;

/**
 * One row of the McMacros service list.
 *
 * <p>Columns (in twelfths of the row): name 2, file 4, toggle key 2, resume after rejoining 1,
 * start with the game 1, status with running time 2, then the delete button.</p>
 */
@DocletIgnore
public class ServiceContainer extends MultiElementContainer<MacroScreen> {
    /** Column boundaries in twelfths of the row width. */
    public static final int[] COLUMNS = {0, 2, 6, 8, 9, 10, 12};

    private static final int ON = 0x702BD4AE;
    private static final int ON_HOVER = 0x701A8F75;
    private static final int OFF = 0x70C2405E;
    private static final int OFF_HOVER = 0x70802A3E;

    public String service;
    protected Button nameBtn;
    protected Button fileBtn;
    protected Button keyBtn;
    protected Button resumeBtn;
    protected Button enableBtn;
    protected Button runningBtn;
    protected Button delBtn;
    private boolean capturing = false;

    public ServiceContainer(int x, int y, int width, int height, Font textRenderer, ServiceScreen parent, String service) {
        super(x, y, width, height, textRenderer, parent);
        this.service = service;
        init();
    }

    /** Left edge of column {@code i} relative to the row. */
    public static int col(int width, int i) {
        return (width - 12) * COLUMNS[i] / 12;
    }

    @Override
    public void init() {
        super.init();

        nameBtn = addRenderableWidget(new Button(0, 0, 1, 1, textRenderer, 0, 0xFF000000, 0x7F7F7F7F, 0xFFFFFFFF, Component.literal(service), (btn) -> {
            openOverlay(new TextPrompt(parent.width / 4, parent.height / 4, parent.width / 2, parent.height / 2, textRenderer, Component.translatable("jsmacrosce.servicerename.prompt"), service, getFirstOverlayParent(), (newService) -> {
                if (!JsMacrosClient.clientCore.services.renameService(service, newService)) {
                    openOverlay(new TextOverlay(parent.width / 4, parent.height / 4, parent.width / 2, parent.height / 2, textRenderer, getFirstOverlayParent(), Component.translatable("jsmacrosce.servicerename.failed").withStyle(s -> s.withColor(ChatFormatting.RED))));
                    return;
                }
                service = newService;
                btn.setMessage(Component.literal(newService));
            }));
        }));

        fileBtn = addRenderableWidget(new Button(0, 0, 1, 1, textRenderer, 0, 0xFF000000, 0x7F7F7F7F, 0xFFFFFFFF, fileText(), (btn) -> parent.setFile(this)));

        keyBtn = addRenderableWidget(new Button(0, 0, 1, 1, textRenderer, 0, 0xFF000000, 0x7F7F7F7F, 0xFFFFFFFF, keyText(), (btn) -> {
            ((ServiceScreen) parent).startCapture(this);
        }));

        resumeBtn = addRenderableWidget(new Button(0, 0, 1, 1, textRenderer, 0, 0xFF000000, 0, 0xFFFFFFFF, Component.empty(), (btn) -> {
            ServiceTrigger t = getTrigger();
            t.resumeOnJoin = !t.isResumeOnJoin();
            updateButtons();
        }));

        enableBtn = addRenderableWidget(new Button(0, 0, 1, 1, textRenderer, 0, 0xFF000000, 0, 0xFFFFFFFF, Component.empty(), (btn) -> {
            if (getEnabled()) {
                JsMacrosClient.clientCore.services.disableService(service);
            } else {
                JsMacrosClient.clientCore.services.enableService(service);
            }
            updateButtons();
        }));

        runningBtn = addRenderableWidget(new Button(0, 0, 1, 1, textRenderer, 0, 0xFF000000, 0, 0xFFFFFFFF, Component.empty(), (btn) -> McServices.toggle(service)));

        delBtn = addRenderableWidget(new Button(0, 0, 1, 1, textRenderer, 0, 0xFF000000, 0x7F7F7F7F, 0xFFFFFFFF, Component.literal("X"), (btn) -> {
            parent.confirmRemoveMacro(this);
        }));

        setPos(x, y, width, height);
        updateButtons();
    }

    private Component fileText() {
        return Component.literal("./" + getTrigger().file.toString().replaceAll("\\\\", "/"));
    }

    private Component keyText() {
        if (capturing) return Component.translatable("jsmacrosce.presskey");
        String key = getTrigger().getToggleKey();
        if (key.isEmpty()) return Component.translatable("mcmacros.key.none").withStyle(ChatFormatting.DARK_GRAY);
        return JsMacrosClient.getKeyText(key);
    }

    private static void paint(Button btn, boolean on) {
        btn.setColor(on ? ON : OFF);
        btn.setHighlightColor(on ? ON_HOVER : OFF_HOVER);
    }

    /** Refreshes the labels and colours of the toggle buttons. */
    public void updateButtons() {
        ServiceTrigger t = getTrigger();
        paint(resumeBtn, t.isResumeOnJoin());
        resumeBtn.setMessage(Component.translatable(t.isResumeOnJoin() ? "mcmacros.on" : "mcmacros.off"));
        boolean enabled = getEnabled();
        paint(enableBtn, enabled);
        enableBtn.setMessage(Component.translatable(enabled ? "mcmacros.on" : "mcmacros.off"));
        keyBtn.setMessage(keyText());
    }

    /** Called by the screen while this row waits for a key. */
    public void setCapturing(boolean capturing) {
        this.capturing = capturing;
        keyBtn.setMessage(keyText());
    }

    /** Stores the new toggle key; an empty string removes it. */
    public void setToggleKey(String key) {
        getTrigger().toggleKey = key.isEmpty() ? null : key;
        setCapturing(false);
    }

    public boolean getEnabled() {
        ServiceManager.ServiceStatus status = JsMacrosClient.clientCore.services.status(service);
        return status == ServiceManager.ServiceStatus.ENABLED || status == ServiceManager.ServiceStatus.STOPPED;
    }

    public boolean getRunning() {
        ServiceManager.ServiceStatus status = JsMacrosClient.clientCore.services.status(service);
        return status == ServiceManager.ServiceStatus.ENABLED || status == ServiceManager.ServiceStatus.RUNNING;
    }

    public ServiceTrigger getTrigger() {
        return JsMacrosClient.clientCore.services.getTrigger(service);
    }

    public void setFile(File file) {
        getTrigger().file = JsMacrosClient.clientCore.config.macroFolder.getAbsoluteFile().toPath().relativize(file.getAbsoluteFile().toPath());
        JsMacrosClient.clientCore.services.disableReload(service);
        fileBtn.setMessage(fileText());
    }

    @Override
    public void setPos(int x, int y, int width, int height) {
        super.setPos(x, y, width, height);
        Button[] cells = {nameBtn, fileBtn, keyBtn, resumeBtn, enableBtn, runningBtn};
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] == null) continue;
            int left = col(width, i);
            int right = col(width, i + 1);
            cells[i].setPos(x + left + 1, y + 1, right - left - 1, height - 2);
            // Buttons lay out their label only when it is set, so set it again for the new size.
            cells[i].setMessage(cells[i].getMessage());
        }
        if (delBtn != null) {
            delBtn.setPos(x + width - 13, y + 1, 12, height - 2);
            delBtn.setMessage(delBtn.getMessage());
        }
    }

    @Override
    //? if >=26.1 {
    /*public void extractRenderState(final GuiGraphicsExtractor drawContext, int mouseX, int mouseY, final float delta) {
    *///? } else {
    public void render(final GuiGraphics drawContext, int mouseX, int mouseY, final float delta) {
    //? }
        // column separators
        for (int i = 1; i < COLUMNS.length; i++) {
            int cx = x + col(width, i);
            drawContext.fill(cx, y + 1, cx + 1, y + height - 1, McTheme.LINE);
        }

        // border
        drawContext.fill(x, y, x + width, y + 1, McTheme.LINE);
        drawContext.fill(x, y + height - 1, x + width, y + height, McTheme.LINE);
        drawContext.fill(x, y + 1, x + 1, y + height - 1, McTheme.LINE);
        drawContext.fill(x + width - 1, y + 1, x + width, y + height - 1, McTheme.LINE);

        boolean running = getRunning();
        paint(runningBtn, running);
        if (running) {
            long ms = McServices.runningMillis(service);
            runningBtn.setMessage(Component.translatable("mcmacros.running.time", ms < 0 ? "0:00" : McServices.formatDuration(ms)));
        } else {
            runningBtn.setMessage(Component.translatable("jsmacrosce.stopped"));
        }
    }

}
