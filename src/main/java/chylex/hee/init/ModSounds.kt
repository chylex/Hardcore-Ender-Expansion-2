package chylex.hee.init
import chylex.hee.system.facades.Resource
import net.minecraft.util.SoundEvent

object ModSounds{
	@JvmField val BLOCK_CAULDRON_BREW              = SoundEvent(Resource.Custom("block.cauldron.brew"))
	@JvmField val BLOCK_DEATH_FLOWER_WITHER        = SoundEvent(Resource.Custom("block.death_flower.wither"))
	@JvmField val BLOCK_EXPERIENCE_GATE_PICKUP     = SoundEvent(Resource.Custom("block.experience_gate.pickup"))
	@JvmField val BLOCK_EXPERIENCE_GATE_LEVELUP    = SoundEvent(Resource.Custom("block.experience_gate.levelup"))
	@JvmField val BLOCK_IGNEOUS_PLATE_COOL         = SoundEvent(Resource.Custom("block.igneous_plate.cool"))
	@JvmField val BLOCK_JAR_O_DUST_SHATTER         = SoundEvent(Resource.Custom("block.jar_o_dust.shatter"))
	@JvmField val BLOCK_LOOT_CHEST_OPEN            = SoundEvent(Resource.Custom("block.loot_chest.open"))
	@JvmField val BLOCK_MINERS_BURIAL_ALTAR_INSERT = SoundEvent(Resource.Custom("block.miners_burial_altar.insert"))
	@JvmField val BLOCK_MINERS_BURIAL_ALTAR_DONE   = SoundEvent(Resource.Custom("block.miners_burial_altar.done"))
	@JvmField val BLOCK_MINERS_BURIAL_ALTAR_SPAWN  = SoundEvent(Resource.Custom("block.miners_burial_altar.spawn"))
	@JvmField val BLOCK_OBSIDIAN_LAND              = SoundEvent(Resource.Custom("block.obsidian.land"))
	@JvmField val BLOCK_PUZZLE_LOGIC_CLICK         = SoundEvent(Resource.Custom("block.puzzle_logic.click"))
	@JvmField val BLOCK_SPAWNER_EXPIRE             = SoundEvent(Resource.Custom("block.spawner.expire"))
	
	@JvmField val ITEM_PUZZLE_MEDALLION_INSERT              = SoundEvent(Resource.Custom("item.puzzle_medallion.insert"))
	@JvmField val ITEM_PUZZLE_MEDALLION_SPAWN               = SoundEvent(Resource.Custom("item.puzzle_medallion.spawn"))
	@JvmField val ITEM_REVITALIZATION_SUBSTANCE_USE_FAIL    = SoundEvent(Resource.Custom("item.revitalization_substance.use.fail"))
	@JvmField val ITEM_REVITALIZATION_SUBSTANCE_USE_SUCCESS = SoundEvent(Resource.Custom("item.revitalization_substance.use.success"))
	@JvmField val ITEM_RING_OF_PRESERVATION_USE             = SoundEvent(Resource.Custom("item.ring_of_preservation.use"))
	@JvmField val ITEM_SCALE_OF_FREEFALL_USE                = SoundEvent(Resource.Custom("item.scale_of_freefall.use"))
	@JvmField val ITEM_TABLE_LINK_USE_FAIL                  = SoundEvent(Resource.Custom("item.table_link.use.fail"))
	@JvmField val ITEM_TABLE_LINK_USE_SPECIAL               = SoundEvent(Resource.Custom("item.table_link.use.special"))
	@JvmField val ITEM_TABLE_LINK_USE_SUCCESS               = SoundEvent(Resource.Custom("item.table_link.use.success"))
	
	@JvmField val ENTITY_GENERIC_TELEPORT              = SoundEvent(Resource.Custom("entity.generic.teleport"))
	@JvmField val ENTITY_IGNEOUS_ROCK_BURN             = SoundEvent(Resource.Custom("entity.igneous_rock.burn"))
	@JvmField val ENTITY_PLAYER_DEATH_NO_SUBTITLES     = SoundEvent(Resource.Custom("entity.player.death.no_subtitles"))
	@JvmField val ENTITY_PLAYER_TELEPORT               = SoundEvent(Resource.Custom("entity.player.teleport"))
	@JvmField val ENTITY_REVITALIZATION_SUBSTANCE_HEAL = SoundEvent(Resource.Custom("entity.revitalization_substance.heal"))
	@JvmField val ENTITY_SPATIAL_DASH_EXPIRE           = SoundEvent(Resource.Custom("entity.spatial_dash.expire"))
	@JvmField val ENTITY_TOKEN_HOLDER_DROP             = SoundEvent(Resource.Custom("entity.token_holder.drop"))
	
	@JvmField val MOB_ENDER_EYE_HIT_FAIL     = SoundEvent(Resource.Custom("mob.ender_eye.hit.fail"))
	@JvmField val MOB_ENDERMAN_FIRST_KILL    = SoundEvent(Resource.Custom("mob.enderman.first_kill"))
	@JvmField val MOB_ENDERMAN_TELEPORT_FAIL = SoundEvent(Resource.Custom("mob.enderman.teleport.fail"))
	@JvmField val MOB_ENDERMAN_TELEPORT_OUT  = SoundEvent(Resource.Custom("mob.enderman.teleport.out"))
	@JvmField val MOB_UNDREAD_DEATH          = SoundEvent(Resource.Custom("mob.undread.death"))
	@JvmField val MOB_UNDREAD_HURT           = SoundEvent(Resource.Custom("mob.undread.hurt"))
	@JvmField val MOB_VILLAGER_TOTEM_DEATH   = SoundEvent(Resource.Custom("mob.villager.totem_death"))
	@JvmField val MOB_VILLAGER_TOTEM_DYING   = SoundEvent(Resource.Custom("mob.villager.totem_dying"))
}
