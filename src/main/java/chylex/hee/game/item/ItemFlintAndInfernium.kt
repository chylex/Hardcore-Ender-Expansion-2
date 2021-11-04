package chylex.hee.game.item

import chylex.hee.HEE
import chylex.hee.game.block.BlockInfusedTNT
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.item.util.doDamage
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.game.world.util.FLAG_NONE
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.removeBlock
import chylex.hee.game.world.util.setBlock
import chylex.hee.init.ModBlocks
import chylex.hee.system.MinecraftForgeEventBus
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.random.nextFloat
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.monster.CreeperEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.event.world.ExplosionEvent

class ItemFlintAndInfernium(properties: Properties) : HeeItem(properties) {
	@SubscribeAllEvents(modid = HEE.ID)
	companion object {
		private const val CREEPER_INFERNIUM_TAG = "Infernium"
		
		const val EXPLOSION_MULTIPLIER = 1.5F
		
		@SubscribeEvent(EventPriority.HIGHEST)
		fun onExplosionStart(e: ExplosionEvent.Start) {
			val explosion = e.explosion
			val creeper = explosion.explosivePlacedBy as? CreeperEntity
			
			if (creeper?.heeTagOrNull?.getBoolean(CREEPER_INFERNIUM_TAG) == true) {
				explosion.size *= EXPLOSION_MULTIPLIER
			}
		}
	}
	
	init {
		MinecraftForgeEventBus.register(this)
	}
	
	fun igniteTNT(world: World, pos: BlockPos, player: PlayerEntity?, ignoreTrap: Boolean) {
		if (pos.getBlock(world) === Blocks.TNT) {
			pos.setBlock(world, ModBlocks.INFUSED_TNT, FLAG_NONE)
		}
		
		ModBlocks.INFUSED_TNT.igniteTNT(world, pos, pos.getState(world).with(BlockInfusedTNT.INFERNIUM, true), player, ignoreTrap)
		pos.removeBlock(world)
	}
	
	override fun onItemUse(context: ItemUseContext): ActionResultType {
		val player = context.player ?: return FAIL
		val world = context.world
		val pos = context.pos
		
		val heldItem = player.getHeldItem(context.hand)
		
		if (!BlockEditor.canEdit(pos, player, heldItem)) {
			return FAIL
		}
		
		if (!world.isRemote) {
			val block = pos.getBlock(world)
			
			if (block === Blocks.TNT || block === ModBlocks.INFUSED_TNT) {
				igniteTNT(world, pos, player, ignoreTrap = false)
			}
			else {
				BlockEditor.place(ModBlocks.ETERNAL_FIRE, player, heldItem, context) ?: return FAIL
			}
			
			SoundEvents.ITEM_FLINTANDSTEEL_USE.playServer(world, pos, SoundCategory.BLOCKS, volume = 1.1F, pitch = world.rand.nextFloat(0.4F, 0.5F))
			heldItem.doDamage(1, player, context.hand)
		}
		
		return SUCCESS
	}
	
	override fun itemInteractionForEntity(stack: ItemStack, player: PlayerEntity, target: LivingEntity, hand: Hand): ActionResultType {
		if (target is CreeperEntity) {
			SoundEvents.ITEM_FLINTANDSTEEL_USE.playServer(target.world, target.posVec, target.soundCategory, volume = 1.1F, pitch = target.rng.nextFloat(0.4F, 0.5F))
			player.swingArm(hand)
			player.getHeldItem(hand).doDamage(1, player, hand)
			
			target.heeTag.putBoolean(CREEPER_INFERNIUM_TAG, true)
			target.ignite()
			return SUCCESS
		}
		
		return PASS
	}
}
