package chylex.hee.game.entity.item

import chylex.hee.game.entity.util.cloneFrom
import chylex.hee.system.random.nextFloat
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.item.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.IPacket
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks

abstract class EntityItemBase(type: EntityType<out EntityItemBase>, world: World) : ItemEntity(type, world) {
	constructor(type: EntityType<out EntityItemBase>, world: World, stack: ItemStack, replacee: Entity) : this(type, world) {
		this.cloneFrom(replacee)
		item = stack
	}
	
	@Suppress("LeakingThis")
	constructor(type: EntityType<out EntityItemBase>, world: World, pos: Vector3d, stack: ItemStack) : this(type, world) {
		item = stack
		rotationYaw = rand.nextFloat(0F, 360F)
		
		setPosition(pos.x, pos.y, pos.z)
		setMotion(rand.nextFloat(-0.1, 0.1), 0.2, rand.nextFloat(-0.1, 0.1))
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
}
