package chylex.hee.game.item
import chylex.hee.game.entity.projectile.EntityProjectileEnderPearl
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemEnderPearl
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemInfusedEnderPearl : ItemEnderPearl(), IInfusableItem{
	init{
		creativeTab = null
	}
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack>{
		val heldItem = player.getHeldItem(hand)
		
		if (!player.capabilities.isCreativeMode){
			heldItem.shrink(1)
		}
		
		if (!world.isRemote){
			world.spawnEntity(EntityProjectileEnderPearl(player, InfusionTag.getList(heldItem)))
		}
		
		SoundEvents.ENTITY_ENDERPEARL_THROW.playServer(world, player.posVec, SoundCategory.NEUTRAL, volume = 0.5F, pitch = 0.4F / itemRand.nextFloat(0.8F, 1.2F))
		
		player.cooldownTracker.setCooldown(this, 20)
		player.addStat(StatList.getObjectUseStats(this)!!)
		
		return ActionResult(SUCCESS, heldItem)
	}
	
	override fun canApplyInfusion(infusion: Infusion): Boolean{
		return ItemAbstractInfusable.onCanApplyInfusion(this, infusion)
	}
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		ItemAbstractInfusable.onAddInformation(stack, lines)
	}
	
	@SideOnly(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean{
		return super.hasEffect(stack) || ItemAbstractInfusable.onHasEffect(stack)
	}
}
