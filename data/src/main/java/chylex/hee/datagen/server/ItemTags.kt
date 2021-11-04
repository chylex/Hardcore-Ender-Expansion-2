package chylex.hee.datagen.server

import chylex.hee.game.item.IHeeItem
import chylex.hee.init.ModItems
import net.minecraft.data.DataGenerator
import net.minecraft.data.ItemTagsProvider
import net.minecraft.item.Item
import net.minecraft.tags.ITag.INamedTag
import net.minecraft.tags.ItemTags
import net.minecraftforge.common.data.ExistingFileHelper

class ItemTags(dataGenerator: DataGenerator, private val blockTags: BlockTags, modId: String, existingFileHelper: ExistingFileHelper?) : ItemTagsProvider(dataGenerator, blockTags, modId, existingFileHelper) {
	override fun registerTags() {
		val itemTags = ItemTags.getAllTags().associateBy { it.name }
		
		for (blockTag in blockTags.allRegisteredTags) {
			itemTags[blockTag.name]?.let { copy(blockTag, it) }
		}
		
		for (item in ModItems.ALL) {
			val tags = (item as? IHeeItem)?.tags
			if (!tags.isNullOrEmpty()) {
				registerTags(item, tags)
			}
		}
	}
	
	private fun registerTags(item: Item, tags: List<INamedTag<Item>>) {
		for (tag in tags) {
			getOrCreateBuilder(tag).addItemEntry(item)
		}
	}
}
