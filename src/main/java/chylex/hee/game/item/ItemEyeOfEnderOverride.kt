package chylex.hee.game.item
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.game.world.feature.stronghold.StrongholdGenerator
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemEnderEye
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumActionResult.PASS
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ItemEyeOfEnderOverride : ItemEnderEye(){
	init{
		translationKey = "eyeOfEnder"
	}
	
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		return PASS
	}
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack>{
		val heldItem = player.getHeldItem(hand)
		
		if (!player.capabilities.isCreativeMode){
			heldItem.shrink(1)
		}
		
		if (!world.isRemote){
			val strongholdPos = StrongholdGenerator.findNearest(world, player.posX, player.posZ)
			
			EntityProjectileEyeOfEnder(player, strongholdPos).apply {
				world.spawnEntity(this)
				
				if (strongholdPos != null){
					playSound(SoundEvents.ENTITY_ENDEREYE_LAUNCH, 1F, 1.2F) // louder + noticeable wind sound if a Stronghold is found
				}
			}
			
			SoundEvents.ENTITY_ENDEREYE_LAUNCH.playServer(world, player.posVec, SoundCategory.NEUTRAL, volume = 0.5F, pitch = 0.4F / itemRand.nextFloat(0.8F, 1.2F))
		}
		
		player.addStat(StatList.getObjectUseStats(this)!!)
		
		return ActionResult(SUCCESS, heldItem)
	}
}
