package chylex.hee.game.entity.item
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import io.netty.buffer.ByteBuf
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData

class EntityItemFreshlyCooked : EntityItem, IEntityAdditionalSpawnData{
	private companion object{
		private const val STOP_SMOKING_AFTER_TICKS = 20 * 90
		
		private val PARTICLE_TICK = ParticleSpawnerVanilla(
			type = SMOKE_NORMAL,
			pos = Constant(0.375F, UP) + InBox(0.12F, 0F, 0.12F),
			mot = Constant(0.03F, UP) + InBox(0.02F)
		)
	}
	
	@Suppress("unused")
	constructor(world: World) : super(world)
	
	constructor(world: World, x: Double, y: Double, z: Double, stack: ItemStack) : super(world, x, y, z, stack)
	
	override fun writeSpawnData(buffer: ByteBuf) = buffer.use {
		writeShort(age)
	}
	
	override fun readSpawnData(buffer: ByteBuf) = buffer.use {
		age = readShort().toInt()
	}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (world.isRemote && age < STOP_SMOKING_AFTER_TICKS){
			val period = 2 + ((4 * age) / STOP_SMOKING_AFTER_TICKS)
			
			if (ticksExisted % period == 0){
				val chance = 1F - 0.75F * square(age.toFloat() / STOP_SMOKING_AFTER_TICKS)
				
				if (rand.nextFloat() < chance){
					PARTICLE_TICK.spawn(Point(this, heightMp = 0.5F, amount = 1), rand)
				}
			}
		}
	}
}
