package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.TERMINAL
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.LootChestStructureTrigger
import chylex.hee.game.world.util.allInBoxMutable
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextItem
import net.minecraft.block.Blocks
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.math.BlockPos

class EnergyShrineRoom_Secondary_Storage(file: String) : EnergyShrineRoom_Generic(file) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(TERMINAL, Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineConnection(TERMINAL, Pos(centerX - 1, 0, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val points = arrayOf(
			Pos(1, 2, 3),
			Pos(1, 2, maxZ - 4),
			Pos(maxX - 3, 2, 3),
			Pos(maxX - 3, 2, maxZ - 4)
		)
		
		for (pos in points) {
			placeShelfDecorations(world, pos)
			placeShelfDecorations(world, pos.up(2))
		}
		
		val rand = world.rand
		val chestZ = rand.nextInt(0, 1)
		val chestPos = rand.nextItem(points).add(rand.nextInt(0, 2), if (rand.nextBoolean()) 0 else 2, chestZ)
		
		world.setState(chestPos, ModBlocks.DARK_CHEST.withFacing(if (chestZ == 0) NORTH else SOUTH))
		world.addTrigger(chestPos, LootChestStructureTrigger(EnergyShrinePieces.LOOT_PICK(rand), rand.nextLong()))
	}
	
	private fun placeShelfDecorations(world: IStructureWorld, pos: BlockPos) {
		val rand = world.rand
		
		val block = when (rand.nextInt(100)) {
			in  0..59 -> ModBlocks.GLOOMTORCH
			in 60..69 -> Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE
			in 70..79 -> Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
			else      -> return
		}
		
		pos.allInBoxMutable(pos.add(2, 0, 1)).forEach {
			if (rand.nextInt(4) != 0) {
				world.setBlock(it, block)
			}
		}
	}
}
