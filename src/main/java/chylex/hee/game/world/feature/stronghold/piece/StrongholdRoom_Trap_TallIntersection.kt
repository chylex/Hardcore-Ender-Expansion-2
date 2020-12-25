package chylex.hee.game.world.feature.stronghold.piece

import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.selectVulnerableEntities
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.STRONGHOLD_TRAP_TALL_INTERSECTION
import chylex.hee.game.world.Pos
import chylex.hee.game.world.breakBlock
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.isAir
import chylex.hee.game.world.isPeaceful
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.system.facades.Facing4
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
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
			val targets = world.selectVulnerableEntities.inBox<EntityPlayer>(area)
			
			if (targets.isEmpty()) {
				return
			}
			
			val rand = world.rand
			var spawnsLeft = rand.nextInt(3, 5) + world.difficulty.id
			
			for(attempt in 1..500) {
				val testPos = Pos(entity).add(
					rand.nextInt(-4, 4) / rand.nextInt(1, 2),
					rand.nextInt(0, 1),
					rand.nextInt(-4, 4) / rand.nextInt(1, 2)
				)
				
				if (StrongholdPieces.isStoneBrick(testPos.getBlock(world)) && Facing4.any { testPos.offset(it).isAir(world) }) {
					testPos.breakBlock(world, false)
					
					EntityMobSilverfish(world).apply {
						setLocationAndAngles(testPos.x + 0.5, testPos.y.toDouble(), testPos.z + 0.5, rand.nextFloat(0F, 360F), 0F)
						delayHideInBlockAI(20 * 30)
						world.addEntity(this)
						
						spawnExplosionParticle()
						attackTarget = rand.nextItem(targets)
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
