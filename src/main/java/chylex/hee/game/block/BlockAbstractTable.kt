package chylex.hee.game.block
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.NonNullList
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

abstract class BlockAbstractTable(builder: BlockSimple.Builder) : BlockSimple(builder){
	companion object{
		const val MIN_TIER = 1
		const val MAX_TIER = 3
		
		val TIER = PropertyInteger.create("tier", MIN_TIER, MAX_TIER)!!
	}
	
	open val minAllowedTier = MIN_TIER
	
	init{
		defaultState = blockState.baseState.withProperty(TIER, MIN_TIER)
	}
	
	final override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, TIER)
	
	final override fun getMetaFromState(state: IBlockState): Int = state.getValue(TIER)
	final override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(TIER, meta.coerceIn(MIN_TIER, MAX_TIER))
	
	override fun damageDropped(state: IBlockState): Int{
		return state.getValue(TIER)
	}
	
	override fun getSubBlocks(tab: CreativeTabs, items: NonNullList<ItemStack>){
		for(tier in minAllowedTier..MAX_TIER){
			items.add(ItemStack(this, 1, tier))
		}
	}
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		lines.add(I18n.format("tile.tooltip.hee.table_base.tier", stack.metadata))
	}
	
	override fun getRenderLayer(): BlockRenderLayer = CUTOUT
}
