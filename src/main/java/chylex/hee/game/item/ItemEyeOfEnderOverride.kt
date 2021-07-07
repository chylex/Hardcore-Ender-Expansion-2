package chylex.hee.game.item

import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.world.generation.feature.stronghold.StrongholdGenerator
import chylex.hee.system.random.nextFloat
import chylex.hee.util.math.PosXZ
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.EnderEyeItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.stats.Stats
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld

class ItemEyeOfEnderOverride(properties: Properties) : EnderEyeItem(properties) {
	override fun onItemUse(context: ItemUseContext): ActionResultType {
		return PASS
	}
	
	override fun onItemRightClick(world: World, player: PlayerEntity, hand: Hand): ActionResult<ItemStack> {
		val heldItem = player.getHeldItem(hand)
		
		if (!player.abilities.isCreativeMode) {
			heldItem.shrink(1)
		}
		
		if (!world.isRemote) {
			val strongholdPos = StrongholdGenerator.findNearest(world as ServerWorld, PosXZ(player.position))
			
			EntityProjectileEyeOfEnder(player, strongholdPos).apply {
				world.addEntity(this)
				
				if (strongholdPos != null) {
					playSound(SoundEvents.ENTITY_ENDER_EYE_LAUNCH, 1F, 1.2F) // louder + noticeable wind sound if a Stronghold is found
				}
			}
			
			SoundEvents.ENTITY_ENDER_EYE_LAUNCH.playServer(world, player.posVec, SoundCategory.NEUTRAL, volume = 0.5F, pitch = 0.4F / random.nextFloat(0.8F, 1.2F))
		}
		
		player.addStat(Stats.ITEM_USED[this])
		
		return ActionResult(SUCCESS, heldItem)
	}
}
