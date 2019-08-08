package chylex.hee.game.block
import chylex.hee.game.block.BlockVoidPortalInner.Type.HUB
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_ACTIVE
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.game.mechanics.portal.EntityPortalContact
import chylex.hee.game.mechanics.portal.SpawnInfo
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.util.Teleporter
import chylex.hee.game.world.util.Teleporter.FxRange.Silent
import chylex.hee.system.util.Facing4
import chylex.hee.system.util.Pos
import chylex.hee.system.util.center
import chylex.hee.system.util.closestTickingTile
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.max
import chylex.hee.system.util.min
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.subtractY
import chylex.hee.system.util.with
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IStringSerializable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class BlockVoidPortalInner(builder: BlockBuilder) : BlockAbstractPortal(builder){
	companion object{
		val TYPE = Property.enum<Type>("type")
		
		private val TELEPORTER = Teleporter(postEvent = false, effectRange = Silent)
		
		fun teleportEntity(entity: Entity, info: SpawnInfo){
			val targetVec = info.pos.center.subtractY(0.45)
			
			if (entity is EntityLivingBase){
				info.yaw?.let { entity.rotationYaw = it }
				entity.rotationPitch = 0F
				
				TELEPORTER.toLocation(entity, targetVec)
			}
			else{
				entity.setPositionAndUpdate(targetVec.x, targetVec.y, targetVec.z)
				entity.motionVec = Vec3d.ZERO
			}
		}
	}
	
	enum class Type(private val serializableName: String) : IStringSerializable{
		HUB("hub"),
		RETURN_ACTIVE("return_active"),
		RETURN_INACTIVE("return_inactive");
		
		override fun getName(): String{
			return serializableName
		}
	}
	
	interface IVoidPortalController : IPortalController{
		val currentInstance: TerritoryInstance?
	}
	
	// Instance
	
	init{
		defaultState = blockState.baseState.with(TYPE, HUB)
	}
	
	override fun createBlockState() = BlockStateContainer(this, TYPE)
	
	override fun getMetaFromState(state: IBlockState) = state[TYPE].ordinal
	override fun getStateFromMeta(meta: Int) = this.with(TYPE, Type.values()[meta])
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityPortalInner.Void()
	}
	
	// Interaction
	
	private fun updateSpawnPortal(entity: Entity, pos: BlockPos){
		if (entity !is EntityPlayer){
			return
		}
		
		val world = entity.world
		val offsets = Facing4.map { facing -> pos.offsetUntil(facing, 1..MAX_SIZE){ it.getBlock(world) !== this } ?: return }
		
		val minPos = offsets.reduce(BlockPos::min)
		val maxPos = offsets.reduce(BlockPos::max)
		
		TerritoryInstance.fromPos(entity)?.updateSpawnPoint(entity, Pos((minPos.x + maxPos.x) / 2, pos.y, (minPos.z + maxPos.z) / 2))
	}
	
	override fun onEntityInside(world: World, pos: BlockPos, entity: Entity){
		if (!EntityPortalContact.shouldTeleport(entity)){
			return
		}
		
		when(pos.getState(world)[TYPE]){
			HUB -> {
				val info = pos.closestTickingTile<TileEntityVoidPortalStorage>(world, MAX_DISTANCE_FROM_FRAME)?.prepareSpawnPoint(entity)
				
				if (info != null){
					updateSpawnPortal(entity, pos)
					teleportEntity(entity, info)
				}
			}
			
			RETURN_ACTIVE -> {
				updateSpawnPortal(entity, pos)
				teleportEntity(entity, TerritoryInstance.THE_HUB_INSTANCE.prepareSpawnPoint(world, entity as? EntityPlayer, clearanceRadius = 2))
			}
			
			else -> {}
		}
	}
}
