package chylex.hee.game.entity.technical
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.INVALID
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.setEnum
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable

class EntityTechnicalTrigger(world: World) : EntityTechnicalBase(world){
	constructor(world: World, type: Types) : this(world){
		this.type = type
	}
	
	interface ITriggerHandler : INBTSerializable<NBTTagCompound>{
		val tickRate: Int
		fun update(entity: EntityTechnicalTrigger)
	}
	
	private object InvalidTriggerHandler : ITriggerHandler{
		override val tickRate = Int.MAX_VALUE
		override fun update(entity: EntityTechnicalTrigger) = entity.setDead()
		override fun serializeNBT() = NBTTagCompound()
		override fun deserializeNBT(nbt: NBTTagCompound){}
	}
	
	enum class Types(val handlerConstructor: () -> ITriggerHandler){
		INVALID({ InvalidTriggerHandler }),
	}
	
	private var type by NotifyOnChange(Types.INVALID){ newValue -> handler = newValue.handlerConstructor() }
	private var handler: ITriggerHandler = InvalidTriggerHandler
	
	override fun entityInit(){}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (!world.isRemote && ticksExisted % handler.tickRate == 0){
			handler.update(this)
		}
	}
	
	override fun writeEntityToNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		setEnum("Type", type)
		setTag("Data", handler.serializeNBT())
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		type = getEnum<Types>("Type") ?: INVALID
		handler.deserializeNBT(getCompoundTag("Data"))
	}
}
