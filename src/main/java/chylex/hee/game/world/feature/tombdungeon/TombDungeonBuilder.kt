package chylex.hee.game.world.feature.tombdungeon
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces.STRUCTURE_SIZE
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonStart
import chylex.hee.game.world.structure.piece.IStructureBuild
import chylex.hee.game.world.structure.piece.IStructureBuilder
import chylex.hee.game.world.structure.piece.StructureBuild
import chylex.hee.game.world.structure.piece.StructureBuild.PositionedPiece
import chylex.hee.game.world.util.Size.Alignment.CENTER
import chylex.hee.game.world.util.Size.Alignment.MAX
import chylex.hee.game.world.util.Transform
import net.minecraft.util.math.BlockPos
import java.util.Random

object TombDungeonBuilder : IStructureBuilder{
	val ENTRANCE_POS: BlockPos = STRUCTURE_SIZE.getPos(CENTER, MAX, MAX).add(-TombDungeonStart.size.centerX, -TombDungeonStart.size.y, -STRUCTURE_SIZE.x / 3)
	
	override fun build(rand: Random): IStructureBuild?{
		val startingPiece = TombDungeonStart.MutableInstance(Transform.NONE)
		val startingPiecePos = ENTRANCE_POS
		
		val build = StructureBuild(STRUCTURE_SIZE, PositionedPiece(startingPiece, startingPiecePos))
		// TODO
		return build.freeze()
	}
}
