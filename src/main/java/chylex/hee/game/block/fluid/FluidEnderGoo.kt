package chylex.hee.game.block.fluid
import chylex.hee.system.Resource
import chylex.hee.system.util.color.IntColor.Companion.RGB
import net.minecraft.block.material.MapColor

object FluidEnderGoo : FluidBase(
	fluidName  = "ender_goo",
	rgbColor   = RGB(136, 26, 190),
	mapColor   = MapColor.PURPLE,
	texStill   = Resource.Custom("block/ender_goo_still"),
	texFlowing = Resource.Custom("block/ender_goo_flowing")
){
	init{
		density = 1500
		viscosity = 1500
		temperature = 233
	}
}
