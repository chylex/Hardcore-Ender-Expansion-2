package chylex.hee.game.entity.util

import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute
import net.minecraft.entity.monster.EndermiteEntity
import net.minecraft.entity.monster.MonsterEntity
import net.minecraft.entity.monster.SilverfishEntity

object DefaultEntityAttributes {
	val hostileMob: MutableAttribute
		get() = MonsterEntity.func_234295_eP_()
	
	val peacefulMob: MutableAttribute
		get() = MobEntity.func_233666_p_()
	
	val endermite: MutableAttribute
		get() = EndermiteEntity.func_234288_m_()
	
	val silverfish: MutableAttribute
		get() = SilverfishEntity.func_234301_m_()
}
