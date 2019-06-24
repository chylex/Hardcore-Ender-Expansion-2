package chylex.hee.game.mechanics.scorching
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item.ToolMaterial

interface IScorchingItem{
	val material: ToolMaterial
	fun canMine(state: IBlockState): Boolean
}
