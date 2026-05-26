package com.example.newexnihilo.client;

import com.example.newexnihilo.CrucibleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity, CrucibleRenderer.State> {
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;
    private final ItemModelResolver itemModelResolver;

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {
        blockModelResolver = context.blockModelResolver();
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CrucibleBlockEntity crucible, State state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(crucible, state, overlay);
        state.fluidId = crucible.getStoredFluidId();
        state.fluidAmount = crucible.getStoredAmount();
        state.progressRatio = crucible.getProgressRatio();
        state.queuedFluidRatio = crucible.getQueuedFluidProportion();
        state.solidRatio = crucible.getSolidProportion();
        state.inputIsBlock = false;
        state.inputBlock.clear();
        state.input.clear();
        Block inputBlock = Block.byItem(crucible.getInput().getItem());
        if (inputBlock != Blocks.AIR) {
            state.inputIsBlock = true;
            blockModelResolver.update(state.inputBlock, inputBlock.defaultBlockState(), BLOCK_DISPLAY_CONTEXT);
        } else {
            itemModelResolver.updateForTopItem(state.input, crucible.getInput(), RenderSubmitHelper.displayContext(),
                    crucible.getLevel(), null, (int) crucible.getBlockPos().asLong());
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        RenderSubmitHelper.submitFluid(state.fluidId, state.fluidAmount, CrucibleBlockEntity.CAPACITY, 0.189F, 0.908F,
                0.126F, 0.874F,
                poseStack, submitNodeCollector, state.lightCoords);
        if (state.inputIsBlock) {
            RenderSubmitHelper.submitCrucibleInputBlock(state.inputBlock, state.progressRatio, state.solidRatio,
                    poseStack, submitNodeCollector, state.lightCoords);
        } else {
            float inputY = 0.42F - Math.min(0.12F, state.progressRatio * 0.12F);
            RenderSubmitHelper.submitFlatItem(state.input, poseStack, submitNodeCollector, state.lightCoords,
                    0.5F, inputY, 0.5F, 0.36F, 0.0F, 90.0F);
        }
    }

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState input = new ItemStackRenderState();
        public final BlockModelRenderState inputBlock = new BlockModelRenderState();
        public String fluidId = "";
        public int fluidAmount;
        public float progressRatio;
        public float queuedFluidRatio;
        public float solidRatio;
        public boolean inputIsBlock;
    }
}
