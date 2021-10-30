package chylex.hee.game.item

import chylex.hee.game.item.builder.HeeItemComponents
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.block.Block
import net.minecraft.block.DispenserBlock
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.item.Rarity
import net.minecraft.item.UseAction
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.NonNullList
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World

abstract class HeeBlockItemWithComponents(block: Block, properties: Properties, components: HeeItemComponents) : HeeBlockItem(block, properties) {
	private val name = components.name
	private val tooltip = components.tooltip.toTypedArray()
	
	private val glint = components.glint
	private val rarity = components.rarity
	private val reequipAnimation = components.reequipAnimation
	private val creativeTab = components.creativeTab
	
	private val useOnAir = components.useOnAir
	private val beforeUseOnBlock = components.beforeUseOnBlock
	private val useOnBlock = components.useOnBlock
	private val useOnEntity = components.useOnEntity
	private val consume = components.consume
	
	private val tickInInventory = components.tickInInventory.toTypedArray()
	private val itemEntity = components.itemEntity
	private val burnTime = components.furnaceBurnTime
	private val durability = components.durability
	private val repair = components.repair
	
	init {
		components.dispenserBehavior?.let { DispenserBlock.registerDispenseBehavior(this, it) }
	}
	
	override fun getDefaultTranslationKey(): String {
		return name?.defaultTranslationKey ?: super.getDefaultTranslationKey()
	}
	
	override fun getTranslationKey(stack: ItemStack): String {
		return name?.getTranslationKey(stack) ?: super.getTranslationKey(stack)
	}
	
	override fun getDisplayName(stack: ItemStack): ITextComponent {
		return name?.getDisplayName(stack) ?: super.getDisplayName(stack)
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag) {
		super.addInformation(stack, world, lines, flags)
		
		for (tooltipComponent in tooltip) {
			tooltipComponent.add(lines, stack, flags.isAdvanced, world)
		}
	}
	
	override fun hasEffect(stack: ItemStack): Boolean {
		return glint?.hasGlint(stack) ?: super.hasEffect(stack)
	}
	
	override fun getRarity(stack: ItemStack): Rarity {
		return rarity ?: super.getRarity(stack)
	}
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean {
		return reequipAnimation?.shouldAnimate(oldStack, newStack, slotChanged) ?: super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
	
	override fun fillItemGroup(group: ItemGroup, items: NonNullList<ItemStack>) {
		if (creativeTab == null) {
			super.fillItemGroup(group, items)
		}
		else if (isInGroup(group)) {
			creativeTab.addItems(items, this)
		}
	}
	
	override fun onItemRightClick(world: World, player: PlayerEntity, hand: Hand): ActionResult<ItemStack> {
		return useOnAir?.use(world, player, hand, player.getHeldItem(hand)) ?: super.onItemRightClick(world, player, hand)
	}
	
	override fun onItemUseFirst(stack: ItemStack, context: ItemUseContext): ActionResultType {
		return beforeUseOnBlock?.beforeUse(context.world, context.pos, context, stack) ?: super.onItemUseFirst(stack, context)
	}
	
	override fun onItemUse(context: ItemUseContext): ActionResultType {
		return useOnBlock?.use(context.world, context.pos, context) ?: super.onItemUse(context)
	}
	
	override fun itemInteractionForEntity(stack: ItemStack, player: PlayerEntity, target: LivingEntity, hand: Hand): ActionResultType {
		return useOnEntity?.use(target.world, target, player, hand, stack) ?: super.itemInteractionForEntity(stack, player, target, hand)
	}
	
	override fun getUseAction(stack: ItemStack): UseAction {
		return consume?.action ?: super.getUseAction(stack)
	}
	
	override fun getUseDuration(stack: ItemStack): Int {
		return consume?.getDuration(stack) ?: super.getUseDuration(stack)
	}
	
	override fun onUsingTick(stack: ItemStack, entity: LivingEntity, tick: Int) {
		consume?.tick(stack, entity, tick)
	}
	
	override fun onItemUseFinish(stack: ItemStack, world: World, entity: LivingEntity): ItemStack {
		return consume?.finish(stack, entity) ?: super.onItemUseFinish(stack, world, entity)
	}
	
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, isSelected: Boolean) {
		for (component in tickInInventory) {
			component.tick(world, entity, stack, slot, isSelected)
		}
	}
	
	override fun hasCustomEntity(stack: ItemStack): Boolean {
		return itemEntity != null && itemEntity.hasEntity(stack)
	}
	
	override fun createEntity(world: World, replacee: Entity, stack: ItemStack): Entity? {
		return itemEntity?.createEntity(world, stack, replacee)
	}
	
	override fun getBurnTime(stack: ItemStack): Int {
		return burnTime ?: super.getBurnTime(stack)
	}
	
	override fun showDurabilityBar(stack: ItemStack): Boolean {
		return durability?.showBar(stack) ?: super.showDurabilityBar(stack)
	}
	
	override fun getDurabilityForDisplay(stack: ItemStack): Double {
		return durability?.getDisplayDurability(stack) ?: super.getDurabilityForDisplay(stack)
	}
	
	override fun getRGBDurabilityForDisplay(stack: ItemStack): Int {
		return durability?.getDisplayDurabilityRGB(stack) ?: super.getRGBDurabilityForDisplay(stack)
	}
	
	override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean {
		return repair != null && repair.isRepairable(toRepair, repairWith)
	}
}
