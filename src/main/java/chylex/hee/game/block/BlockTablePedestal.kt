package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.init.ModItems
import chylex.hee.system.util.copyIf
import chylex.hee.system.util.get
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.abs
import kotlin.math.max

class BlockTablePedestal(builder: BlockBuilder) : BlockSimpleShaped(builder, COMBINED_BOX), ITileEntityProvider{
	companion object{
		val IS_LINKED = Property.bool("linked")
		
		const val DROPPED_ITEM_THROWER_NAME = "[Pedestal]"
		
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
		
		const val PARTICLE_TARGET_Y = BOTTOM_SLAB_TOP_Y + (MIDDLE_PILLAR_TOP_Y - BOTTOM_SLAB_TOP_Y) * 0.5
		
		val COMBINED_BOX: AxisAlignedBB = max(BOTTOM_SLAB_HALF_WIDTH, TOP_SLAB_HALF_WIDTH).let {
			AxisAlignedBB(0.5, 0.0, 0.5, 0.5, TOP_SLAB_TOP_Y, 0.5).grow(it, 0.0, it)
		}
		
		private fun isInsidePickupArea(pos: BlockPos, entity: EntityItem): Boolean{
			return (entity.posY - pos.y) >= PICKUP_TOP_Y && abs(pos.x + 0.5 - entity.posX) <= PICKUP_DIST_XZ && abs(pos.z + 0.5 - entity.posZ) <= PICKUP_DIST_XZ
		}
		
		private fun isItemAreaBlocked(world: World, pos: BlockPos): Boolean{
			return pos.up().let { it.getState(world).getCollisionBoundingBox(world, it) != NULL_AABB }
		}
	}
	
	// Instance
	
	init{
		defaultState = blockState.baseState.with(IS_LINKED, false)
	}
	
	override fun createBlockState() = BlockStateContainer(this, IS_LINKED)
	
	override fun getMetaFromState(state: IBlockState) = if (state[IS_LINKED]) 1 else 0
	override fun getStateFromMeta(meta: Int) = this.with(IS_LINKED, meta == 1)
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityTablePedestal()
	}
	
	// Interaction
	
	override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		if (world.isRemote){
			return
		}
		
		if (entity is EntityItem && entity.age >= 5 && isInsidePickupArea(pos, entity) && entity.thrower != DROPPED_ITEM_THROWER_NAME){
			val stack = entity.item
			
			if (pos.getTile<TileEntityTablePedestal>(world)?.addToInput(stack) == true){
				if (stack.isEmpty){
					entity.setDead()
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
	
	override fun onBlockClicked(world: World, pos: BlockPos, player: EntityPlayer){
		if (!world.isRemote){
			pos.getTile<TileEntityTablePedestal>(world)?.dropAllItems()
		}
	}
	
	override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		if (world.isRemote){
			return true
		}
		
		val tile = pos.getTile<TileEntityTablePedestal>(world) ?: return true
		val heldItem = player.getHeldItem(hand)
		
		if (heldItem.item === ModItems.TABLE_LINK){
			return false
		}
		
		if (heldItem.isEmpty){
			if (player.isSneaking){
				tile.resetLinkedTable(true)
			}
			else{
				tile.dropAllItems()
			}
		}
		else if (!isItemAreaBlocked(world, pos)){
			tile.addToInput(heldItem.copyIf { player.isCreative })
		}
		
		return true
	}
	
	override fun onBlockHarvested(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer){
		if (!world.isRemote && player.isCreative){
			pos.getTile<TileEntityTablePedestal>(world)?.onPedestalDestroyed(dropTableLink = false)
		}
	}
	
	override fun breakBlock(world: World, pos: BlockPos, state: IBlockState){
		if (!world.isRemote){
			pos.getTile<TileEntityTablePedestal>(world)?.onPedestalDestroyed(dropTableLink = true)
		}
		
		super.breakBlock(world, pos, state)
	}
	
	override fun neighborChanged(state: IBlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos){
		if (!world.isRemote && neighborPos == pos.up() && isItemAreaBlocked(world, pos)){
			pos.getTile<TileEntityTablePedestal>(world)?.dropAllItems()
		}
	}
	
	override fun addCollisionBoxToList(state: IBlockState, world: World, pos: BlockPos, entityBox: AxisAlignedBB, collidingBoxes: MutableList<AxisAlignedBB>, entity: Entity?, isActualState: Boolean){
		COLLISION_BOXES.forEach {
			@Suppress("DEPRECATION")
			addCollisionBoxToList(pos, entityBox, collidingBoxes, it)
		}
	}
	
	// Redstone
	
	override fun hasComparatorInputOverride(state: IBlockState): Boolean{
		return true
	}
	
	override fun getComparatorInputOverride(state: IBlockState, world: World, pos: BlockPos): Int{
		return pos.getTile<TileEntityTablePedestal>(world)?.outputComparatorStrength ?: 0
	}
	
	// Client side
	
	override fun getRenderLayer() = CUTOUT
	
	@SideOnly(Side.CLIENT)
	object Color : IBlockColor{
		private const val NONE = -1
		
		override fun colorMultiplier(state: IBlockState, world: IBlockAccess?, pos: BlockPos?, tintIndex: Int): Int{
			if (world == null || pos == null){
				return NONE
			}
			
			return when(tintIndex){
				1 -> pos.getTile<TileEntityTablePedestal>(world)?.tableIndicatorColor?.i ?: NONE
				2 -> pos.getTile<TileEntityTablePedestal>(world)?.statusIndicatorColorClient ?: NONE
				else -> NONE
			}
		}
	}
}
