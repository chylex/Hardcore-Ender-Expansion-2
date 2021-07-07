package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.STRONGHOLD_TRAP_TALL_INTERSECTION
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectVulnerableEntities
import chylex.hee.game.fx.FxVecData
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.trigger.EntityStructureTrigger
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.isAir
import chylex.hee.game.world.util.isPeaceful
import chylex.hee.game.world.util.spawn
import chylex.hee.init.ModEntities
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import chylex.hee.util.math.Pos
import chylex.hee.util.math.bottomCenter
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World
import java.util.Random

class StrongholdRoom_Trap_TallIntersection(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM) {
	object Trigger : ITriggerHandler {
		override fun check(world: World): Boolean {
			return !world.isRemote && !world.isPeaceful
		}
		
		override fun update(entity: EntityTechnicalTrigger) {
			val world = entity.world
			
			val area = entity.boundingBox.grow(2.5, 0.0, 2.5).expand(0.0, 2.0, 0.0)
			val targets = world.selectVulnerableEntities.inBox<PlayerEntity>(area)
			
			if (targets.isEmpty()) {
				return
			}
			
			val rand = world.rand
			var spawnsLeft = rand.nextInt(3, 5) + world.difficulty.id
			
			for (attempt in 1..500) {
				val testPos = Pos(entity).add(
					rand.nextInt(-4, 4) / rand.nextInt(1, 2),
					rand.nextInt(0, 1),
					rand.nextInt(-4, 4) / rand.nextInt(1, 2)
				)
				
				if (StrongholdPieces.isStoneBrick(testPos.getBlock(world)) && Facing4.any { testPos.offset(it).isAir(world) }) {
					testPos.breakBlock(world, false)
					
					world.spawn(ModEntities.SILVERFISH, testPos.bottomCenter, yaw = rand.nextFloat(0F, 360F)) {
						delayHideInBlockAI(20 * 30)
						attackTarget = rand.nextItem(targets)
						PacketClientFX(EntityMobSilverfish.FX_SPAWN_PARTICLE, FxVecData(posVec)).sendToAllAround(this, 8.0)
					}
					
					if (--spawnsLeft == 0) {
						break
					}
				}
			}
			
			entity.remove()
		}
		
		override fun nextTimer(rand: Random): Int {
			return 10
		}
	}
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 1, centerZ), EntityStructureTrigger(STRONGHOLD_TRAP_TALL_INTERSECTION))
	}
}
