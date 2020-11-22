package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.base.TileEntityBase
import chylex.hee.game.block.entity.base.TileEntityBase.Context.NETWORK
import chylex.hee.game.entity.item.EntityItemNoBob
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.selectEntities
import chylex.hee.game.entity.selectExistingEntities
import chylex.hee.game.entity.selectVulnerableEntities
import chylex.hee.game.inventory.size
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.IGNORE_INVINCIBILITY
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.MAGIC_TYPE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_KNOCKBACK
import chylex.hee.game.particle.ParticleExperienceOrbFloating
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.FLAG_SYNC_CLIENT
import chylex.hee.game.world.component1
import chylex.hee.game.world.component2
import chylex.hee.game.world.component3
import chylex.hee.game.world.playClient
import chylex.hee.game.world.playServer
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModItems
import chylex.hee.init.ModSounds
import chylex.hee.init.ModTileEntities
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientUpdateExperience
import chylex.hee.network.fx.FxEntityData
import chylex.hee.network.fx.FxEntityHandler
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.addY
import chylex.hee.system.math.floorToInt
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntityXPOrb
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.use
import net.minecraft.entity.Entity
import net.minecraft.item.Items
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.vector.Vector3d
import java.util.Random
import java.util.WeakHashMap
import kotlin.math.min
import kotlin.math.sin

class TileEntityExperienceGate(type: TileEntityType<TileEntityExperienceGate>) : TileEntityBase(type), ITickableTileEntity{
	constructor() : this(ModTileEntities.EXPERIENCE_GATE)
	
	companion object{
		private val DAMAGE_START_EXTRACTION = Damage(MAGIC_TYPE, PEACEFUL_KNOCKBACK, IGNORE_INVINCIBILITY())
		
		private const val TARGET_XP = 2000
		private const val TARGET_LEVEL_UP = (58F / 115F) * TARGET_XP
		
		private const val EXPERIENCE_TAG = "Experience"
		
		private val PARTICLE_TICK_POS = Constant(0.5F, UP) + InBox(1.5F, 0F, 1.5F)
		
		private val PARTICLE_TICK = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(ParticleExperienceOrbFloating.DEFAULT_COLOR, lifespan = 29..30, scale = 0.825F),
			pos = PARTICLE_TICK_POS,
			mot = InBox(0F, 0F, 0.07F, 0.08F, 0F, 0F)
		)
		
		private val PARTICLE_TICK_SLOW = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(ParticleExperienceOrbFloating.DEFAULT_COLOR, lifespan = 39..40, scale = 0.825F),
			pos = PARTICLE_TICK_POS,
			mot = InBox(0F, 0F, 0.015F, 0.025F, 0F, 0F)
		)
		
		private val PARTICLE_EXPERIENCE = ParticleSpawnerCustom(
			type = ParticleExperienceOrbFloating,
			data = ParticleExperienceOrbFloating.Data(lifespan = 29),
			pos = Constant(0.4F, UP) + InBox(1.4F, 0F, 1.4F),
			mot = Constant(0.16F, UP)
		)
		
		private val PARTICLE_CONSUME = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(color = RGB(255u), scale = 0.75F),
			pos = InBox(0.2F)
		)
		
		val FX_CONSUME = object : FxEntityHandler(){
			override fun handle(entity: Entity, rand: Random){
				val offset = if (entity is EntityItem && entity !is EntityItemNoBob){
					0.35 + (sin((entity.age + 1.0) / 10.0 + entity.hoverStart) * 0.1) // UPDATE 1.15 (taken from ItemRenderer)
				}
				else{
					entity.height * 0.5
				}
				
				val pos = entity.posVec.addY(offset)
				
				PARTICLE_CONSUME.spawn(Point(pos, amount = 9), rand)
				ModSounds.BLOCK_EXPERIENCE_GATE_PICKUP.playClient(pos, SoundCategory.BLOCKS, volume = 0.2F, pitch = rand.nextFloat(0.55F, 1.25F))
			}
		}
		
		private fun tryDrainXp(player: EntityPlayer, amount: Float): Boolean = with(player){
			val xpDrainFloat = amount / xpBarCap()
			
			if (experienceLevel > 0 || experience >= xpDrainFloat){
				experience -= xpDrainFloat
				
				while(experience < 0F){
					val mp = experience * xpBarCap()
					val level = experienceLevel
					
					addExperienceLevel(-1)
					experience = if (level > 0) 1F + (mp / xpBarCap()) else 0F
				}
				
				PacketClientUpdateExperience(experience).sendToPlayer(player)
				return true
			}
			
			return false
		}
	}
	
	private class PlayerExtractionData(val startTime: Long){
		var lastTriggerTime = 0L
	}
	
	// Instance
	
	private var experience by Notifying(0F, FLAG_SYNC_CLIENT or FLAG_MARK_DIRTY)
	private val playerExtraction = WeakHashMap<EntityPlayer, PlayerExtractionData>()
	private var clientLoaded = false
	
	private val entityDetectionArea
		get() = pos.let { (x, y, z) -> AxisAlignedBB(x - 1.0, y + 0.5, z - 1.0, x + 2.0, y + 3.5, z + 2.0) }
	
	private val tokenHolderToCharge
		get() = wrld.selectExistingEntities.inBox<EntityTokenHolder>(AxisAlignedBB(pos).expand(0.0, 4.0, 0.0)).firstOrNull { it.currentCharge < 1F }
	
	private val canStartDraining
		get() = experience < TARGET_XP && tokenHolderToCharge != null
	
	val chargeProgress
		get() = experience.coerceAtMost(TARGET_XP.toFloat()) / TARGET_XP
	
	private var particlePauseTimer = 0
	
	override fun tick(){
		if (wrld.isRemote){
			val rand = wrld.rand
			
			if (particlePauseTimer > 0){
				--particlePauseTimer
			}
			else if (experience < TARGET_XP){
				if (wrld.selectVulnerableEntities.inBox<EntityPlayer>(entityDetectionArea).any()){
					PARTICLE_TICK_SLOW.spawn(Point(pos, rand.nextInt(1, 2)), rand)
				}
				else{
					PARTICLE_TICK.spawn(Point(pos, 6), rand)
				}
			}
			else if (tokenHolderToCharge != null){
				PARTICLE_EXPERIENCE.spawn(Point(pos, 1), rand)
				// TODO sound fx
			}
			
			return
		}
		
		if (experience >= TARGET_XP){
			val tokenHolder = tokenHolderToCharge
			
			if (tokenHolder != null){
				val newCharge = tokenHolder.currentCharge + (1F / 175F)
				
				if (newCharge >= 1F){
					tokenHolder.forceDropToken(Vector3d.ZERO)
					tokenHolder.currentCharge = 0F
					experience = 0F
				}
				else{
					tokenHolder.currentCharge = newCharge
				}
			}
		}
		else if (wrld.totalTime % 4L == 0L){
			for(orb in wrld.selectEntities.inBox<EntityXPOrb>(entityDetectionArea)){ // makes throwing xp bottles into the gate a bit nicer
				if (orb.ticksExisted > 6){
					onCollision(orb)
				}
			}
		}
	}
	
	// Collision
	
	private fun calculateXpLimit(player: EntityPlayer): Float{
		val xpBarCap = player.xpBarCap()
		val xpDrainLimit = minOf(10F, TARGET_XP - experience, 0.2F * xpBarCap)
		
		return if (player.experienceLevel == 0)
			min(xpDrainLimit, player.experience * xpBarCap)
		else
			xpDrainLimit
	}
	
	fun onCollision(player: EntityPlayer){
		if (!canStartDraining || player.isCreative || player.isSpectator){
			return
		}
		
		val currentTime = wrld.totalTime
		
		val extractionData = playerExtraction[player]?.takeIf {
			currentTime - it.lastTriggerTime < 10L
		} ?: PlayerExtractionData(currentTime).also {
			DAMAGE_START_EXTRACTION.dealTo(1F, player)
			playerExtraction[player] = it
		}
		
		if (extractionData.lastTriggerTime == currentTime){
			return
		}
		
		extractionData.lastTriggerTime = currentTime
		
		val xpDrain = (0.6F + ((currentTime - extractionData.startTime) / 9L).toInt() * 0.2F).coerceAtMost(calculateXpLimit(player))
		
		if (xpDrain > 0F && tryDrainXp(player, xpDrain)){
			experience += xpDrain
			
			if (currentTime % 3L == 0L){
				ModSounds.BLOCK_EXPERIENCE_GATE_PICKUP.playServer(wrld, player.posVec, SoundCategory.BLOCKS, volume = 0.1F, pitch = wrld.rand.nextFloat(0.55F, 1.25F))
			}
		}
	}
	
	fun onCollision(entity: EntityItem){
		if (!canStartDraining){
			return
		}
		
		val stack = entity.item
		val item = stack.item
		
		if (item === Items.ENCHANTED_BOOK){
			// TODO
		}
		else if (item === ModItems.KNOWLEDGE_NOTE){
			// TODO
			experience += stack.size
		}
		else{
			return
		}
		
		PacketClientFX(FX_CONSUME, FxEntityData(entity)).sendToAllAround(entity, 16.0)
		entity.remove()
	}
	
	fun onCollision(orb: EntityXPOrb){
		if (!canStartDraining){
			return
		}
		
		val toAdd = min(orb.getXpValue(), TARGET_XP - experience.floorToInt())
		
		experience += toAdd
		orb.xpValue -= toAdd
		
		if (orb.xpValue <= 0){
			PacketClientFX(FX_CONSUME, FxEntityData(orb)).sendToAllAround(orb, 16.0)
			orb.remove()
		}
	}
	
	// Rendering
	
	override fun hasFastRenderer() = true
	
	@Sided(Side.CLIENT)
	override fun getRenderBoundingBox(): AxisAlignedBB{
		return AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 0, 2))
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		putFloat(EXPERIENCE_TAG, experience)
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		if (context == NETWORK){
			if (clientLoaded){
				val previous = experience
				val current = getFloat(EXPERIENCE_TAG)
				
				if ((previous < TARGET_LEVEL_UP && current >= TARGET_LEVEL_UP) || (previous < TARGET_XP && current >= TARGET_XP)){
					ModSounds.BLOCK_EXPERIENCE_GATE_LEVELUP.playClient(pos, SoundCategory.BLOCKS, volume = 0.7F)
				}
				
				if ((previous < TARGET_XP && current >= TARGET_XP) || (previous >= TARGET_XP && current < TARGET_XP)){
					particlePauseTimer = 24
				}
			}
			else{
				clientLoaded = true
			}
		}
		
		experience = getFloat(EXPERIENCE_TAG)
	}
}
