package chylex.hee.game.item
import chylex.hee.game.block.fluid.FluidBase
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.game.world.util.RayTracer
import chylex.hee.system.util.allInCenteredBox
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.setAir
import net.minecraft.block.Block
import net.minecraft.block.BlockLiquid
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialLiquid
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult.FAIL
import net.minecraft.util.EnumActionResult.PASS
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.world.World
import net.minecraftforge.fluids.IFluidBlock
import java.util.Collections

class ItemVoidBucket : ItemAbstractVoidTool(){
	private companion object{
		private const val COLOR_TAG = "Color"
		
		private fun getFluidColor(block: Block): Int{
			if (block is IFluidBlock){
				return when(val fluid = block.fluid){
					is FluidBase -> fluid.rgbColor.toInt()
					else -> fluid.color
				}
			}
			
			val material = block.material
			
			return when{
				material === Material.LAVA -> RGB(205, 90, 17).toInt()
				material is MaterialLiquid -> material.materialMapColor.colorValue
				else -> 0
			}
		}
		
		private fun isFluid(block: Block): Boolean{
			return block is IFluidBlock || block is BlockLiquid
		}
		
		private fun isModifiableFluid(world: World, pos: BlockPos, player: EntityPlayer, stack: ItemStack): Boolean{
			return isFluid(pos.getBlock(world)) && world.isBlockModifiable(player, pos) && BlockEditor.canEdit(pos, player, stack)
		}
		
		private val RAY_TRACE_FLUID = RayTracer(
			canCollideCheck = { _, _, state -> val block = state.block; block.canCollideCheck(state, true) || isFluid(block) }
		)
	}
	
	init{
		maxDamage = 575
	}
	
	override fun getDestroySpeed(stack: ItemStack, state: IBlockState): Float{
		return 1F
	}
	
	override fun onBlockDestroyed(stack: ItemStack, world: World, state: IBlockState, pos: BlockPos, entity: EntityLivingBase): Boolean{
		return false
	}
	
	override fun canApplyAtEnchantingTable(stack: ItemStack, enchantment: Enchantment): Boolean{
		return enchantment === Enchantments.UNBREAKING
	}
	
	// Use handling
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack>{
		val heldItem = player.getHeldItem(hand)
		
		if (heldItem.itemDamage >= heldItem.maxDamage){
			return ActionResult(FAIL, heldItem)
		}
		
		val fluidResult = RAY_TRACE_FLUID.traceBlocksInPlayerReach(player)
		
		if (fluidResult?.typeOfHit != BLOCK){
			return ActionResult(PASS, heldItem)
		}
		
		val clickedPos = fluidResult.blockPos
		
		if (!isModifiableFluid(world, clickedPos, player, heldItem)){
			return ActionResult(FAIL, heldItem)
		}
		
		val clickedBlockColor = getFluidColor(clickedPos.getBlock(world))
		
		if (!world.isRemote){
			val blocksToRemove = if (player.isSneaking)
				Collections.singleton(clickedPos)
			else
				clickedPos.allInCenteredBox(1, 1, 1)
			
			var totalRemoved = 0
			
			for(pos in blocksToRemove.filterIndexed { index, pos -> index == 0 || isModifiableFluid(world, pos, player, heldItem) }){
				pos.setAir(world)
				++totalRemoved
			}
			
			guardItemBreaking(heldItem, player){
				heldItem.damageItem(totalRemoved, player)
			}
		}
		
		heldItem.heeTag.setInteger(COLOR_TAG, clickedBlockColor)
		
		// TODO sound
		
		player.cooldownTracker.setCooldown(this, 13)
		player.addStat(StatList.getObjectUseStats(this)!!)
		
		return ActionResult(SUCCESS, heldItem)
	}
	
	// Client side
	
	object Color : IItemColor{
		private const val NONE = -1
		
		override fun colorMultiplier(stack: ItemStack, tintIndex: Int) = when(tintIndex){
			1 -> stack.heeTagOrNull?.getInteger(COLOR_TAG) ?: NONE // TODO using cooldown to determine the textures looks funny w/ multiple buckets in inventory
			else -> NONE
		}
	}
}
