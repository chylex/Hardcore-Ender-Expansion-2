package chylex.hee.game.block
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.material.Materials
import chylex.hee.init.ModItems

open class BlockEnderGoo : BlockAbstractGoo(FluidEnderGoo, Materials.ENDER_GOO){
	override val filledBucket
		get() = ModItems.ENDER_GOO_BUCKET
}
