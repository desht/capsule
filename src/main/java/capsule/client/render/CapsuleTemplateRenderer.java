package capsule.client.render;

import capsule.client.render.vbo.MultiVBORenderer;
import capsule.structure.CapsuleTemplate;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.IClearable;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.BitSetVoxelShapePart;
import net.minecraft.util.math.shapes.VoxelShapePart;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CapsuleTemplateRenderer {
    private static final Logger LOGGER = LogManager.getLogger();
    public MultiVBORenderer renderBuffer;
    public FakeWorld templateWorld = null;
    private boolean isWorldDirty = true;
    private PlacementSettings lastPlacementSettings;

    public void renderTemplate(MatrixStack poseStack, PlayerEntity player, BlockPos destPos) {
        if (player == null)
            return;

        final Minecraft minecraft = Minecraft.getInstance();

        final Vector3d cameraView = minecraft.gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(destPos.getX() - cameraView.x, destPos.getY() - cameraView.y, destPos.getZ() - cameraView.z);

        renderTemplate(poseStack, cameraView, player);

        poseStack.popPose();
    }

    public void renderTemplate(MatrixStack poseStack, Vector3d cameraView, PlayerEntity player) {
        if (renderBuffer != null) {
            renderBuffer.render(poseStack.last().pose()); //Actually draw whats in the buffer
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        renderBuffer = MultiVBORenderer.of((buffer) -> {
            World level = player.level;
            IVertexBuilder builder = buffer.getBuffer(CustomRenderType.VISUAL_BLOCK);

            BlockRendererDispatcher dispatcher = minecraft.getBlockRenderer();

            MatrixStack stack = new MatrixStack(); //Create a new matrix stack for use in the buffer building process
            stack.pushPose(); //Save position

            for (Map.Entry<BlockPos, BlockState> entry : templateWorld.entrySet()) {
                BlockPos targetPos = entry.getKey();
                BlockState state = entry.getValue();

                stack.pushPose(); //Save position again
                stack.translate(targetPos.getX(), targetPos.getY(), targetPos.getZ());

                IBakedModel ibakedmodel = dispatcher.getBlockModel(state);
                BlockColors blockColors = minecraft.getBlockColors();
                int color = blockColors.getColor(state, templateWorld, targetPos, 0);

                float f = (float) (color >> 16 & 255) / 255.0F;
                float f1 = (float) (color >> 8 & 255) / 255.0F;
                float f2 = (float) (color & 255) / 255.0F;
                try {
                    if (state.getRenderShape() == BlockRenderType.MODEL) {
                        for (Direction direction : Direction.values()) {
                            if (Block.shouldRenderFace(state, templateWorld, targetPos, direction) && !(templateWorld.getBlockState(targetPos.relative(direction)).getBlock().equals(state.getBlock()))) {
                                if (state.getMaterial().isSolidBlocking()) {
                                    renderModelBrightnessColorQuads(stack.last(), builder, f, f1, f2, 1, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getSeed(targetPos)), EmptyModelData.INSTANCE), 15728640, OverlayTexture.NO_OVERLAY);
                                } else {
                                    renderModelBrightnessColorQuads(stack.last(), builder, f, f1, f2, 1, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getSeed(targetPos)), EmptyModelData.INSTANCE), 15728640, OverlayTexture.NO_OVERLAY);
                                }
                            }
                        }
                        if (state.getMaterial().isSolidBlocking())
                            renderModelBrightnessColorQuads(stack.last(), builder, f, f1, f2, 1, ibakedmodel.getQuads(state, null, new Random(MathHelper.getSeed(targetPos)), EmptyModelData.INSTANCE), 15728640, OverlayTexture.NO_OVERLAY);
                        else
                            renderModelBrightnessColorQuads(stack.last(), builder, f, f1, f2, 1, ibakedmodel.getQuads(state, null, new Random(MathHelper.getSeed(targetPos)), EmptyModelData.INSTANCE), 15728640, OverlayTexture.NO_OVERLAY);
                    } else {
                        FluidState ifluidstate = state.getFluidState();
                        if (!ifluidstate.isEmpty()) {
                            renderFluid(stack, targetPos, level, builder, ifluidstate);
                        } else if (state.getRenderShape() == BlockRenderType.ENTITYBLOCK_ANIMATED) {
                            ItemStack itemstack = new ItemStack(state.getBlock());
                            itemstack.getItem().getItemStackTileEntityRenderer().renderByItem(itemstack, ItemCameraTransforms.TransformType.NONE, stack, buffer, 15728640, OverlayTexture.NO_OVERLAY);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.trace("Caught exception whilst rendering {}.", state, e);
                }
                stack.popPose(); // Load the position we saved earlier
            }
            stack.popPose(); //Load after loop
        });

        // renderBuffer.sort((float) (-destPos.getX() + cameraView.x()), (float) (-destPos.getY() + cameraView.y()), (float) (-destPos.getZ() + cameraView.z()));
        renderBuffer.render(poseStack.last().pose()); //Actually draw whats in the buffer
    }

    private static void renderFluid(MatrixStack matrixStack, BlockPos destOriginPos, IBlockDisplayReader world, IVertexBuilder buffer, FluidState ifluidstate) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(ifluidstate.getFluidState().getType().getAttributes().getStillTexture());

        float minU = sprite.getU0();
        float maxU = Math.min(minU + (sprite.getU1() - minU) * 1, sprite.getU1());
        float minV = sprite.getV0();
        float maxV = Math.min(minV + (sprite.getV1() - minV) * 1, sprite.getV1());
        int waterColor = ifluidstate.getFluidState().getType().getAttributes().getColor(world, destOriginPos);
        float red = (float) (waterColor >> 16 & 255) / 255.0F;
        float green = (float) (waterColor >> 8 & 255) / 255.0F;
        float blue = (float) (waterColor & 255) / 255.0F;

        Matrix4f matrix = matrixStack.last().pose();

        vertex(buffer, maxU, minV, red, green, blue, matrix, 0, 1, 0);
        vertex(buffer, minU, minV, red, green, blue, matrix, 0, 1, 1);
        vertex(buffer, minU, maxV, red, green, blue, matrix, 1, 1, 1);
        vertex(buffer, maxU, maxV, red, green, blue, matrix, 1, 1, 0);
    }

    private static void vertex(IVertexBuilder buffer, float maxU, float minV, float red, float green, float blue, Matrix4f matrix, int x, int y, int z) {
        buffer.vertex(matrix, x, y, z).color(red, green, blue, 1.0F).uv(maxU, minV).uv2(256).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    public static void renderModelBrightnessColorQuads(MatrixStack.Entry matrixEntry, IVertexBuilder builder, float red, float green, float blue, float alpha, List<BakedQuad> listQuads, int combinedLightsIn, int combinedOverlayIn) {
        for (BakedQuad bakedquad : listQuads) {
            float f;
            float f1;
            float f2;

            if (bakedquad.isTinted()) {
                f = red * 1f;
                f1 = green * 1f;
                f2 = blue * 1f;
            } else {
                f = 1f;
                f1 = 1f;
                f2 = 1f;
            }

            builder.addVertexData(matrixEntry, bakedquad, f, f1, f2, alpha, combinedLightsIn, combinedOverlayIn);
        }
    }

    public boolean changeTemplateIfDirty(CapsuleTemplate template, World world, BlockPos destPos, BlockPos offPos, PlacementSettings placementSettings, int placeFlag) {
        if (lastPlacementSettings == null ||
                (placementSettings != null && (
                        placementSettings.getRotation() != lastPlacementSettings.getRotation()
                                || placementSettings.getMirror() != lastPlacementSettings.getMirror()))
        ) {
            isWorldDirty = true;
        }
        if (!isWorldDirty) return true;
        if (renderBuffer != null) renderBuffer.close();
        renderBuffer = null;
        lastPlacementSettings = placementSettings;
        if (template.palettes.isEmpty()) {
            return false;
        } else {
            templateWorld = new FakeWorld(world);

            List<Template.BlockInfo> list = CapsuleTemplate.Palette.getRandomPalette(placementSettings, template.palettes, destPos).blocks();
            if (!list.isEmpty() && template.size.getX() >= 1 && template.size.getY() >= 1 && template.size.getZ() >= 1) {
                MutableBoundingBox mutableboundingbox = placementSettings.getBoundingBox();
                List<BlockPos> list1 = Lists.newArrayListWithCapacity(placementSettings.shouldKeepLiquids() ? list.size() : 0);
                List<Pair<BlockPos, CompoundNBT>> list2 = Lists.newArrayListWithCapacity(list.size());
                int i = Integer.MAX_VALUE;
                int j = Integer.MAX_VALUE;
                int k = Integer.MAX_VALUE;
                int l = Integer.MIN_VALUE;
                int i1 = Integer.MIN_VALUE;
                int j1 = Integer.MIN_VALUE;

                for (Template.BlockInfo template$blockinfo : CapsuleTemplate.processBlockInfos(template, templateWorld, offPos, placementSettings, list)) {
                    BlockPos blockpos = template$blockinfo.pos;
                    if (mutableboundingbox == null || mutableboundingbox.isInside(blockpos)) {
                        FluidState fluidstate = placementSettings.shouldKeepLiquids() ? templateWorld.getFluidState(blockpos) : null;
                        BlockState blockstate = template$blockinfo.state.mirror(placementSettings.getMirror()).rotate(templateWorld, blockpos, placementSettings.getRotation());
                        if (template$blockinfo.nbt != null) {
                            TileEntity tileentity = templateWorld.getBlockEntity(blockpos);
                            IClearable.tryClear(tileentity);
                            templateWorld.setBlock(blockpos, Blocks.BARRIER.defaultBlockState(), 20);
                        }

                        if (templateWorld.setBlock(blockpos, blockstate, placeFlag)) {
                            i = Math.min(i, blockpos.getX());
                            j = Math.min(j, blockpos.getY());
                            k = Math.min(k, blockpos.getZ());
                            l = Math.max(l, blockpos.getX());
                            i1 = Math.max(i1, blockpos.getY());
                            j1 = Math.max(j1, blockpos.getZ());
                            list2.add(Pair.of(blockpos, template$blockinfo.nbt));

                            if (fluidstate != null && blockstate.getBlock() instanceof ILiquidContainer) {
                                ((ILiquidContainer) blockstate.getBlock()).placeLiquid(templateWorld, blockpos, blockstate, fluidstate);
                                if (!fluidstate.isSource()) {
                                    list1.add(blockpos);
                                }
                            }
                        }
                    }
                }


                boolean flag = true;
                Direction[] adirection = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

                while (flag && !list1.isEmpty()) {
                    flag = false;
                    Iterator<BlockPos> iterator = list1.iterator();

                    while (iterator.hasNext()) {
                        BlockPos blockpos2 = iterator.next();
                        BlockPos blockpos3 = blockpos2;
                        FluidState fluidstate2 = templateWorld.getFluidState(blockpos2);

                        for (int k1 = 0; k1 < adirection.length && !fluidstate2.isSource(); ++k1) {
                            BlockPos blockpos1 = blockpos3.relative(adirection[k1]);
                            FluidState fluidstate1 = templateWorld.getFluidState(blockpos1);
                            if (fluidstate1.getHeight(templateWorld, blockpos1) > fluidstate2.getHeight(templateWorld, blockpos3) || fluidstate1.isSource() && !fluidstate2.isSource()) {
                                fluidstate2 = fluidstate1;
                                blockpos3 = blockpos1;
                            }
                        }

                        if (fluidstate2.isSource()) {
                            BlockState blockstate2 = templateWorld.getBlockState(blockpos2);
                            Block block = blockstate2.getBlock();
                            if (block instanceof ILiquidContainer) {
                                ((ILiquidContainer) block).placeLiquid(templateWorld, blockpos2, blockstate2, fluidstate2);
                                flag = true;
                                iterator.remove();
                            }
                        }
                    }
                }

                if (i <= l) {
                    if (!placementSettings.getKnownShape()) {
                        VoxelShapePart voxelshapepart = new BitSetVoxelShapePart(l - i + 1, i1 - j + 1, j1 - k + 1);
                        int l1 = i;
                        int i2 = j;
                        int j2 = k;

                        for (Pair<BlockPos, CompoundNBT> pair1 : list2) {
                            BlockPos blockpos5 = pair1.getFirst();
                            voxelshapepart.setFull(blockpos5.getX() - l1, blockpos5.getY() - i2, blockpos5.getZ() - j2, true, true);
                        }

                        Template.updateShapeAtEdge(templateWorld, placeFlag, voxelshapepart, l1, i2, j2);
                    }

                    for (Pair<BlockPos, CompoundNBT> pair : list2) {
                        BlockPos blockpos4 = pair.getFirst();
                        if (!placementSettings.getKnownShape()) {
                            BlockState blockstate1 = templateWorld.getBlockState(blockpos4);
                            BlockState blockstate3 = Block.updateFromNeighbourShapes(blockstate1, templateWorld, blockpos4);
                            if (blockstate1 != blockstate3) {
                                templateWorld.setBlock(blockpos4, blockstate3, placeFlag & -2 | 16);
                            }

                            templateWorld.blockUpdated(blockpos4, blockstate3.getBlock());
                        }
                    }
                }
                isWorldDirty = false;
                return true;
            } else {
                isWorldDirty = false;
                return false;
            }
        }
    }

    public void setWorldDirty() {
        isWorldDirty = true;
    }
}