package chylex.hee.datagen.server

import chylex.hee.game.block.IHeeBlock
import chylex.hee.init.ModBlocks
import chylex.hee.system.getRegistryEntries
import net.minecraft.block.Block
import net.minecraft.data.BlockTagsProvider
import net.minecraft.data.DataGenerator
import net.minecraft.tags.ITag.INamedTag
import net.minecraftforge.common.data.ExistingFileHelper

class BlockTags(generator: DataGenerator, modId: String, existingFileHelper: ExistingFileHelper?) : BlockTagsProvider(generator, modId, existingFileHelper) {
	private val registeredTags = mutableSetOf<INamedTag<Block>>()
	
	val allRegisteredTags: Set<INamedTag<Block>>
		get() = registeredTags
	
	override fun registerTags() {
		for (block in getRegistryEntries<Block>(ModBlocks)) {
			val tags = (block as? IHeeBlock)?.tags
			if (!tags.isNullOrEmpty()) {
				registerTags(block, tags)
			}
		}
	}
	
	private fun registerTags(block: Block, tags: List<INamedTag<Block>>) {
		for (tag in tags) {
			getOrCreateBuilder(tag).addItemEntry(block)
			registeredTags.add(tag)
		}
	}
}
