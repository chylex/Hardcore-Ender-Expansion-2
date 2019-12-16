package chylex.hee.game.mechanics.portal
import chylex.hee.HEE
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.system.util.center
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.heeTagPersistent
import chylex.hee.system.util.heeTagPersistentOrNull
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.setPos
import chylex.hee.system.util.subtractY
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.util.ITeleporter

sealed class DimensionTeleporter{
	private companion object{
		private fun spawnPoint(world: World): Vec3d{
			return world.spawnPoint.let(world::getTopSolidOrLiquidBlock).center.subtractY(0.49)
		}
		
		private fun placeAt(entity: Entity, target: Vec3d, yaw: Float){
			entity.setLocationAndAngles(target.x, target.y, target.z, yaw, 0F)
			entity.motionVec = Vec3d.ZERO
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
			val spawnInfo = (world.provider as? WorldProviderEndCustom)?.getSpawnInfo() ?: return
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
			if (entity.dimension == 0){
				entity.heeTagPersistent.setPos(LAST_PORTAL_POS_TAG, pos)
			}
		}
		
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			placeIntoPortal(entity, world, entity.takeIf { it.dimension == 0 }?.heeTagPersistentOrNull?.getPosOrNull(LAST_PORTAL_POS_TAG), yaw)
		}
	}
	
	object LastHubPortal : ITeleporter{
		private const val LAST_PORTAL_POS_TAG = "LastVoidPortal"
		private const val LAST_PORTAL_DIM_TAG = "LastVoidPortalDim"
		
		fun tryOverrideTeleport(entity: Entity): Boolean{
			val tag = entity.heeTagPersistentOrNull
			
			if (tag.hasKey(LAST_PORTAL_POS_TAG) && tag.hasKey(LAST_PORTAL_DIM_TAG)){
				entity.changeDimension(tag.getInteger(LAST_PORTAL_DIM_TAG), this)
				return true
			}
			
			return false
		}
		
		fun updateForEntity(entity: Entity, pos: BlockPos?){
			with(entity.heeTagPersistent){
				if (entity.dimension == HEE.DIM || pos == null){
					removeTag(LAST_PORTAL_POS_TAG)
					removeTag(LAST_PORTAL_DIM_TAG)
				}
				else{
					setPos(LAST_PORTAL_POS_TAG, pos)
					setInteger(LAST_PORTAL_DIM_TAG, entity.dimension)
				}
			}
		}
		
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			placeIntoPortal(entity, world, entity.heeTagPersistentOrNull?.getPosOrNull(LAST_PORTAL_POS_TAG), yaw)
		}
	}
	
	object Bed : ITeleporter{ // TODO use at the end
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			val dimension = world.provider.dimension
			
			val target = (entity as? EntityPlayer)
				?.let { entity.getBedLocation(dimension) }
				?.let { EntityPlayer.getBedSpawnLocation(world, it, entity.isSpawnForced(dimension)) }
				?.let { it.center.subtractY(0.4) }
			
			placeAt(entity, target ?: spawnPoint(world), yaw)
		}
	}
}
