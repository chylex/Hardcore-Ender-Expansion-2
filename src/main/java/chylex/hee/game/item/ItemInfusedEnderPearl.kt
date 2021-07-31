package chylex.hee.game.item

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.entity.projectile.EntityProjectileEnderPearl
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.system.random.nextFloat
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.EnderPearlItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.stats.Stats
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World
import net.minecraftforge.common.Tags

class ItemInfusedEnderPearl(properties: Properties) : EnderPearlItem(properties), IHeeItem, IInfusableItem {
	override val localization
		get() = LocalizationStrategy.None
	
	override val model
		get() = ItemModel.Copy(Items.ENDER_PEARL)
	
	override val tags
		get() = listOf(Tags.Items.ENDER_PEARLS)
	
	override fun onItemRightClick(world: World, player: PlayerEntity, hand: Hand): ActionResult<ItemStack> {
		val heldItem = player.getHeldItem(hand)
		
		if (!player.abilities.isCreativeMode) {
			heldItem.shrink(1)
		}
		
		if (!world.isRemote) {
			world.addEntity(EntityProjectileEnderPearl(player, InfusionTag.getList(heldItem)))
		}
		
		SoundEvents.ENTITY_ENDER_PEARL_THROW.playServer(world, player.posVec, SoundCategory.NEUTRAL, volume = 0.5F, pitch = 0.4F / random.nextFloat(0.8F, 1.2F))
		
		player.cooldownTracker.setCooldown(this, 20)
		player.addStat(Stats.ITEM_USED[this])
		
		return ActionResult(SUCCESS, heldItem)
	}
	
	override fun canApplyInfusion(infusion: Infusion): Boolean {
		return ItemAbstractInfusable.onCanApplyInfusion(this, infusion)
	}
	
	override fun getTranslationKey(): String {
		return Items.ENDER_PEARL.translationKey
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag) {
		super.addInformation(stack, world, lines, flags)
		ItemAbstractInfusable.onAddInformation(stack, lines)
	}
	
	@Sided(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean {
		return super.hasEffect(stack) || ItemAbstractInfusable.onHasEffect(stack)
	}
}
