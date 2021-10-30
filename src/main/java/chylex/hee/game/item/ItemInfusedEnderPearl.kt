package chylex.hee.game.item

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.entity.projectile.EntityProjectileEnderPearl
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.IItemNameComponent
import chylex.hee.game.item.components.ShootProjectileComponent
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.util.random.nextFloat
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.world.World
import net.minecraftforge.common.Tags

object ItemInfusedEnderPearl : HeeItemBuilder() {
	init {
		includeFrom(ItemAbstractInfusable())
		
		localization = LocalizationStrategy.None
		
		model = ItemModel.Copy(Items.ENDER_PEARL)
		
		tags.add(Tags.Items.ENDER_PEARLS)
		
		maxStackSize = 16
		
		components.name = IItemNameComponent.of(Items.ENDER_PEARL)
		
		components.useOnAir = object : ShootProjectileComponent() {
			override fun use(world: World, player: PlayerEntity, hand: Hand, heldItem: ItemStack): ActionResult<ItemStack> {
				SoundEvents.ENTITY_ENDER_PEARL_THROW.playServer(world, player.posVec, SoundCategory.NEUTRAL, volume = 0.5F, pitch = 0.4F / world.rand.nextFloat(0.8F, 1.2F))
				player.cooldownTracker.setCooldown(heldItem.item, 20)
				return super.use(world, player, hand, heldItem)
			}
			
			override fun createEntity(world: World, player: PlayerEntity, hand: Hand, heldItem: ItemStack): Entity {
				return EntityProjectileEnderPearl(player, InfusionTag.getList(heldItem))
			}
		}
	}
}
