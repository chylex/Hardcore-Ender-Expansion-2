package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.NonNullList
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.IBlockReader
import net.minecraft.world.storage.loot.LootContext

abstract class BlockAbstractTable(builder: BlockBuilder) : BlockSimple(builder){
	companion object{
		const val MIN_TIER = 1
		const val MAX_TIER = 3
		
		val TIER = Property.int("tier", MIN_TIER..MAX_TIER)
	}
	
	open val minAllowedTier = MIN_TIER
	
	init{
		defaultState = stateContainer.baseState.with(TIER, MIN_TIER)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(TIER)
	}
	
	override fun isSolid(state: BlockState): Boolean{
		return true
	}
	
	override fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack>{
		return super.getDrops(state, context) // UPDATE
	}
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState{
		return this.with(TIER, context.item.damage)
	}
	
	override fun fillItemGroup(group: ItemGroup, items: NonNullList<ItemStack>){
		for(tier in minAllowedTier..MAX_TIER){
			items.add(ItemStack(this, 1).apply { damage = tier - MIN_TIER })
		}
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: IBlockReader?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		lines.add(TextComponentTranslation("tile.tooltip.hee.table_base.tier", stack.damage + 1))
	}
	
	override fun getRenderLayer() = CUTOUT
}
