package chylex.hee.game.item

import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.projectile.EntityProjectileEnderPearl
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.world.playServer
import chylex.hee.system.facades.Stats
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.ItemEnderPearl
import chylex.hee.system.migration.Sounds
import chylex.hee.system.random.nextFloat
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World

class ItemInfusedEnderPearl(properties: Properties) : ItemEnderPearl(properties), IInfusableItem {
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: Hand): ActionResult<ItemStack> {
		val heldItem = player.getHeldItem(hand)
		
		if (!player.abilities.isCreativeMode) {
			heldItem.shrink(1)
		}
		
		if (!world.isRemote) {
			world.addEntity(EntityProjectileEnderPearl(player, InfusionTag.getList(heldItem)))
		}
		
		Sounds.ENTITY_ENDER_PEARL_THROW.playServer(world, player.posVec, SoundCategory.NEUTRAL, volume = 0.5F, pitch = 0.4F / random.nextFloat(0.8F, 1.2F))
		
		player.cooldownTracker.setCooldown(this, 20)
		player.addStat(Stats.useItem(this))
		
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
