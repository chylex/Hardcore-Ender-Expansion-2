package chylex.hee.game.world.feature.obsidiantower
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerLevel_Top
import chylex.hee.game.world.structure.piece.IStructureBuild
import chylex.hee.game.world.structure.piece.IStructureBuilder
import chylex.hee.game.world.structure.piece.StructureBuild
import chylex.hee.game.world.structure.piece.StructurePiece
import chylex.hee.game.world.util.Size.Alignment.CENTER
import chylex.hee.game.world.util.Size.Alignment.MIN
import chylex.hee.game.world.util.Transform
import chylex.hee.system.util.nextItem
import net.minecraft.util.Rotation
import java.util.Random

class ObsidianTowerBuilder(private val floors: Int, private val topPiece: ObsidianTowerLevel_Top) : IStructureBuilder{
	override fun build(rand: Random): IStructureBuild?{
		val transform1 = Transform(rotation = rand.nextItem(), mirror = false)
		val transform2 = transform1.copy(rotation = transform1.rotation.add(Rotation.CLOCKWISE_180))
		
		val size = ObsidianTowerPieces.calculateStructureSize(floors)
		val build = StructureBuild<StructurePiece.MutableInstance>(size)
		
		val bottomPos = size.getPos(CENTER, MIN, CENTER)
		var y = 0
		
		for(floor in 0..floors){
			val piece = when(floor){
				floors -> topPiece
				0 -> ObsidianTowerPieces.PIECE_LEVEL_BOTTOM
				else -> ObsidianTowerPieces.PIECE_LEVEL_MIDDLE
			}
			
			build.addPiece(piece.MutableInstance(if (floor % 2 == 0) transform1 else transform2), bottomPos.up(y).subtract(piece.size.getPos(CENTER, MIN, CENTER)))
			y += piece.size.y
		}
		
		return build.freeze()
	}
}
