package chylex.hee.game.item

import chylex.hee.game.Resource
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.ICreativeTabComponent
import chylex.hee.game.item.components.IItemGlintComponent
import chylex.hee.game.item.components.ITooltipComponent
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.properties.ItemTint
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Rarity
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.IBlockReader

object ItemBindingEssence : HeeItemBuilder() {
	init {
		includeFrom(ItemAbstractInfusable(object : IInfusableItem {
			override fun canApplyInfusion(target: Item, infusion: Infusion): Boolean {
				return true
			}
		}))
		
		model = ItemModel.Layers(
			"binding_essence_primary",
			"binding_essence_secondary",
			"binding_essence_tertiary",
			"binding_essence_quaternary"
		)
		
		tint = object : ItemTint() {
			@Sided(Side.CLIENT)
			override fun tint(stack: ItemStack, tintIndex: Int): Int {
				val list = InfusionTag.getList(stack).sortedBy(Infusion::ordinal)
				if (list.isEmpty()) {
					return WHITE
				}
				
				return when (tintIndex) {
					0    -> list[0].primaryColor.i
					1    -> (list.getOrNull(1)?.primaryColor ?: list[0].secondaryColor).i
					2    -> (list.getOrNull(2)?.primaryColor ?: list.getOrNull(1)?.secondaryColor ?: list[0].primaryColor).i
					3    -> (list.getOrNull(3)?.primaryColor ?: list.getOrNull(2)?.primaryColor ?: (if (list.size == 2) list[0].secondaryColor else list[0].primaryColor)).i
					else -> NO_TINT
				}
			}
		}
		
		maxStackSize = 16
		
		components.tooltip.add(object : ITooltipComponent {
			override fun add(lines: MutableList<ITextComponent>, stack: ItemStack, advanced: Boolean, world: IBlockReader?) {
				val list = InfusionTag.getList(stack)
				if (list.isEmpty) {
					return
				}
				
				val applicableTo = list
					.flatMap { it.targetItems.filter { item -> Resource.isCustom(item.registryName!!) } }
					.groupingBy { it }
					.eachCount()
					.entries
					.sortedWith(compareBy({ -it.value }, { Item.getIdFromItem(it.key) }))
				
				lines.add(StringTextComponent(""))
				lines.add(TranslationTextComponent("hee.infusions.applicable.title"))
				
				for ((item, count) in applicableTo) {
					lines.add(TranslationTextComponent("hee.infusions.applicable.item", item.getDisplayName(ItemStack(item)), count))
				}
			}
		})
		
		components.glint = IItemGlintComponent { false }
		components.rarity = Rarity.UNCOMMON
		
		components.creativeTab = ICreativeTabComponent { tab, item ->
			tab.add(ItemStack(item))
			
			for (infusion in Infusion.values()) {
				tab.add(ItemStack(item).also { InfusionTag.setList(it, InfusionList(infusion)) })
			}
		}
	}
}
