package chylex.hee.game.item
import chylex.hee.HEE
import chylex.hee.game.block.BlockInfusedTNT
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.migration.vanilla.EntityCreeper
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.FLAG_NONE
import chylex.hee.system.util.doDamage
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import chylex.hee.system.util.removeBlock
import chylex.hee.system.util.setBlock
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.ExplosionEvent

class ItemFlintAndInfernium(properties: Properties) : Item(properties){
	@SubscribeAllEvents(modid = HEE.ID)
	companion object{
		private const val CREEPER_INFERNIUM_TAG = "Infernium"
		
		const val EXPLOSION_MULTIPLIER = 1.5F
		
		@SubscribeEvent(EventPriority.HIGHEST)
		fun onExplosionStart(e: ExplosionEvent.Start){
			val explosion = e.explosion
			val creeper = explosion.explosivePlacedBy as? EntityCreeper
			
			if (creeper?.heeTagOrNull?.getBoolean(CREEPER_INFERNIUM_TAG) == true){
				explosion.size *= EXPLOSION_MULTIPLIER
			}
		}
	}
	
	init{
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	fun igniteTNT(world: World, pos: BlockPos, player: EntityPlayer?, ignoreTrap: Boolean){
		if (pos.getBlock(world) === Blocks.TNT){
			pos.setBlock(world, ModBlocks.INFUSED_TNT, FLAG_NONE)
		}
		
		ModBlocks.INFUSED_TNT.igniteTNT(world, pos, pos.getState(world).with(BlockInfusedTNT.INFERNIUM, true), player, ignoreTrap)
		pos.removeBlock(world)
	}
	
	override fun onItemUse(context: ItemUseContext): ActionResultType{
		val player = context.player ?: return FAIL
		val world = context.world
		val pos = context.pos
		
		val heldItem = player.getHeldItem(context.hand)
		
		if (!BlockEditor.canEdit(pos, player, heldItem)){
			return FAIL
		}
		
		if (!world.isRemote){
			val block = pos.getBlock(world)
			
			if (block === Blocks.TNT || block === ModBlocks.INFUSED_TNT){
				igniteTNT(world, pos, player, ignoreTrap = false)
			}
			else{
				BlockEditor.place(ModBlocks.ETERNAL_FIRE, player, heldItem, context)
			}
			
			Sounds.ITEM_FLINTANDSTEEL_USE.playServer(world, pos, SoundCategory.BLOCKS, volume = 1.1F, pitch = world.rand.nextFloat(0.4F, 0.5F))
			heldItem.doDamage(1, player, context.hand)
		}
		
		return SUCCESS
	}
	
	override fun itemInteractionForEntity(stack: ItemStack, player: EntityPlayer, target: EntityLivingBase, hand: Hand): Boolean{
		if (target is EntityCreeper){
			Sounds.ITEM_FLINTANDSTEEL_USE.playServer(target.world, target.posVec, target.soundCategory, volume = 1.1F, pitch = target.rng.nextFloat(0.4F, 0.5F))
			player.swingArm(hand)
			player.getHeldItem(hand).doDamage(1, player, hand)
			
			target.heeTag.putBoolean(CREEPER_INFERNIUM_TAG, true)
			target.ignite()
			return true
		}
		
		return false
	}
}
