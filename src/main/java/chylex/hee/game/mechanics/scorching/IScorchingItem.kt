package chylex.hee.game.mechanics.scorching
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item.ToolMaterial
import net.minecraftforge.event.entity.player.CriticalHitEvent

interface IScorchingItem{
	val material: ToolMaterial
	fun canMine(state: IBlockState): Boolean
	fun onHit(e: CriticalHitEvent)
}
