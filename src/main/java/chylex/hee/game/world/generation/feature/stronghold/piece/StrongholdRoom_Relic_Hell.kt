package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.util.math.Pos
import net.minecraft.item.ItemStack

class StrongholdRoom_Relic_Hell(file: String, relicItem: ItemStack) : StrongholdRoom_Relic(file, relicItem) {
	override val lootChestPos = Pos(centerX, 2, 1)
}
