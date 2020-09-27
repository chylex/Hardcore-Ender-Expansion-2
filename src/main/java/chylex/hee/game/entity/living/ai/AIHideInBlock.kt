package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.posVec
import chylex.hee.game.world.Pos
import chylex.hee.game.world.getState
import chylex.hee.game.world.setState
import chylex.hee.system.facades.Facing6
import chylex.hee.system.math.addY
import chylex.hee.system.migration.EntityCreature
import chylex.hee.system.random.nextItem
import net.minecraft.block.BlockState
import net.minecraft.world.GameRules.MOB_GRIEFING

class AIHideInBlock(
	private val entity: EntityCreature,
	private val chancePerTick: Int,
	private val tryHideInBlock: (BlockState) -> BlockState?
) : AIBaseContinuous(){
	override fun tick(){
		if (entity.attackTarget == null &&
			entity.navigator.noPath() &&
			entity.world.gameRules.getBoolean(MOB_GRIEFING) &&
			entity.rng.nextInt(chancePerTick) == 0
		){
			val world = entity.world
			val targetPos = Pos(entity.posVec.addY(0.5)).offset(entity.rng.nextItem(Facing6))
			
			tryHideInBlock(targetPos.getState(world))?.let{
				targetPos.setState(world, it)
				entity.spawnExplosionParticle()
				entity.remove()
			}
		}
	}
}
