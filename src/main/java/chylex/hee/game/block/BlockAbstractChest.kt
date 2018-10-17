package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityBaseChest
import chylex.hee.init.ModGuiHandler.GuiType
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.selectExistingEntities
import net.minecraft.block.BlockDirectional.FACING
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.passive.EntityOcelot
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumBlockRenderType.ENTITYBLOCK_ANIMATED
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumHand
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

abstract class BlockAbstractChest<T : TileEntityBaseChest>(builder: BlockSimple.Builder) : BlockSimple(builder), ITileEntityProvider{
	private companion object{
		private val AABB = AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375)
	}
	
	init{
		defaultState = blockState.baseState.withProperty(FACING, NORTH)
	}
	
	override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, FACING)
	
	// Placement and interation
	
	abstract fun createNewTileEntity(): T
	abstract val guiType: GuiType
	
	final override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return createNewTileEntity()
	}
	
	override fun getStateForPlacement(world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase, hand: EnumHand): IBlockState{
		return defaultState.withProperty(FACING, placer.horizontalFacing.opposite)
	}
	
	final override fun onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack){
		if (stack.hasDisplayName()){
			pos.getTile<TileEntityBaseChest>(world)?.setCustomName(stack.displayName)
		}
	}
	
	final override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		if (world.isRemote){
			return true
		}
		
		val posAbove = pos.up()
		
		if (posAbove.getState(world).doesSideBlockChestOpening(world, posAbove, DOWN)){
			return true
		}
		
		if (world.selectExistingEntities.inBox<EntityOcelot>(AxisAlignedBB(posAbove)).any { it.isSitting }){
			return true // TODO should maybe figure out how to make Ocelots automatically sit on custom chests (EntityAIOcelotSit)
		}
		
		pos.getTile<TileEntityBaseChest>(world)?.let {
			guiType.open(player, pos.x, pos.y, pos.z)
		}
		
		return true
	}
	
	// State handling
	
	override fun withRotation(state: IBlockState, rot: Rotation): IBlockState{
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)))
	}
	
	override fun withMirror(state: IBlockState, mirror: Mirror): IBlockState{
		return state.withProperty(FACING, mirror.mirror(state.getValue(FACING)))
	}
	
	override fun getMetaFromState(state: IBlockState): Int = state.getValue(FACING).index
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(FACING, EnumFacing.byIndex(meta))
	
	// Shape and rendering
	
	final override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB = AABB
	final override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing): BlockFaceShape = UNDEFINED
	
	final override fun isFullCube(state: IBlockState): Boolean = false
	final override fun isOpaqueCube(state: IBlockState): Boolean = false
	final override fun getRenderType(state: IBlockState): EnumBlockRenderType = ENTITYBLOCK_ANIMATED
	final override fun hasCustomBreakingProgress(state: IBlockState): Boolean = true
}
