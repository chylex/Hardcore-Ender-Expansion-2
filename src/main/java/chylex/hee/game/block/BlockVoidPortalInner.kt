package chylex.hee.game.block
import chylex.hee.game.block.BlockVoidPortalInner.Type.HUB
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_ACTIVE
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.block.util.Property
import chylex.hee.game.mechanics.portal.EntityPortalContact
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.storage.TerritoryGlobalStorage
import chylex.hee.game.world.util.Teleporter
import chylex.hee.game.world.util.Teleporter.FxRange.Silent
import chylex.hee.system.util.center
import chylex.hee.system.util.closestTickingTile
import chylex.hee.system.util.getState
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.subtractY
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IStringSerializable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class BlockVoidPortalInner(builder: BlockSimple.Builder) : BlockAbstractPortal(builder){
	companion object{
		val TYPE = Property.enum<Type>("type")
		
		private val TELEPORTER = Teleporter(postEvent = false, effectRange = Silent)
		
		private fun teleportEntity(entity: Entity, target: BlockPos){
			val targetVec = target.center.subtractY(0.45)
			
			if (entity is EntityLivingBase){
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
		defaultState = blockState.baseState.withProperty(TYPE, HUB)
	}
	
	override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, TYPE)
	
	override fun getMetaFromState(state: IBlockState): Int = state.getValue(TYPE).ordinal
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(TYPE, Type.values()[meta])
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityPortalInner.Void()
	}
	
	// Interaction
	
	override fun onEntityInside(world: World, pos: BlockPos, entity: Entity){
		if (!EntityPortalContact.shouldTeleport(entity)){
			return
		}
		
		when(pos.getState(world).getValue(TYPE)){
			HUB -> {
				pos.closestTickingTile<TileEntityVoidPortalStorage>(world, MAX_DISTANCE_FROM_FRAME)
					?.currentInstance
					?.let(TerritoryGlobalStorage.get()::forInstance)
					?.let { teleportEntity(entity, it.lastSpawnPos) }
			}
			
			RETURN_ACTIVE -> {
				teleportEntity(entity, world.spawnPoint)
			}
			
			else -> {}
		}
	}
}
