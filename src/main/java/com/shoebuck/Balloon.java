package com.shoebuck;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class Balloon extends Block implements Risable {
	public Balloon(BlockBehaviour.Properties props) {
	      super(props);
	   }

	   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState state2, boolean bool) {
	      level.scheduleTick(pos, this, this.getDelayAfterPlace());
	   }

	   public BlockState updateShape(BlockState state1, Direction dir, BlockState state2, LevelAccessor accessor, BlockPos pos1, BlockPos pos2) {
	      accessor.scheduleTick(pos1, this, this.getDelayAfterPlace());
	      return super.updateShape(state1, dir, state2, accessor, pos1, pos2);
	   }

	   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rnd) {
	      if ((level.getBlockState(pos.above()).isAir() && pos.getY() <= level.getMaxBuildHeight()) && !(level.getBlockState(pos.below()).getBlock() instanceof Rope)) {
	    	 EntityRisingBalloon.rise(level, pos, state);
	         level.removeBlock(pos, false);
	      }
	   }

	   protected int getDelayAfterPlace() {
	      return 2;
	   }
}