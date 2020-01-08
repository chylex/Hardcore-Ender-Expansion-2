package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.util.ColoredBlocks
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.util.Pos
import net.minecraft.item.DyeColor

class StrongholdRoom_Decor_GlassCorners(file: String, private val glassColor: DyeColor) : StrongholdRoom_Decor_Generic(file){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		for(x in intArrayOf(-3, 3)) for(z in intArrayOf(-3, 3)){
			val column = Pos(centerX + x, 2, centerZ + z)
			world.placeCube(column, column.up(2), Single(ColoredBlocks.STAINED_GLASS.getValue(glassColor)))
		}
	}
}
