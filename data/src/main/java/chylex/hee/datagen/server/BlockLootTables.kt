package chylex.hee.datagen.server

import chylex.hee.HEE
import chylex.hee.datagen.server.util.BlockLootTableProvider
import chylex.hee.game.block.IHeeBlock
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockDrop.FlowerPot
import chylex.hee.game.block.properties.BlockDrop.Manual
import chylex.hee.game.block.properties.BlockDrop.NamedTile
import chylex.hee.game.block.properties.BlockDrop.Nothing
import chylex.hee.game.block.properties.BlockDrop.OneOf
import chylex.hee.game.block.properties.BlockDrop.Self
import chylex.hee.init.ModBlocks
import chylex.hee.system.getRegistryEntries
import net.minecraft.block.Block
import net.minecraft.block.FlowerPotBlock
import net.minecraft.data.DataGenerator
import net.minecraft.loot.LootTables

class BlockLootTables(generator: DataGenerator) : BlockLootTableProvider(generator) {
	override val consumer = object : RegistrationConsumer() {
		override fun addTables() {
			for (block in getRegistryEntries<Block>(ModBlocks)) {
				(block as? IHeeBlock)?.let { registerDrop(block, it.drop) }
			}
		}
		
		private fun registerDrop(block: Block, drop: BlockDrop) {
			if (block.lootTable == LootTables.EMPTY && drop != Nothing) {
				HEE.log.error("[BlockLootTables] block has empty loot table but declares drops: " + block.registryName)
				return
			}
			
			when (drop) {
				Nothing, Manual -> return
				Self            -> registerDropSelfLootTable(block)
				NamedTile       -> registerLootTable(block, withName)
				FlowerPot       -> registerFlowerPot(block as FlowerPotBlock)
				is OneOf        -> registerDropping(block, drop.item)
			}
		}
	}
}
