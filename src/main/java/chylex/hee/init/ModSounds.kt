package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.forge.named
import chylex.hee.system.forge.registerAllFields
import net.minecraft.util.SoundEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModSounds {
	@JvmField val BLOCK_CAULDRON_BREW              = sound("block.cauldron.brew")
	@JvmField val BLOCK_DEATH_FLOWER_WITHER        = sound("block.death_flower.wither")
	@JvmField val BLOCK_EXPERIENCE_GATE_PICKUP     = sound("block.experience_gate.pickup")
	@JvmField val BLOCK_EXPERIENCE_GATE_LEVELUP    = sound("block.experience_gate.levelup")
	@JvmField val BLOCK_IGNEOUS_PLATE_COOL         = sound("block.igneous_plate.cool")
	@JvmField val BLOCK_JAR_O_DUST_SHATTER         = sound("block.jar_o_dust.shatter")
	@JvmField val BLOCK_LOOT_CHEST_OPEN            = sound("block.loot_chest.open")
	@JvmField val BLOCK_MINERS_BURIAL_ALTAR_INSERT = sound("block.miners_burial_altar.insert")
	@JvmField val BLOCK_MINERS_BURIAL_ALTAR_DONE   = sound("block.miners_burial_altar.done")
	@JvmField val BLOCK_MINERS_BURIAL_ALTAR_SPAWN  = sound("block.miners_burial_altar.spawn")
	@JvmField val BLOCK_OBSIDIAN_LAND              = sound("block.obsidian.land")
	@JvmField val BLOCK_PUZZLE_LOGIC_CLICK         = sound("block.puzzle_logic.click")
	@JvmField val BLOCK_SPAWNER_EXPIRE             = sound("block.spawner.expire")
	
	@JvmField val ITEM_PUZZLE_MEDALLION_INSERT              = sound("item.puzzle_medallion.insert")
	@JvmField val ITEM_PUZZLE_MEDALLION_SPAWN               = sound("item.puzzle_medallion.spawn")
	@JvmField val ITEM_REVITALIZATION_SUBSTANCE_USE_FAIL    = sound("item.revitalization_substance.use.fail")
	@JvmField val ITEM_REVITALIZATION_SUBSTANCE_USE_SUCCESS = sound("item.revitalization_substance.use.success")
	@JvmField val ITEM_RING_OF_PRESERVATION_USE             = sound("item.ring_of_preservation.use")
	@JvmField val ITEM_SCALE_OF_FREEFALL_USE                = sound("item.scale_of_freefall.use")
	@JvmField val ITEM_TABLE_LINK_USE_FAIL                  = sound("item.table_link.use.fail")
	@JvmField val ITEM_TABLE_LINK_USE_SPECIAL               = sound("item.table_link.use.special")
	@JvmField val ITEM_TABLE_LINK_USE_SUCCESS               = sound("item.table_link.use.success")
	
	@JvmField val ENTITY_GENERIC_TELEPORT              = sound("entity.generic.teleport")
	@JvmField val ENTITY_IGNEOUS_ROCK_BURN             = sound("entity.igneous_rock.burn")
	@JvmField val ENTITY_PLAYER_DEATH_NO_SUBTITLES     = sound("entity.player.death.no_subtitles")
	@JvmField val ENTITY_PLAYER_TELEPORT               = sound("entity.player.teleport")
	@JvmField val ENTITY_REVITALIZATION_SUBSTANCE_HEAL = sound("entity.revitalization_substance.heal")
	@JvmField val ENTITY_SPATIAL_DASH_EXPIRE           = sound("entity.spatial_dash.expire")
	@JvmField val ENTITY_TOKEN_HOLDER_DROP             = sound("entity.token_holder.drop")
	
	@JvmField val MOB_ENDER_EYE_HIT_FAIL     = sound("mob.ender_eye.hit.fail")
	@JvmField val MOB_ENDERMAN_FIRST_KILL    = sound("mob.enderman.first_kill")
	@JvmField val MOB_ENDERMAN_TELEPORT_FAIL = sound("mob.enderman.teleport.fail")
	@JvmField val MOB_ENDERMAN_TELEPORT_OUT  = sound("mob.enderman.teleport.out")
	@JvmField val MOB_UNDREAD_DEATH          = sound("mob.undread.death")
	@JvmField val MOB_UNDREAD_HURT           = sound("mob.undread.hurt")
	@JvmField val MOB_UNDREAD_CURSE          = sound("mob.undread.curse")
	@JvmField val MOB_UNDREAD_FUSE           = sound("mob.undread.fuse")
	@JvmField val MOB_VILLAGER_TOTEM_DEATH   = sound("mob.villager.totem_death")
	@JvmField val MOB_VILLAGER_TOTEM_DYING   = sound("mob.villager.totem_dying")
	
	@SubscribeEvent
	fun onRegisterSounds(e: RegistryEvent.Register<SoundEvent>) {
		e.registerAllFields(this)
	}
	
	private fun sound(name: String): SoundEvent {
		return SoundEvent(Resource.Custom(name)) named name
	}
}
