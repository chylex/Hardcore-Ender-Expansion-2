package chylex.hee.init
import chylex.hee.system.Resource
import net.minecraft.util.SoundEvent

object ModSounds{
	@JvmField val BLOCK_LOOT_CHEST_OPEN = SoundEvent(Resource.Custom("block.loot_chest.open"))
	@JvmField val BLOCK_OBSIDIAN_LAND   = SoundEvent(Resource.Custom("block.obsidian.land"))
	
	@JvmField val ITEM_REVITALIZATION_SUBSTANCE_USE_SUCCESS = SoundEvent(Resource.Custom("item.revitalization_substance.use.success"))
	@JvmField val ITEM_REVITALIZATION_SUBSTANCE_USE_FAIL    = SoundEvent(Resource.Custom("item.revitalization_substance.use.fail"))
	@JvmField val ITEM_TABLE_LINK_USE_SUCCESS               = SoundEvent(Resource.Custom("item.table_link.use.success"))
	@JvmField val ITEM_TABLE_LINK_USE_SPECIAL               = SoundEvent(Resource.Custom("item.table_link.use.special"))
	@JvmField val ITEM_TABLE_LINK_USE_FAIL                  = SoundEvent(Resource.Custom("item.table_link.use.fail"))
	
	@JvmField val ENTITY_REVITALIZATION_SUBSTANCE_HEAL = SoundEvent(Resource.Custom("entity.revitalization_substance.heal"))
	
	@JvmField val MOB_UNDREAD_HURT  = SoundEvent(Resource.Custom("mob.undread.hurt"))
	@JvmField val MOB_UNDREAD_DEATH = SoundEvent(Resource.Custom("mob.undread.death"))
}
