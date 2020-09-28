package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.with
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.selectVulnerableEntities
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.STRONGHOLD_TRAP_PRISON
import chylex.hee.game.world.Pos
import chylex.hee.game.world.breakBlock
import chylex.hee.game.world.center
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.isPeaceful
import chylex.hee.game.world.max
import chylex.hee.game.world.min
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import chylex.hee.system.migration.BlockSkull
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItemOrNull
import chylex.hee.system.random.nextRounded
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.use
import net.minecraft.block.Blocks
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.World
import java.util.Random
import kotlin.math.min

class StrongholdRoom_Trap_Prison(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM){
	class Trigger : ITriggerHandler{
		private companion object{
			private const val SPAWNS_LEFT_TAG = "SpawnsLeft"
		}
		
		private var spawnsLeft = -1
		
		override fun check(world: World): Boolean{
			return !world.isRemote && !world.isPeaceful
		}
		
		override fun update(entity: EntityTechnicalTrigger){
			val world = entity.world
			val facing = entity.horizontalFacing
			
			val pos1 = Pos(entity)
			val pos2 = pos1.offset(facing, 7).offset(facing.rotateYCCW(), 4).offset(UP, 2)
			
			val (x1, y1, z1) = pos1.center
			val (x2, y2, z2) = pos2.center
			
			val targets = world.selectVulnerableEntities.inBox<EntityPlayer>(AxisAlignedBB(x1, y1, z1, x2, y2, z2))
			
			if (spawnsLeft == -1){
				if (targets.isEmpty()){
					return
				}
				else{
					spawnsLeft = 2 + world.difficulty.id
				}
			}
			
			val rand = world.rand
			val minPos = pos1.min(pos2)
			val maxPos = pos1.max(pos2)
			
			repeat(min(spawnsLeft, rand.nextRounded(1.66F))){
				for(attempt in 1..8){
					val testPos = Pos(rand.nextInt(minPos.x, maxPos.x), maxPos.y + 1, rand.nextInt(minPos.z, maxPos.z))
					
					if (StrongholdPieces.isStoneBrick(testPos.getBlock(world))){
						testPos.breakBlock(world, false)
						
						EntityMobSilverfish(world).apply {
							setLocationAndAngles(testPos.x + 0.5, testPos.y.toDouble(), testPos.z + 0.5, rand.nextFloat(0F, 360F), 0F)
							world.addEntity(this)
							
							spawnExplosionParticle()
							attackTarget = rand.nextItemOrNull(targets)
						}
						
						--spawnsLeft
						break
					}
				}
			}
			
			if (spawnsLeft == 0){
				entity.remove()
			}
		}
		
		override fun nextTimer(rand: Random): Int{
			return 14
		}
		
		override fun serializeNBT() = TagCompound().apply {
			putShort(SPAWNS_LEFT_TAG, spawnsLeft.toShort())
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			spawnsLeft = getShort(SPAWNS_LEFT_TAG).toInt()
		}
	}
	
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(ROOM, Pos(maxX, 0, maxZ - 2), EAST),
		StrongholdConnection(ROOM, Pos(0, 0, maxZ - 2), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.addTrigger(Pos(1, 1, 5), EntityStructureTrigger(STRONGHOLD_TRAP_PRISON, EAST))
		
		val rand = world.rand
		
		// Chest
		
		world.addTrigger(Pos(1, 1, 1), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
		
		// Skull
		
		val skullType = if (rand.nextInt(3) == 0) Blocks.SKELETON_SKULL else Blocks.ZOMBIE_HEAD
		world.setState(Pos(maxX - 1, 1, 1), skullType.with(BlockSkull.ROTATION, 10))
		
		// Redstone
		
		repeat(5 + rand.nextInt(5 + rand.nextInt(6))){
			val redstonePos = Pos(rand.nextInt(1, maxX - 1), 1, rand.nextInt(1, 5))
			
			if (world.isAir(redstonePos)){
				world.setBlock(redstonePos, Blocks.REDSTONE_WIRE)
			}
		}
	}
}
