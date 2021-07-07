package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.block.util.CHEST_TYPE
import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.LootChestStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import net.minecraft.state.properties.ChestType
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

class EnergyShrineRoom_Primary_TwoFloorFork(file: String) : EnergyShrineRoom_Generic(file) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineConnection(ROOM, Pos(0, 0, 3), WEST),
		EnergyShrineConnection(ROOM, Pos(1, 5, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		world.setState(Pos(1, 6, maxZ - 1), ModBlocks.DARK_CHEST.withFacing(EAST).with(CHEST_TYPE, ChestType.RIGHT))
		world.addTrigger(Pos(1, 6, maxZ - 1), LootChestStructureTrigger(EnergyShrinePieces.LOOT_GENERAL, rand.nextLong()))
		
		world.setState(Pos(1, 6, maxZ - 2), ModBlocks.DARK_CHEST.withFacing(EAST).with(CHEST_TYPE, ChestType.LEFT))
		world.addTrigger(Pos(1, 6, maxZ - 2), LootChestStructureTrigger(EnergyShrinePieces.LOOT_GENERAL, rand.nextLong()))
	}
}
