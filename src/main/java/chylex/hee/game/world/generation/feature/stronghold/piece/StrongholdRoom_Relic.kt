package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.util.withFacing
import chylex.hee.game.inventory.util.setStack
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.file.IStructurePieceFromFile
import chylex.hee.game.world.generation.structure.file.IStructurePieceFromFile.Delegate
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.TileEntityStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.math.BlockPos

abstract class StrongholdRoom_Relic(file: String, private val relicItem: ItemStack) : StrongholdAbstractPiece(), IStructurePieceFromFile by Delegate("stronghold/$file", StrongholdPieces.PALETTE) {
	final override val type = StrongholdPieceType.ROOM
	
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH)
	)
	
	protected abstract val lootChestPos: BlockPos
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		
		// floor and ceiling
		
		world.placeCube(Pos(0, 0, 0), Pos(maxX, 0, maxZ), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		world.placeCube(Pos(0, maxY, 0), Pos(maxX, maxY, maxZ), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		
		// 3 walls opposite of entrance
		
		world.placeCube(Pos(0, 1, 0), Pos(0, maxY - 1, maxZ), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		world.placeCube(Pos(1, 1, 0), Pos(maxX, maxY - 1, 0), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		world.placeCube(Pos(maxX, 1, 0), Pos(maxX, maxY - 1, maxZ), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		
		// entrance wall with door cutout
		
		world.placeCube(Pos(1, 1, maxZ), Pos(centerX - 3, 4, maxZ), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		world.placeCube(Pos(centerX + 3, 1, maxZ), Pos(maxX, 4, maxZ), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		world.placeCube(Pos(1, 5, maxZ), Pos(maxX, maxY - 1, maxZ), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		
		// contents
		
		generator.generate(world)
		
		val chestState = ModBlocks.LOOT_CHEST.withFacing(SOUTH)
		val chestTile = TileEntityLootChest().apply { sourceInventory.setStack(13, relicItem.copy()) }
		
		world.addTrigger(lootChestPos, TileEntityStructureTrigger(chestState, chestTile))
	}
}
