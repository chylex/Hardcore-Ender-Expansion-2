package chylex.hee.game.world.structure.piece
import chylex.hee.game.world.structure.IStructureGenerator
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.StructurePiece.Instance
import chylex.hee.game.world.structure.world.OffsetStructureWorld
import chylex.hee.game.world.util.Size
import net.minecraft.util.math.BlockPos

class StructureBuild(val size: Size, startingPiece: Instance){
	class PositionedPiece(val instance: Instance, val offset: BlockPos){
		val pieceBox = instance.size.toBoundingBox(offset)
	}
	
	private val structureBox = size.toBoundingBox(BlockPos.ORIGIN)
	private val pieces = mutableListOf(PositionedPiece(startingPiece, size.centerPos.subtract(startingPiece.size.centerPos)))
	
	val generatedPieces: List<PositionedPiece> = pieces
	
	fun addPiece(newPiece: Instance, newPieceConnection: IStructurePieceConnection, targetPiece: PositionedPiece, targetPieceConnection: IStructurePieceConnection): Boolean{
		val alignedPos = targetPiece.offset
			.add(targetPieceConnection.offset)
			.offset(targetPieceConnection.facing)
			.subtract(newPieceConnection.offset)
		
		val addedPiece = PositionedPiece(newPiece, alignedPos)
		val addedBox = addedPiece.pieceBox
		
		if (addedBox.isInside(structureBox) && pieces.none { addedBox.intersects(it.pieceBox) }){
			newPiece.useConnection(newPieceConnection)
			targetPiece.instance.useConnection(targetPieceConnection)
			
			pieces.add(addedPiece)
			return true
		}
		
		return false
	}
	
	fun freeze(): IStructureGenerator{
		val finalPieces = pieces.toTypedArray()
		
		return object : IStructureGenerator{
			override val size = this@StructureBuild.size
			
			override fun generate(world: IStructureWorld){
				for(piece in finalPieces){
					piece.instance.generate(OffsetStructureWorld(world, piece.offset))
				}
			}
		}
	}
}
