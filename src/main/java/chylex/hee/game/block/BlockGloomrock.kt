package chylex.hee.game.block
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.ENERGY_SHRINE_GLOBAL
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectEntities
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity

class BlockGloomrock(builder: Builder) : BlockSimple(builder){
	override fun canEntitySpawn(state: IBlockState, entity: Entity): Boolean{
		val pos = entity.posVec
		val size = EnergyShrinePieces.STRUCTURE_SIZE
		
		val trigger = entity
			.world
			.selectEntities
			.inBox<EntityTechnicalTrigger>(size.toCenteredBoundingBox(pos))
			.find { it.triggerType == ENERGY_SHRINE_GLOBAL }
		
		return trigger == null || !size.toCenteredBoundingBox(trigger.posVec).contains(pos)
	}
}
