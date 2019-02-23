package chylex.hee.game.world.structure.piece
import chylex.hee.game.world.structure.IStructureGenerator
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode.APPEND
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode.MERGE
import chylex.hee.game.world.structure.piece.StructurePiece.Instance
import chylex.hee.game.world.structure.piece.StructurePiece.MutableInstance
import chylex.hee.game.world.structure.world.OffsetStructureWorld
import chylex.hee.game.world.util.Size
import net.minecraft.util.math.BlockPos

class StructureBuild<T : MutableInstance>(val size: Size, startingPiece: T){
	enum class AddMode{
		APPEND, MERGE
	}
	
	class PositionedPiece<T : Instance>(val instance: T, val offset: BlockPos){
		val pieceBox = instance.size.toBoundingBox(offset)
		
		fun freeze(): PositionedPiece<Instance>{
			return PositionedPiece(instance.freeze(), offset)
		}
	}
	
	private val structureBox = size.toBoundingBox(BlockPos.ORIGIN)
	private val pieces = mutableListOf(PositionedPiece(startingPiece, size.centerPos.subtract(startingPiece.size.centerPos)))
	private var chain = -1
	
	val generatedPieces: List<PositionedPiece<T>> = pieces
	
	fun addPiece(newPiece: T, newPieceConnection: IStructurePieceConnection, targetPiece: PositionedPiece<T>, targetPieceConnection: IStructurePieceConnection, mode: AddMode = APPEND): PositionedPiece<T>?{
		val alignedPos = targetPiece.offset
			.add(targetPieceConnection.offset)
			.offset(targetPieceConnection.facing, if (mode == MERGE) 0 else 1)
			.subtract(newPieceConnection.offset)
		
		val addedPiece = PositionedPiece(newPiece, alignedPos)
		val addedBox = addedPiece.pieceBox
		
		if (addedBox.isInside(structureBox) && pieces.none { addedBox.intersects(it.pieceBox) && (mode == APPEND || it !== targetPiece) }){
			val targetInstance = targetPiece.instance
			
			newPiece.useConnection(newPieceConnection, targetInstance)
			targetInstance.useConnection(targetPieceConnection, newPiece)
			
			pieces.add(addedPiece)
			
			if (chain != -1){
				++chain
			}
			
			return addedPiece
		}
		
		return null
	}
	
	fun beginChain(){
		if (chain != -1){
			throw IllegalStateException("cannot begin a structure build chain when the last one has not finished")
		}
		
		chain = 0
	}
	
	fun commitChain(){
		chain = -1
	}
	
	fun revertChain(){
		while(--chain >= 0){ // ends at -1
			val removed = pieces.removeAt(pieces.lastIndex).instance
			
			for(neighbor in removed.restoreAllConnections()){
				if (!neighbor.restoreConnection(removed)){
					throw IllegalStateException("failed reverting chain, found an asymmetric connection")
				}
			}
		}
	}
	
	inline fun guardChain(func: () -> Boolean): Boolean{
		beginChain()
		
		return if (func()){
			commitChain()
			true
		}
		else{
			revertChain()
			false
		}
	}
	
	fun freeze(): IStructureGenerator{
		val finalPieces = pieces.map(PositionedPiece<*>::freeze).toTypedArray()
		
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
