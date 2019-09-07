package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.STRONGHOLD_TRAP_TALL_INTERSECTION
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.system.util.Facing4
import chylex.hee.system.util.Pos
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.isAir
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.selectVulnerableEntities
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.EnumDifficulty.PEACEFUL
import net.minecraft.world.World
import java.util.Random

class StrongholdRoom_Trap_TallIntersection(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM){
	object Trigger : ITriggerHandler{
		override fun check(world: World): Boolean{
			return !world.isRemote && world.difficulty != PEACEFUL
		}
		
		override fun update(entity: EntityTechnicalTrigger){
			val world = entity.world
			
			val area = entity.entityBoundingBox.grow(2.5, 0.0, 2.5).expand(0.0, 2.0, 0.0)
			val targets = world.selectVulnerableEntities.inBox<EntityPlayer>(area)
			
			if (targets.isEmpty()){
				return
			}
			
			val rand = world.rand
			var spawnsLeft = rand.nextInt(3, 5) + world.difficulty.id
			
			for(attempt in 1..500){
				val testPos = Pos(entity).add(
					rand.nextInt(-4, 4) / rand.nextInt(1, 2),
					rand.nextInt(0, 1),
					rand.nextInt(-4, 4) / rand.nextInt(1, 2)
				)
				
				if (StrongholdPieces.isStoneBrick(testPos.getBlock(world)) && Facing4.any { testPos.offset(it).isAir(world) }){
					testPos.breakBlock(world, false)
					
					EntityMobSilverfish(world).apply {
						setLocationAndAngles(testPos.x + 0.5, testPos.y.toDouble(), testPos.z + 0.5, rand.nextFloat(0F, 360F), 0F)
						delayHideInBlockAI(20 * 30)
						world.spawnEntity(this)
						
						spawnExplosionParticle()
						attackTarget = rand.nextItem(targets)
					}
					
					if (--spawnsLeft == 0){
						break
					}
				}
			}
			
			entity.setDead()
		}
		
		override fun nextTimer(rand: Random): Int{
			return 10
		}
	}
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 1, centerZ), EntityStructureTrigger(STRONGHOLD_TRAP_TALL_INTERSECTION))
	}
}
