package chylex.hee.game.entity.item
import chylex.hee.system.util.Pos
import chylex.hee.system.util.cloneFrom
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getState
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextBiasedFloat
import chylex.hee.system.util.nextFloat
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.MoverType
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.SoundEvents.ENTITY_GENERIC_BURN
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class EntityItemIgneousRock : EntityItem{
	@Suppress("unused")
	constructor(world: World) : super(world)
	
	constructor(world: World, stack: ItemStack, replacee: Entity) : super(world, replacee.posX, replacee.posY, replacee.posZ, stack){
		this.cloneFrom(replacee)
		lifespan = 1200 + (world.rand.nextBiasedFloat(3F) * 1200F).floorToInt()
	}
	
	private var prevMotionVec = Vec3d.ZERO
	
	init{
		isImmuneToFire = true
	}
	
	override fun onUpdate(){
		prevMotionVec = motionVec
		super.onUpdate()
		
		val pos = Pos(posX, posY, posZ)
		
		if (Pos(prevPosX, prevPosY, prevPosZ) != pos || ticksExisted % 25 == 0){
			if (pos.getState(world).material == Material.WATER){
				motionY = 0.2
				motionX = (rand.nextFloat() - rand.nextFloat()) * 0.2
				motionZ = (rand.nextFloat() - rand.nextFloat()) * 0.2
				super.playSound(ENTITY_GENERIC_BURN, 0.4F, rand.nextFloat(2.0F, 2.4F))
			}
		}
		
		if (isInLava){
			lifespan -= 3
			
			world.handleMaterialAcceleration(entityBoundingBox, Material.LAVA, this)
			motionX *= 0.9
			motionZ *= 0.9
		}
		
		// TODO FX
	}
	
	override fun move(type: MoverType, x: Double, y: Double, z: Double){
		if (isInLava){
			super.move(type, x * 0.2, y * 0.01, z * 0.2)
		}
		else{
			super.move(type, x, y, z)
		}
	}
	
	override fun playSound(sound: SoundEvent, volume: Float, pitch: Float){
		if (sound == ENTITY_GENERIC_BURN && volume == 0.4F && pitch >= 2.0F){ // UPDATE: find a better way, all item handling has changed anyway
			motionVec = prevMotionVec // this disables vanilla lava handling, but also breaks hasNoGravity
			return
		}
		
		super.playSound(sound, volume, pitch)
	}
	
	override fun isEntityInvulnerable(source: DamageSource): Boolean{
		return super.isEntityInvulnerable(source) || source.isFireDamage
	}
	
	override fun isBurning(): Boolean = true
}
