package chylex.hee.game.entity.living.behavior
import chylex.hee.client.MC
import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.entity.living.behavior.EnderEyeAttack.Melee
import chylex.hee.game.entity.lookPosVec
import chylex.hee.game.entity.motionY
import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.selectExistingEntities
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.OBSIDIAN_TOWER_TOP_GLOWSTONE
import chylex.hee.game.world.Pos
import chylex.hee.game.world.breakBlock
import chylex.hee.game.world.component1
import chylex.hee.game.world.component2
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.getTile
import chylex.hee.game.world.isAir
import chylex.hee.game.world.playClient
import chylex.hee.game.world.playServer
import chylex.hee.game.world.setBlock
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModBlocks
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxEntityData
import chylex.hee.network.fx.FxEntityHandler
import chylex.hee.system.math.Quaternion
import chylex.hee.system.math.addY
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import chylex.hee.system.math.directionTowards
import chylex.hee.system.math.floorToInt
import chylex.hee.system.math.square
import chylex.hee.system.math.toPitch
import chylex.hee.system.math.toYaw
import chylex.hee.system.migration.Blocks
import chylex.hee.system.migration.EntityLightningBolt
import chylex.hee.system.migration.EntityPlayerMP
import chylex.hee.system.migration.Sounds
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextItem
import chylex.hee.system.random.nextItemOrNull
import chylex.hee.system.random.nextVector2
import chylex.hee.system.random.removeItemOrNull
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.use
import net.minecraft.client.particle.DiggingParticle
import net.minecraft.entity.Entity
import net.minecraft.network.play.server.SPlaySoundEffectPacket
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.util.INBTSerializable
import java.util.Random
import kotlin.math.min

sealed class EnderEyePhase : INBTSerializable<TagCompound>{
	open fun tick(entity: EntityBossEnderEye) = this
	
	override fun serializeNBT() = TagCompound()
	override fun deserializeNBT(nbt: TagCompound){}
	
	object Hibernated : EnderEyePhase()
	
	class OpenEye : EnderEyePhase(){
		private companion object{
			private const val TIMER_TAG = "Timer"
		}
		
		private var timer: Byte = 35
		
		override fun tick(entity: EntityBossEnderEye): EnderEyePhase{
			--timer
			
			if (timer == 15.toByte()){
				for(nearby in entity.world.selectExistingEntities.allInRange(entity.posVec, 8.0)){
					if (nearby !== entity){
						entity.performBlastKnockback(nearby, 0.75F)
					}
				}
				
				Sounds.ENTITY_GENERIC_EXPLODE.playServer(entity.world, entity.posVec, SoundCategory.HOSTILE, volume = 0.5F) // TODO knockback fx
			}
			else if (timer < 0){
				val totalSpawners = entity.totalSpawners.toInt()
				
				if (totalSpawners == 0 || entity.dimension != DimensionType.THE_END){
					return Floating.withScreech(entity, 0)
				}
				
				val instance = TerritoryInstance.fromPos(entity)
				val territory = instance?.territory
				
				if (territory != TerritoryType.OBSIDIAN_TOWERS){
					return Floating.withScreech(entity, 0)
				}
				
				val chunks = instance.territory.chunks
				val (startChunkX, startChunkZ) = instance.topLeftChunk
				
				val world = entity.world
				val remainingSpawners = mutableListOf<BlockPos>()
				val glowstonePositions = mutableListOf<BlockPos>()
				val glowstonePositionGroups = mutableMapOf<BlockPos, MutableList<BlockPos>>()
				
				for(chunkX in startChunkX until (startChunkX + chunks))
				for(chunkZ in startChunkZ until (startChunkZ + chunks)){
					val chunk = world.getChunk(chunkX, chunkZ)
					
					for((pos, tile) in chunk.tileEntityMap){
						if (tile is TileEntitySpawnerObsidianTower){
							remainingSpawners.add(pos)
						}
					}
					
					for(entityList in chunk.entityLists)
					for(trigger in entityList.getByClass(EntityTechnicalTrigger::class.java)){
						if (trigger.triggerType == OBSIDIAN_TOWER_TOP_GLOWSTONE){
							val pos = Pos(trigger)
							
							glowstonePositions.add(pos)
							
							val group = glowstonePositionGroups.entries.find { it.key.distanceSqTo(pos) < square(20) }
							
							if (group == null){
								glowstonePositionGroups[pos] = mutableListOf(pos)
							}
							else{
								group.value.add(pos)
							}
						}
					}
				}
				
				val percentage = when(val count = remainingSpawners.size){
					0             -> 0
					totalSpawners -> 100
					else          -> 1 + ((count.toFloat() / totalSpawners) * 99F).floorToInt()
				}
				
				return Spawners(remainingSpawners, glowstonePositions, glowstonePositionGroups.flatMap { listOf(entity.rng.nextItem(it.value)) }.toMutableList(), percentage.toByte())
			}
			
			return this
		}
		
		override fun serializeNBT() = TagCompound().apply {
			putByte(TIMER_TAG, timer)
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			timer = getByte(TIMER_TAG)
		}
	}
	
	class Spawners(
		private val remainingSpawnerPositions: MutableList<BlockPos>,
		private val towerGlowstonePositions: MutableList<BlockPos>,
		private val remainingLightningPositions: MutableList<BlockPos>,
		private var spawnerPercentage: Byte
	) : EnderEyePhase(){
		private companion object{
			private const val TIMER_TAG = "Timer"
			private const val SPAWNER_PERCENTAGE = "SpawnerPercentage"
			private const val SPAWNER_POSITIONS = "SpawnerPositions"
			private const val GLOWSTONE_POSITIONS = "GlowstonePositions"
			private const val LIGHTNING_POSITIONS = "LightningPositions"
		}
		
		private var timer: Byte = 120
		
		override fun tick(entity: EntityBossEnderEye): EnderEyePhase{
			--timer
			
			if (timer in 30..100){
				if (timer % 3 == 0){
					val rand = entity.rng
					
					rand.removeItemOrNull(remainingSpawnerPositions)?.let { pos ->
						breakSpawner(entity, pos)
						
						if (timer % 6 == 0 && rand.nextInt(10) < 6){
							playSpawnerBreakSound(entity)
						}
					}
				}
				
				if ((101 - timer) % 18 == 0){
					val world = entity.world as ServerWorld
					
					entity.rng.removeItemOrNull(remainingLightningPositions)?.let {
						world.addLightningBolt(EntityLightningBolt(world, it.x + 0.5, it.y.toDouble(), it.z + 0.5, true)) // TODO make lightning visible from all distances in the End
					}
				}
			}
			else if (timer == 29.toByte()){
				playSpawnerBreakSound(entity)
				remainingSpawnerPositions.forEach { breakSpawner(entity, it) }
				remainingSpawnerPositions.clear()
			}
			else if (timer < 0){
				return Floating.withScreech(entity, spawnerPercentage.toInt())
			}
			
			return this
		}
		
		private fun breakSpawner(entity: EntityBossEnderEye, pos: BlockPos){
			val world = entity.world
			
			if (pos.getTile<TileEntitySpawnerObsidianTower>(world) != null){
				pos.breakBlock(world, drops = false)
				
				repeat(3){
					entity.rng.nextItemOrNull(towerGlowstonePositions)?.let(entity.spawnerParticles::add)
				}
			}
		}
		
		private fun playSpawnerBreakSound(entity: EntityBossEnderEye){
			val rand = entity.rng
			
			val (offX, offY, offZ) = rand.nextVector2(xz = rand.nextFloat(7.0, 11.0), y = rand.nextFloat(-3.0, 2.0))
			val soundType = ModBlocks.SPAWNER_OBSIDIAN_TOWERS.defaultState.soundType
			
			for(player in entity.world.players){
				if (player.getDistanceSq(entity) < square(32.0)){
					val conn = (player as EntityPlayerMP).connection
					val packet = SPlaySoundEffectPacket(soundType.breakSound, SoundCategory.BLOCKS, player.posX + offX, player.posY + offY, player.posZ + offZ, soundType.volume * 1.6F, soundType.pitch * 0.95F)
					
					conn.sendPacket(packet)
				}
			}
		}
		
		override fun serializeNBT() = TagCompound().apply {
			putByte(TIMER_TAG, timer)
			putByte(SPAWNER_PERCENTAGE, spawnerPercentage)
			
			putLongArray(SPAWNER_POSITIONS, remainingSpawnerPositions.map(BlockPos::toLong))
			putLongArray(GLOWSTONE_POSITIONS, towerGlowstonePositions.map(BlockPos::toLong))
			putLongArray(LIGHTNING_POSITIONS, remainingLightningPositions.map(BlockPos::toLong))
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			timer = getByte(TIMER_TAG)
			spawnerPercentage = getByte(SPAWNER_PERCENTAGE)
			
			remainingSpawnerPositions.clear()
			remainingSpawnerPositions.addAll(getLongArray(SPAWNER_POSITIONS).map(BlockPos::fromLong))
			
			towerGlowstonePositions.clear()
			towerGlowstonePositions.addAll(getLongArray(GLOWSTONE_POSITIONS).map(BlockPos::fromLong))
			
			remainingLightningPositions.clear()
			remainingLightningPositions.addAll(getLongArray(LIGHTNING_POSITIONS).map(BlockPos::fromLong))
		}
	}
	
	class Floating(remainingSpawnerPercentage: Int) : EnderEyePhase(){
		companion object{
			private const val CURRENT_TAG = "Current"
			private const val TARGET_TAG = "Target"
			
			fun withScreech(entity: EntityBossEnderEye, remainingSpawnerPercentage: Int): Floating{
				// TODO screech on first tick
				
				//val (sound, volume) = when(screechSpawnerPercentage){
				//	0 ->
				//	100 ->
				//	else ->
				//}
				return Floating(remainingSpawnerPercentage)
			}
			
			val FX_FINISH = object : FxEntityHandler(){
				override fun handle(entity: Entity, rand: Random){
					val world = entity.world
					val state = ModBlocks.OBSIDIAN_SMOOTH.defaultState
					
					val (x, y, z) = entity.posVec.addY(entity.height * 0.5)
					val w = entity.width * 0.62F
					val h = entity.height * 0.62F
					val pos = Pos(entity)
					
					repeat(35){
						MC.particleManager.addEffect(DiggingParticle(world, x + rand.nextFloat(-w, w), y + rand.nextFloat(-h, h), z + rand.nextFloat(-w, w), 0.0, 0.0, 0.0, state).setBlockPos(pos))
					}
					
					state.soundType.breakSound.playClient(entity.posVec, SoundCategory.HOSTILE)
				}
			}
		}
		
		private var animatedSpawnerPercentage = -15F
		private var targetSpawnerPercentage = remainingSpawnerPercentage.toFloat()
		
		override fun tick(entity: EntityBossEnderEye): EnderEyePhase{
			if (animatedSpawnerPercentage < targetSpawnerPercentage){
				animatedSpawnerPercentage = min(animatedSpawnerPercentage + 0.55F, targetSpawnerPercentage)
			}
			
			val currentPercentage = animatedSpawnerPercentage.floorToInt().coerceAtLeast(0)
			val currentPercentageFloat = currentPercentage / 100F
			
			entity.motionY = 0.015 - (currentPercentageFloat * 0.013)
			entity.health = 150F + min(150F, 150F * 1.06F * currentPercentageFloat)
			
			val prevDemonLevel = entity.demonLevel
			val newDemonLevel = when(currentPercentage){
				100 -> EntityBossEnderEye.DEMON_EYE_LEVEL
				in 78..99 -> 5
				in 57..77 -> 4
				in 37..56 -> 3
				in 18..36 -> 2
				in  1..17 -> 1
				else      -> 0
			}.toByte()
			
			if (newDemonLevel != prevDemonLevel){
				entity.updateDemonLevel(newDemonLevel)
				Sounds.BLOCK_ANVIL_PLACE.playServer(entity.world, entity.posVec, SoundCategory.HOSTILE, volume = 0.3F, pitch = 0.5F + (newDemonLevel * 0.15F))
				// TODO custom sound
			}
			
			if (animatedSpawnerPercentage >= targetSpawnerPercentage){
				entity.realMaxHealth = entity.health
				entity.motionY = 0.0
				PacketClientFX(FX_FINISH, FxEntityData(entity)).sendToAllAround(entity, 32.0)
				
				val world = entity.world
				val pos = Pos(entity)
				
				for(yOffset in 1..5){
					val obsidianPos = pos.down(yOffset)
					
					if (obsidianPos.isAir(world)){
						continue
					}
					
					if (obsidianPos.getBlock(world) === ModBlocks.OBSIDIAN_CHISELED_LIT){
						obsidianPos.breakBlock(world, false)
					}
					
					val ladderPos = obsidianPos.down().offset(entity.horizontalFacing, 7)
					
					if (ladderPos.getBlock(world) === Blocks.LADDER){
						ladderPos.setBlock(world, Blocks.OBSIDIAN)
					}
					
					break
				}
				
				return Staring()
			}
			
			return this
		}
		
		override fun serializeNBT() = TagCompound().apply {
			putFloat(CURRENT_TAG, animatedSpawnerPercentage)
			putFloat(TARGET_TAG, targetSpawnerPercentage)
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			animatedSpawnerPercentage = getFloat(CURRENT_TAG)
			targetSpawnerPercentage = getFloat(TARGET_TAG)
		}
	}
	
	class Staring : EnderEyePhase(){
		private var timer = 55
		private var slerpProgress = 0F
		
		private lateinit var startRot: Quaternion
		private lateinit var targetRot: Quaternion
		
		override fun tick(entity: EntityBossEnderEye): EnderEyePhase{
			val target = entity.attackTarget ?: entity.forceFindNewTarget()
			
			if (target == null || --timer < 0){
				return Ready()
			}
			
			if (!::startRot.isInitialized){
				startRot = Quaternion.fromYawPitch(entity.rotationYawHead, entity.rotationPitch)
			}
			
			if (timer < 50){
				targetRot = entity.lookPosVec.directionTowards(target.lookPosVec).let { Quaternion.fromYawPitch(it.toYaw(), it.toPitch()) }
				
				if (slerpProgress < 1F){
					slerpProgress += 0.025F
				}
				
				val next = startRot.slerp(targetRot, slerpProgress)
				entity.rotationYawHead = next.rotationYaw
				entity.rotationPitch = next.rotationPitch
			}
			
			return this
		}
	}
	
	class Ready : EnderEyePhase(){
		private val defaultAttack = Melee()
		
		var currentAttack: EnderEyeAttack = defaultAttack
		
		override fun tick(entity: EntityBossEnderEye): EnderEyePhase{
			if (entity.isSleeping || !currentAttack.tick(entity)){
				currentAttack = defaultAttack.also { it.reset(entity) }
			}
			
			return this
		}
	}
}
