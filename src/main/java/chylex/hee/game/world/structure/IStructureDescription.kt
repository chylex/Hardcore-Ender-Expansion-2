package chylex.hee.game.world.structure
import chylex.hee.game.world.structure.file.Palette
import chylex.hee.game.world.structure.piece.IStructureBuilder
import chylex.hee.game.world.structure.piece.StructurePiece
import chylex.hee.game.world.util.PosXZ
import chylex.hee.game.world.util.Size
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IStructureDescription{
	val STRUCTURE_SIZE: Size
	
	val STRUCTURE_BUILDER: IStructureBuilder
	val STRUCTURE_LOCATOR: (World, PosXZ) -> BlockPos?
	
	val PALETTE: Palette
	val ALL_PIECES: Array<out StructurePiece<*>>
	
	companion object{
		val NULL_LOCATOR: (World, PosXZ) -> BlockPos? = { _, _ -> null}
	}
}
