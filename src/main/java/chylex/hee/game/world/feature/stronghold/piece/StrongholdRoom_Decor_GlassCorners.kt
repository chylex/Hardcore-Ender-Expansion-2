package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.util.Pos
import chylex.hee.system.util.with
import net.minecraft.block.BlockStainedGlass
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor

class StrongholdRoom_Decor_GlassCorners(file: String, private val glassColor: EnumDyeColor) : StrongholdRoom_Decor_Generic(file){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		for(x in intArrayOf(-3, 3)) for(z in intArrayOf(-3, 3)){
			val column = Pos(centerX + x, 2, centerZ + z)
			world.placeCube(column, column.up(2), Single(Blocks.STAINED_GLASS.with(BlockStainedGlass.COLOR, glassColor)))
		}
	}
}
