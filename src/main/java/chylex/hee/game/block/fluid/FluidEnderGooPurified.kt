package chylex.hee.game.block.fluid
import chylex.hee.system.Resource
import chylex.hee.system.util.color.IntColor.Companion.RGB
import net.minecraft.block.material.MapColor

object FluidEnderGooPurified : FluidBase(
	fluidName  = "purified_ender_goo",
	rgbColor   = RGB(189, 88, 234),
	mapColor   = MapColor.MAGENTA,
	texStill   = Resource.Custom("block/purified_ender_goo_still"),
	texFlowing = Resource.Custom("block/purified_ender_goo_flowing")
){
	init{
		density = 1400
		viscosity = 1400
		temperature = 290
	}
}
