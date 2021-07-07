package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.LootChestStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

class EnergyShrineRoom_Primary_TwoFloorT(file: String) : EnergyShrineRoom_Generic(file) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineConnection(ROOM, Pos(maxX, 5, 2), EAST),
		EnergyShrineConnection(ROOM, Pos(0, 5, 3), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val cornerBlock = getContext(instance).cornerBlock
		world.setBlock(Pos(2, 0, 1), cornerBlock)
		world.setBlock(Pos(maxX - 2, 0, 1), cornerBlock)
		world.setBlock(Pos(1, 5, 1), cornerBlock)
		world.setBlock(Pos(maxX - 1, 5, 1), cornerBlock)
		
		placeWallBanner(world, instance, Pos(6, 5, maxZ - 3), EAST)
		placeWallBanner(world, instance, Pos(maxX - 6, 5, maxZ - 3), WEST)
		
		val rand = world.rand
		val chestPos = Pos(if (rand.nextBoolean()) 4 else maxX - 4, 6, maxZ - 1)
		
		world.setState(chestPos, ModBlocks.DARK_CHEST.withFacing(NORTH))
		world.addTrigger(chestPos, LootChestStructureTrigger(EnergyShrinePieces.LOOT_PICK(rand), rand.nextLong()))
	}
}
