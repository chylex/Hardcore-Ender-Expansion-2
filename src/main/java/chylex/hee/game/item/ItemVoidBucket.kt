package chylex.hee.game.item

import chylex.hee.game.Resource
import chylex.hee.game.item.properties.CustomToolMaterial.VOID_BUCKET
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.properties.ItemTint
import chylex.hee.game.item.util.ItemProperty
import chylex.hee.game.item.util.cleanupNBT
import chylex.hee.game.item.util.doDamage
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.game.world.util.allInCenteredBox
import chylex.hee.game.world.util.getFluidState
import chylex.hee.game.world.util.setAir
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.nbt.hasKey
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemStack
import net.minecraft.stats.Stats
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceContext.FluidMode
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.world.World
import java.util.Collections

class ItemVoidBucket(properties: Properties) : ItemAbstractVoidTool(properties, VOID_BUCKET) {
	companion object {
		private const val COOLDOWN_TAG = "Cooldown"
		private const val COLOR_TAG = "Color"
		
		private const val COOLDOWN_TICKS = 13
		
		val VOID_BUCKET_COOLDOWN_PROPERTY = ItemProperty(Resource.Custom("void_bucket_cooldown")) { stack ->
			(stack.heeTagOrNull?.getByte(COOLDOWN_TAG) ?: 0) / COOLDOWN_TICKS.toFloat()
		}
		
		private fun getFluidColor(state: FluidState) = when {
			state.isEmpty                           -> 0
			state.fluid.isEquivalentTo(Fluids.LAVA) -> RGB(205, 90, 17).i
			else                                    -> state.fluid.attributes.color
		}
		
		private fun isModifiableFluid(world: World, pos: BlockPos, player: PlayerEntity, stack: ItemStack): Boolean {
			return !pos.getFluidState(world).isEmpty && world.isBlockModifiable(player, pos) && BlockEditor.canEdit(pos, player, stack)
		}
	}
	
	override val model
		get() = ItemModel.WithOverrides(
			ItemModel.Simple,
			Resource.Custom("void_bucket_cooldown") to mapOf(
				0.01F to ItemModel.Suffixed("_fluid_level_1", ItemModel.Layers("void_bucket", "void_bucket_fluid_level_1")),
				0.30F to ItemModel.Suffixed("_fluid_level_2", ItemModel.Layers("void_bucket", "void_bucket_fluid_level_2")),
				0.50F to ItemModel.Suffixed("_fluid_level_3", ItemModel.Layers("void_bucket", "void_bucket_fluid_level_3")),
				0.70F to ItemModel.Suffixed("_fluid_level_4", ItemModel.Layers("void_bucket", "void_bucket_fluid_level_4")),
			)
		)
	
	override fun getDestroySpeed(stack: ItemStack, state: BlockState): Float {
		return 1F
	}
	
	override fun onBlockDestroyed(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, entity: LivingEntity): Boolean {
		return false
	}
	
	override fun canApplyAtEnchantingTable(stack: ItemStack, enchantment: Enchantment): Boolean {
		return enchantment === Enchantments.UNBREAKING
	}
	
	// Use handling
	
	override fun onItemRightClick(world: World, player: PlayerEntity, hand: Hand): ActionResult<ItemStack> {
		val heldItem = player.getHeldItem(hand)
		
		if (heldItem.damage >= heldItem.maxDamage || heldItem.heeTagOrNull.hasKey(COOLDOWN_TAG)) {
			return ActionResult(FAIL, heldItem)
		}
		
		val fluidResult = rayTrace(world, player, FluidMode.ANY)
		
		if (fluidResult.type != BLOCK) {
			return ActionResult(PASS, heldItem)
		}
		
		val clickedPos = (fluidResult as BlockRayTraceResult).pos
		
		if (!isModifiableFluid(world, clickedPos, player, heldItem)) {
			return ActionResult(FAIL, heldItem)
		}
		
		val clickedBlockColor = getFluidColor(clickedPos.getFluidState(world))
		
		if (!world.isRemote) {
			val blocksToRemove = if (player.isSneaking)
				Collections.singleton(clickedPos)
			else
				clickedPos.allInCenteredBox(1, 1, 1).toList()
			
			var totalRemoved = 0
			
			for (pos in blocksToRemove) {
				if (isModifiableFluid(world, pos, player, heldItem)) {
					pos.setAir(world)
					++totalRemoved
				}
			}
			
			guardItemBreaking(heldItem) {
				heldItem.doDamage(totalRemoved, player, hand)
			}
		}
		
		with(heldItem.heeTag) {
			putByte(COOLDOWN_TAG, COOLDOWN_TICKS.toByte())
			putInt(COLOR_TAG, clickedBlockColor)
		}
		
		// TODO sound
		
		player.addStat(Stats.ITEM_USED[this])
		
		return ActionResult(SUCCESS, heldItem)
	}
	
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean) {
		with(stack.heeTagOrNull ?: return) {
			if (hasKey(COOLDOWN_TAG)) {
				val cooldown = getByte(COOLDOWN_TAG).toInt()
				
				if (cooldown <= 1) {
					remove(COOLDOWN_TAG)
					remove(COLOR_TAG)
					stack.cleanupNBT()
				}
				else {
					putByte(COOLDOWN_TAG, (cooldown - 1).toByte())
				}
			}
		}
	}
	
	// Client side
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean {
		return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
	
	override val tint: ItemTint
		get() = Tint
	
	private object Tint : ItemTint() {
		@Sided(Side.CLIENT)
		override fun tint(stack: ItemStack, tintIndex: Int) = when (tintIndex) {
			1    -> stack.heeTagOrNull?.getInt(COLOR_TAG) ?: NO_TINT
			else -> NO_TINT
		}
	}
}
