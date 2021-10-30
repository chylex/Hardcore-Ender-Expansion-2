package chylex.hee.game.item

import chylex.hee.HEE
import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.Resource
import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.IDamageDealer.Companion.TITLE_STARVE
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.MAGIC_TYPE
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.NON_LETHAL
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.ConsumeFoodComponent
import chylex.hee.game.item.components.IItemNameComponent
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.util.ItemProperty
import chylex.hee.game.potion.util.makeInstance
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.nbt.getEnum
import chylex.hee.util.nbt.putEnum
import chylex.hee.util.random.nextFloat
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Food
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.potion.Effects
import net.minecraft.util.SoundEvents
import net.minecraftforge.event.entity.player.PlayerEvent

@SubscribeAllEvents(modid = HEE.ID)
object ItemVoidSalad : HeeItemBuilder() {
	private const val PLAYER_RESPAWN_HUNGRY_TAG = "MegaVoidSalad"
	private const val TYPE_TAG = "Type"
	
	private val DAMAGE_SAFE = Damage(MAGIC_TYPE, NON_LETHAL)
	private val DAMAGE_LETHAL = Damage(MAGIC_TYPE)
	
	private val SALAD_TYPE_PROPERTY = ItemProperty(Resource.Custom("void_salad_type")) { stack ->
		getSaladType(stack).ordinal.toFloat()
	}
	
	private val FOOD: Food = Food.Builder().hunger(0).saturation(0F).setAlwaysEdible().build()
	
	init {
		localization = LocalizationStrategy.None
		localizationExtra[Type.SINGLE.translationKey] = "Void Salad"
		localizationExtra[Type.DOUBLE.translationKey] = "Void Void Salad"
		localizationExtra[Type.MEGA.translationKey] = "Mega Void Salad"
		
		model = ItemModel.WithOverrides(
			ItemModel.Simple,
			SALAD_TYPE_PROPERTY to mapOf(
				1F to ItemModel.Named("void_void_salad"),
				2F to ItemModel.Named("mega_void_salad"),
			)
		)
		
		properties.add(SALAD_TYPE_PROPERTY)
		
		maxStackSize = 1
		food = FOOD
		
		components.name = object : IItemNameComponent {
			override fun getTranslationKey(stack: ItemStack): String {
				return getSaladType(stack).translationKey
			}
		}
		
		components.consume = object : ConsumeFoodComponent(FOOD) {
			override fun getDuration(stack: ItemStack): Int {
				return 70
			}
			
			override fun tick(stack: ItemStack, entity: LivingEntity, tick: Int) {
				if (tick in 26..56 && tick % 4 == 0) {
					val rand = entity.rng
					entity.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + (0.5F * rand.nextInt(2)), rand.nextFloat(0.8F, 1.2F))
				}
			}
			
			override fun finish(stack: ItemStack, entity: LivingEntity): ItemStack {
				super.finish(stack, entity)
				applyVoidEffect(stack, entity)
				return ItemStack(Items.BOWL)
			}
		}
	}
	
	enum class Type(private val translationKeySuffix: String) {
		SINGLE("single"),
		DOUBLE("double"),
		MEGA("mega");
		
		val translationKey
			get() = "item.hee.void_salad.$translationKeySuffix"
	}
	
	fun setSaladType(stack: ItemStack, type: Type) {
		stack.heeTag.putEnum(TYPE_TAG, type)
	}
	
	fun getSaladType(stack: ItemStack): Type {
		return stack.heeTagOrNull?.getEnum<Type>(TYPE_TAG) ?: Type.SINGLE
	}
	
	@SubscribeEvent
	fun onPlayerClone(e: PlayerEvent.Clone) {
		if (e.isWasDeath && e.original.heeTagOrNull?.getBoolean(PLAYER_RESPAWN_HUNGRY_TAG) == true) {
			setHungerAndResetSaturation(e.player, 1)
		}
	}
	
	private fun setHungerAndResetSaturation(player: PlayerEntity, hunger: Int) {
		with(player.foodStats) {
			foodLevel = hunger
			addStats(0, 0F) // resets saturation to the same level as hunger
			
			if (hunger > 0) {
				addExhaustion((4F * hunger) + 2.75F)
			}
		}
	}
	
	private fun applyVoidEffect(stack: ItemStack, entity: LivingEntity) {
		when (getSaladType(stack)) {
			Type.SINGLE -> {
				DAMAGE_SAFE.dealTo(2F, entity, TITLE_STARVE)
			}
			
			Type.DOUBLE -> {
				DAMAGE_SAFE.dealTo(10F, entity, TITLE_STARVE)
				entity.addPotionEffect(Effects.NAUSEA.makeInstance(20 * 15))
			}
			
			Type.MEGA   -> {
				entity.heeTag.putBoolean(PLAYER_RESPAWN_HUNGRY_TAG, true)
				DAMAGE_LETHAL.dealTo(Float.MAX_VALUE, entity, TITLE_STARVE)
			}
		}
		
		if (entity is PlayerEntity) {
			setHungerAndResetSaturation(entity, 0)
		}
	}
}
