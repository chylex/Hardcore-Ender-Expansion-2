package chylex.hee.game.item

import chylex.hee.game.block.util.DISPENSER_FACING
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.PlayerUseItemOnBlockComponent
import chylex.hee.game.item.util.size
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import net.minecraft.dispenser.IBlockSource
import net.minecraft.dispenser.OptionalDispenseBehavior
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BoneMealItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.util.FakePlayerFactory
import java.util.Random

object ItemCompost : HeeItemBuilder() {
	private const val BONE_MEAL_EQUIVALENT = 2
	
	init {
		components.useOnBlock = object : PlayerUseItemOnBlockComponent() {
			override fun use(world: World, pos: BlockPos, player: PlayerEntity, heldItem: ItemStack, context: ItemUseContext): ActionResultType {
				if (world.isRemote) {
					return SUCCESS
				}
				
				if (applyCompost(world, pos, heldItem, player)) {
					return SUCCESS
				}
				
				return PASS
			}
		}
		
		components.dispenserBehavior = object : OptionalDispenseBehavior() {
			override fun dispenseStack(source: IBlockSource, stack: ItemStack): ItemStack {
				val world = source.world
				val pos = source.blockPos.offset(source.blockState[DISPENSER_FACING])
				
				isSuccessful = applyCompost(world, pos, stack)
				return stack
			}
		}
	}
	
	val FX_USE = object : FxBlockHandler() {
		override fun handle(pos: BlockPos, world: World, rand: Random) {
			BoneMealItem.spawnBonemealParticles(world, pos, 25)
		}
	}
	
	private fun applyCompost(world: World, pos: BlockPos, stack: ItemStack, player: PlayerEntity? = null): Boolean {
		if (world !is ServerWorld) {
			return false
		}
		
		val simulatedItem = ItemStack(ModItems.COMPOST, BONE_MEAL_EQUIVALENT)
		
		repeat(BONE_MEAL_EQUIVALENT) {
			if (player == null) {
				BoneMealItem.applyBonemeal(simulatedItem, world, pos, FakePlayerFactory.getMinecraft(world))
			}
			else {
				BoneMealItem.applyBonemeal(simulatedItem, world, pos, player)
			}
		}
		
		if (simulatedItem.size == BONE_MEAL_EQUIVALENT) {
			return false
		}
		
		stack.shrink(1)
		PacketClientFX(FX_USE, FxBlockData(pos)).sendToAllAround(world, pos, 64.0)
		return true
	}
}
