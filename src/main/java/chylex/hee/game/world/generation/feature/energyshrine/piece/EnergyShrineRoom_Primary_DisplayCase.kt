package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.block.util.BUTTON_ATTACH_FACE
import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.IBlockPicker.Weighted.Companion.Weighted
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.util.collection.mutableWeightedListOf
import chylex.hee.util.math.Pos
import net.minecraft.block.Blocks
import net.minecraft.state.properties.AttachFace
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

class EnergyShrineRoom_Primary_DisplayCase(file: String) : EnergyShrineRoom_Generic(file) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(3, 0, maxZ), SOUTH),
		EnergyShrineConnection(ROOM, Pos(2, 0, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
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
				9 to Blocks.STONE_BUTTON.withFacing(if (rand.nextBoolean()) NORTH else EAST).with(BUTTON_ATTACH_FACE, AttachFace.FLOOR)
			),
			
			15 to Air,
			 5 to Air
		)
		
		for (z in 4..(maxZ - 4)) {
			placeDecoration(world, Pos(5, 2, z), decorations.removeItem(rand))
		}
		
		placeWallBanner(world, instance, Pos(4, 3, 3), WEST)
		placeWallBanner(world, instance, Pos(4, 3, maxZ - 3), WEST)
	}
}
