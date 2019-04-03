package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.STRONGHOLD_GLOBAL
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType.ROOM
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdRoomConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.system.util.Pos
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.isAir
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.posVec
import chylex.hee.system.util.removeItem
import chylex.hee.system.util.selectVulnerableEntities
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST
import net.minecraft.world.EnumDifficulty.PEACEFUL
import net.minecraft.world.World
import java.util.Random
import kotlin.math.min

class StrongholdRoom_Main_Portal(file: String) : StrongholdAbstractPieceFromFile(file, ROOM){
	class Spawner : ITriggerHandler{
		override fun check(world: World): Boolean{
			return !world.isRemote && world.difficulty != PEACEFUL
		}
		
		override fun update(entity: EntityTechnicalTrigger){
			val world = entity.world
			val selector = world.selectVulnerableEntities
			
			val box = StrongholdPieces.STRUCTURE_SIZE.toCenteredBoundingBox(entity.posVec)
			val playersInRange = selector.inBox<EntityPlayer>(box).toList()
			
			if (playersInRange.isEmpty() ||
				world.loadedEntityList.count { it is EntityMobSilverfish } >= 60 ||
				selector.inBox<EntityMobSilverfish>(box).count() >= min(30, 15 + (5 * playersInRange.size))
			){
				return
			}
			
			val maxNearby = 3 + (2 * playersInRange.size)
			val targets = playersInRange.filter { selector.inRange<EntityMobSilverfish>(it.posVec, 32.0).count() < maxNearby }.toMutableList()
			
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
					).offsetUntil(DOWN, 0..5){
						StrongholdPieces.isStoneBrick(it.getBlock(world))
					}?.up()
					
					if (spawnPos == null || !spawnPos.isAir(world) || world.getLight(spawnPos) > 7){
						continue
					}
					
					val mob = EntityMobSilverfish(world)
						.apply { setLocationAndAngles(spawnPos.x + 0.5, spawnPos.y.toDouble(), spawnPos.z + 0.5, rand.nextFloat(0F, 360F), 0F) }
						.takeIf { playersInRange.none(it::canEntityBeSeen) }
					
					if (mob != null){
						mob.disableHideInBlockAI()
						world.spawnEntity(mob)
						
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
		StrongholdRoomConnection(Pos(centerX, 0, 0), NORTH),
		StrongholdRoomConnection(Pos(centerX, 6, 0), NORTH),
		StrongholdRoomConnection(Pos(centerX, 0, maxZ), SOUTH),
		StrongholdRoomConnection(Pos(centerX, 6, maxZ), SOUTH),
		StrongholdRoomConnection(Pos(maxX, 0, centerZ), EAST),
		StrongholdRoomConnection(Pos(maxX, 6, centerZ), EAST),
		StrongholdRoomConnection(Pos(0, 0, centerZ), WEST),
		StrongholdRoomConnection(Pos(0, 6, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, centerY, centerZ), EntityStructureTrigger(STRONGHOLD_GLOBAL))
		
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
