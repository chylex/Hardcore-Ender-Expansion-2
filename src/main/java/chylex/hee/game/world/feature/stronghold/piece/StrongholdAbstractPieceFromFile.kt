package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdRoomConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.file.StructureFiles
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

abstract class StrongholdAbstractPieceFromFile(file: String, override val type: StrongholdPieceType) : StrongholdAbstractPiece(){
	private val generator = StructureFiles.loadWithCache("stronghold/$file").Generator(StrongholdPieces.PALETTE)
	
	final override val size = generator.size
	
	protected val maxX = size.maxX
	protected val maxY = size.maxY
	protected val maxZ = size.maxZ
	
	protected val centerX = size.centerX
	protected val centerY = size.centerY
	protected val centerZ = size.centerZ
	
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdRoomConnection(Pos(centerX, 0, 0), NORTH),
		StrongholdRoomConnection(Pos(centerX, 0, maxZ), SOUTH),
		StrongholdRoomConnection(Pos(maxX, 0, centerZ), EAST),
		StrongholdRoomConnection(Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		generator.generate(world)
	}
}
