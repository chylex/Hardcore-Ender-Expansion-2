package chylex.hee.game.block.fluid
import chylex.hee.game.block.fluid.distances.FlowingFluid5
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.facades.Resource
import net.minecraft.block.material.MaterialColor
import net.minecraft.util.BlockRenderLayer.SOLID
import net.minecraftforge.fluids.FluidAttributes.Builder
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties

object FluidEnderGoo : FluidBase(
	fluidName  = "ender_goo",
	rgbColor   = RGB(136, 26, 190),
	mapColor   = MaterialColor.PURPLE,
	resistance = 150F,
	texStill   = Resource.Custom("block/ender_goo_still"),
	texFlowing = Resource.Custom("block/ender_goo_flowing")
){
	override fun attr(attributes: Builder): Builder = with(attributes){
		density(1500)
		viscosity(1500)
		temperature(233)
	}
	
	override fun props(properties: Properties): Properties = with(properties){
		block { ModBlocks.ENDER_GOO }
		bucket { ModItems.ENDER_GOO_BUCKET }
		renderLayer(SOLID)
	}
	
	override fun constructFlowingFluid(properties: Properties) = FlowingFluid5(properties)
}
