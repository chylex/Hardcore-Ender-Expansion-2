package chylex.hee.game.item
import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.projectile.EntityProjectileExperienceBottle
import chylex.hee.game.inventory.heeTag
import chylex.hee.game.inventory.heeTagOrNull
import chylex.hee.game.inventory.size
import chylex.hee.game.world.playServer
import chylex.hee.system.facades.Stats
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.ItemExpBottle
import chylex.hee.system.migration.Items
import chylex.hee.system.migration.Sounds
import chylex.hee.system.random.nextFloat
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Rarity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.NonNullList
import net.minecraft.util.SoundCategory
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import kotlin.math.min

class ItemExperienceBottleCustom(builder: Properties) : ItemExpBottle(builder){
	companion object{
		private const val EXPERIENCE_TAG = "Experience"
		
		const val MAX_EXPERIENCE = 25
	}
	
	private fun setExperienceAmount(stack: ItemStack, amount: Int){
		if (amount == 0){
			stack.size = 0
		}
		else{
			stack.heeTag.putInt(EXPERIENCE_TAG, amount)
		}
	}
	
	fun getExperienceAmountPerItem(stack: ItemStack): Int{
		return stack.heeTagOrNull?.getInt(EXPERIENCE_TAG) ?: 0
	}
	
	fun isFullOfExperience(stack: ItemStack): Boolean{
		return getExperienceAmountPerItem(stack) >= MAX_EXPERIENCE
	}
	
	fun createBottles(experience: Int): List<ItemStack>{
		val stacks = mutableListOf<ItemStack>()
		var remaining = experience
		
		while(remaining > 0){
			val xp = min(remaining, MAX_EXPERIENCE)
			
			remaining -= xp
			stacks.add(ItemStack(this).also { setExperienceAmount(it, xp) })
		}
		
		return stacks
	}
	
	fun mergeBottles(from: ItemStack, into: ItemStack): Boolean{
		if (from.size != 1 || into.size != 1){
			return false
		}
		
		val xpFrom = getExperienceAmountPerItem(from)
		val xpInto = getExperienceAmountPerItem(into)
		val mergedAmount = min(xpFrom, MAX_EXPERIENCE - xpInto)
		
		if (mergedAmount == 0){
			return false
		}
		
		setExperienceAmount(from, xpFrom - mergedAmount)
		setExperienceAmount(into, xpInto + mergedAmount)
		return true
	}
	
	override fun getTranslationKey(): String{
		return Items.EXPERIENCE_BOTTLE.translationKey
	}
	
	override fun getRarity(stack: ItemStack): Rarity{
		return Rarity.UNCOMMON
	}
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: Hand): ActionResult<ItemStack>{
		val heldItem = player.getHeldItem(hand)
		val originalItem = heldItem.copy()
		
		if (!player.abilities.isCreativeMode){
			heldItem.shrink(1)
		}
		
		if (!world.isRemote){
			world.addEntity(EntityProjectileExperienceBottle(player, originalItem))
			Sounds.ENTITY_EXPERIENCE_BOTTLE_THROW.playServer(world, player.posVec, SoundCategory.NEUTRAL, volume = 0.5F, pitch = 0.4F / random.nextFloat(0.8F, 1.2F))
		}
		
		player.addStat(Stats.useItem(this))
		
		return ActionResult(SUCCESS, heldItem)
	}
	
	override fun fillItemGroup(tab: ItemGroup, items: NonNullList<ItemStack>){}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		lines.add(TranslationTextComponent("item.hee.experience_bottle.tooltip", getExperienceAmountPerItem(stack)))
	}
}
