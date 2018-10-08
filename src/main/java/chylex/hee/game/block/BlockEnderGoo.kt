package chylex.hee.game.block
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.material.Materials
import chylex.hee.init.ModItems
import net.minecraft.entity.Entity
import kotlin.math.pow

open class BlockEnderGoo : BlockAbstractGoo(FluidEnderGoo, Materials.ENDER_GOO){
	override val filledBucket
		get() = ModItems.ENDER_GOO_BUCKET
	
	// Behavior
	
	override fun modifyEntityMotion(entity: Entity, level: Int){
		val strength = ((quantaPerBlock - level) / quantaPerBlockFloat).pow(1.75F)
		
		entity.motionX *= 0.8 - (0.75 * strength)
		entity.motionY *= 1.0 - (0.24 * strength)
		entity.motionZ *= 0.8 - (0.75 * strength)
	}
}
