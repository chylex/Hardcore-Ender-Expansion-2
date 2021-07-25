package chylex.hee.game.entity

import chylex.hee.game.entity.properties.EntitySize
import chylex.hee.game.entity.properties.EntityTrackerInfo
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityClassification.MISC

interface IHeeEntityType<T : Entity> {
	val classification: EntityClassification
		get() = MISC
	
	val size: EntitySize
	val tracker: EntityTrackerInfo
	
	val isImmuneToFire: Boolean
		get() = false
	
	val disableSerialization: Boolean
		get() = false
}
