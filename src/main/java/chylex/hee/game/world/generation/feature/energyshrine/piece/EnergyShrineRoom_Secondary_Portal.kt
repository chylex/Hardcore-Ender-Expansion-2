package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.block.util.ColoredBlocks
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextItem
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH

class EnergyShrineRoom_Secondary_Portal(file: String) : EnergyShrineRoom_Generic(file) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val carpet = Single(ColoredBlocks.CARPET.getValue(world.rand.nextItem()))
		
		world.placeCube(Pos(3, 1, 2), Pos(3, 1, maxZ - 1), carpet)
		world.placeCube(Pos(maxX - 3, 1, 2), Pos(maxX - 3, 1, maxZ - 1), carpet)
		
		placeWallBanner(world, instance, Pos(1, maxY - 2, maxZ - 2), NORTH)
		placeWallBanner(world, instance, Pos(maxX - 1, maxY - 2, maxZ - 2), NORTH)
	}
}
