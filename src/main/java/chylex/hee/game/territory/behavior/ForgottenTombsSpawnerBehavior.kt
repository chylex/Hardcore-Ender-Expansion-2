package chylex.hee.game.territory.behavior

import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.game.entity.util.lookPosVec
import chylex.hee.game.entity.util.selectAllEntities
import chylex.hee.game.territory.TerritoryType
import chylex.hee.game.territory.generator.Generator_ForgottenTombs
import chylex.hee.game.territory.storage.ForgottenTombsEndData
import chylex.hee.game.territory.system.ITerritoryBehavior
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.world.util.distanceSqTo
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.game.world.util.spawn
import chylex.hee.init.ModEntities
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import chylex.hee.util.math.Pos
import chylex.hee.util.math.bottomCenter
import chylex.hee.util.math.center
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.square
import net.minecraft.entity.EntitySpawnPlacementRegistry
import net.minecraft.entity.SpawnReason.NATURAL
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceContext.BlockMode
import net.minecraft.util.math.RayTraceContext.FluidMode
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraft.world.Difficulty.PEACEFUL
import net.minecraft.world.server.ServerWorld
import kotlin.math.abs
import kotlin.math.min

class ForgottenTombsSpawnerBehavior(private val instance: TerritoryInstance, private val endData: ForgottenTombsEndData) : ITerritoryBehavior {
	override fun tick(world: ServerWorld) {
		if (world.difficulty == PEACEFUL || endData.isPortalActivated || world.gameTime % 20L != 0L) {
			return
		}
		
		val players = instance.players.filter { it.isAlive && !it.isSpectator }
		if (players.isEmpty()) {
			return
		}
		
		val maxUndreadsInTerritory = min(40, 10 + (10 * players.size))
		val allUndreads = world.selectAllEntities.filter { it is EntityMobUndread && TerritoryInstance.fromPos(it) == instance }
		if (allUndreads.size >= maxUndreadsInTerritory) {
			return
		}
		
		val rand = world.rand
		val maxY = TerritoryType.FORGOTTEN_TOMBS.height.last - Generator_ForgottenTombs.EntranceCave.ELLIPSOID_Y_OFFSET - 30 // roughly where the first level starts
		val maxSpawns = min(3, maxUndreadsInTerritory - allUndreads.size)
		
		for (spawn in 1..maxSpawns) {
			val pickedPlayer = rand.nextItem(players)
			val playerY = pickedPlayer.posY
			
			if (playerY > maxY) {
				continue
			}
			
			val playerDepth = maxY - pickedPlayer.posY
			val maxUndreadsInDepth = 2 + (playerDepth * 0.28).floorToInt()
			
			if (allUndreads.count { abs(it.posY - playerY) < 8 } >= maxUndreadsInDepth) {
				continue
			}
			
			val spawnAttempts = 2 + (playerDepth * 0.1).floorToInt().coerceAtMost(5)
			
			for (attempt in 1..spawnAttempts) {
				val testPos = Pos(pickedPlayer).add(
					rand.nextInt(-19, 19),
					rand.nextInt(-8, 8),
					rand.nextInt(-19, 19)
				).offsetUntil(UP, -5..3) {
					EntitySpawnPlacementRegistry.canSpawnEntity(ModEntities.UNDREAD, world, NATURAL, it, rand) &&
					world.hasNoCollisions(ModEntities.UNDREAD.getBoundingBoxWithSizeApplied(it.x + 0.5, it.y.toDouble(), it.z + 0.5))
				}
				
				if (testPos == null || players.any { testPos.distanceSqTo(it) < square(15) }) {
					continue
				}
				
				if (players.any { world.rayTraceBlocks(RayTraceContext(it.lookPosVec, testPos.center, BlockMode.OUTLINE, FluidMode.NONE, it)).type == Type.MISS }) {
					continue
				}
				
				world.spawn(ModEntities.UNDREAD, testPos.bottomCenter, yaw = rand.nextFloat(0F, 360F))
				break
			}
		}
	}
}
