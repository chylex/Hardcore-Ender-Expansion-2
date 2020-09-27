package chylex.hee.game.loot.conditions
import chylex.hee.game.entity.living.ICritTracker
import chylex.hee.system.facades.Resource
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameter
import net.minecraft.world.storage.loot.LootParameters

class ConditionCriticalHit(override val expectedValue: Boolean) : ILootConditionBoolean{
	override fun isTrue(context: LootContext): Boolean{
		return context.get(LootParameters.LAST_DAMAGE_PLAYER) != null && (context.get(LootParameters.THIS_ENTITY) as? ICritTracker)?.wasLastHitCritical == true
	}
	
	override fun getRequiredParameters(): MutableSet<LootParameter<*>>{
		return mutableSetOf(LootParameters.LAST_DAMAGE_PLAYER, LootParameters.THIS_ENTITY)
	}
	
	object Serializer : ILootConditionBoolean.Serializer<ConditionCriticalHit>(Resource.Custom("killed_by_critical_hit"), ConditionCriticalHit::class.java){
		override fun construct(expectedValue: Boolean) = ConditionCriticalHit(expectedValue)
	}
}
