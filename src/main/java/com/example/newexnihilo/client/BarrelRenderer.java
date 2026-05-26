package com.example.newexnihilo.client;

import com.example.newexnihilo.BarrelBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public class BarrelRenderer implements BlockEntityRenderer<BarrelBlockEntity, BarrelRenderer.State> {
    private static final float CONTENT_MIN = 0.123F;
    private static final float CONTENT_MAX = 0.877F;

    private final ItemModelResolver itemModelResolver;

    public BarrelRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BarrelBlockEntity barrel, State state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(barrel, state, overlay);
        state.fluidId = barrel.getFluidId();
        state.fluidAmount = barrel.getFluidAmount();
        state.transitionTargetFluidId = barrel.getTransitionTargetFluid();
        state.transitionProgressRatio = barrel.getTransitionProgressRatio();
        state.compostAmount = barrel.getCompostAmount();
        state.compostProgressRatio = barrel.getCompostProgressRatio();
        state.compostReady = barrel.isCompostReady();
        state.output.clear();
        itemModelResolver.updateForTopItem(state.output, barrel.getOutput(), RenderSubmitHelper.displayContext(),
                barrel.getLevel(), null, (int) barrel.getBlockPos().asLong());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        RenderSubmitHelper.submitCompost(state.compostAmount, BarrelBlockEntity.COMPOST_COMPLETE, state.compostProgressRatio,
                state.compostReady,
                0.064F, 0.932F, CONTENT_MIN, CONTENT_MAX,
                poseStack, submitNodeCollector, state.lightCoords);
        RenderSubmitHelper.submitFluid(state.fluidId, state.fluidAmount, BarrelBlockEntity.CAPACITY, 0.064F, 0.932F,
                CONTENT_MIN, CONTENT_MAX,
                poseStack, submitNodeCollector, state.lightCoords);
        RenderSubmitHelper.submitFluidTransitionOverlay(state.transitionTargetFluidId, state.fluidAmount,
                BarrelBlockEntity.CAPACITY, state.transitionProgressRatio, 0.064F, 0.932F, CONTENT_MIN, CONTENT_MAX,
                poseStack, submitNodeCollector, state.lightCoords);
        RenderSubmitHelper.submitFlatItem(state.output, poseStack, submitNodeCollector, state.lightCoords,
                0.5F, 0.92F, 0.5F, 0.45F, 0.0F, 90.0F);
    }

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState output = new ItemStackRenderState();
        public String fluidId = "";
        public String transitionTargetFluidId = "";
        public int fluidAmount;
        public float transitionProgressRatio;
        public int compostAmount;
        public float compostProgressRatio;
        public boolean compostReady;
    }
}
