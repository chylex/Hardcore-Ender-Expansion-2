package chylex.hee.game.mechanics.portal
import chylex.hee.HEE
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.entity.dimensionKey
import chylex.hee.game.entity.heeTagPersistent
import chylex.hee.game.entity.heeTagPersistentOrNull
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.game.world.center
import chylex.hee.proxy.Environment
import chylex.hee.system.math.subtractY
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.serialization.getPosOrNull
import chylex.hee.system.serialization.hasKey
import chylex.hee.system.serialization.putPos
import net.minecraft.entity.Entity
import net.minecraft.util.RegistryKey
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.util.ITeleporter
import java.util.function.Function

sealed class DimensionTeleporter{
	interface ICustomTeleporter : ITeleporter{
		@JvmDefault
		override fun placeEntity(entity: Entity, currentWorld: ServerWorld, destWorld: ServerWorld, yaw: Float, repositionEntity: Function<Boolean, Entity>): Entity{
			val newEntity = repositionEntity.apply(false)
			placeEntity(destWorld, newEntity, yaw)
			return newEntity
		}
		
		fun placeEntity(world: World, entity: Entity, yaw: Float)
	}
	
	companion object{
		fun changeDimension(entity: Entity, dimension: RegistryKey<World>, teleporter: ICustomTeleporter): Boolean{
			return Environment.getDimensionOrNull(dimension)?.let { entity.changeDimension(it, teleporter) } != null
		}
		
		private fun spawnPoint(world: World): Vector3d{
			return world.getHeight(MOTION_BLOCKING, world.spawnPoint).center.subtractY(0.49)
		}
		
		private fun placeAt(entity: Entity, target: Vector3d, yaw: Float){
			entity.rotationYaw = yaw
			entity.rotationPitch = 0F
			entity.setPositionAndUpdate(target.x, target.y, target.z)
			entity.motion = Vector3d.ZERO
		}
		
		private fun placeAt(entity: Entity, yaw: Float, spawnInfo: SpawnInfo){
			placeAt(entity, spawnInfo.pos.center.subtractY(0.45), spawnInfo.yaw ?: yaw)
		}
		
		private fun placeIntoPortal(entity: Entity, world: World, target: BlockPos?, yaw: Float){
			if (target != null){
				BlockAbstractPortal.ensureClearance(world, target, radius = 1)
				placeAt(entity, target.center.subtractY(0.45), yaw)
			}
			else{
				placeAt(entity, spawnPoint(world), yaw)
			}
		}
	}
	
	// To End
	
	object EndSpawnPortal : ICustomTeleporter{
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			val spawnInfo = (world.dimension as? WorldProviderEndCustom)?.getSpawnInfo() ?: return
			placeAt(entity, yaw, spawnInfo)
		}
	}
	
	class EndTerritoryPortal(private val spawnInfo: SpawnInfo) : ICustomTeleporter{
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			placeAt(entity, yaw, spawnInfo)
		}
	}
	
	// From End
	
	object LastEndPortal : ICustomTeleporter{
		private const val LAST_PORTAL_POS_TAG = "LastEndPortal"
		
		fun updateForEntity(entity: Entity, pos: BlockPos){
			if (entity.dimensionKey === World.OVERWORLD){
				entity.heeTagPersistent.putPos(LAST_PORTAL_POS_TAG, pos)
			}
		}
		
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			placeIntoPortal(entity, world, entity.takeIf { it.dimensionKey === World.OVERWORLD }?.heeTagPersistentOrNull?.getPosOrNull(LAST_PORTAL_POS_TAG), yaw)
		}
	}
	
	object LastHubPortal : ICustomTeleporter{
		private const val LAST_PORTAL_POS_TAG = "LastVoidPortal"
		private const val LAST_PORTAL_DIM_TAG = "LastVoidPortalDim"
		
		fun tryOverrideTeleport(entity: Entity): Boolean{
			val tag = entity.heeTagPersistentOrNull
			
			if (tag.hasKey(LAST_PORTAL_POS_TAG) && tag.hasKey(LAST_PORTAL_DIM_TAG)){
				val dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, ResourceLocation(tag.getString(LAST_PORTAL_DIM_TAG)))
				
				if (changeDimension(entity, dimension, this)){
					return true
				}
			}
			
			return false
		}
		
		fun updateForEntity(entity: Entity, pos: BlockPos?){
			with(entity.heeTagPersistent){
				if (entity.dimensionKey === HEE.dim || pos == null){
					remove(LAST_PORTAL_POS_TAG)
					remove(LAST_PORTAL_DIM_TAG)
				}
				else{
					putPos(LAST_PORTAL_POS_TAG, pos)
					putString(LAST_PORTAL_DIM_TAG, entity.dimensionKey.registryName.toString())
				}
			}
		}
		
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			placeIntoPortal(entity, world, entity.heeTagPersistentOrNull?.getPosOrNull(LAST_PORTAL_POS_TAG), yaw)
		}
	}
	
	object Bed : ICustomTeleporter{ // TODO use at the end
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			val dimension = world.dimensionType
			
			val target = (entity as? EntityPlayer)
				?.let { entity.getBedLocation(dimension) }
				?.let { EntityPlayer.checkBedValidRespawnPosition(world, it, entity.isSpawnForced(dimension)).orElse(null) }
			
			placeAt(entity, target ?: spawnPoint(world), yaw)
		}
	}
}
