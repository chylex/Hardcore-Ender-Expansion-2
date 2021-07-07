package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.block.util.BUTTON_ATTACH_FACE
import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.util.collection.weightedListOf
import chylex.hee.util.math.Pos
import net.minecraft.block.Blocks
import net.minecraft.state.properties.AttachFace
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

class EnergyShrineRoom_Primary_Secretariat(file: String) : EnergyShrineRoom_Generic(file) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(maxX - 3, 0, maxZ), SOUTH),
		EnergyShrineConnection(ROOM, Pos(0, 0, maxZ - 3), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		val decorations = weightedListOf(
			35 to Air,
			25 to Single(Blocks.STONE_BUTTON.withFacing(if (rand.nextBoolean()) NORTH else EAST).with(BUTTON_ATTACH_FACE, AttachFace.FLOOR)),
			20 to Single(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE),
			20 to Single(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE),
			15 to Single(ModBlocks.POTTED_DEATH_FLOWER_WITHERED)
		)
		
		for (x in 2..6) {
			world.setState(Pos(x, 2, 4), decorations.generateItem(rand).pick(rand))
		}
		
		placeWallBanner(world, instance, Pos(4, 2, maxZ - 5), SOUTH)
	}
}
