package chylex.hee.game.item
import chylex.hee.HEE
import chylex.hee.game.block.BlockInfusedTNT
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.FLAG_NONE
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import chylex.hee.system.util.setAir
import chylex.hee.system.util.setBlock
import chylex.hee.system.util.with
import net.minecraft.block.BlockTNT
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumActionResult.FAIL
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HEE.ID)
class ItemFlintAndInfernium : Item(){
	companion object{
		private const val CREEPER_INFERNIUM_TAG = "Infernium"
		
		const val EXPLOSION_MULTIPLIER = 1.5F
		
		@JvmStatic
		@SubscribeEvent(priority = HIGHEST)
		fun onExplosionStart(e: ExplosionEvent.Start){
			val explosion = e.explosion
			val creeper = explosion.explosivePlacedBy as? EntityCreeper
			
			if (creeper?.heeTagOrNull?.getBoolean(CREEPER_INFERNIUM_TAG) == true){
				explosion.size *= EXPLOSION_MULTIPLIER
			}
		}
	}
	
	init{
		maxStackSize = 1
		maxDamage = 25
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		val heldItem = player.getHeldItem(hand)
		
		if (!BlockEditor.canEdit(pos, player, heldItem)){
			return FAIL
		}
		
		if (!world.isRemote){
			val block = pos.getBlock(world)
			
			if (block === Blocks.TNT || block === ModBlocks.INFUSED_TNT){
				if (block === Blocks.TNT){
					pos.setBlock(world, ModBlocks.INFUSED_TNT, FLAG_NONE)
				}
				
				ModBlocks.INFUSED_TNT.explode(world, pos, pos.getState(world).with(BlockTNT.EXPLODE, true).with(BlockInfusedTNT.INFERNIUM, true), player)
				pos.setAir(world)
			}
			else{
				BlockEditor.place(ModBlocks.ETERNAL_FIRE, player, heldItem, pos, facing, hitX, hitY, hitZ)
			}
			
			SoundEvents.ITEM_FLINTANDSTEEL_USE.playServer(world, pos, SoundCategory.BLOCKS, volume = 1.1F, pitch = world.rand.nextFloat(0.4F, 0.5F))
			heldItem.damageItem(1, player)
		}
		
		return SUCCESS
	}
	
	override fun itemInteractionForEntity(stack: ItemStack, player: EntityPlayer, target: EntityLivingBase, hand: EnumHand): Boolean{
		if (target is EntityCreeper){
			SoundEvents.ITEM_FLINTANDSTEEL_USE.playServer(target.world, target.posVec, target.soundCategory, volume = 1.1F, pitch = target.rng.nextFloat(0.4F, 0.5F))
			player.swingArm(hand)
			player.getHeldItem(hand).damageItem(1, player)
			
			target.heeTag.setBoolean(CREEPER_INFERNIUM_TAG, true)
			target.ignite()
			return true
		}
		
		return false
	}
}
