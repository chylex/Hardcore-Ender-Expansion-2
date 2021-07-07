package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.block.util.SKULL_ROTATION
import chylex.hee.game.block.util.with
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.STRONGHOLD_TRAP_PRISON
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectVulnerableEntities
import chylex.hee.game.fx.FxVecData
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.EntityStructureTrigger
import chylex.hee.game.world.generation.trigger.LootChestStructureTrigger
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.isPeaceful
import chylex.hee.game.world.util.max
import chylex.hee.game.world.util.min
import chylex.hee.game.world.util.spawn
import chylex.hee.init.ModEntities
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItemOrNull
import chylex.hee.system.random.nextRounded
import chylex.hee.util.math.Pos
import chylex.hee.util.math.addY
import chylex.hee.util.math.bottomCenter
import chylex.hee.util.math.center
import chylex.hee.util.math.component1
import chylex.hee.util.math.component2
import chylex.hee.util.math.component3
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.UP
import net.minecraft.util.Direction.WEST
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.World
import java.util.Random
import kotlin.math.min

class StrongholdRoom_Trap_Prison(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM) {
	class Trigger : ITriggerHandler {
		private companion object {
			private const val SPAWNS_LEFT_TAG = "SpawnsLeft"
		}
		
		private var spawnsLeft = -1
		
		override fun check(world: World): Boolean {
			return !world.isRemote && !world.isPeaceful
		}
		
		override fun update(entity: EntityTechnicalTrigger) {
			val world = entity.world
			val facing = entity.horizontalFacing
			
			val pos1 = Pos(entity).offset(facing.rotateY(), 2)
			val pos2 = pos1.offset(facing, 7).offset(facing.rotateYCCW(), 4).offset(UP, 2)
			
			val (x1, y1, z1) = pos1.center
			val (x2, y2, z2) = pos2.center
			
			val targets = world.selectVulnerableEntities.inBox<PlayerEntity>(AxisAlignedBB(x1, y1, z1, x2, y2, z2))
			
			if (spawnsLeft == -1) {
				if (targets.isEmpty()) {
					return
				}
				else {
					spawnsLeft = 2 + world.difficulty.id
				}
			}
			
			val rand = world.rand
			val minPos = pos1.min(pos2)
			val maxPos = pos1.max(pos2)
			
			repeat(min(spawnsLeft, rand.nextRounded(1.66F))) {
				for (attempt in 1..8) {
					val testPos = Pos(rand.nextInt(minPos.x, maxPos.x), maxPos.y + 1, rand.nextInt(minPos.z, maxPos.z))
					
					if (StrongholdPieces.isStoneBrick(testPos.getBlock(world))) {
						testPos.breakBlock(world, false)
						
						world.spawn(ModEntities.SILVERFISH, testPos.bottomCenter, yaw = rand.nextFloat(0F, 360F)) {
							attackTarget = rand.nextItemOrNull(targets)
							PacketClientFX(EntityMobSilverfish.FX_SPAWN_PARTICLE, FxVecData(posVec.addY(0.35))).sendToAllAround(this, 8.0)
						}
						
						--spawnsLeft
						break
					}
				}
			}
			
			if (spawnsLeft == 0) {
				entity.remove()
			}
		}
		
		override fun nextTimer(rand: Random): Int {
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
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.addTrigger(Pos(1, 1, 3), EntityStructureTrigger(STRONGHOLD_TRAP_PRISON, EAST))
		
		val rand = world.rand
		
		// Chest
		
		world.addTrigger(Pos(1, 1, 1), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
		
		// Skull
		
		val skullType = if (rand.nextInt(3) == 0) Blocks.SKELETON_SKULL else Blocks.ZOMBIE_HEAD
		world.setState(Pos(maxX - 1, 1, 1), skullType.with(SKULL_ROTATION, 10))
		
		// Redstone
		
		repeat(5 + rand.nextInt(5 + rand.nextInt(6))) {
			val redstonePos = Pos(rand.nextInt(1, maxX - 1), 1, rand.nextInt(1, 5))
			
			if (world.isAir(redstonePos)) {
				world.setBlock(redstonePos, Blocks.REDSTONE_WIRE)
			}
		}
	}
}
