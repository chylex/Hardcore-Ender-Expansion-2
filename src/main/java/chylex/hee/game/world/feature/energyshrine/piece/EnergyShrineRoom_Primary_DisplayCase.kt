package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IBlockPicker.Weighted.Companion.Weighted
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.system.collection.MutableWeightedList.Companion.mutableWeightedListOf
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.withFacing

class EnergyShrineRoom_Primary_DisplayCase(file: String) : EnergyShrineRoom_Generic(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(3, 0, maxZ), SOUTH),
		EnergyShrineConnection(ROOM, Pos(2, 0, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		val decorations = mutableWeightedListOf(
			75 to Single(ModBlocks.DARK_CHEST.withFacing(WEST)),
			
			25 to Single(ModBlocks.GLOOMTORCH),
			 5 to Single(ModBlocks.GLOOMTORCH),
			
			15 to Single(ModBlocks.POTTED_DEATH_FLOWER_WITHERED),
			15 to Single(Blocks.FLOWER_POT),
			
			25 to Weighted(
				5 to Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultState,
				8 to Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultState,
				9 to Blocks.STONE_BUTTON.withFacing(UP)
			),
			
			15 to Air,
			 5 to Air
		)
		
		for(z in 4..(maxZ - 4)){
			placeDecoration(world, Pos(5, 2, z), decorations.removeItem(rand))
		}
		
		placeWallBanner(world, instance, Pos(4, 3, 3), WEST)
		placeWallBanner(world, instance, Pos(4, 3, maxZ - 3), WEST)
	}
}
