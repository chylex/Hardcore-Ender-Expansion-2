package chylex.hee.game.item
import chylex.hee.client.render.util.NO_TINT
import chylex.hee.game.item.util.CustomToolMaterial.VOID_BUCKET
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.vanilla.Enchantments
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.allInCenteredBox
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.doDamage
import chylex.hee.system.util.facades.Stats
import chylex.hee.system.util.getFluidState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.setAir
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.enchantment.Enchantment
import net.minecraft.fluid.Fluids
import net.minecraft.fluid.IFluidState
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceContext.FluidMode
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.world.World
import java.util.Collections
import kotlin.streams.toList

class ItemVoidBucket(properties: Properties) : ItemAbstractVoidTool(properties, VOID_BUCKET){
	private companion object{
		private const val COLOR_TAG = "Color"
		
		private fun getFluidColor(state: IFluidState) = when{
			state.isEmpty -> 0
			state.fluid.isEquivalentTo(Fluids.LAVA) -> RGB(205, 90, 17).i // UPDATE
			else -> state.fluid.attributes.color
		}
		
		private fun isModifiableFluid(world: World, pos: BlockPos, player: EntityPlayer, stack: ItemStack): Boolean{
			return !pos.getFluidState(world).isEmpty && world.isBlockModifiable(player, pos) && BlockEditor.canEdit(pos, player, stack)
		}
	}
	
	override fun getDestroySpeed(stack: ItemStack, state: BlockState): Float{
		return 1F
	}
	
	override fun onBlockDestroyed(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, entity: EntityLivingBase): Boolean{
		return false
	}
	
	override fun canApplyAtEnchantingTable(stack: ItemStack, enchantment: Enchantment): Boolean{
		return enchantment === Enchantments.UNBREAKING
	}
	
	// Use handling
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: Hand): ActionResult<ItemStack>{
		val heldItem = player.getHeldItem(hand)
		
		if (heldItem.damage >= heldItem.maxDamage){
			return ActionResult(FAIL, heldItem)
		}
		
		val fluidResult = rayTrace(world, player, FluidMode.ANY)
		
		if (fluidResult.type != BLOCK){
			return ActionResult(PASS, heldItem)
		}
		
		val clickedPos = (fluidResult as BlockRayTraceResult).pos
		
		if (!isModifiableFluid(world, clickedPos, player, heldItem)){
			return ActionResult(FAIL, heldItem)
		}
		
		val clickedBlockColor = getFluidColor(clickedPos.getFluidState(world))
		
		if (!world.isRemote){
			val blocksToRemove = if (player.isSneaking)
				Collections.singleton(clickedPos)
			else
				clickedPos.allInCenteredBox(1, 1, 1).toList()
			
			var totalRemoved = 0
			
			for(pos in blocksToRemove){
				if (isModifiableFluid(world, pos, player, heldItem)){
					pos.setAir(world)
					++totalRemoved
				}
			}
			
			guardItemBreaking(heldItem, player, hand){
				heldItem.doDamage(totalRemoved, player, hand)
			}
		}
		
		heldItem.heeTag.putInt(COLOR_TAG, clickedBlockColor)
		
		// TODO sound
		
		player.cooldownTracker.setCooldown(this, 13)
		player.addStat(Stats.useItem(this))
		
		return ActionResult(SUCCESS, heldItem)
	}
	
	// Client side
	
	object Color : IItemColor{
		override fun getColor(stack: ItemStack, tintIndex: Int) = when(tintIndex){
			1 -> stack.heeTagOrNull?.getInt(COLOR_TAG) ?: NO_TINT // TODO using cooldown to determine the textures looks funny w/ multiple buckets in inventory
			else -> NO_TINT
		}
	}
}
