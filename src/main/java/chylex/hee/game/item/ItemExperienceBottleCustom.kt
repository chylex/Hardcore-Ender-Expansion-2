package chylex.hee.game.item

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.dispenser.DispenseExperienceBottle
import chylex.hee.game.entity.projectile.EntityProjectileExperienceBottle
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.ICreativeTabComponent
import chylex.hee.game.item.components.IItemNameComponent
import chylex.hee.game.item.components.ITooltipComponent
import chylex.hee.game.item.components.ShootProjectileComponent
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.util.size
import chylex.hee.init.ModItems
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.random.nextFloat
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.Rarity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import kotlin.math.min

object ItemExperienceBottleCustom : HeeItemBuilder() {
	private const val EXPERIENCE_TAG = "Experience"
	
	private const val LANG_TOOLTIP_EXPERIENCE = "item.hee.experience_bottle.tooltip"
	
	const val MAX_EXPERIENCE = 25
	
	init {
		localization = LocalizationStrategy.None
		localizationExtra[LANG_TOOLTIP_EXPERIENCE] = "§a%s §2experience"
		
		model = ItemModel.Copy(Items.EXPERIENCE_BOTTLE)
		
		components.name = IItemNameComponent.of(Items.EXPERIENCE_BOTTLE)
		components.tooltip.add(ITooltipComponent { _, stack, _, _ -> TranslationTextComponent(LANG_TOOLTIP_EXPERIENCE, getExperienceAmountPerItem(stack)) })
		
		components.rarity = Rarity.UNCOMMON
		components.creativeTab = ICreativeTabComponent { _, _ -> }
		
		components.useOnAir = object : ShootProjectileComponent() {
			override fun use(world: World, player: PlayerEntity, hand: Hand, heldItem: ItemStack): ActionResult<ItemStack> {
				SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW.playServer(world, player.posVec, SoundCategory.NEUTRAL, volume = 0.5F, pitch = 0.4F / world.rand.nextFloat(0.8F, 1.2F))
				return super.use(world, player, hand, heldItem)
			}
			
			override fun createEntity(world: World, player: PlayerEntity, hand: Hand, heldItem: ItemStack): Entity {
				return EntityProjectileExperienceBottle(player, heldItem.copy())
			}
		}
		
		components.dispenserBehavior = DispenseExperienceBottle
	}
	
	private fun setExperienceAmount(stack: ItemStack, amount: Int) {
		if (amount == 0) {
			stack.size = 0
		}
		else {
			stack.heeTag.putInt(EXPERIENCE_TAG, amount)
		}
	}
	
	fun getExperienceAmountPerItem(stack: ItemStack): Int {
		return stack.heeTagOrNull?.getInt(EXPERIENCE_TAG) ?: 0
	}
	
	fun isFullOfExperience(stack: ItemStack): Boolean {
		return getExperienceAmountPerItem(stack) >= MAX_EXPERIENCE
	}
	
	fun createBottles(experience: Int): List<ItemStack> {
		val stacks = mutableListOf<ItemStack>()
		var remaining = experience
		
		while (remaining > 0) {
			val xp = min(remaining, MAX_EXPERIENCE)
			
			remaining -= xp
			stacks.add(ItemStack(ModItems.EXPERIENCE_BOTTLE).also { setExperienceAmount(it, xp) })
		}
		
		return stacks
	}
	
	fun mergeBottles(from: ItemStack, into: ItemStack): Boolean {
		if (from.size != 1 || into.size != 1) {
			return false
		}
		
		val xpFrom = getExperienceAmountPerItem(from)
		val xpInto = getExperienceAmountPerItem(into)
		val mergedAmount = min(xpFrom, MAX_EXPERIENCE - xpInto)
		
		if (mergedAmount == 0) {
			return false
		}
		
		setExperienceAmount(from, xpFrom - mergedAmount)
		setExperienceAmount(into, xpInto + mergedAmount)
		return true
	}
}
