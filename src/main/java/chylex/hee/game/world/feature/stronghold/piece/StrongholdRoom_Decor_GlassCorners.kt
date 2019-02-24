package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType.ROOM
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.util.Pos
import net.minecraft.block.BlockStainedGlass
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor

class StrongholdRoom_Decor_GlassCorners(file: String, private val glassColor: EnumDyeColor) : StrongholdAbstractPieceFromFile(file, ROOM){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		for(x in intArrayOf(-3, 3)) for(z in intArrayOf(-3, 3)){
			val column = Pos(centerX + x, 2, centerZ + z)
			world.placeCube(column, column.up(2), Single(Blocks.STAINED_GLASS.defaultState.withProperty(BlockStainedGlass.COLOR, glassColor)))
		}
	}
}
