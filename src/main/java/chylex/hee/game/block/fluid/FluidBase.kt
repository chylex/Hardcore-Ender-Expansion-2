package chylex.hee.game.block.fluid
import chylex.hee.system.util.color.IntColor
import net.minecraft.block.material.MapColor
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid

abstract class FluidBase(fluidName: String, val rgbColor: IntColor, val mapColor: MapColor, texStill: ResourceLocation, texFlowing: ResourceLocation) : Fluid(fluidName, texStill, texFlowing){
	val fogColor = rgbColor.asVec
}
