package chylex.hee.game.block
import chylex.hee.game.block.BlockVoidPortalInner.Type.HUB
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_ACTIVE
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.game.mechanics.portal.DimensionTeleporter
import chylex.hee.game.mechanics.portal.EntityPortalContact
import chylex.hee.game.mechanics.portal.SpawnInfo
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.util.Teleporter
import chylex.hee.game.world.util.Teleporter.FxRange.Silent
import chylex.hee.system.util.center
import chylex.hee.system.util.closestTickingTile
import chylex.hee.system.util.get
import chylex.hee.system.util.getState
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.subtractY
import chylex.hee.system.util.with
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
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
	
	override fun onEntityInside(world: World, pos: BlockPos, entity: Entity){
		if (!EntityPortalContact.shouldTeleport(entity)){
			return
		}
		
		when(pos.getState(world)[TYPE]){
			HUB -> {
				val instance = pos.closestTickingTile<TileEntityVoidPortalStorage>(world, MAX_DISTANCE_FROM_FRAME)?.currentInstance
				
				if (instance != null){
					teleportEntity(entity, instance.prepareSpawnPoint(world, clearanceRadius = 1))
				}
			}
			
			RETURN_ACTIVE -> {
				DimensionTeleporter.EndSpawnPortal.getSpawnInfo(world)?.let { teleportEntity(entity, it) }
			}
			
			else -> {}
		}
	}
}
