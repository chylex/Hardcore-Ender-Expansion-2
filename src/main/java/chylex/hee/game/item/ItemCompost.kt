package chylex.hee.game.item
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientFX.IFXData
import chylex.hee.network.client.PacketClientFX.IFXHandler
import chylex.hee.system.util.readPos
import chylex.hee.system.util.size
import chylex.hee.system.util.use
import chylex.hee.system.util.writePos
import io.netty.buffer.ByteBuf
import net.minecraft.block.BlockDispenser
import net.minecraft.block.BlockDispenser.FACING
import net.minecraft.dispenser.IBlockSource
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Bootstrap.BehaviorDispenseOptional
import net.minecraft.item.Item
import net.minecraft.item.ItemDye
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumActionResult.FAIL
import net.minecraft.util.EnumActionResult.PASS
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class ItemCompost : Item(){
	companion object{
		private const val BONE_MEAL_EQUIVALENT = 2
		
		class FxUseData(private val pos: BlockPos) : IFXData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writePos(pos)
			}
		}
		
		@JvmStatic
		val FX_USE = object : IFXHandler{
			override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
				ItemDye.spawnBonemealParticles(world, readPos(), 25)
			}
		}
		
		private fun applyCompost(world: World, pos: BlockPos, playerInfo: Pair<EntityPlayer, EnumHand>? = null): Boolean{
			val simulatedItem = ItemStack(ModItems.COMPOST, BONE_MEAL_EQUIVALENT)
			
			repeat(BONE_MEAL_EQUIVALENT){
				if (playerInfo == null){
					ItemDye.applyBonemeal(simulatedItem, world, pos)
				}
				else{
					ItemDye.applyBonemeal(simulatedItem, world, pos, playerInfo.first, playerInfo.second)
				}
			}
			
			if (simulatedItem.size == BONE_MEAL_EQUIVALENT){
				return false
			}
			
			if (!world.isRemote){
				PacketClientFX(FX_USE, FxUseData(pos)).sendToAllAround(world, pos, 64.0)
			}
			
			return true
		}
	}
	
	init{
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, object : BehaviorDispenseOptional(){
			override fun dispenseStack(source: IBlockSource, stack: ItemStack): ItemStack{
				val world = source.world
				val pos = source.blockPos.offset(source.blockState.getValue(FACING))
				
				successful = false
				
				if (applyCompost(world, pos)){
					stack.shrink(1)
					successful = true
				}
				
				return stack
			}
		})
	}
	
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		val heldItem = player.getHeldItem(hand)
		
		if (!BlockEditor.canEdit(pos, player, heldItem)){
			return FAIL
		}
		
		if (applyCompost(world, pos, Pair(player, hand))){
			heldItem.shrink(1)
			return SUCCESS
		}
		
		return PASS
	}
}
