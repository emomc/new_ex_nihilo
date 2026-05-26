package com.example.newexnihilo.client;

import com.example.newexnihilo.SieveBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SieveRenderer implements BlockEntityRenderer<SieveBlockEntity, SieveRenderer.State> {
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;
    private final ItemModelResolver itemModelResolver;

    public SieveRenderer(BlockEntityRendererProvider.Context context) {
        blockModelResolver = context.blockModelResolver();
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SieveBlockEntity sieve, State state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(sieve, state, overlay);
        state.progressRatio = sieve.getProgressRatio();
        int seed = (int) sieve.getBlockPos().asLong();
        state.mesh.clear();
        state.inputBlock.clear();
        itemModelResolver.updateForTopItem(state.mesh, sieve.getMesh(), RenderSubmitHelper.displayContext(),
                sieve.getLevel(), null, seed);
        Block inputBlock = Block.byItem(sieve.getInput().getItem());
        if (inputBlock != Blocks.AIR) {
            blockModelResolver.update(state.inputBlock, inputBlock.defaultBlockState(), BLOCK_DISPLAY_CONTEXT);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        RenderSubmitHelper.submitSieveMeshItem(state.mesh, poseStack, submitNodeCollector, state.lightCoords);
        RenderSubmitHelper.submitSieveInputBlock(state.inputBlock, state.progressRatio,
                poseStack, submitNodeCollector, state.lightCoords);
    }

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState mesh = new ItemStackRenderState();
        public final BlockModelRenderState inputBlock = new BlockModelRenderState();
        public float progressRatio;
    }
}
