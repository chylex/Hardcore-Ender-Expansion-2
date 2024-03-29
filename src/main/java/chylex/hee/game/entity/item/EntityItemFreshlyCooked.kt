package chylex.hee.game.entity.item

import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.init.ModEntities
import chylex.hee.util.math.square
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer
import net.minecraft.particles.ParticleTypes.SMOKE
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData

class EntityItemFreshlyCooked : EntityItemBase, IEntityAdditionalSpawnData {
	@Suppress("unused")
	constructor(type: EntityType<EntityItemFreshlyCooked>, world: World) : super(type, world)
	constructor(world: World, pos: Vector3d, stack: ItemStack) : super(ModEntities.ITEM_FRESHLY_COOKED, world, pos, stack)
	
	companion object {
		val TYPE = BaseType<EntityItemFreshlyCooked>()
		
		private const val STOP_SMOKING_AFTER_TICKS = 20 * 90
		
		private val PARTICLE_TICK = ParticleSpawnerVanilla(
			type = SMOKE,
			pos = Constant(0.375F, UP) + InBox(0.12F, 0F, 0.12F),
			mot = Constant(0.03F, UP) + InBox(0.02F)
		)
	}
	
	override fun writeSpawnData(buffer: PacketBuffer) {
		buffer.writeShort(age)
	}
	
	override fun readSpawnData(buffer: PacketBuffer) {
		age = buffer.readShort().toInt()
	}
	
	override fun tick() {
		super.tick()
		
		if (world.isRemote && age < STOP_SMOKING_AFTER_TICKS) {
			val period = 2 + ((4 * age) / STOP_SMOKING_AFTER_TICKS)
			
			if (ticksExisted % period == 0) {
				val chance = 1F - 0.75F * square(age.toFloat() / STOP_SMOKING_AFTER_TICKS)
				
				if (rand.nextFloat() < chance) {
					PARTICLE_TICK.spawn(Point(this, heightMp = 0.5F, amount = 1), rand)
				}
			}
		}
	}
}
