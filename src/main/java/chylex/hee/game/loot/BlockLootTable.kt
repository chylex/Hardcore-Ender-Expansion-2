package chylex.hee.game.loot
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.world.IBlockAccess
import net.minecraft.world.WorldServer
import net.minecraft.world.storage.loot.LootContext

class BlockLootTable(private val resource: ResourceLocation){
	fun generateDrops(drops: MutableList<ItemStack>, world: IBlockAccess, fortune: Int){
		if (world !is WorldServer){
			return
		}
		
		for(drop in world.lootTableManager.getLootTableFromLocation(resource).generateLootForPools(world.rand, BlockLootContext(world, fortune))){
			drops.add(drop)
		}
	}
	
	class BlockLootContext(world: WorldServer, val fortune: Int): LootContext(0F, world, world.lootTableManager, null, null, null)
}
