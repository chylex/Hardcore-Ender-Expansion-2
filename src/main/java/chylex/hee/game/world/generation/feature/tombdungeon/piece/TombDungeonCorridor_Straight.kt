package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnectionType.CORRIDOR
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

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
			val rand = world.rand
			
			world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 1, size.maxZ - 1), Air)
			
			if (rand.nextInt(4) == 0) {
				placeCrumblingCeiling(world, instance, 1)
			}
			
			for (z in 1 until length step 3) {
				if (rand.nextInt(16) != 0) {
					continue
				}
				
				val type = rand.nextInt(2)
				if (type == 0 || rand.nextInt(13) == 0) {
					placeWallTorch(world, Pos(1, 2, z), EAST)
				}
				if (type == 1 || rand.nextInt(13) == 0) {
					placeWallTorch(world, Pos(size.maxX - 1, 2, z), WEST)
				}
			}
		}
		
		placeCobwebs(world, instance)
	}
}
