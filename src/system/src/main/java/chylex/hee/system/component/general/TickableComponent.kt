package chylex.hee.system.component.general

import chylex.hee.system.component.EntityComponents
import net.minecraft.entity.Entity

interface TickableComponent {
	@JvmDefault fun tickClient() {}
	@JvmDefault fun tickServer() {}
}

@JvmName("tickEntity")
fun <T : Entity> EntityComponents.tick(entity: T) {
	if (entity.world.isRemote) {
		this.on(TickableComponent::tickClient)
	}
	else {
		this.on(TickableComponent::tickServer)
	}
}
