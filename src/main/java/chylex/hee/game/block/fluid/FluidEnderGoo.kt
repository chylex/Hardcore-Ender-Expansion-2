package chylex.hee.game.block.fluid

import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.facades.Resource
import net.minecraft.block.material.MaterialColor
import net.minecraftforge.fluids.FluidAttributes.Builder
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties

object FluidEnderGoo : FluidBase(
	fluidName  = "ender_goo",
	rgbColor   = RGB(147, 37, 194),
	mapColor   = MaterialColor.PURPLE,
	resistance = 150F,
	texStill   = Resource.Custom("block/ender_goo_still"),
	texFlowing = Resource.Custom("block/ender_goo_flowing")
) {
	override fun attr(attributes: Builder): Builder = with(attributes) {
		density(1500)
		viscosity(1500)
		temperature(233)
	}
	
	override fun props(properties: Properties): Properties = with(properties) {
		block { ModBlocks.ENDER_GOO }
		bucket { ModItems.ENDER_GOO_BUCKET }
	}
	
	override fun constructFlowingFluid(properties: Properties) = FlowingFluid5(properties)
}
