package chylex.hee.game.block.fluid

import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.util.color.RGB
import net.minecraft.block.material.MaterialColor
import net.minecraftforge.fluids.FluidAttributes.Builder
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties

object FluidEnderGooPurified : FluidBase(
	registryName  = "purified_ender_goo",
	localizedName = "Purified Ender Goo",
	rgbColor      = RGB(189, 88, 234),
	mapColor      = MaterialColor.MAGENTA,
	resistance    = 40F
) {
	override fun attr(attributes: Builder): Builder = with(attributes) {
		density(1400)
		viscosity(1400)
		temperature(290)
	}
	
	override fun props(properties: Properties): Properties = with(properties) {
		block { ModBlocks.PURIFIED_ENDER_GOO }
		bucket { ModItems.PURIFIED_ENDER_GOO_BUCKET }
		tickRate(16)
	}
	
	override fun constructFlowingFluid(properties: Properties) = FlowingFluid5(properties)
}
