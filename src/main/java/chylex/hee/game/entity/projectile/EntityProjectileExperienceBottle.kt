package chylex.hee.game.entity.projectile

import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.util.math.Pos
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.item.ExperienceBottleEntity
import net.minecraft.entity.item.ExperienceOrbEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.IPacket
import net.minecraft.potion.PotionUtils
import net.minecraft.potion.Potions
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks

class EntityProjectileExperienceBottle(type: EntityType<out ExperienceBottleEntity>, world: World) : ExperienceBottleEntity(type, world) {
	constructor(thrower: LivingEntity, stack: ItemStack) : this(ModEntities.EXPERIENCE_BOTTLE, thrower.world) {
		shooter = thrower
		item = stack
		setPosition(thrower.posX, thrower.posY + thrower.eyeHeight - 0.1F, thrower.posZ)
		setDirectionAndMovement(thrower, thrower.rotationPitch, thrower.rotationYaw, -20F, 0.7F, 1F)
	}
	
	constructor(world: World, x: Double, y: Double, z: Double, stack: ItemStack) : this(ModEntities.EXPERIENCE_BOTTLE, world) {
		item = stack
		setPosition(x, y, z)
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun onImpact(result: RayTraceResult) {
		if (!world.isRemote) {
			world.playEvent(2002, Pos(this), PotionUtils.getPotionColor(Potions.WATER))
			
			var experience = ModItems.EXPERIENCE_BOTTLE.getExperienceAmountPerItem(item)
			
			while (experience > 0) {
				ExperienceOrbEntity.getXPSplit(experience).also {
					experience -= it
					world.addEntity(ExperienceOrbEntity(world, posX, posY, posZ, it))
				}
			}
			
			remove()
		}
	}
}
