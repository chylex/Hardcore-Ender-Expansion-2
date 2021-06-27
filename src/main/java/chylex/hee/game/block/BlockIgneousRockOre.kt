package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.inventory.doDamage
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageDealer.Companion.TITLE_IN_FIRE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.FIRE_TYPE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.ItemTool
import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.fluid.FluidState
import net.minecraft.item.ItemStack
import net.minecraft.particles.ParticleTypes.LAVA
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.BlockEvent.BreakEvent
import java.util.Random

class BlockIgneousRockOre(builder: BlockBuilder) : BlockSimple(builder) {
	private companion object {
		private val DAMAGE_MINING = Damage(FIRE_TYPE(15), PEACEFUL_EXCLUSION)
		
		private val PARTICLE_TICK = ParticleSpawnerVanilla(
			type = LAVA,
			pos = InBox(0.825F)
		)
		
		private fun getToolHarvestLevel(stack: ItemStack): Int? {
			return (stack.item as? ItemTool)?.tier?.harvestLevel
		}
	}
	
	init {
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun getExpDrop(state: BlockState, world: IWorldReader, pos: BlockPos, fortune: Int, silktouch: Int): Int {
		return 9
	}
	
	// Custom harvesting handling
	
	override fun harvestBlock(world: World, player: EntityPlayer, pos: BlockPos, state: BlockState, tile: TileEntity?, stack: ItemStack) {
		val heldItem = player.getHeldItem(MAIN_HAND)
		val tierDifference = getToolHarvestLevel(heldItem)?.let { super.getHarvestLevel(state) - it } ?: return
		
		if (tierDifference > 0) {
			heldItem.doDamage(tierDifference, player, MAIN_HAND)
		}
		else {
			super.harvestBlock(world, player, pos, state, tile, stack)
		}
	}
	
	override fun removedByPlayer(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, willHarvest: Boolean, fluid: FluidState): Boolean {
		val heldItem = player.getHeldItem(MAIN_HAND)
		
		if ((getToolHarvestLevel(heldItem) ?: 0) < super.getHarvestLevel(state) || EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItem(MAIN_HAND)) == 0) {
			DAMAGE_MINING.dealTo(2F, player, TITLE_IN_FIRE)
		}
		
		return super.removedByPlayer(state, world, pos, player, willHarvest, fluid)
	}
	
	@SubscribeEvent
	fun onBlockBreak(e: BreakEvent) {
		if (e.state.block === this) {
			val harvestLevel = e.player?.getHeldItem(MAIN_HAND)?.let(::getToolHarvestLevel) ?: 0
			
			if (harvestLevel < super.getHarvestLevel(e.state)) {
				e.expToDrop = 0
			}
		}
	}
	
	override fun getHarvestLevel(state: BlockState) = 0 // use super.getHarvestLevel for the real value
	override fun canHarvestBlock(state: BlockState, world: IBlockReader, pos: BlockPos, player: EntityPlayer): Boolean = true
	
	// Client side
	
	@Sided(Side.CLIENT)
	override fun animateTick(state: BlockState, world: World, pos: BlockPos, rand: Random) {
		if (rand.nextInt(4) != 0) {
			PARTICLE_TICK.spawn(Point(pos, 1), rand)
		}
	}
}
