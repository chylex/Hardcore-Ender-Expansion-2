package chylex.hee.game.world.feature.energyshrine.piece

import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.CORRIDOR
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.math.Size
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH

class EnergyShrineCorridor_Straight(length: Int) : EnergyShrineAbstractPiece() {
	override val size = Size(4, 6, length)
	
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(CORRIDOR, Pos(size.centerX, 0, size.maxZ), SOUTH),
		EnergyShrineConnection(CORRIDOR, Pos(size.centerX - 1, 0, 0), NORTH)
	)
	
	override val ceilingBlock
		get() = ModBlocks.GLOOMROCK_BRICKS
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 2, size.maxZ - 1), Air)
	}
}
