package chylex.hee.game.mechanics.scorching
import net.minecraft.block.BlockState
import net.minecraft.item.IItemTier
import net.minecraftforge.event.entity.player.CriticalHitEvent

interface IScorchingItem{
	val material: IItemTier
	fun canMine(state: BlockState): Boolean
	fun onHit(e: CriticalHitEvent)
}
