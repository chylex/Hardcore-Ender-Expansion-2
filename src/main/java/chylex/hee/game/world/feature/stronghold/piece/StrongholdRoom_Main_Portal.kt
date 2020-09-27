package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.selectVulnerableEntities
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.STRONGHOLD_GLOBAL
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.isAir
import chylex.hee.game.world.isPeaceful
import chylex.hee.game.world.offsetUntilExcept
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.removeItem
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random
import kotlin.math.min

class StrongholdRoom_Main_Portal(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM){
	class Spawner : ITriggerHandler{
		override fun check(world: World): Boolean{
			return !world.isRemote && !world.isPeaceful
		}
		
		override fun update(entity: EntityTechnicalTrigger){
			val world = entity.world as ServerWorld
			val selector = world.selectVulnerableEntities
			
			val box = StrongholdPieces.STRUCTURE_SIZE.toCenteredBoundingBox(entity.posVec)
			val playersInRange = selector.inBox<EntityPlayer>(box)
			
			if (playersInRange.isEmpty() ||
				world.entities.filter { it is EntityMobSilverfish }.count() >= 60 ||
				selector.inBox<EntityMobSilverfish>(box).size >= min(30, 15 + (5 * playersInRange.size))
			){
				return
			}
			
			val maxNearby = 3 + (2 * playersInRange.size)
			val targets = playersInRange.filter { selector.inRange<EntityMobSilverfish>(it.posVec, 32.0).size < maxNearby }.toMutableList()
			
			if (targets.isEmpty()){
				return
			}
			
			val rand = world.rand
			var nextAttempts = 8
			
			do{
				val target = rand.removeItem(targets)
				var spawnsLeft = rand.nextInt(2, 4)
				
				for(attempt in 1..nextAttempts){
					val spawnPos = Pos(target).add(
						rand.nextInt(8, 20) * (if (rand.nextBoolean()) 1 else -1),
						rand.nextInt(-4, 10),
						rand.nextInt(8, 20) * (if (rand.nextBoolean()) 1 else -1)
					).offsetUntilExcept(DOWN, 0..5){
						StrongholdPieces.isStoneBrick(it.getBlock(world))
					}
					
					if (spawnPos == null || !spawnPos.isAir(world) || world.getLight(spawnPos) > 7){
						continue
					}
					
					val mob = EntityMobSilverfish(world)
						.apply { setLocationAndAngles(spawnPos.x + 0.5, spawnPos.y.toDouble(), spawnPos.z + 0.5, rand.nextFloat(0F, 360F), 0F) }
						.takeIf { playersInRange.none(it::canEntityBeSeen) }
					
					if (mob != null){
						mob.disableHideInBlockAI()
						world.addEntity(mob)
						
						if (--spawnsLeft == 0){
							break
						}
					}
				}
				
				nextAttempts -= 2
			}while(targets.isNotEmpty() && nextAttempts > 0)
		}
		
		override fun nextTimer(rand: Random): Int{
			return rand.nextInt(20, 60)
		}
	}
	
	override val extraWeightMultiplier = 4
	
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(ROOM, Pos(centerX, 0, 0), NORTH),
		StrongholdConnection(ROOM, Pos(centerX, 6, 0), NORTH),
		StrongholdConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH),
		StrongholdConnection(ROOM, Pos(centerX, 6, maxZ), SOUTH),
		StrongholdConnection(ROOM, Pos(maxX, 0, centerZ), EAST),
		StrongholdConnection(ROOM, Pos(maxX, 6, centerZ), EAST),
		StrongholdConnection(ROOM, Pos(0, 0, centerZ), WEST),
		StrongholdConnection(ROOM, Pos(0, 6, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.addTrigger(size.centerPos, EntityStructureTrigger(STRONGHOLD_GLOBAL))
		
		for(connection in connections){
			val offset = connection.offset
			
			if (offset.y == 0 && !instance.isConnectionUsed(connection)){
				val shifted = offset.offset(connection.facing, -1)
				val perpendicular = connection.facing.rotateY()
				
				val addX = perpendicular.xOffset
				val addZ = perpendicular.zOffset
				
				world.placeCube(shifted.add(-addX, 1, -addZ), shifted.add(addX, 3, addZ), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
			}
		}
	}
}
