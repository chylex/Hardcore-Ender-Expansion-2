package chylex.hee.game.entity.technical
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.INVALID
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Main_Portal
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_CornerHoles
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_Prison
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_TallIntersection
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.setEnum
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable
import java.util.Random

class EntityTechnicalTrigger(world: World) : EntityTechnicalBase(world){
	constructor(world: World, type: Types) : this(world){
		this.type = type
	}
	
	interface ITriggerHandler : INBTSerializable<NBTTagCompound>{
		fun update(entity: EntityTechnicalTrigger)
		fun nextTimer(rand: Random): Int
	}
	
	private object InvalidTriggerHandler : ITriggerHandler{
		override fun update(entity: EntityTechnicalTrigger) = entity.setDead()
		override fun nextTimer(rand: Random) = Int.MAX_VALUE
		override fun serializeNBT() = NBTTagCompound()
		override fun deserializeNBT(nbt: NBTTagCompound){}
	}
	
	enum class Types(val handlerConstructor: () -> ITriggerHandler){
		INVALID({ InvalidTriggerHandler }),
		STRONGHOLD_GLOBAL(StrongholdRoom_Main_Portal::Spawner),
		STRONGHOLD_TRAP_CORNER_HOLES(StrongholdRoom_Trap_CornerHoles::Trigger),
		STRONGHOLD_TRAP_PRISON(StrongholdRoom_Trap_Prison::Trigger),
		STRONGHOLD_TRAP_TALL_INTERSECTION({ StrongholdRoom_Trap_TallIntersection.Trigger })
	}
	
	private var type by NotifyOnChange(Types.INVALID){ newValue -> handler = newValue.handlerConstructor() }
	private var handler: ITriggerHandler = InvalidTriggerHandler
	
	private var timer = 0
	
	override fun entityInit(){}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (!world.isRemote && --timer < 0){
			handler.update(this)
			timer = handler.nextTimer(rand)
		}
	}
	
	override fun writeEntityToNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		setEnum("Type", type)
		setTag("Data", handler.serializeNBT())
		
		setShort("Timer", timer.toShort())
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		type = getEnum<Types>("Type") ?: INVALID
		handler.deserializeNBT(getCompoundTag("Data"))
		
		timer = getShort("Timer").toInt()
	}
}
