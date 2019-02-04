package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import net.minecraft.entity.Entity
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class EntityStructureTrigger(private val entityConstructor: (World) -> Entity, private val yOffset: Double) : IStructureTrigger{
	override fun realize(world: World, pos: BlockPos, rotation: Rotation){
		entityConstructor(world).apply {
			setLocationAndAngles(pos.x + 0.5, pos.y + this@EntityStructureTrigger.yOffset, pos.z + 0.5, getRotatedYaw(rotation), rotationPitch)
			world.spawnEntity(this)
		}
	}
}
