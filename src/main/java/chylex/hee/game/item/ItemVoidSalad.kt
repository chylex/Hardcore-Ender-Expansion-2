package chylex.hee.game.item
import chylex.hee.HEE
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.Damage.Companion.TITLE_STARVE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.MAGIC_TYPE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.NON_LETHAL
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.nextFloat
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.init.MobEffects.NAUSEA
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.util.NonNullList
import net.minecraft.world.World
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HEE.ID)
class ItemVoidSalad : ItemFood(0, 0F, false){
	private companion object{
		private val DAMAGE_SAFE = Damage(MAGIC_TYPE, NON_LETHAL)
		private val DAMAGE_LETHAL = Damage(MAGIC_TYPE)
		
		private const val PLAYER_RESPAWN_HUNGRY_TAG = "MegaVoidSalad"
		
		@JvmStatic
		@SubscribeEvent
		fun onPlayerClone(e: PlayerEvent.Clone){
			if (e.isWasDeath && e.original.heeTagOrNull?.getBoolean(PLAYER_RESPAWN_HUNGRY_TAG) == true){
				setHungerAndResetSaturation(e.entityPlayer, 1)
			}
		}
		
		private fun setHungerAndResetSaturation(player: EntityPlayer, hunger: Int){
			with(player.foodStats){
				foodLevel = hunger
				addStats(0, 0F) // resets saturation to the same level as hunger
				
				if (hunger > 0){
					addExhaustion((4F * hunger) + 2.75F)
				}
			} // UPDATE make sure all of this still works as expected
		}
	}
	
	enum class Type{
		SINGLE, DOUBLE, MEGA
	}
	
	init{
		maxStackSize = 1
		hasSubtypes = true
		
		setAlwaysEdible()
	}
	
	override fun getMaxItemUseDuration(stack: ItemStack): Int{
		return 70
	}
	
	override fun onUsingTick(stack: ItemStack, player: EntityLivingBase, count: Int){
		if (count in 26..56 && count % 4 == 0){
			val rand = player.rng
			player.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + (0.5F * rand.nextInt(2)), rand.nextFloat(0.8F, 1.2F))
		}
	}
	
	override fun onItemUseFinish(stack: ItemStack, world: World, entity: EntityLivingBase): ItemStack{
		super.onItemUseFinish(stack, world, entity)
		return ItemStack(Items.BOWL)
	}
	
	override fun onFoodEaten(stack: ItemStack, world: World, player: EntityPlayer){
		val type = Type.values().getOrNull(stack.metadata) ?: return
		
		when(type){
			Type.SINGLE ->
				DAMAGE_SAFE.dealTo(2F, player, TITLE_STARVE)
			
			Type.DOUBLE -> {
				DAMAGE_SAFE.dealTo(10F, player, TITLE_STARVE)
				player.addPotionEffect(PotionEffect(NAUSEA, 20 * 15, 0))
			}
			
			Type.MEGA -> {
				player.heeTag.setBoolean(PLAYER_RESPAWN_HUNGRY_TAG, true)
				DAMAGE_LETHAL.dealTo(Float.MAX_VALUE, player, TITLE_STARVE)
			}
		}
		
		setHungerAndResetSaturation(player, 0)
	}
	
	override fun getTranslationKey(stack: ItemStack): String{
		return when(stack.metadata){
			2 -> "item.hee.void_salad.mega"
			1 -> "item.hee.void_salad.double"
			else -> "item.hee.void_salad.single"
		}
	}
	
	override fun getSubItems(tab: CreativeTabs, items: NonNullList<ItemStack>){
		if (isInCreativeTab(tab)){
			items.add(ItemStack(this, 1, Type.SINGLE.ordinal))
			// hide the rest
		}
	}
}
