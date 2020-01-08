package chylex.hee.game.item
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.vanilla.BlockDispenser
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.ItemBoneMeal
import chylex.hee.system.util.size
import net.minecraft.block.DispenserBlock.FACING
import net.minecraft.dispenser.IBlockSource
import net.minecraft.dispenser.OptionalDispenseBehavior
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class ItemCompost(properties: Properties) : Item(properties){
	companion object{
		private const val BONE_MEAL_EQUIVALENT = 2
		
		val FX_USE = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				ItemBoneMeal.spawnBonemealParticles(world, pos, 25)
			}
		}
		
		private fun applyCompost(world: World, pos: BlockPos, player: EntityPlayer? = null): Boolean{
			val simulatedItem = ItemStack(ModItems.COMPOST, BONE_MEAL_EQUIVALENT)
			
			repeat(BONE_MEAL_EQUIVALENT){
				if (player == null){
					ItemBoneMeal.applyBonemeal(simulatedItem, world, pos)
				}
				else{
					ItemBoneMeal.applyBonemeal(simulatedItem, world, pos, player)
				}
			}
			
			if (simulatedItem.size == BONE_MEAL_EQUIVALENT){
				return false
			}
			
			if (!world.isRemote){
				PacketClientFX(FX_USE, FxBlockData(pos)).sendToAllAround(world, pos, 64.0)
			}
			
			return true
		}
	}
	
	init{
		BlockDispenser.registerDispenseBehavior(this, object : OptionalDispenseBehavior(){
			override fun dispenseStack(source: IBlockSource, stack: ItemStack): ItemStack{
				val world = source.world
				val pos = source.blockPos.offset(source.blockState[FACING])
				
				successful = false
				
				if (applyCompost(world, pos)){
					stack.shrink(1)
					successful = true
				}
				
				return stack
			}
		})
	}
	
	override fun onItemUse(context: ItemUseContext): ActionResultType{
		val player = context.player ?: return FAIL
		val world = context.world
		val pos = context.pos
		
		val heldItem = player.getHeldItem(context.hand)
		
		if (!BlockEditor.canEdit(pos, player, heldItem)){
			return FAIL
		}
		
		if (applyCompost(world, pos, player)){
			heldItem.shrink(1)
			return SUCCESS
		}
		
		return PASS
	}
}
