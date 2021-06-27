package chylex.hee.game.block.dispenser

import chylex.hee.game.entity.projectile.EntityProjectileExperienceBottle
import net.minecraft.dispenser.IPosition
import net.minecraft.dispenser.ProjectileDispenseBehavior
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

object DispenseExperienceBottle : ProjectileDispenseBehavior() {
	override fun getProjectileEntity(world: World, position: IPosition, stack: ItemStack): ProjectileEntity {
		return EntityProjectileExperienceBottle(world, position.x, position.y, position.z, stack)
	}
	
	override fun getProjectileInaccuracy(): Float {
		return super.getProjectileInaccuracy() * 0.5F
	}
	
	override fun getProjectileVelocity(): Float {
		return super.getProjectileVelocity() * 1.25F
	}
}
