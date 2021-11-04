package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.STRONGHOLD_TRAP_CORNER_HOLES
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectVulnerableEntities
import chylex.hee.game.fx.FxVecData
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.trigger.EntityStructureTrigger
import chylex.hee.game.world.util.isPeaceful
import chylex.hee.game.world.util.spawn
import chylex.hee.init.ModEntities
import chylex.hee.network.client.PacketClientFX
import chylex.hee.util.math.Pos
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextItemOrNull
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World
import java.util.Random
import kotlin.math.min

class StrongholdRoom_Trap_CornerHoles(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM) {
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
			val rand = world.rand
			
			if (spawnsLeft == -1) {
				val area = entity.boundingBox.grow(3.5, 0.0, 3.5).expand(0.0, 2.0, 0.0)
				
				if (world.selectVulnerableEntities.inBox<PlayerEntity>(area).isEmpty()) {
					return
				}
				else {
					spawnsLeft = rand.nextInt(5, 7) + ((world.difficulty.id - 1) * 2)
				}
			}
			
			val targetArea = entity.boundingBox.grow(6.0, 0.0, 6.0).expand(0.0, 4.0, 0.0)
			val targets = world.selectVulnerableEntities.inBox<PlayerEntity>(targetArea)
			
			repeat(min(spawnsLeft, rand.nextInt(1, 3))) {
				val spawnPos = entity.posVec.add(
					4.5 * (if (rand.nextBoolean()) 1 else -1),
					2.0,
					4.5 * (if (rand.nextBoolean()) 1 else -1)
				)
				
				world.spawn(ModEntities.SILVERFISH, spawnPos, yaw = rand.nextFloat(0F, 360F)) {
					delayHideInBlockAI(20 * 30)
					fallDistance = 1.5F
					attackTarget = rand.nextItemOrNull(targets)
					PacketClientFX(EntityMobSilverfish.FX_SPAWN_PARTICLE, FxVecData(posVec)).sendToAllAround(this, 8.0)
				}
				
				--spawnsLeft
			}
			
			if (spawnsLeft == 0) {
				entity.remove()
			}
		}
		
		override fun nextTimer(rand: Random): Int {
			return if (spawnsLeft == -1)
				10
			else
				4 + (3 * rand.nextInt(0, 2))
		}
		
		override fun serializeNBT() = TagCompound().apply {
			putShort(SPAWNS_LEFT_TAG, spawnsLeft.toShort())
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			spawnsLeft = getShort(SPAWNS_LEFT_TAG).toInt()
		}
	}
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 1, centerZ), EntityStructureTrigger(STRONGHOLD_TRAP_CORNER_HOLES))
	}
}
