package chylex.hee.game.entity.projectile

import chylex.hee.game.world.Pos
import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityXPBottle
import chylex.hee.system.migration.EntityXPOrb
import chylex.hee.system.migration.PotionTypes
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.network.IPacket
import net.minecraft.potion.PotionUtils
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks

class EntityProjectileExperienceBottle(type: EntityType<out EntityXPBottle>, world: World) : EntityXPBottle(type, world) {
	constructor(thrower: EntityLivingBase, stack: ItemStack) : this(ModEntities.EXPERIENCE_BOTTLE, thrower.world) {
		owner = thrower
		ownerId = thrower.uniqueID
		
		item = stack
		setPosition(thrower.posX, thrower.posY + thrower.eyeHeight - 0.1F, thrower.posZ)
		shoot(thrower, thrower.rotationPitch, thrower.rotationYaw, -20F, 0.7F, 1F)
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
			world.playEvent(2002, Pos(this), PotionUtils.getPotionColor(PotionTypes.WATER))
			
			var experience = ModItems.EXPERIENCE_BOTTLE.getExperienceAmountPerItem(item)
			
			while(experience > 0) {
				EntityXPOrb.getXPSplit(experience).also {
					experience -= it
					world.addEntity(EntityXPOrb(world, posX, posY, posZ, it))
				}
			}
			
			remove()
		}
	}
}
