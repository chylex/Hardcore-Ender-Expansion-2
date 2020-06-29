package chylex.hee.game.mechanics.portal
import chylex.hee.HEE
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.center
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.heeTagPersistent
import chylex.hee.system.util.heeTagPersistentOrNull
import chylex.hee.system.util.putPos
import chylex.hee.system.util.subtractY
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING

sealed class DimensionTeleporter{
	// UPDATE
	interface ITeleporter{
		fun placeEntity(world: World, entity: Entity, yaw: Float)
	}
	
	companion object{
		fun changeDimension(entity: Entity, dimension: DimensionType, teleporter: ITeleporter){
			// UPDATE
		}
		
		private fun spawnPoint(world: World): Vec3d{
			return world.getHeight(MOTION_BLOCKING, world.spawnPoint).center.subtractY(0.49)
		}
		
		private fun placeAt(entity: Entity, target: Vec3d, yaw: Float){
			entity.setLocationAndAngles(target.x, target.y, target.z, yaw, 0F)
			entity.motion = Vec3d.ZERO
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
	
	object EndSpawnPortal : ITeleporter{
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			val spawnInfo = (world.dimension as? WorldProviderEndCustom)?.getSpawnInfo() ?: return
			placeAt(entity, yaw, spawnInfo)
		}
	}
	
	class EndTerritoryPortal(private val spawnInfo: SpawnInfo) : ITeleporter{
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			placeAt(entity, yaw, spawnInfo)
		}
	}
	
	// From End
	
	object LastEndPortal : ITeleporter{
		private const val LAST_PORTAL_POS_TAG = "LastEndPortal"
		
		fun updateForEntity(entity: Entity, pos: BlockPos){
			if (entity.dimension == DimensionType.OVERWORLD){
				entity.heeTagPersistent.putPos(LAST_PORTAL_POS_TAG, pos)
			}
		}
		
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			placeIntoPortal(entity, world, entity.takeIf { it.dimension == DimensionType.OVERWORLD }?.heeTagPersistentOrNull?.getPosOrNull(LAST_PORTAL_POS_TAG), yaw)
		}
	}
	
	object LastHubPortal : ITeleporter{
		private const val LAST_PORTAL_POS_TAG = "LastVoidPortal"
		private const val LAST_PORTAL_DIM_TAG = "LastVoidPortalDim"
		
		fun tryOverrideTeleport(entity: Entity): Boolean{
			val tag = entity.heeTagPersistentOrNull
			
			if (tag.hasKey(LAST_PORTAL_POS_TAG) && tag.hasKey(LAST_PORTAL_DIM_TAG)){
				val dimension = DimensionType.byName(ResourceLocation(tag.getString(LAST_PORTAL_DIM_TAG)))
				
				if (dimension != null){
					changeDimension(entity, dimension, this)
					return true
				}
			}
			
			return false
		}
		
		fun updateForEntity(entity: Entity, pos: BlockPos?){
			with(entity.heeTagPersistent){
				if (entity.dimension === HEE.dim || pos == null){
					remove(LAST_PORTAL_POS_TAG)
					remove(LAST_PORTAL_DIM_TAG)
				}
				else{
					putPos(LAST_PORTAL_POS_TAG, pos)
					putString(LAST_PORTAL_DIM_TAG, entity.dimension.registryName.toString())
				}
			}
		}
		
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			placeIntoPortal(entity, world, entity.heeTagPersistentOrNull?.getPosOrNull(LAST_PORTAL_POS_TAG), yaw)
		}
	}
	
	object Bed : ITeleporter{ // TODO use at the end
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			val dimension = world.dimension.type
			
			val target = (entity as? EntityPlayer)
				?.let { entity.getBedLocation(dimension) }
				?.let { EntityPlayer.checkBedValidRespawnPosition(world, it, entity.isSpawnForced(dimension)).orElse(null) }
			
			placeAt(entity, target ?: spawnPoint(world), yaw)
		}
	}
}
