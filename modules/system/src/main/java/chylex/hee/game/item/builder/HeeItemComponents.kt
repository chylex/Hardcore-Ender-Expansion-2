package chylex.hee.game.item.builder

import chylex.hee.game.item.components.IBeforeUseItemOnBlockComponent
import chylex.hee.game.item.components.IConsumeItemComponent
import chylex.hee.game.item.components.ICreativeTabComponent
import chylex.hee.game.item.components.IItemDurabilityComponent
import chylex.hee.game.item.components.IItemEntityComponent
import chylex.hee.game.item.components.IItemGlintComponent
import chylex.hee.game.item.components.IItemNameComponent
import chylex.hee.game.item.components.IReequipAnimationComponent
import chylex.hee.game.item.components.IRepairItemComponent
import chylex.hee.game.item.components.ITickInInventoryComponent
import chylex.hee.game.item.components.ITooltipComponent
import chylex.hee.game.item.components.IUseItemOnAirComponent
import chylex.hee.game.item.components.IUseItemOnBlockComponent
import chylex.hee.game.item.components.IUseItemOnEntityComponent
import net.minecraft.dispenser.IDispenseItemBehavior
import net.minecraft.item.Rarity

class HeeItemComponents {
	var name: IItemNameComponent? = null
	val tooltip = mutableListOf<ITooltipComponent>()
	
	var glint: IItemGlintComponent? = null
	var rarity: Rarity? = null
	var reequipAnimation: IReequipAnimationComponent? = null
	var creativeTab: ICreativeTabComponent? = null
	
	var useOnAir: IUseItemOnAirComponent? = null
	var beforeUseOnBlock: IBeforeUseItemOnBlockComponent? = null
	var useOnBlock: IUseItemOnBlockComponent? = null
	var useOnEntity: IUseItemOnEntityComponent? = null
	var consume: IConsumeItemComponent? = null
	
	val tickInInventory = mutableListOf<ITickInInventoryComponent>()
	var itemEntity: IItemEntityComponent? = null
	var furnaceBurnTime: Int? = null
	var durability: IItemDurabilityComponent? = null
	var repair: IRepairItemComponent? = null
	
	var dispenserBehavior: IDispenseItemBehavior? = null
	
	fun includeFrom(source: HeeItemComponents) {
		source.name?.let { this.name = it }
		this.tooltip.addAll(source.tooltip)
		
		source.glint?.let { this.glint = it }
		source.rarity?.let { this.rarity = it }
		source.reequipAnimation?.let { this.reequipAnimation = it }
		source.creativeTab?.let { this.creativeTab = it }
		
		source.useOnAir?.let { this.useOnAir = it }
		source.beforeUseOnBlock?.let { this.beforeUseOnBlock = it }
		source.useOnBlock?.let { this.useOnBlock = it }
		source.useOnEntity?.let { this.useOnEntity = it }
		source.consume?.let { this.consume = it }
		
		this.tickInInventory.addAll(source.tickInInventory)
		source.itemEntity?.let { this.itemEntity = it }
		source.furnaceBurnTime?.let { this.furnaceBurnTime = it }
		source.durability?.let { this.durability = it }
		source.repair?.let { this.repair = it }
		
		source.dispenserBehavior?.let { this.dispenserBehavior = it }
	}
}
