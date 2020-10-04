package chylex.hee.game.block
import chylex.hee.client.color.NO_TINT
import chylex.hee.client.render.block.IBlockLayerCutout
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.Property
import chylex.hee.game.inventory.copyIf
import chylex.hee.game.mechanics.table.PedestalStatusIndicator
import chylex.hee.game.world.getState
import chylex.hee.game.world.getTile
import chylex.hee.init.ModItems
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.entity.Entity
import net.minecraft.state.StateContainer.Builder
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockReader
import net.minecraft.world.ILightReader
import net.minecraft.world.IWorld
import net.minecraft.world.World
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

class BlockTablePedestal(builder: BlockBuilder) : BlockSimpleShaped(builder, COMBINED_BOX), IBlockLayerCutout{
	companion object{
		val IS_LINKED = Property.bool("linked")
		
		val DROPPED_ITEM_THROWER: UUID = UUID.fromString("f6496b88-3669-4d2b-a9d6-cd2e36f188eb")
		
		// Collision checks
		
		private const val BOTTOM_SLAB_HALF_WIDTH = 6.0 / 16.0
		private const val BOTTOM_SLAB_TOP_Y = 4.0 / 16.0
		
		private const val MIDDLE_PILLAR_HALF_WIDTH = 3.5 / 16.0
		private const val MIDDLE_PILLAR_TOP_Y = BOTTOM_SLAB_TOP_Y + (6.5 / 16.0)
		
		private const val TOP_SLAB_HALF_WIDTH = 7.0 / 16.0
		private const val TOP_SLAB_TOP_Y = MIDDLE_PILLAR_TOP_Y + (3.0 / 16.0)
		
		private const val PICKUP_TOP_Y = TOP_SLAB_TOP_Y - 0.0625
		private const val PICKUP_DIST_XZ = TOP_SLAB_HALF_WIDTH - 0.0625
		
		private val COLLISION_BOXES = arrayOf(
			AxisAlignedBB(
				0.5 - BOTTOM_SLAB_HALF_WIDTH,               0.0, 0.5 - BOTTOM_SLAB_HALF_WIDTH,
				0.5 + BOTTOM_SLAB_HALF_WIDTH, BOTTOM_SLAB_TOP_Y, 0.5 + BOTTOM_SLAB_HALF_WIDTH
			),
			AxisAlignedBB(
				0.5 - MIDDLE_PILLAR_HALF_WIDTH,   BOTTOM_SLAB_TOP_Y, 0.5 - MIDDLE_PILLAR_HALF_WIDTH,
				0.5 + MIDDLE_PILLAR_HALF_WIDTH, MIDDLE_PILLAR_TOP_Y, 0.5 + MIDDLE_PILLAR_HALF_WIDTH
			),
			AxisAlignedBB(
				0.5 - TOP_SLAB_HALF_WIDTH, MIDDLE_PILLAR_TOP_Y, 0.5 - TOP_SLAB_HALF_WIDTH,
				0.5 + TOP_SLAB_HALF_WIDTH,      TOP_SLAB_TOP_Y, 0.5 + TOP_SLAB_HALF_WIDTH
			)
		)
		
		const val ITEM_SPAWN_OFFSET_Y = TOP_SLAB_TOP_Y + 0.01
		const val PARTICLE_TARGET_Y = BOTTOM_SLAB_TOP_Y + (MIDDLE_PILLAR_TOP_Y - BOTTOM_SLAB_TOP_Y) * 0.5
		
		val COMBINED_BOX: AxisAlignedBB = max(BOTTOM_SLAB_HALF_WIDTH, TOP_SLAB_HALF_WIDTH).let {
			AxisAlignedBB(0.5, 0.0, 0.5, 0.5, TOP_SLAB_TOP_Y, 0.5).grow(it, 0.0, it)
		}
		
		val COLLISION_SHAPE: VoxelShape = COLLISION_BOXES.map { it.asVoxelShape }.reduce { acc, next -> VoxelShapes.or(acc, next) }
		
		private fun isInsidePickupArea(pos: BlockPos, entity: EntityItem): Boolean{
			return (entity.posY - pos.y) >= PICKUP_TOP_Y && abs(pos.x + 0.5 - entity.posX) <= PICKUP_DIST_XZ && abs(pos.z + 0.5 - entity.posZ) <= PICKUP_DIST_XZ
		}
		
		private fun isItemAreaBlocked(world: IWorld, pos: BlockPos): Boolean{
			return !pos.up().getState(world).getCollisionShape(world, pos).isEmpty
		}
	}
	
	// Instance
	
	init{
		defaultState = stateContainer.baseState.with(IS_LINKED, false)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(IS_LINKED)
	}
	
	override fun hasTileEntity(state: BlockState): Boolean{
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return TileEntityTablePedestal()
	}
	
	// Interaction
	
	override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity){
		if (world.isRemote){
			return
		}
		
		if (entity is EntityItem && entity.age >= 5 && isInsidePickupArea(pos, entity) && entity.throwerId != DROPPED_ITEM_THROWER){
			val stack = entity.item
			
			if (pos.getTile<TileEntityTablePedestal>(world)?.addToInput(stack) == true){
				if (stack.isEmpty){
					entity.remove()
				}
				else{
					entity.item = stack
				}
			}
		}
		else if (entity is EntityPlayer && entity.posY >= pos.y && !entity.isCreative){
			pos.getTile<TileEntityTablePedestal>(world)?.moveOutputToPlayerInventory(entity.inventory)
		}
	}
	
	override fun onBlockClicked(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer){
		if (!world.isRemote){
			pos.getTile<TileEntityTablePedestal>(world)?.dropAllItems()
		}
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, hand: Hand, hit: BlockRayTraceResult): ActionResultType{
		if (world.isRemote){
			return SUCCESS
		}
		
		val tile = pos.getTile<TileEntityTablePedestal>(world) ?: return SUCCESS
		val heldItem = player.getHeldItem(hand)
		
		if (heldItem.item === ModItems.TABLE_LINK){
			return PASS
		}
		
		if (heldItem.isEmpty){
			if (player.isSneaking){
				tile.resetLinkedTable(true)
			}
			else{
				tile.dropAllItems()
			}
		}
		else if (tile.isDedicatedOutput){
			tile.dropAllItems()
		}
		else if (!isItemAreaBlocked(world, pos)){
			tile.addToInput(heldItem.copyIf { player.isCreative })
		}
		
		return SUCCESS
	}
	
	override fun onBlockHarvested(world: World, pos: BlockPos, state: BlockState, player: EntityPlayer){
		if (!world.isRemote && player.isCreative){
			pos.getTile<TileEntityTablePedestal>(world)?.onPedestalDestroyed(dropTableLink = false)
		}
	}
	
	override fun onReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, isMoving: Boolean){
		if (newState.block !== this){
			pos.getTile<TileEntityTablePedestal>(world)?.onPedestalDestroyed(dropTableLink = true)
		}
		
		super.onReplaced(state, world, pos, newState, isMoving)
	}
	
	override fun neighborChanged(state: BlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos, isMoving: Boolean){
		if (!world.isRemote && neighborPos == pos.up() && isItemAreaBlocked(world, pos)){
			pos.getTile<TileEntityTablePedestal>(world)?.dropAllItems()
		}
		
		return super.neighborChanged(state, world, pos, neighborBlock, neighborPos, isMoving)
	}
	
	override fun getCollisionShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape{
		return COLLISION_SHAPE
	}
	
	// Redstone
	
	override fun hasComparatorInputOverride(state: BlockState): Boolean{
		return true
	}
	
	override fun getComparatorInputOverride(state: BlockState, world: World, pos: BlockPos): Int{
		return pos.getTile<TileEntityTablePedestal>(world)?.outputComparatorStrength ?: 0
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	object Color : IBlockColor{
		override fun getColor(state: BlockState, world: ILightReader?, pos: BlockPos?, tintIndex: Int): Int{
			if (world == null || pos == null){
				return NO_TINT
			}
			
			return when(tintIndex){
				1 -> pos.getTile<TileEntityTablePedestal>(world)?.tableIndicatorColor?.i ?: NO_TINT
				2 -> pos.getTile<TileEntityTablePedestal>(world)?.statusIndicatorColorClient ?: PedestalStatusIndicator.Contents.NONE.color.i
				else -> NO_TINT
			}
		}
	}
}
