package chylex.hee.game.item
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.getMaterial
import chylex.hee.system.util.getTile
import chylex.hee.system.util.isReplaceable
import chylex.hee.system.util.setState
import chylex.hee.system.util.withFacing
import com.mojang.authlib.GameProfile
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.block.BlockDispenser
import net.minecraft.dispenser.IBlockSource
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.init.Bootstrap.BehaviorDispenseOptional
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.inventory.EntityEquipmentSlot.HEAD
import net.minecraft.item.Item
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumActionResult.FAIL
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

class ItemEndermanHead : Item(){ // UPDATE redo this
	init{
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, object : BehaviorDispenseOptional(){
			override fun dispenseStack(source: IBlockSource, stack: ItemStack): ItemStack{
				successful = true
				
				// TODO implement placement behavior if anyone even cares
				
				if (ItemArmor.dispenseArmor(source, stack).isEmpty){
					successful = false
				}
				
				return stack
			}
		})
	}
	
	fun setupTileEntity(tile: TileEntitySkull){
		tile.playerProfile = GameProfile(null, "MHF_Enderman") // UPDATE and this
	}
	
	fun createTileEntity(): TileEntitySkull{
		return TileEntitySkull().apply(::setupTileEntity)
	}
	
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		var pos = pos
		var facing = facing
		
		if (facing == EnumFacing.DOWN){
			return FAIL
		}
		
		if (pos.isReplaceable(world)){
			facing = EnumFacing.UP
			pos = pos.down()
		}
		
		if (!pos.isReplaceable(world)){
			if (!pos.getMaterial(world).isSolid && !world.isSideSolid(pos, facing, true)){
				return FAIL
			}
			
			pos = pos.offset(facing)
		}
		
		val stack = player.getHeldItem(hand)
		
		if (!player.canPlayerEdit(pos, facing, stack) || !Blocks.SKULL.canPlaceBlockAt(world, pos)){
			return FAIL
		}
		
		if (world.isRemote){
			return SUCCESS
		}
		
		pos.setState(world, ModBlocks.ENDERMAN_HEAD.withFacing(facing), 11)
		
		pos.getTile<TileEntitySkull>(world)?.let {
			setupTileEntity(it)
			
			if (facing == UP){
				it.skullRotation = MathHelper.floor((player.rotationYaw * 16F / 360F) + 0.5) and 15
			}
		}
		
		if (player is EntityPlayerMP){
			CriteriaTriggers.PLACED_BLOCK.trigger(player, pos, stack)
		}
		
		stack.shrink(1)
		return SUCCESS
	}
	
	override fun getEquipmentSlot(stack: ItemStack): EntityEquipmentSlot{
		return HEAD
	}
	
	override fun isValidArmor(stack: ItemStack, slot: EntityEquipmentSlot, entity: Entity): Boolean{
		return slot == HEAD
	}
}
