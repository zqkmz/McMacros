package com.jsmacrosce.jsmacros.client.gui.containers;

import com.jsmacrosce.doclet.DocletIgnore;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import com.jsmacrosce.jsmacros.client.JsMacrosClient;
import com.jsmacrosce.jsmacros.client.config.ClientConfigV2;
import com.jsmacrosce.jsmacros.client.config.Sorting;
import com.jsmacrosce.jsmacros.client.gui.screens.ServiceScreen;
import com.jsmacrosce.jsmacros.core.service.ServiceTrigger;
import com.jsmacrosce.wagyourgui.containers.MultiElementContainer;
import com.jsmacrosce.wagyourgui.elements.Button;
import com.jsmacrosce.wagyourgui.overlays.TextPrompt;

import java.nio.file.Path;

@DocletIgnore
public class ServiceListTopbar extends MultiElementContainer<ServiceScreen> {

    public ServiceListTopbar(ServiceScreen parent, int x, int y, int width, int height, Font textRenderer) {
        super(x, y, width, height, textRenderer, parent);
        init();
    }

    @Override
    public void init() {
        super.init();
        ClientConfigV2 cfg = JsMacrosClient.clientCore.config.getOptions(ClientConfigV2.class);

        header(0, Component.translatable("jsmacrosce.servicename"), cfg.sortServicesMethod == Sorting.ServiceSortMethod.Name, () -> cfg.sortServicesMethod = Sorting.ServiceSortMethod.Name);
        header(1, Component.translatable("jsmacrosce.file"), cfg.sortServicesMethod == Sorting.ServiceSortMethod.FileName, () -> cfg.sortServicesMethod = Sorting.ServiceSortMethod.FileName);
        header(2, Component.translatable("mcmacros.header.key"), false, null);
        header(3, Component.translatable("mcmacros.header.resume"), false, null);
        header(4, Component.translatable("mcmacros.header.autostart"), cfg.sortServicesMethod == Sorting.ServiceSortMethod.Enabled, () -> cfg.sortServicesMethod = Sorting.ServiceSortMethod.Enabled);
        header(5, Component.translatable("jsmacrosce.runningstatus"), cfg.sortServicesMethod == Sorting.ServiceSortMethod.Running, () -> cfg.sortServicesMethod = Sorting.ServiceSortMethod.Running);

        addRenderableWidget(new Button(x + width - 13, y + 1, 12, height - 3, textRenderer, 0, 0xFF000000, 0x7F7F7F7F, 0xFFFFFFFF, Component.literal("+"), (btn) -> {
            openOverlay(new TextPrompt(parent.width / 4, parent.height / 4, parent.width / 2, parent.height / 2, textRenderer, Component.translatable("jsmacrosce.servicename"), "", getFirstOverlayParent(), (name) -> {
                if (JsMacrosClient.clientCore.services.registerService(name, new ServiceTrigger(Path.of(".").normalize(), false))) {
                    parent.addService(name);
                }
            }));
        }));
    }

    /** One column header; clicking a sortable header sorts the list by that column. */
    private void header(int column, Component label, boolean selected, Runnable sort) {
        int left = ServiceContainer.col(width, column);
        int right = ServiceContainer.col(width, column + 1);
        addRenderableWidget(new Button(x + left + 1, y + 1, right - left - 1, height - 3, textRenderer, selected ? 0x3FFFFFFF : 0, 0xFF000000, sort == null ? 0 : 0x7F7F7F7F, 0xFFFFFFFF, label, (btn) -> {
            if (sort == null) return;
            sort.run();
            parent.reload();
        }));
    }

    @Override
    //? if >=26.1 {
    /*public void extractRenderState(final GuiGraphicsExtractor drawContext, int mouseX, int mouseY, final float delta) {
    *///? } else {
    public void render(final GuiGraphics drawContext, int mouseX, int mouseY, final float delta) {
    //? }
        drawContext.fill(x, y, x + width, y + 1, com.jsmacrosce.wagyourgui.McTheme.LINE);
        drawContext.fill(x, y + height - 2, x + width, y + height - 1, com.jsmacrosce.wagyourgui.McTheme.LINE);
        drawContext.fill(x, y + height - 1, x + width, y + height, com.jsmacrosce.wagyourgui.McTheme.LINE);
        drawContext.fill(x, y + 1, x + 1, y + height - 1, com.jsmacrosce.wagyourgui.McTheme.LINE);
        drawContext.fill(x + width - 1, y + 1, x + width, y + height - 1, com.jsmacrosce.wagyourgui.McTheme.LINE);
        for (int i = 1; i < ServiceContainer.COLUMNS.length; i++) {
            int cx = x + ServiceContainer.col(width, i);
            drawContext.fill(cx, y + 1, cx + 1, y + height - 1, com.jsmacrosce.wagyourgui.McTheme.LINE);
        }
    }

}
