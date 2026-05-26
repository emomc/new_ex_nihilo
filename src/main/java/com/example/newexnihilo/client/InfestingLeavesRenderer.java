package com.example.newexnihilo.client;

import com.example.newexnihilo.InfestingLeavesBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

public class InfestingLeavesRenderer implements BlockEntityRenderer<InfestingLeavesBlockEntity, InfestingLeavesRenderer.State> {
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;

    public InfestingLeavesRenderer(BlockEntityRendererProvider.Context context) {
        blockModelResolver = context.blockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(InfestingLeavesBlockEntity leaves, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(leaves, state, overlay);
        state.original.clear();
        blockModelResolver.update(state.original, leaves.getOriginalState(), BLOCK_DISPLAY_CONTEXT);
        state.variant = leaves.getVariant().getSerializedName();
        state.progressRatio = leaves.getProgressRatio();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState) {
        if (!state.original.isEmpty()) {
            state.original.submitMultiLayer(poseStack, submitNodeCollector, state.lightCoords,
                    OverlayTexture.NO_OVERLAY, 0);
        }
        float infestingRatio = Math.min(1.0F, state.progressRatio / 0.45F);
        float infestingAlpha = state.progressRatio < 0.72F
                ? 1.0F
                : Math.max(0.0F, 1.0F - (state.progressRatio - 0.72F) / 0.28F);
        float infestedRatio = Math.max(0.0F, (state.progressRatio - 0.28F) / 0.72F);
        RenderSubmitHelper.submitInfestingLeavesOverlay("infesting", state.variant, infestingRatio, infestingAlpha,
                poseStack, submitNodeCollector, state.lightCoords);
        RenderSubmitHelper.submitInfestingLeavesOverlay("infested", state.variant, infestedRatio,
                poseStack, submitNodeCollector, state.lightCoords);
    }

    public static class State extends BlockEntityRenderState {
        public final BlockModelRenderState original = new BlockModelRenderState();
        public String variant = "oak";
        public float progressRatio;
    }
}
