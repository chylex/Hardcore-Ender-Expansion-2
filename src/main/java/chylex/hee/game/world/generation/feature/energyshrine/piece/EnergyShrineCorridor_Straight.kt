package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.CORRIDOR
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH

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
