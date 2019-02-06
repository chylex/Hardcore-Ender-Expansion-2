package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.system.util.Pos
import net.minecraft.item.ItemStack

class StrongholdRoom_Relic_Hell(file: String, relicItem: ItemStack) : StrongholdRoom_Relic(file, relicItem){
	override val lootChestPos = Pos(centerX, 2, 1)
}
