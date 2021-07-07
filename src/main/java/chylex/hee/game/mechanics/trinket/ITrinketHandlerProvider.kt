package chylex.hee.game.mechanics.trinket

import net.minecraft.entity.player.PlayerEntity

/**
 * Describes a Trinket item which itself can handle [ITrinketHandler] requests. The interface must be applied to a class extending [Item][net.minecraft.item.Item].
 * If an **active** Trinket implementing this interface is placed in the Trinket slot, its [ITrinketHandler] will be used instead of the default one.
 */
interface ITrinketHandlerProvider : ITrinketItem {
	fun createTrinketHandler(player: PlayerEntity): ITrinketHandler
}
