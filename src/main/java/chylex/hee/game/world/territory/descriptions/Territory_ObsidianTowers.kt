package chylex.hee.game.world.territory.descriptions
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.world.component1
import chylex.hee.game.world.component2
import chylex.hee.game.world.territory.ITerritoryDescription
import chylex.hee.game.world.territory.TerritoryDifficulty
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.random.nextFloat
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import java.util.Random

object Territory_ObsidianTowers : ITerritoryDescription{
	override val difficulty
		get() = TerritoryDifficulty.BOSS
	
	override val colors = object : TerritoryColors(){
		override val tokenTop    = RGB(146, 130, 185)
		override val tokenBottom = RGB( 81, 173, 250)
		
		override val portalSeed = 5555L
		
		override fun nextPortalColor(rand: Random, color: FloatArray){
			if (rand.nextBoolean()){
				color[0] = rand.nextFloat(0.2F, 0.5F)
				color[1] = rand.nextFloat(0.7F, 0.9F)
				color[2] = 1F
			}
			else{
				color[0] = rand.nextFloat(0.5F, 0.7F)
				color[1] = 0.5F
				color[2] = rand.nextFloat(0.9F, 1F)
			}
		}
	}
	
	override val environment = object : TerritoryEnvironment(){
		override val fogColor = Vector3d(0.0, 0.0, 0.0)
		override val fogDensity = 0.01F
		override val fogRenderDistanceModifier = 0.005F
		
		override val voidRadiusMpXZ = 2F
		override val voidRadiusMpY = 2.5F
	}
	
	override fun prepareSpawnPoint(world: World, spawnPoint: BlockPos, instance: TerritoryInstance){
		super.prepareSpawnPoint(world, spawnPoint, instance)
		
		if (world.players.none { TerritoryInstance.fromPos(it) == instance }){
			val chunks = instance.territory.chunks
			val (startChunkX, startChunkZ) = instance.topLeftChunk
			
			var boss: EntityBossEnderEye? = null
			
			for(chunkX in startChunkX until (startChunkX + chunks))
			for(chunkZ in startChunkZ until (startChunkZ + chunks)){
				val chunk = world.getChunk(chunkX, chunkZ)
				
				for(entityList in chunk.entityLists){
					val bosses = entityList.getByClass(EntityBossEnderEye::class.java)
					
					if (bosses.isNotEmpty()){
						boss = bosses.takeIf { boss == null }?.singleOrNull() ?: return
					}
				}
			}
			
			boss?.resetToSpawnAfterTerritoryReloads(spawnPoint)
		}
	}
}
