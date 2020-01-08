package chylex.hee.game.world.structure.trigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import net.minecraft.entity.Entity
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class EntityStructureTrigger private constructor(private val entityConstructor: (World) -> Entity, private val entityLocator: (BlockPos, Transform) -> Vec3d) : IStructureTrigger{
	constructor(
		entityConstructor: (World) -> Entity,
		yOffset: Double
	) : this(
		entityConstructor,
		{ pos, _ -> Vec3d(pos.x + 0.5, pos.y + yOffset, pos.z + 0.5) }
	)
	
	constructor(
		entityConstructor: (World) -> Entity,
		nudgeFacing: Direction,
		nudgeAmount: Double,
		yOffset: Double
	) : this(
		entityConstructor,
		{ pos, transform -> transform(nudgeFacing).let { Vec3d(pos.x + 0.5 + (nudgeAmount * it.xOffset), pos.y + yOffset, pos.z + 0.5 + (nudgeAmount * it.zOffset)) } }
	){
		require(nudgeFacing.yOffset == 0){ "entity trigger can only be nudged on x/z axis" }
	}
	
	constructor(
		triggerType: EntityTechnicalTrigger.Types,
		facing: Direction = SOUTH
	) : this(
		{ world -> EntityTechnicalTrigger(world, triggerType).apply { rotationYaw = facing.horizontalAngle } },
		yOffset = 0.0
	)
	
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){}
	
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		val (x, y, z) = entityLocator(pos, transform)
		
		entityConstructor(world).apply {
			setLocationAndAngles(x, y, z, rotationYaw, rotationPitch)
			transform(this)
			world.addEntity(this)
		}
	}
}
