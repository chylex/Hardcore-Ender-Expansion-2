package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.world.feature.stronghold.connection.StrongholdRoomConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.setStack
import net.minecraft.block.BlockDirectional.FACING
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.math.BlockPos

abstract class StrongholdRoom_Relic(file: String, private val relicItem: ItemStack) : StrongholdAbstractPieceFromFile(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdRoomConnection(Pos(centerX, 0, maxZ), SOUTH)
	)
	
	protected abstract val lootChestPos: BlockPos
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val chestState = ModBlocks.LOOT_CHEST.defaultState.withProperty(FACING, SOUTH)
		val chestTile = TileEntityLootChest().apply { sourceInventory.setStack(13, relicItem.copy()) }
		
		world.addTrigger(lootChestPos, TileEntityStructureTrigger(chestState, chestTile))
	}
}
