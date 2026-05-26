package com.example.newexnihilo.client;

import com.example.newexnihilo.ExampleMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

final class RenderSubmitHelper {
    private static final Identifier SEA_WATER_TEXTURE =
            Identifier.fromNamespaceAndPath(ExampleMod.MODID, "textures/block/sea_water.png");
    private static final Identifier WITCH_WATER_TEXTURE =
            Identifier.fromNamespaceAndPath(ExampleMod.MODID, "textures/block/witch_water.png");
    private static final Identifier WATER_TEXTURE =
            Identifier.withDefaultNamespace("textures/block/water_still.png");
    private static final Identifier LAVA_TEXTURE =
            Identifier.withDefaultNamespace("textures/block/lava_still.png");
    private static final Identifier DIRT_TEXTURE =
            Identifier.withDefaultNamespace("textures/block/dirt.png");
    private static final Identifier ROOTED_DIRT_TEXTURE =
            Identifier.withDefaultNamespace("textures/block/rooted_dirt.png");
    private static final int LAVA_LIGHT = 0xF000F0;
    private static final float ANIMATED_FLUID_FRAME_V = 1.0F / 32.0F;

    private RenderSubmitHelper() {
    }

    static void submitFluid(String fluidId, int amount, int capacity, float yMin, float yMax, PoseStack poseStack,
            SubmitNodeCollector collector, int light) {
        submitFluid(fluidId, amount, capacity, yMin, yMax, 0.15625F, 0.84375F, poseStack, collector, light);
    }

    static void submitFluid(String fluidId, int amount, int capacity, float yMin, float yMax, float min, float max,
            PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (fluidId == null || fluidId.isEmpty() || amount <= 0 || capacity <= 0) {
            return;
        }
        FluidVisual visual = fluidVisual(fluidId);
        float y = yMin + (yMax - yMin) * Math.min(1.0F, amount / (float) capacity);
        submitTexturedSurface(visual.texture(), y, min, max, visual.frameV(), poseStack, collector, visual.light(light),
                visual.red(), visual.green(), visual.blue(), visual.alpha());
    }

    static void submitFluidTransitionOverlay(String fluidId, int amount, int capacity, float progress, float yMin,
            float yMax, float min, float max, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (fluidId == null || fluidId.isEmpty() || amount <= 0 || capacity <= 0 || progress <= 0.0F) {
            return;
        }
        FluidVisual visual = fluidVisual(fluidId);
        float clampedProgress = Math.min(1.0F, progress);
        float y = yMin + (yMax - yMin) * Math.min(1.0F, amount / (float) capacity) + 0.004F;
        int alpha = Math.max(32, Math.round(visual.alpha() * clampedProgress));
        submitTexturedSurface(visual.texture(), y, min, max, visual.frameV(), poseStack, collector, visual.light(light),
                visual.red(), visual.green(), visual.blue(), alpha);
    }

    static void submitCompost(int amount, int capacity, float yMin, float yMax, PoseStack poseStack,
            SubmitNodeCollector collector, int light) {
        submitCompost(amount, capacity, yMin, yMax, 0.15625F, 0.84375F, poseStack, collector, light);
    }

    static void submitCompost(int amount, int capacity, float yMin, float yMax, float min, float max,
            PoseStack poseStack, SubmitNodeCollector collector, int light) {
        submitCompost(amount, capacity, 0.0F, yMin, yMax, min, max, poseStack, collector, light);
    }

    static void submitCompost(int amount, int capacity, float fermentRatio, float yMin, float yMax, float min, float max,
            PoseStack poseStack, SubmitNodeCollector collector, int light) {
        submitCompost(amount, capacity, fermentRatio, false, yMin, yMax, min, max, poseStack, collector, light);
    }

    static void submitCompost(int amount, int capacity, float fermentRatio, boolean ready, float yMin, float yMax,
            float min, float max, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (amount <= 0 || capacity <= 0) {
            return;
        }
        float y = yMin + (yMax - yMin) * Math.min(1.0F, amount / (float) capacity);
        float progress = Math.max(0.0F, Math.min(1.0F, fermentRatio));
        Identifier texture = compostTexture(amount, capacity, progress, ready);
        int red = ready ? 255 : Math.round(118.0F + progress * 92.0F);
        int green = ready ? 255 : Math.round(162.0F - progress * 38.0F);
        int blue = ready ? 255 : Math.round(72.0F + progress * 20.0F);
        submitTexturedCuboid(texture, yMin, y, min, max, poseStack, collector, light,
                red, green, blue, 255);
    }

    static void submitMesh(String meshName, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (meshName == null || meshName.isEmpty()) {
            return;
        }
        Identifier texture = Identifier.fromNamespaceAndPath(ExampleMod.MODID, "textures/block/" + meshName + ".png");
        submitTexturedSurface(texture, 0.8325F, 0.0625F, 0.9375F, poseStack, collector, light,
                255, 255, 255, 255);
    }

    static void submitSieveMeshItem(ItemStackRenderState item, PoseStack poseStack, SubmitNodeCollector collector,
            int light) {
        if (item == null || item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.translate(0.53F, 0.53F, -0.6875F);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    static void submitFlatItem(ItemStackRenderState item, PoseStack poseStack, SubmitNodeCollector collector, int light,
            float x, float y, float z, float scale, float yRot, float xRot) {
        if (item == null || item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.scale(scale, scale, scale);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    static void submitSieveInputBlock(BlockModelRenderState model, float progressRatio, PoseStack poseStack,
            SubmitNodeCollector collector, int light) {
        if (model == null || model.isEmpty()) {
            return;
        }
        float clampedProgress = Math.max(0.0F, Math.min(1.0F, progressRatio));
        float layerHeight = 0.16F - clampedProgress * 0.14F;
        poseStack.pushPose();
        poseStack.translate(0.03125F, 0.822F, 0.03125F);
        poseStack.scale(0.9375F, layerHeight, 0.9375F);
        model.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    static void submitCrucibleInputBlock(BlockModelRenderState model, float progressRatio, PoseStack poseStack,
            SubmitNodeCollector collector, int light) {
        submitCrucibleInputBlock(model, progressRatio, 0.25F, poseStack, collector, light);
    }

    static void submitCrucibleInputBlock(BlockModelRenderState model, float progressRatio, float fillRatio,
            PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (model == null || model.isEmpty()) {
            return;
        }
        float clampedProgress = Math.max(0.0F, Math.min(1.0F, progressRatio));
        float clampedFill = Math.max(0.0F, Math.min(1.0F, fillRatio));
        float layerHeight = Math.max(0.025F, 0.055F + clampedFill * 0.68F - clampedProgress * 0.055F);
        poseStack.pushPose();
        poseStack.translate(0.07F, 0.191F, 0.07F);
        poseStack.scale(0.86F, layerHeight, 0.86F);
        model.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    static void submitInfestingLeavesOverlay(String phase, String variant, float progressRatio, PoseStack poseStack,
            SubmitNodeCollector collector, int light) {
        submitInfestingLeavesOverlay(phase, variant, progressRatio, 1.0F, poseStack, collector, light);
    }

    static void submitInfestingLeavesOverlay(String phase, String variant, float progressRatio, float maxAlphaRatio,
            PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (phase == null || phase.isEmpty() || variant == null || variant.isEmpty()) {
            return;
        }
        float clamped = Math.max(0.0F, Math.min(1.0F, progressRatio));
        float clampedAlpha = Math.max(0.0F, Math.min(1.0F, maxAlphaRatio));
        if (clamped <= 0.0F || clampedAlpha <= 0.0F) {
            return;
        }
        float eased = clamped * clamped * (3.0F - 2.0F * clamped);
        int alpha = Math.max(24, Math.round(255.0F * eased * clampedAlpha));
        Identifier texture = Identifier.fromNamespaceAndPath(ExampleMod.MODID,
                "textures/block/" + phase + "_" + variant + "_leaves.png");
        submitTexturedCuboid(texture, -0.0015F, 1.0015F, -0.0015F, 1.0015F, poseStack, collector, light,
                255, 255, 255, alpha);
    }

    static ItemDisplayContext displayContext() {
        return ItemDisplayContext.FIXED;
    }

    private static FluidVisual fluidVisual(String fluidId) {
        if (fluidId.endsWith("lava")) {
            return new FluidVisual(LAVA_TEXTURE, 255, 255, 255, 255, true, ANIMATED_FLUID_FRAME_V);
        }
        if (fluidId.endsWith("witch_water")) {
            return new FluidVisual(WITCH_WATER_TEXTURE, 255, 255, 255, 224, false, ANIMATED_FLUID_FRAME_V);
        }
        if (fluidId.endsWith("sea_water")) {
            return new FluidVisual(SEA_WATER_TEXTURE, 255, 255, 255, 224, false, ANIMATED_FLUID_FRAME_V);
        }
        return new FluidVisual(WATER_TEXTURE, 64, 118, 228, 224, false, ANIMATED_FLUID_FRAME_V);
    }

    private static Identifier compostTexture(int amount, int capacity, float progress, boolean ready) {
        if (ready) {
            return DIRT_TEXTURE;
        }
        if (amount < capacity || progress < 0.35F) {
            return DIRT_TEXTURE;
        }
        return progress < 0.75F ? ROOTED_DIRT_TEXTURE : DIRT_TEXTURE;
    }

    private static void submitTexturedSurface(Identifier texture, float y, float min, float max, PoseStack poseStack,
            SubmitNodeCollector collector, int light, int red, int green, int blue, int alpha) {
        submitTexturedSurface(texture, y, min, max, 1.0F, poseStack, collector, light, red, green, blue, alpha);
    }

    private static void submitTexturedSurface(Identifier texture, float y, float min, float max, float vMax,
            PoseStack poseStack, SubmitNodeCollector collector, int light, int red, int green, int blue, int alpha) {
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(texture),
                (pose, consumer) -> {
                    vertex(consumer, pose, min, y, min, 0.0F, 0.0F, light, red, green, blue, alpha, 0.0F, 1.0F, 0.0F);
                    vertex(consumer, pose, min, y, max, 0.0F, vMax, light, red, green, blue, alpha, 0.0F, 1.0F, 0.0F);
                    vertex(consumer, pose, max, y, max, 1.0F, vMax, light, red, green, blue, alpha, 0.0F, 1.0F, 0.0F);
                    vertex(consumer, pose, max, y, min, 1.0F, 0.0F, light, red, green, blue, alpha, 0.0F, 1.0F, 0.0F);
                    vertex(consumer, pose, max, y - 0.002F, min, 1.0F, 0.0F, light, red, green, blue, alpha, 0.0F, -1.0F, 0.0F);
                    vertex(consumer, pose, max, y - 0.002F, max, 1.0F, vMax, light, red, green, blue, alpha, 0.0F, -1.0F, 0.0F);
                    vertex(consumer, pose, min, y - 0.002F, max, 0.0F, vMax, light, red, green, blue, alpha, 0.0F, -1.0F, 0.0F);
                    vertex(consumer, pose, min, y - 0.002F, min, 0.0F, 0.0F, light, red, green, blue, alpha, 0.0F, -1.0F, 0.0F);
                });
    }

    private static void submitTexturedCuboid(Identifier texture, float yMin, float yMax, float min, float max,
            PoseStack poseStack, SubmitNodeCollector collector, int light, int red, int green, int blue, int alpha) {
        float bottom = Math.max(0.0F, Math.min(yMin, yMax - 0.002F));
        float top = Math.max(bottom + 0.002F, yMax);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(texture),
                (pose, consumer) -> {
                    vertex(consumer, pose, min, top, min, 0.0F, 0.0F, light, red, green, blue, alpha, 0.0F, 1.0F, 0.0F);
                    vertex(consumer, pose, min, top, max, 0.0F, 1.0F, light, red, green, blue, alpha, 0.0F, 1.0F, 0.0F);
                    vertex(consumer, pose, max, top, max, 1.0F, 1.0F, light, red, green, blue, alpha, 0.0F, 1.0F, 0.0F);
                    vertex(consumer, pose, max, top, min, 1.0F, 0.0F, light, red, green, blue, alpha, 0.0F, 1.0F, 0.0F);

                    vertex(consumer, pose, min, bottom, max, 0.0F, 1.0F, light, red, green, blue, alpha, 0.0F, 0.0F, 1.0F);
                    vertex(consumer, pose, max, bottom, max, 1.0F, 1.0F, light, red, green, blue, alpha, 0.0F, 0.0F, 1.0F);
                    vertex(consumer, pose, max, top, max, 1.0F, 0.0F, light, red, green, blue, alpha, 0.0F, 0.0F, 1.0F);
                    vertex(consumer, pose, min, top, max, 0.0F, 0.0F, light, red, green, blue, alpha, 0.0F, 0.0F, 1.0F);

                    vertex(consumer, pose, max, bottom, min, 0.0F, 1.0F, light, red, green, blue, alpha, 0.0F, 0.0F, -1.0F);
                    vertex(consumer, pose, min, bottom, min, 1.0F, 1.0F, light, red, green, blue, alpha, 0.0F, 0.0F, -1.0F);
                    vertex(consumer, pose, min, top, min, 1.0F, 0.0F, light, red, green, blue, alpha, 0.0F, 0.0F, -1.0F);
                    vertex(consumer, pose, max, top, min, 0.0F, 0.0F, light, red, green, blue, alpha, 0.0F, 0.0F, -1.0F);

                    vertex(consumer, pose, min, bottom, min, 0.0F, 1.0F, light, red, green, blue, alpha, -1.0F, 0.0F, 0.0F);
                    vertex(consumer, pose, min, bottom, max, 1.0F, 1.0F, light, red, green, blue, alpha, -1.0F, 0.0F, 0.0F);
                    vertex(consumer, pose, min, top, max, 1.0F, 0.0F, light, red, green, blue, alpha, -1.0F, 0.0F, 0.0F);
                    vertex(consumer, pose, min, top, min, 0.0F, 0.0F, light, red, green, blue, alpha, -1.0F, 0.0F, 0.0F);

                    vertex(consumer, pose, max, bottom, max, 0.0F, 1.0F, light, red, green, blue, alpha, 1.0F, 0.0F, 0.0F);
                    vertex(consumer, pose, max, bottom, min, 1.0F, 1.0F, light, red, green, blue, alpha, 1.0F, 0.0F, 0.0F);
                    vertex(consumer, pose, max, top, min, 1.0F, 0.0F, light, red, green, blue, alpha, 1.0F, 0.0F, 0.0F);
                    vertex(consumer, pose, max, top, max, 0.0F, 0.0F, light, red, green, blue, alpha, 1.0F, 0.0F, 0.0F);
                });
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u,
            float v, int light, int red, int green, int blue, int alpha, float normalX, float normalY, float normalZ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, normalX, normalY, normalZ);
    }

    private record FluidVisual(Identifier texture, int red, int green, int blue, int alpha, boolean fullBright,
            float frameV) {
        int light(int fallback) {
            return fullBright ? LAVA_LIGHT : fallback;
        }
    }
}
