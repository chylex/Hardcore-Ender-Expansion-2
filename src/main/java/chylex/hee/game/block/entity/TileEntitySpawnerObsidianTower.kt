package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.block.entity.base.TileEntityBaseSpawner
import chylex.hee.game.entity.living.EntityMobAngryEnderman
import chylex.hee.game.entity.living.EntityMobEnderman
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.particle.ParticleSpellCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerRoomData
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerSpawnerLevel
import chylex.hee.init.ModTileEntities
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.migration.Difficulty.PEACEFUL
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.forge.EventResult
import chylex.hee.system.migration.vanilla.EntityLiving
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.NBTList.Companion.putList
import chylex.hee.system.util.NBTObjectList
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.clone
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.getPos
import chylex.hee.system.util.getState
import chylex.hee.system.util.isAnyPlayerWithinRange
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItemOrNull
import chylex.hee.system.util.playClient
import chylex.hee.system.util.putEnum
import chylex.hee.system.util.putPos
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.square
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.use
import net.minecraft.entity.Entity
import net.minecraft.entity.SpawnReason
import net.minecraft.particles.ParticleTypes.EXPLOSION
import net.minecraft.particles.ParticleTypes.SMOKE
import net.minecraft.potion.EffectInstance
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.event.ForgeEventFactory
import java.util.Random
import kotlin.math.max

class TileEntitySpawnerObsidianTower(type: TileEntityType<TileEntitySpawnerObsidianTower>) : TileEntityBaseSpawner(type){
	constructor() : this(ModTileEntities.SPAWNER_OBSIDIAN_TOWER)
	
	companion object{
		private val PARTICLE_SMOKE = ParticleSpawnerVanilla(
			type = SMOKE,
			pos = InBox(0.5F)
		)
		
		private val PARTICLE_BREAK = ParticleSpawnerVanilla(
			type = EXPLOSION,
			pos = InBox(0.5F)
		)
		
		private val PARTICLE_SPELL_POS = Constant(0.25F, DOWN) + InBox(0.4F, 0.2F, 0.4F)
		private val PARTICLE_SPELL_MOT = InBox(0.01F)
		
		private const val SPAWN_AREA_RADIUS_XZ = 5.0
		private const val SPAWN_AREA_BOX_RANGE_XZ = 4.0
		private const val SPAWN_AREA_BOX_HEIGHT = 4.0
		private const val ACTIVATION_DISTANCE = 7.0 // rough estimate
		
		private const val TOWER_OFFSET_TAG = "TowerOffset"
		private const val TOWER_LEVEL_TAG = "TowerLevel"
		private const val TOWER_EFFECTS_TAG = "TowerEffects"
		private const val REMAINING_MOB_SPAWNS_TAG = "RemainingMobSpawns"
		private const val NEXT_SPAWN_COOLDOWN_TAG = "NextSpawnCooldown"
		
		val FX_BREAK = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				PARTICLE_BREAK.spawn(Point(pos, rand.nextInt(18, 20)), rand)
				Sounds.BLOCK_GLASS_BREAK.playClient(pos, SoundCategory.BLOCKS, volume = 1.3F, pitch = rand.nextFloat(0.7F, 0.8F))
			}
		}
	}
	
	var offset: BlockPos = BlockPos.ZERO
	
	private var level = ObsidianTowerSpawnerLevel.INTRODUCTION
	private var effects = emptyArray<EffectInstance>()
	
	private var remainingMobSpawns = 5
	private var nextSpawnCooldown = 0
	
	private val resetSpawnCooldown
		get() = if (level == ObsidianTowerSpawnerLevel.INTRODUCTION) 5 else 15
	
	private val floorCenter
		get() = pos.subtract(offset)
	
	override val clientRotationSpeed
		get() = 2.25F
	
	constructor(data: ObsidianTowerRoomData, rand: Random) : this(){
		this.level = data.spawnerLevel
		this.effects = data.effects.toTypedArray()
		
		this.remainingMobSpawns = level.mobLimitPerSpawner.takeUnless { it.isEmpty() }?.let(rand::nextInt) ?: remainingMobSpawns
		this.nextSpawnCooldown = resetSpawnCooldown
	}
	
	override fun createClientEntity(): Entity{
		return EntityMobEnderman(wrld)
	}
	
	// Tick handling
	
	override fun tickClient(){
		val rand = wrld.rand
		val time = wrld.totalTime
		
		if (time % 3L == 0L && rand.nextInt(max(2, 5 - effects.size)) == 0){
			val color = rand.nextItemOrNull(effects)?.let { IntColor(it.potion.liquidColor) }
			
			if (color != null){
				ParticleSpawnerCustom(
					type = ParticleSpellCustom,
					data = ParticleSpellCustom.Data(color, scale = (0.6F)..(0.7F)),
					pos = PARTICLE_SPELL_POS,
					mot = PARTICLE_SPELL_MOT
				).spawn(Point(pos, 1), rand)
			}
		}
		else if (time % 4L == 0L && rand.nextBoolean()){
			PARTICLE_SMOKE.spawn(Point(pos, 1), rand)
		}
	}
	
	override fun tickServer(){
		if (wrld.difficulty == PEACEFUL){
			return
		}
		
		val floorCenter = floorCenter
		
		if (!floorCenter.isAnyPlayerWithinRange(wrld, ACTIVATION_DISTANCE)){
			return
		}
		
		val searchArea = AxisAlignedBB(floorCenter).expand(0.0, SPAWN_AREA_BOX_HEIGHT, 0.0).grow(SPAWN_AREA_BOX_RANGE_XZ, 0.0, SPAWN_AREA_BOX_RANGE_XZ)
		
		if (getEntitiesInSpawnArea<EntityPlayer>(searchArea).none()){
			nextSpawnCooldown = max(nextSpawnCooldown, resetSpawnCooldown)
			return
		}
		
		val spawnedEndermen = getEntitiesInSpawnArea<EntityMobEnderman>(searchArea).count()
		val maxEndermen = level.mobLimitInSpawnArea
		
		if (spawnedEndermen >= maxEndermen){
			nextSpawnCooldown = max(nextSpawnCooldown, 30)
			return
		}
		
		if (--nextSpawnCooldown < 0){
			val rand = wrld.rand
			val amount = minOf(remainingMobSpawns, maxEndermen - spawnedEndermen, rand.nextInt(1, 2))
			
			repeat(amount){
				triggerSpawn(rand)
			}
			
			remainingMobSpawns -= amount
			
			if (remainingMobSpawns <= 0 || level == ObsidianTowerSpawnerLevel.INTRODUCTION){
				PacketClientFX(FX_BREAK, FxBlockData(pos)).sendToAllAround(this, 12.0)
				pos.breakBlock(wrld, false)
			}
			else{
				nextSpawnCooldown = 20 * (level.baseCooldown - wrld.difficulty.id - rand.nextInt(0, 2))
				markDirty()
			}
		}
	}
	
	// Spawn helpers
	
	private fun isEntityInRange(entity: Entity): Boolean{
		return floorCenter.let { square(it.x + 0.5 - entity.posX) + square(it.z + 0.5 - entity.posZ) } <= square(SPAWN_AREA_RADIUS_XZ)
	}
	
	private inline fun <reified T : Entity> getEntitiesInSpawnArea(searchArea: AxisAlignedBB): List<T>{
		return wrld.selectVulnerableEntities.inBox<T>(searchArea).filter(::isEntityInRange)
	}
	
	private fun canEntitySpawn(entity: EntityLiving): Boolean{
		val posBelow = Pos(entity).down()
		
		return (
			posBelow.getState(wrld).canEntitySpawn(wrld, posBelow, entity.type) &&
			isEntityInRange(entity) &&
			entity.isNotColliding(wrld) &&
			ForgeEventFactory.canEntitySpawn(entity, wrld, entity.posX, entity.posY, entity.posZ, null, SpawnReason.SPAWNER) != EventResult.DENY
		)
	}
	
	private fun triggerSpawn(rand: Random){
		val (x, y, z) = floorCenter
		val enderman = EntityMobAngryEnderman(wrld)
		
		for(attempt in 1..50){
			enderman.setPosition(x + 0.5 + rand.nextFloat(-SPAWN_AREA_BOX_RANGE_XZ, SPAWN_AREA_BOX_RANGE_XZ), y + 0.01, z + 0.5 + rand.nextFloat(-SPAWN_AREA_BOX_RANGE_XZ, SPAWN_AREA_BOX_RANGE_XZ))
			
			if (canEntitySpawn(enderman)){
				enderman.setLocationAndAngles(enderman.posX, enderman.posY, enderman.posZ, rand.nextFloat(0F, 360F), 0F)
				
				for(effect in effects){
					enderman.addPotionEffect(effect.clone())
				}
				
				wrld.addEntity(enderman)
				enderman.spawnExplosionParticle()
				break
			}
		}
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.writeNBT(nbt, context)
		
		if (context == STORAGE){
			putPos(TOWER_OFFSET_TAG, offset)
			putEnum(TOWER_LEVEL_TAG, level)
			putInt(REMAINING_MOB_SPAWNS_TAG, remainingMobSpawns)
			putInt(NEXT_SPAWN_COOLDOWN_TAG, nextSpawnCooldown)
		}
		
		putList(TOWER_EFFECTS_TAG, NBTObjectList.of(effects.map { it.write(TagCompound()) }))
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		super.readNBT(nbt, context)
		
		if (context == STORAGE){
			offset = getPos(TOWER_OFFSET_TAG)
			level = getEnum<ObsidianTowerSpawnerLevel>(TOWER_LEVEL_TAG) ?: level
			remainingMobSpawns = getInt(REMAINING_MOB_SPAWNS_TAG)
			nextSpawnCooldown = getInt(NEXT_SPAWN_COOLDOWN_TAG)
		}
		
		effects = getListOfCompounds(TOWER_EFFECTS_TAG).mapNotNull { EffectInstance.read(it) }.toTypedArray()
	}
}
