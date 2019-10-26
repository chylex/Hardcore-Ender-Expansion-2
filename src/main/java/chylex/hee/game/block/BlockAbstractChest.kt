package chylex.hee.game.block
import chylex.hee.game.block.entity.base.TileEntityBaseChest
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.entity.living.ai.AIOcelotSitOverride.IOcelotCanSitOn
import chylex.hee.init.ModGuiHandler.GuiType
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.util.get
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.withFacing
import net.minecraft.block.BlockDirectional.FACING
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.passive.EntityOcelot
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumBlockRenderType.ENTITYBLOCK_ANIMATED
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

abstract class BlockAbstractChest<T : TileEntityBaseChest>(builder: BlockBuilder) : BlockSimple(builder), ITileEntityProvider, IOcelotCanSitOn{
	private companion object{
		private val AABB = AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375)
	}
	
	init{
		defaultState = blockState.baseState.withFacing(NORTH)
	}
	
	override fun createBlockState() = BlockStateContainer(this, FACING)
	
	// Placement and interaction
	
	abstract fun createNewTileEntity(): T
	abstract val guiType: GuiType
	
	final override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return createNewTileEntity()
	}
	
	override fun getStateForPlacement(world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase, hand: EnumHand): IBlockState{
		return this.withFacing(placer.horizontalFacing.opposite)
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
			return true
		}
		
		pos.getTile<TileEntityBaseChest>(world)?.let {
			guiType.open(player, pos.x, pos.y, pos.z)
		}
		
		return true
	}
	
	// Ocelot behavior
	
	override fun canOcelotSitOn(world: World, pos: BlockPos): Boolean{
		return pos.getTile<TileEntityBaseChest>(world)?.isLidClosed == true
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
	
	final override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos) = AABB
	final override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) = UNDEFINED
	
	final override fun isFullCube(state: IBlockState) = false
	final override fun isOpaqueCube(state: IBlockState) = false
	final override fun getRenderType(state: IBlockState) = ENTITYBLOCK_ANIMATED
	final override fun hasCustomBreakingProgress(state: IBlockState) = true
}
