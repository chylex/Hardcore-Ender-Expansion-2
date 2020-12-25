package chylex.hee.game.block

import chylex.hee.game.block.entity.base.TileEntityBaseChest
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.entity.living.ai.AIOcelotSitOverride.IOcelotCanSitOn
import chylex.hee.game.entity.selectExistingEntities
import chylex.hee.game.world.getState
import chylex.hee.game.world.getTile
import chylex.hee.init.ModContainers
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityCat
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.TileEntityChest
import chylex.hee.system.migration.supply
import net.minecraft.block.AbstractChestBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType.ENTITYBLOCK_ANIMATED
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.ChestBlock.FACING
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateContainer.Builder
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityMerger.ICallbackWrapper
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.World

abstract class BlockAbstractChest<T : TileEntityBaseChest>(builder: BlockBuilder) : AbstractChestBlock<T>(builder.p, supply(null)), IOcelotCanSitOn {
	private companion object {
		private val AABB = AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375).asVoxelShape
	}
	
	init {
		defaultState = stateContainer.baseState.withFacing(NORTH)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(FACING)
	}
	
	override fun getShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
		return AABB
	}
	
	// Tile entity
	
	abstract fun createTileEntity(): T
	
	override fun hasTileEntity(state: BlockState): Boolean {
		return true
	}
	
	final override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return createTileEntity()
	}
	
	final override fun createNewTileEntity(world: IBlockReader): TileEntity {
		return createTileEntity()
	}
	
	@Sided(Side.CLIENT)
	override fun combine(state: BlockState, world: World, pos: BlockPos, unknown: Boolean): ICallbackWrapper<out TileEntityChest> {
		return (Blocks.ENDER_CHEST as AbstractChestBlock<*>).combine(state, world, pos, unknown) // UPDATE reduce hackiness
	}
	
	// Placement and interaction
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState {
		return this.withFacing(context.placementHorizontalFacing.opposite)
	}
	
	final override fun onBlockPlacedBy(world: World, pos: BlockPos, state: BlockState, placer: EntityLivingBase?, stack: ItemStack) {
		if (stack.hasDisplayName()) {
			pos.getTile<TileEntityBaseChest>(world)?.setCustomName(stack.displayName)
		}
	}
	
	final override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, hand: Hand, hit: BlockRayTraceResult): ActionResultType {
		if (world.isRemote) {
			return SUCCESS
		}
		
		val posAbove = pos.up()
		
		if (posAbove.getState(world).isNormalCube(world, posAbove)) {
			return SUCCESS
		}
		
		if (world.selectExistingEntities.inBox<EntityCat>(AxisAlignedBB(posAbove)).any { it.isSitting }) {
			return SUCCESS
		}
		
		openChest(world, pos, player)
		return SUCCESS
	}
	
	protected open fun openChest(world: World, pos: BlockPos, player: EntityPlayer) {
		pos.getTile<TileEntityBaseChest>(world)?.let {
			ModContainers.open(player, it, pos)
		}
	}
	
	// Ocelot behavior
	
	override fun canOcelotSitOn(world: IWorldReader, pos: BlockPos): Boolean {
		return pos.getTile<TileEntityBaseChest>(world)?.isLidClosed == true
	}
	
	// State handling
	
	override fun rotate(state: BlockState, rot: Rotation): BlockState {
		return state.withFacing(rot.rotate(state[FACING]))
	}
	
	override fun mirror(state: BlockState, mirror: Mirror): BlockState {
		return state.withFacing(mirror.mirror(state[FACING]))
	}
	
	// Rendering
	
	final override fun getRenderType(state: BlockState) = ENTITYBLOCK_ANIMATED
}
