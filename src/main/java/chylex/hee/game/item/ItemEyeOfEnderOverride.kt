package chylex.hee.game.item
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.game.world.feature.stronghold.StrongholdGenerator
import chylex.hee.game.world.util.PosXZ
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.ItemEnderEye
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.facades.Stats
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld

class ItemEyeOfEnderOverride(properties: Properties) : ItemEnderEye(properties){
	override fun onItemUse(context: ItemUseContext): ActionResultType{
		return PASS
	}
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: Hand): ActionResult<ItemStack>{
		val heldItem = player.getHeldItem(hand)
		
		if (!player.abilities.isCreativeMode){
			heldItem.shrink(1)
		}
		
		if (!world.isRemote){
			val strongholdPos = StrongholdGenerator.findNearest(world as ServerWorld, PosXZ(player.position))
			
			EntityProjectileEyeOfEnder(player, strongholdPos).apply {
				world.addEntity(this)
				
				if (strongholdPos != null){
					playSound(Sounds.ENTITY_ENDER_EYE_LAUNCH, 1F, 1.2F) // louder + noticeable wind sound if a Stronghold is found
				}
			}
			
			Sounds.ENTITY_ENDER_EYE_LAUNCH.playServer(world, player.posVec, SoundCategory.NEUTRAL, volume = 0.5F, pitch = 0.4F / random.nextFloat(0.8F, 1.2F))
		}
		
		player.addStat(Stats.useItem(this))
		
		return ActionResult(SUCCESS, heldItem)
	}
}
