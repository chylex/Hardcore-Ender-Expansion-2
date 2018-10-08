package chylex.hee.game.block.fluid
import chylex.hee.game.render.util.RGB
import net.minecraft.block.material.MapColor
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fluids.Fluid

abstract class FluidBase(fluidName: String, val rgbColor: RGB, val mapColor: MapColor, texStill: ResourceLocation, texFlowing: ResourceLocation) : Fluid(fluidName, texStill, texFlowing){
	val fogColor = rgbColor.let { (r, g, b) -> Vec3d(r / 255.0, g / 255.0, b / 255.0) }
}
