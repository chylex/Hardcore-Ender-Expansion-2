package chylex.hee.game.block
import chylex.hee.HEE
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientFX.IFXData
import chylex.hee.network.client.PacketClientFX.IFXHandler
import chylex.hee.system.util.copyIf
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readPos
import chylex.hee.system.util.use
import chylex.hee.system.util.writePos
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random
import kotlin.math.abs
import kotlin.math.max

class BlockTablePedestal(builder: BlockSimple.Builder) : BlockSimpleShaped(builder, COMBINED_BOX), ITileEntityProvider{
	companion object{
		val IS_LINKED = PropertyBool.create("linked")!!
		
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
		
		val COMBINED_BOX = max(BOTTOM_SLAB_HALF_WIDTH, TOP_SLAB_HALF_WIDTH).let {
			AxisAlignedBB(0.5, 0.0, 0.5, 0.5, TOP_SLAB_TOP_Y, 0.5).grow(it, 0.0, it)!!
		}
		
		private fun isInsidePickupArea(pos: BlockPos, entity: EntityItem): Boolean{
			return (entity.posY - pos.y) >= PICKUP_TOP_Y && abs(pos.x + 0.5 - entity.posX) <= PICKUP_DIST_XZ && abs(pos.z + 0.5 - entity.posZ) <= PICKUP_DIST_XZ
		}
		
		private fun isItemAreaBlocked(world: World, pos: BlockPos): Boolean{
			return pos.up().let { it.getState(world).getCollisionBoundingBox(world, it) != NULL_AABB }
		}
		
		// Item pickup
		
		class FxItemPickupData(private val pedestalPos: BlockPos) : IFXData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writePos(pedestalPos)
			}
		}
		
		@JvmStatic
		val FX_ITEM_PICKUP = object : IFXHandler{
			override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
				val player = HEE.proxy.getClientSidePlayer() ?: return
				val pedestalPos = buffer.readPos()
				
				PARTICLE_ITEM_PICKUP.spawn(Point(pedestalPos.up(), 10), rand)
				SoundEvents.ENTITY_ITEM_PICKUP.playClient(player.posVec, SoundCategory.PLAYERS, volume = 0.2F, pitch = rand.nextFloat(0.6F, 3.4F))
			}
		}
		
		private val PARTICLE_ITEM_PICKUP = ParticleSpawnerVanilla(
			type = SMOKE_NORMAL,
			pos = Constant(0.15F, DOWN) + InBox(0.425F)
		)
	}
	
	// Instance
	
	init{
		defaultState = blockState.baseState.withProperty(IS_LINKED, false)
	}
	
	override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, IS_LINKED)
	
	override fun getMetaFromState(state: IBlockState): Int = if (state.getValue(IS_LINKED)) 1 else 0
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(IS_LINKED, meta == 1)
	
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
			if (pos.getTile<TileEntityTablePedestal>(world)?.moveOutputToPlayerInventory(entity.inventory) == true){
				PacketClientFX(FX_ITEM_PICKUP, FxItemPickupData(pos)).sendToPlayer(entity)
			}
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
		else if (!BlockTablePedestal.isItemAreaBlocked(world, pos)){
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
		COLLISION_BOXES.forEach { addCollisionBoxToList(pos, entityBox, collidingBoxes, it) }
	}
	
	// Redstone
	
	override fun hasComparatorInputOverride(state: IBlockState): Boolean{
		return true
	}
	
	override fun getComparatorInputOverride(state: IBlockState, world: World, pos: BlockPos): Int{
		return pos.getTile<TileEntityTablePedestal>(world)?.outputComparatorStrength ?: 0
	}
	
	// Client
	
	override fun getRenderLayer(): BlockRenderLayer = CUTOUT
	
	@SideOnly(Side.CLIENT)
	object Color : IBlockColor{
		private const val NONE = -1
		
		override fun colorMultiplier(state: IBlockState, world: IBlockAccess?, pos: BlockPos?, tintIndex: Int): Int{
			if (world == null || pos == null){
				return NONE
			}
			
			return when(tintIndex){
				1 -> pos.getTile<TileEntityTablePedestal>(world)?.tableIndicatorColor ?: NONE
				2 -> pos.getTile<TileEntityTablePedestal>(world)?.statusIndicatorColorClient ?: NONE
				else -> NONE
			}
		}
	}
}
