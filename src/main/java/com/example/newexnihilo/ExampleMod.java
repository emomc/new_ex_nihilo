package com.example.newexnihilo;

import java.lang.reflect.Method;
import java.util.List;
import com.example.newexnihilo.client.ModClientEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "new_ex_nihilo";

    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ModEvents::commonSetup);
        modEventBus.addListener(ModEvents::registerCapabilities);
        modEventBus.addListener(ModEvents::registerClientExtensions);
        modEventBus.addListener(ModEvents::registerFluidModels);
        modEventBus.addListener(ModClientEvents::clientSetup);
        ModContent.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        registerFoldedCreativeTabs();
    }

    private static void registerFoldedCreativeTabs() {
        if (!ModList.get().isLoaded("folding_creative_tabs_support")) {
            return;
        }

        try {
            Class<?> api = Class.forName("com.example.folding_creative_tabs_support.api.FoldedCreativeTabsApi");
            Method register = api.getMethod("register", Identifier.class, ResourceKey.class, List.class);
            register.invoke(
                    null,
                    Identifier.fromNamespaceAndPath(MODID, "main"),
                    creativeTabKey("all"),
                    List.of(
                            creativeTabKey("blocks"),
                            creativeTabKey("tools"),
                            creativeTabKey("resources"),
                            creativeTabKey("materials"),
                            creativeTabKey("misc")));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to register folded creative tabs", exception);
        }
    }

    private static ResourceKey<CreativeModeTab> creativeTabKey(String path) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MODID, path));
    }
}
