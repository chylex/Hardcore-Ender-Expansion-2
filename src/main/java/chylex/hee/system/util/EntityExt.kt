package chylex.hee.system.util
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.Vec3d

var Entity.posVec
	get() = this.positionVector
	set(value){
		this.posX = value.x
		this.posY = value.y
		this.posZ = value.z
	}

var Entity.motionVec
	get() = Vec3d(this.motionX, this.motionY, this.motionZ)
	set(value){
		this.motionX = value.x
		this.motionY = value.y
		this.motionZ = value.z
	}

val Entity.lookVec
	get() = this.getLook(0F)

fun EntityItem.cloneFrom(source: Entity){
	motionX = source.motionX
	motionY = source.motionY
	motionZ = source.motionZ
	isAirBorne = source.isAirBorne
	
	if (source is EntityItem){
		lifespan = source.lifespan
		setPickupDelay(NBTTagCompound().also { source.writeEntityToNBT(it) }.getInteger("PickupDelay")) // UPDATE: replace with an AT
		
		thrower = source.thrower
		owner = source.owner
	}
}
