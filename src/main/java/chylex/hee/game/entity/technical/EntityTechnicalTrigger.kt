package chylex.hee.game.entity.technical

import chylex.hee.game.entity.properties.EntityTrackerInfo
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.INVALID
import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrineGenerator
import chylex.hee.game.world.generation.feature.energyshrine.piece.EnergyShrineRoom_Main_Start
import chylex.hee.game.world.generation.feature.obsidiantower.piece.ObsidianTowerLevel_Top
import chylex.hee.game.world.generation.feature.stronghold.StrongholdGenerator
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Main_Portal
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Trap_CornerHoles
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Trap_Prison
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Trap_TallIntersection
import chylex.hee.game.world.generation.feature.tombdungeon.piece.TombDungeonRoom_End
import chylex.hee.game.world.generation.feature.tombdungeon.piece.TombDungeonRoom_Tomb
import chylex.hee.init.ModEntities
import chylex.hee.system.heeTag
import chylex.hee.util.delegate.NotifyOnChange
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getEnum
import chylex.hee.util.nbt.putEnum
import chylex.hee.util.nbt.use
import net.minecraft.entity.EntityType
import net.minecraft.network.PacketBuffer
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import java.util.Random

class EntityTechnicalTrigger(type: EntityType<EntityTechnicalTrigger>, world: World) : EntityTechnicalBase(type, world), IEntityAdditionalSpawnData {
	constructor(world: World, type: Types) : this(ModEntities.TECHNICAL_TRIGGER, world) {
		this.type = type
	}
	
	constructor(world: World, type: Types, nbt: TagCompound) : this(world, type) {
		handler.deserializeNBT(nbt)
	}
	
	object Type : BaseType<EntityTechnicalTrigger>() {
		override val tracker
			get() = EntityTrackerInfo.Defaults.TECHNICAL.copy(trackingRange = 16)
	}
	
	private companion object {
		private const val TYPE_TAG = "Type"
		private const val DATA_TAG = "Data"
		private const val TIMER_TAG = "Timer"
	}
	
	// Handler interface
	
	interface ITriggerHandler : INBTSerializable<TagCompound> {
		fun check(world: World): Boolean
		fun update(entity: EntityTechnicalTrigger)
		fun nextTimer(rand: Random): Int
		
		override fun serializeNBT() = TagCompound()
		override fun deserializeNBT(nbt: TagCompound) {}
	}
	
	// Known handlers
	
	private object InvalidTriggerHandler : ITriggerHandler {
		override fun check(world: World) = true
		override fun update(entity: EntityTechnicalTrigger) = entity.remove()
		override fun nextTimer(rand: Random) = Int.MAX_VALUE
	}
	
	enum class Types(val handlerConstructor: () -> ITriggerHandler) {
		INVALID({ InvalidTriggerHandler }),
		STRONGHOLD_GENERATOR({ StrongholdGenerator.GeneratorTrigger }),
		STRONGHOLD_GLOBAL(StrongholdRoom_Main_Portal::Spawner),
		STRONGHOLD_TRAP_CORNER_HOLES(StrongholdRoom_Trap_CornerHoles::Trigger),
		STRONGHOLD_TRAP_PRISON(StrongholdRoom_Trap_Prison::Trigger),
		STRONGHOLD_TRAP_TALL_INTERSECTION({ StrongholdRoom_Trap_TallIntersection.Trigger }),
		ENERGY_SHRINE_GENERATOR({ EnergyShrineGenerator.GeneratorTrigger }),
		ENERGY_SHRINE_GLOBAL({ EnergyShrineRoom_Main_Start.Particles }),
		TOMB_DUNGEON_UNDREAD_SPAWNER(TombDungeonRoom_Tomb::MobSpawnerTrigger),
		TOMB_DUNGEON_END(TombDungeonRoom_End::Trigger),
		OBSIDIAN_TOWER_TOP_GLOWSTONE(ObsidianTowerLevel_Top::GlowstoneTrigger),
		OBSIDIAN_TOWER_DEATH_ANIMATION(ObsidianTowerLevel_Top::DeathAnimationTrigger)
	}
	
	// Entity
	
	val triggerType
		get() = type
	
	private var type by NotifyOnChange(INVALID) { newValue -> handler = newValue.handlerConstructor() }
	private var handler: ITriggerHandler = InvalidTriggerHandler
	
	private var timer = 0
	
	override fun registerData() {}
	
	override fun writeSpawnData(buffer: PacketBuffer) {
		buffer.writeInt(type.ordinal)
	}
	
	override fun readSpawnData(buffer: PacketBuffer) {
		type = Types.values().getOrNull(buffer.readInt()) ?: INVALID
	}
	
	override fun tick() {
		super.tick()
		
		if (handler.check(world) && --timer < 0) {
			handler.update(this)
			timer = handler.nextTimer(rand)
		}
	}
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		putEnum(TYPE_TAG, this@EntityTechnicalTrigger.type)
		put(DATA_TAG, handler.serializeNBT())
		
		putShort(TIMER_TAG, timer.toShort())
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		this@EntityTechnicalTrigger.type = getEnum<Types>(TYPE_TAG) ?: INVALID
		handler.deserializeNBT(getCompound(DATA_TAG))
		
		timer = getShort(TIMER_TAG).toInt()
	}
}
