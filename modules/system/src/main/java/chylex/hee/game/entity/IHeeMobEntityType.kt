package chylex.hee.game.entity

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.entity.properties.EntitySpawnPlacement
import chylex.hee.game.entity.properties.EntityTrackerInfo
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute

interface IHeeMobEntityType<T : MobEntity> : IHeeEntityType<T> {
	override val localization: LocalizationStrategy
		get() = LocalizationStrategy.Default
	
	override val classification: EntityClassification
	
	override val tracker
		get() = EntityTrackerInfo.Defaults.MOB
	
	val attributes: MutableAttribute
	
	val placement: EntitySpawnPlacement<T>?
		get() = null
	
	val spawnEggName: String?
		get() = null
}
