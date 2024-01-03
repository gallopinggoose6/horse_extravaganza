package com.shoebuck;

import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class EntityRisingBalloon extends Entity {
	   private BlockState blockState = Blocks.SAND.defaultBlockState();
	   public int time;
	   public int stalltime;
	   @Nullable
	   public CompoundTag blockData;
	   protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(EntityRisingBalloon.class, EntityDataSerializers.BLOCK_POS);
	   
	   public EntityRisingBalloon(EntityType<? extends EntityRisingBalloon> type, Level level) {
	      super(type, level);
	   }

	   private EntityRisingBalloon(Level level, double xo, double yo, double zo, BlockState state) {
	      this(ShoeBuck.BALLOON_ENTITY.get(), level);
	      this.blockState = state;
	      this.blocksBuilding = true;
	      this.setPos(xo, yo, zo);
	      this.setDeltaMovement(Vec3.ZERO);
	      this.xo = xo;
	      this.yo = yo;
	      this.zo = zo;
	      this.setStartPos(this.blockPosition());
	   }
	   
	   @Override
	   public boolean canBeCollidedWith() {
		   return true;
	   }

	   public static EntityRisingBalloon rise(Level level, BlockPos pos, BlockState state) {
	      EntityRisingBalloon risingballoon = new EntityRisingBalloon(level, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, state);
	      level.addFreshEntity(risingballoon);
	      return risingballoon;
	   }
	   
	   public boolean isAttackable() {
	      return false;
	   }

	   public void setStartPos(BlockPos pos) {
	      this.entityData.set(DATA_START_POS, pos);
	   }

	   public BlockPos getStartPos() {
	      return this.entityData.get(DATA_START_POS);
	   }

	   protected Entity.MovementEmission getMovementEmission() {
	      return Entity.MovementEmission.NONE;
	   }

	   protected void defineSynchedData() {
	      this.entityData.define(DATA_START_POS, BlockPos.ZERO);
	   }

	   public boolean isPickable() {
	      return !this.isRemoved();
	   }

	   public void tick() {
	      if (this.blockState.isAir()) {
	         this.discard();
	      } else {
	         Block block = this.blockState.getBlock();
	         ++this.time;
	         if (!this.isNoGravity()) {
	            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, +0.04D, 0.0D));
	         }

	         this.move(MoverType.SELF, this.getDeltaMovement());
	         Level level = this.level();
	         if (!level.isClientSide) {
	            BlockPos blockpos = this.blockPosition();
	            double d0 = this.getDeltaMovement().lengthSqr();
	            if (d0 > 1.0D) {
	               BlockHitResult blockhitresult = level.clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
	               if (blockhitresult.getType() != BlockHitResult.Type.MISS) {
	                  blockpos = blockhitresult.getBlockPos();
	               }
	            }

	            if (!this.onGround()) {
	               if (!level.isClientSide && (this.time > 100 && (blockpos.getY() >= level.getMaxBuildHeight() || blockpos.getY() < level.getMinBuildHeight()) || this.time > 600)) {
	                  this.discard();
	               }
	            } else {
	               BlockState blockstate = level.getBlockState(blockpos);
	               this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
	               if (!blockstate.is(Blocks.MOVING_PISTON)) {
	                     this.discard();
	                     this.callOnBrokenAfterFall(block, blockpos);
	               }
	            }
	            
	            if (this.getDeltaMovement().y == 0.0D) {
	            	if(this.stalltime > 1) {
	            		//level.setBlockAndUpdate(blockpos, this.blockState);
	            		level.setBlock(blockpos, this.blockState, 30);
	            		//((ServerLevel)level).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockpos, this.blockState));
	            		this.discard();
	            	}
	            	++stalltime;
	            } else {
	            	this.stalltime = 0;
	            }
	         }

	         this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
	      }
	   }

	   public void callOnBrokenAfterFall(Block block, BlockPos pos) {
	      if (block instanceof Risable) {
	         ((Risable)block).onBrokenAfterRise(this.level(), pos, this);
	      }

	   }

	   protected void addAdditionalSaveData(CompoundTag p_31973_) {
	      p_31973_.put("BlockState", NbtUtils.writeBlockState(this.blockState));
	      p_31973_.putInt("Time", this.time);
	      if (this.blockData != null) {
	         p_31973_.put("TileEntityData", this.blockData);
	      }
	   }

	   protected void readAdditionalSaveData(CompoundTag p_31964_) {
	      this.blockState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), p_31964_.getCompound("BlockState"));
	      this.time = p_31964_.getInt("Time");

	      if (p_31964_.contains("TileEntityData", 10)) {
	         this.blockData = p_31964_.getCompound("TileEntityData");
	      }

	      if (this.blockState.isAir()) {
	         this.blockState = Blocks.SAND.defaultBlockState();
	      }

	   }

	   public boolean displayFireAnimation() {
	      return false;
	   }

	   public void fillCrashReportCategory(CrashReportCategory p_31962_) {
	      super.fillCrashReportCategory(p_31962_);
	      p_31962_.setDetail("Immitating BlockState", this.blockState.toString());
	   }

	   public BlockState getBlockState() {
	      return this.blockState;
	   }

	   protected Component getTypeName() {
	      return Component.translatable("entity.minecraft.falling_block_type", this.blockState.getBlock().getName());
	   }

	   public boolean onlyOpCanSetNbt() {
	      return true;
	   }

	   public Packet<ClientGamePacketListener> getAddEntityPacket() {
	      return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
	   }

	   public void recreateFromPacket(ClientboundAddEntityPacket p_149654_) {
	      super.recreateFromPacket(p_149654_);
	      this.blockState = Block.stateById(p_149654_.getData());
	      this.blocksBuilding = true;
	      double d0 = p_149654_.getX();
	      double d1 = p_149654_.getY();
	      double d2 = p_149654_.getZ();
	      this.setPos(d0, d1, d2);
	      this.setStartPos(this.blockPosition());
	   }
	}
