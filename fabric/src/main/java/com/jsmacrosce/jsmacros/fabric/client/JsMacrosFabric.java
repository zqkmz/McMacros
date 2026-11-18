package com.jsmacrosce.jsmacros.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if <26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.ResourceLocation;
import com.jsmacrosce.jsmacros.client.mcmacros.McHud;
//?}
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
*///? } else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?}
import com.jsmacrosce.jsmacros.client.JsMacros;
import com.jsmacrosce.jsmacros.client.JsMacrosClient;
import com.jsmacrosce.jsmacros.client.api.classes.inventory.CommandManager;
import com.jsmacrosce.jsmacros.client.tick.TickBasedEvents;
import com.jsmacrosce.jsmacros.fabric.client.api.classes.CommandBuilderFabric;
import com.jsmacrosce.jsmacros.fabric.client.api.classes.CommandManagerFabric;

public class JsMacrosFabric implements ModInitializer, ClientModInitializer {

    @Override
    public void onInitializeClient() {
        JsMacrosClient.onInitializeClient();
        ClientTickEvents.END_CLIENT_TICK.register(TickBasedEvents::onTick);
        //? if >=26.1 {
        /*KeyMappingHelper.registerKeyMapping(JsMacrosClient.keyBinding);
        *///? } else {
        KeyBindingHelper.registerKeyBinding(JsMacrosClient.keyBinding);
        //?}
        CommandBuilderFabric.registerEvent();
        //? if <26.1 {
        HudElementRegistry.addLast(ResourceLocation.fromNamespaceAndPath("mcmacros", "running_services"), (graphics, deltaTracker) -> McHud.render(graphics));
        //?}
    }

    @Override
    public void onInitialize() {
        JsMacros.onInitialize();

        // initialize loader-specific stuff
        CommandManager.instance = new CommandManagerFabric();
    }

}
