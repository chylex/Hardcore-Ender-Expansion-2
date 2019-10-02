package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityIgneousPlate
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.game.entity.technical.EntityTechnicalIgneousPlateLogic
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Facing6
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setAir
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.BlockFurnace
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.EnumBlockRenderType.ENTITYBLOCK_ANIMATED
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

class BlockIgneousPlate(builder: BlockBuilder) : BlockSimple(builder), ITileEntityProvider{
	companion object{
		val FACING_NOT_DOWN = Property.facing("facing", Facing6.minusElement(DOWN))
		
		private const val BB_SIDE_MIN = 0.125
		private const val BB_SIDE_MAX = 0.875
		private const val BB_TOP = 0.125
		
		private val BOUNDING_BOX = mapOf(
			UP    to AxisAlignedBB(BB_SIDE_MIN, 0.0, BB_SIDE_MIN, BB_SIDE_MAX, BB_TOP, BB_SIDE_MAX),
			NORTH to AxisAlignedBB(BB_SIDE_MIN, BB_SIDE_MIN, 1.0 - BB_TOP, BB_SIDE_MAX, BB_SIDE_MAX, 1.0),
			SOUTH to AxisAlignedBB(BB_SIDE_MIN, BB_SIDE_MIN, 0.0, BB_SIDE_MAX, BB_SIDE_MAX, BB_TOP),
			EAST  to AxisAlignedBB(0.0, BB_SIDE_MIN, BB_SIDE_MIN, BB_TOP, BB_SIDE_MAX, BB_SIDE_MAX),
			WEST  to AxisAlignedBB(1.0 - BB_TOP, BB_SIDE_MIN, BB_SIDE_MIN, 1.0, BB_SIDE_MAX, BB_SIDE_MAX)
		)
		
		private fun canPlacePlateAt(world: World, pos: BlockPos, facing: EnumFacing): Boolean{
			val furnacePos = pos.offset(facing.opposite)
			val state = furnacePos.getState(world)
			
			return (
				state.propertyKeys.contains(BlockFurnace.FACING) &&
				state[BlockFurnace.FACING] != facing &&
				furnacePos.getTile<TileEntityFurnace>(world) != null
			)
		}
	}
	
	init{
		defaultState = blockState.baseState.with(FACING_NOT_DOWN, UP)
	}
	
	override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, FACING_NOT_DOWN)
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityIgneousPlate()
	}
	
	// Placement rules
	
	override fun canPlaceBlockAt(world: World, pos: BlockPos): Boolean{
		return FACING_NOT_DOWN.allowedValues.any { canPlacePlateAt(world, pos, it) }
	}
	
	override fun getStateForPlacement(world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase, hand: EnumHand): IBlockState{
		return if (canPlacePlateAt(world, pos, facing))
			defaultState.with(FACING_NOT_DOWN, facing)
		else
			FACING_NOT_DOWN.allowedValues.firstOrNull { canPlacePlateAt(world, pos, it) }?.let { defaultState.with(FACING_NOT_DOWN, it) } ?: defaultState
	}
	
	override fun onBlockAdded(world: World, pos: BlockPos, state: IBlockState){
		pos.offset(state[FACING_NOT_DOWN].opposite).getTile<TileEntityFurnace>(world)?.let(EntityTechnicalIgneousPlateLogic.Companion::createForFurnace)
	}
	
	override fun neighborChanged(state: IBlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos){
		if (!canPlacePlateAt(world, pos, state[FACING_NOT_DOWN]) && pos.getBlock(world) === this){
			dropBlockAsItem(world, pos, state, 0)
			pos.setAir(world)
		}
	}
	
	// Interactions
	
	override fun getBlockHardness(state: IBlockState, world: World, pos: BlockPos): Float{
		val tile = pos.getTile<TileEntityIgneousPlate>(world) ?: return 0F
		
		return when{
			tile.isOverheating -> 10F
			tile.isWorking -> 4F
			else -> 0F
		}
	}
	
	override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		val heldItem = player.getHeldItem(hand)
		
		if (heldItem.item === Items.WATER_BUCKET){
			if (!world.isRemote && tryCoolPlate(world, pos, state) && !player.capabilities.isCreativeMode){
				player.setHeldItem(hand, ItemStack(Items.BUCKET))
			}
			
			return true
		}
		
		return false
	}
	
	fun tryCoolPlate(world: World, pos: BlockPos, state: IBlockState): Boolean{
		return pos.offset(state[FACING_NOT_DOWN].opposite).getTile<TileEntityFurnace>(world)?.let(EntityTechnicalIgneousPlateLogic.Companion::triggerCooling) == true
	}
	
	// State handling
	
	override fun withRotation(state: IBlockState, rot: Rotation): IBlockState{
		return state.with(FACING_NOT_DOWN, rot.rotate(state[FACING_NOT_DOWN]))
	}
	
	override fun withMirror(state: IBlockState, mirror: Mirror): IBlockState{
		return state.with(FACING_NOT_DOWN, mirror.mirror(state[FACING_NOT_DOWN]))
	}
	
	override fun getMetaFromState(state: IBlockState): Int = state[FACING_NOT_DOWN].index
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState.with(FACING_NOT_DOWN, EnumFacing.byIndex(meta))
	
	// Shape and rendering
	
	override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos) = BOUNDING_BOX[state[FACING_NOT_DOWN]] ?: BOUNDING_BOX.getValue(UP)
	override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing): BlockFaceShape = UNDEFINED
	
	override fun isFullCube(state: IBlockState): Boolean = false
	override fun isOpaqueCube(state: IBlockState): Boolean = false
	override fun getRenderType(state: IBlockState) = ENTITYBLOCK_ANIMATED
	override fun hasCustomBreakingProgress(state: IBlockState) = true
}
