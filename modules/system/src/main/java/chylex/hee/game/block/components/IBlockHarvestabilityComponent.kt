package chylex.hee.game.block.components

import chylex.hee.util.forge.EventResult
import net.minecraft.entity.player.PlayerEntity

fun interface IBlockHarvestabilityComponent {
	fun canHarvest(player: PlayerEntity): EventResult
}
