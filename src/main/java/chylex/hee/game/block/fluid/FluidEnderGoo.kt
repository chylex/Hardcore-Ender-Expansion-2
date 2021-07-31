package chylex.hee.game.block.fluid

import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.util.color.RGB
import net.minecraft.block.material.MaterialColor
import net.minecraftforge.fluids.FluidAttributes.Builder
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties

object FluidEnderGoo : FluidBase(
	registryName  = "ender_goo",
	localizedName = "Ender Goo",
	rgbColor      = RGB(147, 37, 194),
	mapColor      = MaterialColor.PURPLE,
	resistance    = 150F
) {
	override fun attr(attributes: Builder): Builder = with(attributes) {
		density(1500)
		viscosity(1500)
		temperature(233)
	}
	
	override fun props(properties: Properties): Properties = with(properties) {
		block { ModBlocks.ENDER_GOO }
		bucket { ModItems.ENDER_GOO_BUCKET }
		tickRate(18)
	}
	
	override fun constructFlowingFluid(properties: Properties) = FlowingFluid5(properties)
}
