package chylex.hee.game.world.structure.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection.AlignmentType.EVEN
import chylex.hee.game.world.structure.piece.IStructurePieceConnection.AlignmentType.EVEN_MIRRORED
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode.APPEND
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode.MERGE
import chylex.hee.game.world.structure.world.OffsetStructureWorld
import chylex.hee.game.world.util.Size
import net.minecraft.util.math.BlockPos

class StructureBuild<T : StructurePiece<*>.MutableInstance>(val size: Size){
	constructor(size: Size, startingPiece: PositionedPiece<T>) : this(size){
		pieces.add(startingPiece)
	}
	
	constructor(size: Size, startingPiece: T) : this(size, PositionedPiece<T>(startingPiece, size.centerPos.subtract(startingPiece.size.centerPos)))
	
	enum class AddMode{
		APPEND, MERGE
	}
	
	class PositionedPiece<T : StructurePiece<*>.Instance>(val instance: T, val offset: BlockPos){
		val pieceBox = instance.size.toBoundingBox(offset)
		
		fun freeze(): PositionedPiece<StructurePiece<*>.Instance>{
			return PositionedPiece(instance.freeze(), offset)
		}
	}
	
	private val structureBox = size.toBoundingBox(BlockPos.ZERO)
	private val pieces = mutableListOf<PositionedPiece<T>>()
	private var chain = -1
	
	val generatedPieces: List<PositionedPiece<T>> = pieces
	
	private fun commitPiece(newPiece: PositionedPiece<T>){
		pieces.add(newPiece)
		
		if (chain != -1){
			++chain
		}
	}
	
	fun addPiece(newPiece: T, newPiecePos: BlockPos, mode: AddMode = APPEND): PositionedPiece<T>?{
		val addedPiece = PositionedPiece(newPiece, newPiecePos)
		val addedBox = addedPiece.pieceBox
		
		if (addedBox.isInside(structureBox) && (mode == MERGE || pieces.none { addedBox.intersects(it.pieceBox) })){
			return addedPiece.apply(::commitPiece)
		}
		
		return null
	}
	
	fun addPiece(newPiece: T, newPieceConnection: IStructurePieceConnection, targetPiece: PositionedPiece<T>, targetPieceConnection: IStructurePieceConnection, mode: AddMode = APPEND): PositionedPiece<T>?{
		val alignedPos = targetPiece.offset.add(alignConnections(newPieceConnection, targetPieceConnection, mode))
		
		val addedPiece = PositionedPiece(newPiece, alignedPos)
		val addedBox = addedPiece.pieceBox
		
		if (addedBox.isInside(structureBox) && pieces.none { addedBox.intersects(it.pieceBox) && (mode == APPEND || it !== targetPiece) }){
			val targetInstance = targetPiece.instance
			
			newPiece.useConnection(newPieceConnection, targetInstance)
			targetInstance.useConnection(targetPieceConnection, newPiece)
			
			return addedPiece.apply(::commitPiece)
		}
		
		return null
	}
	
	private fun alignConnections(newPieceConnection: IStructurePieceConnection, targetPieceConnection: IStructurePieceConnection, mode: AddMode): BlockPos{
		val newWidth = newPieceConnection.alignment
		val targetWidth = targetPieceConnection.alignment
		
		val targetFacing = targetPieceConnection.facing
		val unalignedPos = targetPieceConnection.offset.offset(targetFacing, if (mode == MERGE) 0 else 1).subtract(newPieceConnection.offset)
		
		return when{
			targetWidth == EVEN && newWidth == EVEN ->
				unalignedPos.offset(targetFacing.rotateY(), 1)
			
			targetWidth == EVEN_MIRRORED && newWidth == EVEN_MIRRORED ->
				unalignedPos.offset(targetFacing.rotateYCCW(), 1)
			
			else ->
				unalignedPos
		}
		
	}
	
	fun beginChain(){
		check(chain == -1){ "cannot begin a structure build chain when the last one has not finished" }
		chain = 0
	}
	
	fun commitChain(){
		chain = -1
	}
	
	fun revertChain(){
		while(--chain >= 0){ // ends at -1
			val removed = pieces.removeAt(pieces.lastIndex).instance
			
			for(neighbor in removed.restoreAllConnections()){
				check(neighbor.restoreConnection(removed)){ "failed reverting chain, found an asymmetric connection" }
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
	
	inline fun guardChain(attempts: Int, func: () -> Boolean): Boolean{
		for(attempt in 1..attempts){
			if (guardChain(func)){
				return true
			}
		}
		
		return false
	}
	
	fun freeze(): IStructureBuild{
		val finalPieces = pieces.map(PositionedPiece<*>::freeze).toTypedArray()
		
		return object : IStructureBuild{
			override val size = this@StructureBuild.size
			
			override val boundingBoxes
				get() = finalPieces.map { it.pieceBox }
			
			override fun generate(world: IStructureWorld){
				for(piece in finalPieces){
					piece.instance.generate(OffsetStructureWorld(world, piece.offset))
				}
			}
		}
	}
}
