package chylex.hee.game.entity.item
import chylex.hee.game.item.infusion.Infusion.FIRE
import chylex.hee.game.item.infusion.Infusion.HARMLESS
import chylex.hee.game.item.infusion.Infusion.POWER
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.mechanics.ExplosionBuilder
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.system.util.useHeeTag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.MoverType
import net.minecraft.entity.item.EntityTNTPrimed
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Explosion
import net.minecraft.world.World

class EntityInfusedTNT : EntityTNTPrimed{
	private companion object{
		private val PARTICLE_TICK = ParticleSpawnerVanilla(SMOKE_NORMAL)
	}
	
	@Suppress("unused")
	constructor(world: World) : super(world)
	
	@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") // UPDATE
	constructor(world: World, pos: BlockPos, infusions: InfusionList, igniter: EntityLivingBase?) : super(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, igniter){
		this.infusions = infusions
	}
	
	constructor(world: World, pos: BlockPos, infusions: InfusionList, explosion: Explosion) : this(world, pos, infusions, explosion.explosivePlacedBy){
		fuse = rand.nextInt(fuse / 4) + (fuse / 8)
	}
	
	private var infusions = InfusionList.EMPTY
	
	override fun onUpdate(){
		prevPosX = posX
		prevPosY = posY
		prevPosZ = posZ
		
		if (!hasNoGravity()){
			motionY -= 0.04
		}
		
		move(MoverType.SELF, motionX, motionY, motionZ)
		motionX *= 0.98
		motionY *= 0.98
		motionZ *= 0.98
		
		if (onGround){
			motionX *= 0.7
			motionZ *= 0.7
			motionY *= -0.5
		}
		
		if (--fuse <= 0){
			setDead()
			blowUp()
		}
		else{
			handleWaterMovement()
			PARTICLE_TICK.spawn(Point(posX, posY + 0.5, posZ, 1), rand)
		}
	}
	
	// Explosion handling
	
	private fun blowUp(){
		if (world.isRemote){
			return
		}
		
		val strength = if (infusions.has(POWER)) 6F else 4F
		
		val isFiery = infusions.has(FIRE)
		val isHarmless = infusions.has(HARMLESS)
		
		with(ExplosionBuilder()){
			this.destroyBlocks = !isHarmless
			this.damageEntities = !isHarmless
			
			this.spawnFire = isFiery
			
			trigger(world, this@EntityInfusedTNT, posX, posY + (height / 16.0), posZ, strength)
		}
	}
	
	// Serialization
	
	override fun writeEntityToNBT(nbt: NBTTagCompound) = useHeeTag(nbt){
		InfusionTag.setList(this, infusions)
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = useHeeTag(nbt){
		infusions = InfusionTag.getList(this)
	}
}
