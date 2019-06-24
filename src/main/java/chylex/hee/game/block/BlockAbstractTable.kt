package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.system.util.get
import chylex.hee.system.util.with
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.NonNullList
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

abstract class BlockAbstractTable(builder: BlockBuilder) : BlockSimple(builder){
	companion object{
		const val MIN_TIER = 1
		const val MAX_TIER = 3
		
		val TIER = Property.int("tier", MIN_TIER..MAX_TIER)
	}
	
	open val minAllowedTier = MIN_TIER
	
	init{
		defaultState = blockState.baseState.with(TIER, MIN_TIER)
	}
	
	final override fun createBlockState() = BlockStateContainer(this, TIER)
	
	final override fun getMetaFromState(state: IBlockState) = state[TIER]
	final override fun getStateFromMeta(meta: Int) = this.with(TIER, meta.coerceIn(MIN_TIER, MAX_TIER))
	
	override fun damageDropped(state: IBlockState) = state[TIER]
	
	override fun getSubBlocks(tab: CreativeTabs, items: NonNullList<ItemStack>){
		for(tier in minAllowedTier..MAX_TIER){
			items.add(ItemStack(this, 1, tier))
		}
	}
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		lines.add(I18n.format("tile.tooltip.hee.table_base.tier", stack.metadata))
	}
	
	override fun getRenderLayer() = CUTOUT
}
