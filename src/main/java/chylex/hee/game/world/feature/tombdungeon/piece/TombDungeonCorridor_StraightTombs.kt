package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.CORRIDOR
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.TOMB_ENTRANCE_OUTSIDE
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.math.Size
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.random.nextItem
import java.util.Random

class TombDungeonCorridor_StraightTombs(entranceSpacing: Int, configuration: Configuration, tombsPerSide: Int, private val tombConstructor: (Boolean) -> TombDungeonAbstractPiece, override val isFancy: Boolean) : TombDungeonAbstractPiece(), ITombDungeonPieceWithTombs{
	enum class Configuration{
		WEST, EAST, BOTH;
		
		companion object{
			fun random(rand: Random): Configuration{
				return if (rand.nextInt(3) != 0)
					BOTH
				else
					rand.nextItem()
			}
		}
	}
	
	override val size = Size(5, 5, tombsPerSide * entranceSpacing)
	override val sidePathAttachWeight = 0
	override val secretAttachWeight = 1
	
	override val connections = mutableListOf<IStructurePieceConnection>().also {
		it.add(TombDungeonConnection(CORRIDOR, Pos(size.centerX, 0, size.maxZ), SOUTH))
		it.add(TombDungeonConnection(CORRIDOR, Pos(size.centerX, 0, 0), NORTH))
		
		val offset = entranceSpacing / 2
		
		for(tombIndex in 0 until tombsPerSide){
			if (configuration == Configuration.WEST || configuration == Configuration.BOTH){
				it.add(TombDungeonConnection(TOMB_ENTRANCE_OUTSIDE, Pos(0, 0, offset + (tombIndex * entranceSpacing)), WEST))
			}
			
			if (configuration == Configuration.EAST || configuration == Configuration.BOTH){
				it.add(TombDungeonConnection(TOMB_ENTRANCE_OUTSIDE, Pos(size.maxX, 0, offset + (tombIndex * entranceSpacing)), EAST))
			}
		}
	}.toTypedArray()
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 1, size.maxZ - 1), Air)
		
		if (world.rand.nextInt(5) == 0){
			placeCrumblingCeiling(world, instance, 1)
		}
		
		placeCobwebs(world, instance)
	}
	
	override fun constructTomb(): TombDungeonAbstractPiece{
		return tombConstructor(isFancy)
	}
}
