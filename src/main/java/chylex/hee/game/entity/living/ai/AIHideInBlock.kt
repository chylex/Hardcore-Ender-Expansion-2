package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.living.ai.util.AIBaseContinuous
import chylex.hee.system.util.Facing6
import chylex.hee.system.util.Pos
import chylex.hee.system.util.getState
import chylex.hee.system.util.posVec
import chylex.hee.system.util.setState
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityCreature

class AIHideInBlock(
	private val entity: EntityCreature,
	private val chancePerTick: Int,
	private val tryHideInBlock: (IBlockState) -> IBlockState?
) : AIBaseContinuous(){
	override fun tick(){
		if (entity.attackTarget == null &&
			entity.navigator.noPath() &&
			entity.world.gameRules.getBoolean("mobGriefing") &&
			entity.rng.nextInt(chancePerTick) == 0
		){
			val world = entity.world
			val targetPos = Pos(entity.posVec.add(0.0, 0.5, 0.0)).offset(Facing6.randomOne(entity.rng))
			
			tryHideInBlock(targetPos.getState(world))?.let{
				targetPos.setState(world, it)
				entity.spawnExplosionParticle()
				entity.setDead()
			}
		}
	}
}
