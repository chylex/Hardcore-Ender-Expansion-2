package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.SECRET_CONNECTOR
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.migration.Facing
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.setStack
import chylex.hee.system.util.withFacing
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import java.util.Random

class TombDungeonSecret_LootChest(file: String) : TombDungeonSecret(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(SECRET_CONNECTOR, Pos(centerX, 0, maxZ), Facing.SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val chestState = ModBlocks.LOOT_CHEST.withFacing(Facing.SOUTH)
		val chestTile = TileEntityLootChest().apply { sourceInventory.setStack(13, ItemStack(ModItems.RING_OF_HUNGER)) }
		
		world.addTrigger(Pos(centerX, 1, 2), TileEntityStructureTrigger(chestState, chestTile))
	}
	
	override fun pickRandomEntrancePoint(rand: Random): BlockPos{
		return Pos(
			rand.nextInt(rand.nextInt(centerX - 2, centerX - 1), rand.nextInt(centerX + 1, centerX + 2)),
			rand.nextInt(1, maxY),
			maxZ
		)
	}
	
	override fun pickChestPosition(rand: Random): Pair<BlockPos, Direction>? = null
}
