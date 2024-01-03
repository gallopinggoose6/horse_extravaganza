package com.shoebuck;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface Risable {
   default void onHit(Level level, BlockPos pos, BlockState state1, BlockState state2, EntityRisingBalloon entity) {
   }

   default void onBrokenAfterRise(Level level, BlockPos pos, EntityRisingBalloon entity) {
   }

   default DamageSource getFallDamageSource(Entity entity) {
      return entity.damageSources().fallingBlock(entity);
   }
}