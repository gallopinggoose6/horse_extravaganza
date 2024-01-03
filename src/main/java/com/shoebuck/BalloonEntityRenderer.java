package com.shoebuck;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class BalloonEntityRenderer extends EntityRenderer<EntityRisingBalloon> {
	   private final BlockRenderDispatcher dispatcher;

	   public BalloonEntityRenderer(EntityRendererProvider.Context ctx) {
	      super(ctx);
	      this.shadowRadius = 0.5F;
	      this.dispatcher = ctx.getBlockRenderDispatcher();
	   }

	   public void render(EntityRisingBalloon entity, float p_114635_, float p_114636_, PoseStack p_114637_, MultiBufferSource p_114638_, int p_114639_) {
	      BlockState blockstate = entity.getBlockState();
	      if (blockstate.getRenderShape() == RenderShape.MODEL) {
	         Level level = entity.level();
	         if (blockstate != level.getBlockState(entity.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
	            p_114637_.pushPose();
	            BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
	            p_114637_.translate(-0.5D, 0.0D, -0.5D);
	            var model = this.dispatcher.getBlockModel(blockstate);
	            for (var renderType : model.getRenderTypes(blockstate, RandomSource.create(blockstate.getSeed(entity.getStartPos())), net.minecraftforge.client.model.data.ModelData.EMPTY))
	               this.dispatcher.getModelRenderer().tesselateBlock(level, model, blockstate, blockpos, p_114637_, p_114638_.getBuffer(renderType), false, RandomSource.create(), blockstate.getSeed(entity.getStartPos()), OverlayTexture.NO_OVERLAY, net.minecraftforge.client.model.data.ModelData.EMPTY, renderType);
	            p_114637_.popPose();
	            super.render(entity, p_114635_, p_114636_, p_114637_, p_114638_, p_114639_);
	         }
	      }
	   }

	   public ResourceLocation getTextureLocation(EntityRisingBalloon p_114632_) {
	      return TextureAtlas.LOCATION_BLOCKS;
	   }
	}
