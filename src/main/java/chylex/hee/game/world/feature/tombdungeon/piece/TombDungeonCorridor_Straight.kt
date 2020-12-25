package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.CORRIDOR
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.math.Size
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH

class TombDungeonCorridor_Straight(length: Int, override val isFancy: Boolean) : TombDungeonAbstractPiece() {
	override val size = Size(5, 5, length)
	override val sidePathAttachWeight = 0
	override val secretAttachWeight = 1
	
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(CORRIDOR, Pos(size.centerX, 0, size.maxZ), SOUTH),
		TombDungeonConnection(CORRIDOR, Pos(size.centerX, 0, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		placeLayout(world)
		
		val length = size.z
		
		if (length > 1 || (length == 1 && !instance.hasAvailableConnections)) {
			placeConnections(world, instance)
		}
		
		if (length > 2) {
			world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 1, size.maxZ - 1), Air)
			
			if (world.rand.nextInt(4) == 0) {
				placeCrumblingCeiling(world, instance, 1)
			}
		}
		
		placeCobwebs(world, instance)
	}
}
