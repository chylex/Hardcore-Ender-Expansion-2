package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.CORRIDOR
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

class EnergyShrineCorridor_StraightLit(length: Int) : EnergyShrineAbstractPiece() {
	override val size = Size(6, 6, length)
	
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(CORRIDOR, Pos(size.centerX, 0, size.maxZ), SOUTH),
		EnergyShrineConnection(CORRIDOR, Pos(size.centerX - 1, 0, 0), NORTH)
	)
	
	override val ceilingBlock
		get() = ModBlocks.GLOOMROCK_BRICKS
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.placeCube(Pos(2, 1, 1), Pos(size.maxX - 2, size.maxY - 2, size.maxZ - 1), Air)
		
		world.placeCube(Pos(1, 1, 1), Pos(1, size.maxY - 2, size.maxZ - 1), Single(ModBlocks.GLOOMROCK_BRICKS))
		world.placeCube(Pos(size.maxX - 1, 1, 1), Pos(size.maxX - 1, size.maxY - 2, size.maxZ - 1), Single(ModBlocks.GLOOMROCK_BRICKS))
		
		world.setState(Pos(1, 2, size.centerZ), ModBlocks.GLOOMTORCH.withFacing(EAST))
		world.setState(Pos(size.maxX - 1, 2, size.centerZ), ModBlocks.GLOOMTORCH.withFacing(WEST))
	}
}
