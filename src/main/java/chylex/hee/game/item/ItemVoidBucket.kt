package chylex.hee.game.item
import chylex.hee.client.color.NO_TINT
import chylex.hee.game.inventory.cleanupNBT
import chylex.hee.game.inventory.doDamage
import chylex.hee.game.inventory.heeTag
import chylex.hee.game.inventory.heeTagOrNull
import chylex.hee.game.item.properties.CustomToolMaterial.VOID_BUCKET
import chylex.hee.game.world.BlockEditor
import chylex.hee.game.world.allInCenteredBox
import chylex.hee.game.world.getFluidState
import chylex.hee.game.world.setAir
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.facades.Resource
import chylex.hee.system.facades.Stats
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.serialization.hasKey
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
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

class ItemVoidBucket(properties: Properties) : ItemAbstractVoidTool(properties, VOID_BUCKET){
	private companion object{
		private const val COOLDOWN_TAG = "Cooldown"
		private const val COLOR_TAG = "Color"
		
		private const val COOLDOWN_TICKS = 13
		
		private fun getFluidColor(state: IFluidState) = when{
			state.isEmpty -> 0
			state.fluid.isEquivalentTo(Fluids.LAVA) -> RGB(205, 90, 17).i
			else -> state.fluid.attributes.color
		}
		
		private fun isModifiableFluid(world: World, pos: BlockPos, player: EntityPlayer, stack: ItemStack): Boolean{
			return !pos.getFluidState(world).isEmpty && world.isBlockModifiable(player, pos) && BlockEditor.canEdit(pos, player, stack)
		}
	}
	
	init{
		addPropertyOverride(Resource.Custom("void_bucket_cooldown")){
			stack, _, _ -> (stack.heeTagOrNull?.getByte(COOLDOWN_TAG) ?: 0) / COOLDOWN_TICKS.toFloat()
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
		
		if (heldItem.damage >= heldItem.maxDamage || heldItem.heeTagOrNull.hasKey(COOLDOWN_TAG)){
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
			
			guardItemBreaking(heldItem){
				heldItem.doDamage(totalRemoved, player, hand)
			}
		}
		
		with(heldItem.heeTag){
			putByte(COOLDOWN_TAG, COOLDOWN_TICKS.toByte())
			putInt(COLOR_TAG, clickedBlockColor)
		}
		
		// TODO sound
		
		player.addStat(Stats.useItem(this))
		
		return ActionResult(SUCCESS, heldItem)
	}
	
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		with(stack.heeTagOrNull ?: return){
			if (hasKey(COOLDOWN_TAG)){
				val cooldown = getByte(COOLDOWN_TAG).toInt()
				
				if (cooldown <= 1){
					remove(COOLDOWN_TAG)
					remove(COLOR_TAG)
					stack.cleanupNBT()
				}
				else{
					putByte(COOLDOWN_TAG, (cooldown - 1).toByte())
				}
			}
		}
	}
	
	// Client side
	
	object Color : IItemColor{
		override fun getColor(stack: ItemStack, tintIndex: Int) = when(tintIndex){
			1 -> stack.heeTagOrNull?.getInt(COLOR_TAG) ?: NO_TINT
			else -> NO_TINT
		}
	}
}
