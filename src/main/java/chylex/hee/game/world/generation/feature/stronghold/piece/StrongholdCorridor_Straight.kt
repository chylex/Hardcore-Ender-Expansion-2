package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.CORRIDOR
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH

class StrongholdCorridor_Straight(length: Int) : StrongholdAbstractPiece() {
	override val type = StrongholdPieceType.CORRIDOR
	override val size = Size(5, 5, length)
	
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(CORRIDOR, Pos(size.centerX, 0, 0), NORTH),
		StrongholdConnection(CORRIDOR, Pos(size.centerX, 0, size.maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 1, size.maxZ - 1), Air)
	}
}
