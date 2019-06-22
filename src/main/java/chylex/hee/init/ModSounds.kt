package chylex.hee.init
import chylex.hee.system.Resource
import net.minecraft.util.SoundEvent

object ModSounds{
	@JvmField val BLOCK_OBSIDIAN_LAND   = SoundEvent(Resource.Custom("block.obsidian.land"))
	@JvmField val BLOCK_LOOT_CHEST_OPEN = SoundEvent(Resource.Custom("block.loot_chest.open"))
	
	@JvmField val REVITALIZATION_SUBSTANCE_USE      = SoundEvent(Resource.Custom("item.revitalization_substance.use"))
	@JvmField val REVITALIZATION_SUBSTANCE_USE_FAIL = SoundEvent(Resource.Custom("item.revitalization_substance.use.fail"))
	@JvmField val ITEM_TABLE_LINK_USE               = SoundEvent(Resource.Custom("item.table_link.use"))
	@JvmField val ITEM_TABLE_LINK_USE_FAIL          = SoundEvent(Resource.Custom("item.table_link.use.fail"))
}
