package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.facades.Facing6
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.setAir
import chylex.hee.system.util.withFacing
import net.minecraft.block.Block
import net.minecraft.block.BlockDirectional.FACING
import net.minecraft.block.state.BlockFaceShape.SOLID
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

class BlockGloomtorch(builder: BlockBuilder) : BlockSimple(builder){
	private companion object{
		private const val BB_SIDE_MIN = 0.421875
		private const val BB_SIDE_MAX = 0.578125
		private const val BB_TOP = 0.59375
		
		private val BOUNDING_BOX = mapOf(
			UP    to AxisAlignedBB(BB_SIDE_MIN, 0.0, BB_SIDE_MIN, BB_SIDE_MAX, BB_TOP, BB_SIDE_MAX),
			DOWN  to AxisAlignedBB(BB_SIDE_MIN, 1.0 - BB_TOP, BB_SIDE_MIN, BB_SIDE_MAX, 1.0, BB_SIDE_MAX),
			NORTH to AxisAlignedBB(BB_SIDE_MIN, BB_SIDE_MIN, 1.0 - BB_TOP, BB_SIDE_MAX, BB_SIDE_MAX, 1.0),
			SOUTH to AxisAlignedBB(BB_SIDE_MIN, BB_SIDE_MIN, 0.0, BB_SIDE_MAX, BB_SIDE_MAX, BB_TOP),
			EAST  to AxisAlignedBB(0.0, BB_SIDE_MIN, BB_SIDE_MIN, BB_TOP, BB_SIDE_MAX, BB_SIDE_MAX),
			WEST  to AxisAlignedBB(1.0 - BB_TOP, BB_SIDE_MIN, BB_SIDE_MIN, 1.0, BB_SIDE_MAX, BB_SIDE_MAX)
		)
		
		private fun canPlaceGloomtorchAt(world: World, pos: BlockPos, facing: EnumFacing): Boolean{
			val supportingPos = pos.offset(facing.opposite)
			val state = supportingPos.getState(world)
			val block = state.block
			
			return (facing == UP && block.canPlaceTorchOnTop(state, world, supportingPos)) || (state.getBlockFaceShape(world, supportingPos, facing) == SOLID && !isExceptBlockForAttachWithPiston(block))
		}
	}
	
	init{
		defaultState = blockState.baseState.withFacing(UP)
	}
	
	override fun createBlockState() = BlockStateContainer(this, FACING)
	
	// Placement rules
	
	override fun canPlaceBlockAt(world: World, pos: BlockPos): Boolean{
		return Facing6.any { canPlaceGloomtorchAt(world, pos, it) }
	}
	
	override fun getStateForPlacement(world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase, hand: EnumHand): IBlockState{
		return if (canPlaceGloomtorchAt(world, pos, facing))
			this.withFacing(facing)
		else
			Facing6.firstOrNull { canPlaceGloomtorchAt(world, pos, it) }?.let(this::withFacing) ?: defaultState
	}
	
	override fun neighborChanged(state: IBlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos){
		if (!canPlaceGloomtorchAt(world, pos, state[FACING]) && pos.getBlock(world) === this){
			dropBlockAsItem(world, pos, state, 0)
			pos.setAir(world)
		}
	}
	
	// State handling
	
	override fun withRotation(state: IBlockState, rot: Rotation): IBlockState{
		return state.withFacing(rot.rotate(state[FACING]))
	}
	
	override fun withMirror(state: IBlockState, mirror: Mirror): IBlockState{
		return state.withFacing(mirror.mirror(state[FACING]))
	}
	
	override fun getMetaFromState(state: IBlockState) = state[FACING].index
	override fun getStateFromMeta(meta: Int) = this.withFacing(EnumFacing.byIndex(meta))
	
	// Shape and rendering
	
	override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB{
		return BOUNDING_BOX[state[FACING]] ?: BOUNDING_BOX.getValue(UP)
	}
	
	override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) = UNDEFINED
	
	override fun isFullCube(state: IBlockState) = false
	override fun isOpaqueCube(state: IBlockState) = false
	override fun getRenderLayer() = CUTOUT
}
