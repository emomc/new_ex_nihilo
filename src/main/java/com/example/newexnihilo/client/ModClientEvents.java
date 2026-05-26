package com.example.newexnihilo.client;

import com.example.newexnihilo.ModContent;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class ModClientEvents {
    private ModClientEvents() {
    }

    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(ModContent.BARREL_BLOCK_ENTITY.get(), BarrelRenderer::new);
            BlockEntityRenderers.register(ModContent.CRUCIBLE_BLOCK_ENTITY.get(), CrucibleRenderer::new);
            BlockEntityRenderers.register(ModContent.SIEVE_BLOCK_ENTITY.get(), SieveRenderer::new);
            BlockEntityRenderers.register(ModContent.INFESTING_LEAVES_BLOCK_ENTITY.get(), InfestingLeavesRenderer::new);
            BlockEntityRenderers.register(ModContent.INFESTED_LEAVES_BLOCK_ENTITY.get(), InfestedLeavesRenderer::new);
        });
    }
}
