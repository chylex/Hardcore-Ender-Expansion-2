package chylex.hee.game.block.fluid
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.named
import net.minecraft.block.material.MaterialColor
import net.minecraft.fluid.Fluid
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.FluidAttributes
import net.minecraftforge.fluids.FluidAttributes.Builder
import net.minecraftforge.fluids.ForgeFlowingFluid
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties
import java.util.function.Supplier

abstract class FluidBase(fluidName: String, val rgbColor: IntColor, val mapColor: MaterialColor, val resistance: Float, texStill: ResourceLocation, texFlowing: ResourceLocation){
	val fogColor = rgbColor.asVec
	
	lateinit var still: ForgeFlowingFluid.Source
	lateinit var flowing: ForgeFlowingFluid.Flowing
	
	init{
		val supplyStill   = Supplier<Fluid> { still }
		val supplyFlowing = Supplier<Fluid> { flowing }
		
		val attr = FluidAttributes.builder(texStill, texFlowing).color(rgbColor.i).let(::attr) // TODO translation key
		val props = Properties(supplyFlowing, supplyStill, attr).explosionResistance(resistance).let(::props)
		
		still   = ForgeFlowingFluid.Source(props) named fluidName
		flowing = ForgeFlowingFluid.Flowing(props) named "flowing_$fluidName"
	}
	
	abstract fun attr(attributes: Builder): Builder
	abstract fun props(properties: Properties): Properties
}
