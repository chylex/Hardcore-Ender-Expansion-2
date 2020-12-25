package chylex.hee.game.world.feature.energyshrine.piece

import chylex.hee.game.block.withFacing
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.system.collection.MutableWeightedList.Companion.mutableWeightedListOf
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import net.minecraft.block.Blocks

class EnergyShrineRoom_Primary_SplitT(file: String) : EnergyShrineRoom_Generic(file) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineConnection(ROOM, Pos(centerX - 1, 0, 0), NORTH),
		EnergyShrineConnection(ROOM, Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		val decorations = mutableWeightedListOf(
			75 to Single(ModBlocks.DARK_CHEST.withFacing(WEST)),
			
			20 to Single(ModBlocks.POTTED_DEATH_FLOWER_WITHERED),
			15 to Single(Blocks.FLOWER_POT),
			15 to Single(Blocks.FLOWER_POT),
			
			50 to Air,
			25 to Air,
			 5 to Air
		)
		
		for(z in 3..(maxZ - 3)) {
			placeDecoration(world, Pos(maxX - 1, 2, z), decorations.removeItem(rand))
		}
		
		placeWallBanner(world, instance, Pos(maxX - 1, 3, 1), WEST)
		placeWallBanner(world, instance, Pos(maxX - 1, 3, maxZ - 1), WEST)
	}
}
