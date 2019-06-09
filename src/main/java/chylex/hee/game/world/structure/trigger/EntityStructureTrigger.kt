package chylex.hee.game.world.structure.trigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.util.Transform
import net.minecraft.entity.Entity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class EntityStructureTrigger(private val entityConstructor: (World) -> Entity, private val yOffset: Double) : IStructureTrigger{
	constructor(
		triggerType: EntityTechnicalTrigger.Types,
		facing: EnumFacing = EnumFacing.SOUTH
	) : this(
		{ world -> EntityTechnicalTrigger(world, triggerType).apply { rotationYaw = facing.horizontalAngle } },
		0.0
	)
	
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		entityConstructor(world).apply {
			setLocationAndAngles(pos.x + 0.5, pos.y + this@EntityStructureTrigger.yOffset, pos.z + 0.5, rotationYaw, rotationPitch)
			transform(this)
			world.spawnEntity(this)
		}
	}
}
