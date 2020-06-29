package chylex.hee.game.block.fluid
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.named
import net.minecraft.block.material.MaterialColor
import net.minecraft.fluid.Fluid
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader
import net.minecraftforge.fluids.FluidAttributes
import net.minecraftforge.fluids.FluidAttributes.Builder
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.ForgeFlowingFluid
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties
import java.util.function.BiFunction
import java.util.function.Supplier

@Suppress("LeakingThis")
abstract class FluidBase(fluidName: String, val rgbColor: IntColor, val mapColor: MaterialColor, val resistance: Float, texStill: ResourceLocation, texFlowing: ResourceLocation){
	val fogColor = rgbColor.asVec
	
	lateinit var still: ForgeFlowingFluid.Source
	lateinit var flowing: ForgeFlowingFluid.Flowing
	
	init{
		val supplyStill   = Supplier<Fluid> { still }
		val supplyFlowing = Supplier<Fluid> { flowing }
		
		val attr = FluidAttributesFixColor.Builder(texStill, texFlowing).color(rgbColor.i).let(::attr)
		val props = Properties(supplyStill, supplyFlowing, attr).explosionResistance(resistance).let(::props)
		
		still   = ForgeFlowingFluid.Source(props) named fluidName
		flowing = constructFlowingFluid(props) named "flowing_$fluidName"
	}
	
	abstract fun attr(attributes: Builder): Builder
	abstract fun props(properties: Properties): Properties
	
	abstract fun constructFlowingFluid(properties: Properties): ForgeFlowingFluid.Flowing
	
	private class FluidAttributesFixColor(builder: FluidAttributes.Builder, fluid: Fluid) : FluidAttributes(builder, fluid){
		class Builder(stillTexture: ResourceLocation, flowingTexture: ResourceLocation) : FluidAttributes.Builder(stillTexture, flowingTexture, BiFunction(::FluidAttributesFixColor))
		
		// keep the color property, but stop it from tinting the texture
		
		override fun getColor(stack: FluidStack) = RGB(255u).i
		override fun getColor(world: ILightReader, pos: BlockPos) = RGB(255u).i
	}
}
