package chylex.hee.game.entity.technical
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.INVALID
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Main_Start
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Main_Portal
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_CornerHoles
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_Prison
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_TallIntersection
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.setEnum
import chylex.hee.system.util.use
import io.netty.buffer.ByteBuf
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import java.util.Random

class EntityTechnicalTrigger(world: World) : EntityTechnicalBase(world), IEntityAdditionalSpawnData{
	constructor(world: World, type: Types) : this(world){
		this.type = type
	}
	
	private companion object{
		private const val TYPE_TAG = "Type"
		private const val DATA_TAG = "Data"
		private const val TIMER_TAG = "Timer"
	}
	
	// Handler interface
	
	interface ITriggerHandler : INBTSerializable<TagCompound>{
		fun check(world: World): Boolean
		fun update(entity: EntityTechnicalTrigger)
		fun nextTimer(rand: Random): Int
		
		@JvmDefault override fun serializeNBT() = TagCompound()
		@JvmDefault override fun deserializeNBT(nbt: TagCompound){}
	}
	
	// Known handlers
	
	private object InvalidTriggerHandler : ITriggerHandler{
		override fun check(world: World) = true
		override fun update(entity: EntityTechnicalTrigger) = entity.setDead()
		override fun nextTimer(rand: Random) = Int.MAX_VALUE
	}
	
	enum class Types(val handlerConstructor: () -> ITriggerHandler){
		INVALID({ InvalidTriggerHandler }),
		STRONGHOLD_GLOBAL(StrongholdRoom_Main_Portal::Spawner),
		STRONGHOLD_TRAP_CORNER_HOLES(StrongholdRoom_Trap_CornerHoles::Trigger),
		STRONGHOLD_TRAP_PRISON(StrongholdRoom_Trap_Prison::Trigger),
		STRONGHOLD_TRAP_TALL_INTERSECTION({ StrongholdRoom_Trap_TallIntersection.Trigger }),
		ENERGY_SHRINE_GLOBAL({ EnergyShrineRoom_Main_Start.Particles })
	}
	
	// Entity
	
	val triggerType
		get() = type
	
	private var type by NotifyOnChange(INVALID){ newValue -> handler = newValue.handlerConstructor() }
	private var handler: ITriggerHandler = InvalidTriggerHandler
	
	private var timer = 0
	
	override fun entityInit(){}
	
	override fun writeSpawnData(buffer: ByteBuf) = buffer.use {
		writeInt(type.ordinal)
	}
	
	override fun readSpawnData(buffer: ByteBuf) = buffer.use {
		type = Types.values().getOrNull(readInt()) ?: INVALID
	}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (handler.check(world) && --timer < 0){
			handler.update(this)
			timer = handler.nextTimer(rand)
		}
	}
	
	override fun writeEntityToNBT(nbt: TagCompound) = with(nbt.heeTag){
		setEnum(TYPE_TAG, type)
		setTag(DATA_TAG, handler.serializeNBT())
		
		setShort(TIMER_TAG, timer.toShort())
	}
	
	override fun readEntityFromNBT(nbt: TagCompound) = with(nbt.heeTag){
		type = getEnum<Types>(TYPE_TAG) ?: INVALID
		handler.deserializeNBT(getCompoundTag(DATA_TAG))
		
		timer = getShort(TIMER_TAG).toInt()
	}
}
