package chylex.hee.game.item
import chylex.hee.HEE
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.Damage.Companion.TITLE_STARVE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.MAGIC_TYPE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.NON_LETHAL
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.makeEffect
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.putEnum
import net.minecraft.item.Food
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.event.entity.player.PlayerEvent

class ItemVoidSalad(properties: Properties) : Item(properties){
	@SubscribeAllEvents(modid = HEE.ID)
	companion object{
		private val DAMAGE_SAFE = Damage(MAGIC_TYPE, NON_LETHAL)
		private val DAMAGE_LETHAL = Damage(MAGIC_TYPE)
		
		private const val PLAYER_RESPAWN_HUNGRY_TAG = "MegaVoidSalad"
		private const val TYPE_TAG = "Type"
		
		val FOOD: Food = Food.Builder().hunger(0).saturation(0F).setAlwaysEdible().build()
		
		@SubscribeEvent
		fun onPlayerClone(e: PlayerEvent.Clone){
			if (e.isWasDeath && e.original.heeTagOrNull?.getBoolean(PLAYER_RESPAWN_HUNGRY_TAG) == true){
				setHungerAndResetSaturation(e.player, 1)
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
		SINGLE, DOUBLE, MEGA // UPDATE fix texture
	}
	
	init{
		addPropertyOverride(Resource.Custom("void_salad_type")){
			stack, _, _ -> getSaladType(stack).ordinal.toFloat()
		}
	}
	
	fun getSaladType(stack: ItemStack): Type{
		return stack.heeTagOrNull?.getEnum<Type>(TYPE_TAG) ?: Type.SINGLE
	}
	
	fun setSaladType(stack: ItemStack, type: Type){
		stack.heeTag.putEnum(TYPE_TAG, type)
	}
	
	override fun getUseDuration(stack: ItemStack): Int{
		return 70
	}
	
	override fun onUsingTick(stack: ItemStack, player: EntityLivingBase, count: Int){
		if (count in 26..56 && count % 4 == 0){
			val rand = player.rng
			player.playSound(Sounds.ENTITY_GENERIC_EAT, 0.5F + (0.5F * rand.nextInt(2)), rand.nextFloat(0.8F, 1.2F))
		}
	}
	
	override fun onItemUseFinish(stack: ItemStack, world: World, entity: EntityLivingBase): ItemStack{
		super.onItemUseFinish(stack, world, entity)
		applyVoidEffect(stack, entity)
		return ItemStack(Items.BOWL)
	}
	
	private fun applyVoidEffect(stack: ItemStack, entity: EntityLivingBase){
		when(getSaladType(stack)){
			Type.SINGLE ->
				DAMAGE_SAFE.dealTo(2F, entity, TITLE_STARVE)
			
			Type.DOUBLE -> {
				DAMAGE_SAFE.dealTo(10F, entity, TITLE_STARVE)
				entity.addPotionEffect(Potions.NAUSEA.makeEffect(20 * 15))
			}
			
			Type.MEGA -> {
				entity.heeTag.putBoolean(PLAYER_RESPAWN_HUNGRY_TAG, true)
				DAMAGE_LETHAL.dealTo(Float.MAX_VALUE, entity, TITLE_STARVE)
			}
		}
		
		if (entity is EntityPlayer){
			setHungerAndResetSaturation(entity, 0)
		}
	}
	
	override fun getTranslationKey(stack: ItemStack): String{
		return when(getSaladType(stack)){
			Type.SINGLE -> "item.hee.void_salad.single"
			Type.DOUBLE -> "item.hee.void_salad.double"
			Type.MEGA   -> "item.hee.void_salad.mega"
		}
	}
}
