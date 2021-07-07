package chylex.hee.game.block

import chylex.hee.game.block.util.Property
import chylex.hee.game.entity.Teleporter
import chylex.hee.game.entity.Teleporter.FxRange.Extended
import chylex.hee.game.entity.living.EntityMobAngryEnderman
import chylex.hee.game.entity.living.EntityMobEnderman
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectExistingEntities
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.particle.ParticleDeathFlowerHeal
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.Gaussian
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.potion.util.makeInstance
import chylex.hee.game.world.util.allInCenteredSphereMutable
import chylex.hee.game.world.util.blocksMovement
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.isPeaceful
import chylex.hee.game.world.util.offsetUntilExcept
import chylex.hee.game.world.util.perDimensionData
import chylex.hee.game.world.util.setBlock
import chylex.hee.game.world.util.setState
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.removeItem
import chylex.hee.util.buffer.readPos
import chylex.hee.util.buffer.use
import chylex.hee.util.buffer.writePos
import chylex.hee.util.math.center
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.potion.Effects
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.WorldSavedData
import java.util.Random
import kotlin.math.min
import kotlin.math.roundToInt

interface IBlockDeathFlowerDecaying {
	val thisAsBlock: Block
	
	val healedFlowerBlock: Block
	val witheredFlowerBlock: Block
	
	fun healDeathFlower(world: World, pos: BlockPos) {
		val state = pos.getState(world)
		val newDecayLevel = state[LEVEL] - world.rand.nextInt(1, 2)
		
		if (newDecayLevel < MIN_LEVEL) {
			pos.setBlock(world, healedFlowerBlock)
		}
		else {
			pos.setState(world, state.with(LEVEL, newDecayLevel))
		}
		
		PacketClientFX(FX_HEAL, FxHealData(pos, newDecayLevel)).sendToAllAround(world, pos, 64.0)
	}
	
	fun implOnBlockAdded(world: World, pos: BlockPos) {
		world.pendingBlockTicks.scheduleTick(pos, thisAsBlock, world.rand.nextInt(TICK_RATE / 4, TICK_RATE))
	}
	
	fun implUpdateTick(world: World, pos: BlockPos, state: BlockState, rand: Random) {
		if (pos.getBlock(world) !== thisAsBlock || world !is ServerWorld) {
			return
		}
		
		world.pendingBlockTicks.scheduleTick(pos, thisAsBlock, TICK_RATE)
		
		if (world.isPeaceful) {
			return
		}
		
		val currentDecayLevel = state[LEVEL]
		
		if (currentDecayLevel < MAX_LEVEL && rand.nextInt(45) == 0) {
			pos.setState(world, state.with(LEVEL, currentDecayLevel + 1))
		}
		else if (currentDecayLevel == MAX_LEVEL && rand.nextInt(8) == 0 && (world.dayTime % 24000L) in 15600..20400) {
			for (testPos in pos.allInCenteredSphereMutable(WITHER_FLOWER_RADIUS, avoidNipples = true)) {
				val block = testPos.getBlock(world)
				
				if (block is IBlockDeathFlowerDecaying) {
					testPos.setBlock(world, block.witheredFlowerBlock)
				}
			}
			
			if (DimensionWitherData.get(world).onWitherTriggered(world)) {
				val center = pos.center
				
				repeat(rand.nextInt(4, 5)) {
					val enderman = EntityMobAngryEnderman(world)
					val yaw = rand.nextFloat(0F, 360F)
					
					for (attempt in 1..64) {
						val testPos = pos.add(
							rand.nextInt(1, 6) * (if (rand.nextBoolean()) 1 else -1),
							rand.nextInt(1, 4),
							rand.nextInt(1, 6) * (if (rand.nextBoolean()) 1 else -1)
						).offsetUntilExcept(DOWN, 1..8) {
							it.blocksMovement(world)
						}
						
						if (testPos != null) {
							enderman.setLocationAndAngles(testPos.x + rand.nextFloat(-0.5, 0.5), testPos.y + 0.01, testPos.z + rand.nextFloat(-0.5, 0.5), yaw, 0F)
							
							if (world.hasNoCollisions(enderman, enderman.boundingBox.grow(0.2, 0.0, 0.2))) {
								world.addEntity(enderman)
								break
							}
						}
					}
				}
				
				SoundEvents.ENTITY_ENDERMAN_TELEPORT.playServer(world, center, SoundCategory.HOSTILE, volume = 1.25F)
				
				val broadcastSoundPacket = PacketClientFX(FX_WITHER, FxBlockData(pos))
				
				for (player in world.selectExistingEntities.inRange<PlayerEntity>(center, WITHER_PLAYER_RADIUS)) {
					val distanceMp = center.distanceTo(player.posVec) / WITHER_PLAYER_RADIUS
					val witherSeconds = 30 - (25 * distanceMp).roundToInt()
					
					player.addPotionEffect(Effects.WITHER.makeInstance(20 * witherSeconds))
					broadcastSoundPacket.sendToPlayer(player)
				}
			}
		}
		else if (rand.nextInt(18) == 0) {
			val center = pos.center
			val nearbyEndermen = world.selectExistingEntities.inRange<EntityMobEnderman>(center, 128.0).filter { it.attackTarget == null }.toMutableList()
			
			if (nearbyEndermen.isNotEmpty() && DimensionWitherData.get(world).onTeleportTriggered(world)) {
				repeat(min(rand.nextInt(2, 3), nearbyEndermen.size)) {
					TELEPORT.nearLocation(rand.removeItem(nearbyEndermen), rand, center, distance = (1.5)..(5.0), attempts = 64)
				}
			}
		}
	}
	
	companion object {
		const val MIN_LEVEL = 1
		const val MAX_LEVEL = 14
		
		val LEVEL = Property.int("level", MIN_LEVEL..MAX_LEVEL)
		
		private const val TICK_RATE = 1600
		
		private const val WITHER_FLOWER_RADIUS = 4
		private const val WITHER_PLAYER_RADIUS = 1024.0
		
		private val TELEPORT = Teleporter(causedInstability = 15u, effectRange = Extended(8F))
		
		private val PARTICLE_POS = Constant(0.5F, DOWN) + InBox(-0.5F, 0.5F, 0F, 0.6F, -0.5F, 0.5F)
		private val PARTICLE_MOT = Gaussian(0.02F)
		
		class FxHealData(private val pos: BlockPos, private val newLevel: Int) : IFxData {
			override fun write(buffer: PacketBuffer) = buffer.use {
				writePos(pos)
				writeByte(newLevel)
			}
		}
		
		val FX_HEAL = object : IFxHandler<FxHealData> {
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) {
				val pos = buffer.readPos()
				val newLevel = buffer.readByte()
				
				val healLevel: Float
				val particleAmount: Int
				
				if (newLevel < MIN_LEVEL) {
					healLevel = 1F
					particleAmount = 30
				}
				else {
					healLevel = 1F - ((1F + newLevel.toFloat() - MIN_LEVEL) / (1 + MAX_LEVEL - MIN_LEVEL)) // intentionally maps 13..1 to (0F)..(0.86F)
					particleAmount = 4
				}
				
				ParticleSpawnerCustom(
					type = ParticleDeathFlowerHeal,
					data = ParticleDeathFlowerHeal.Data(healLevel),
					pos = PARTICLE_POS,
					mot = PARTICLE_MOT
				).spawn(Point(pos, particleAmount), rand)
			}
		}
		
		val FX_WITHER = object : FxBlockHandler() {
			override fun handle(pos: BlockPos, world: World, rand: Random) {
				repeat(2) {
					ModSounds.BLOCK_DEATH_FLOWER_WITHER.playClient(pos, SoundCategory.BLOCKS)
				}
			}
		}
	}
	
	class DimensionWitherData private constructor() : WorldSavedData(NAME) {
		companion object {
			fun get(world: ServerWorld) = world.perDimensionData(NAME, ::DimensionWitherData)
			
			private const val NAME = "HEE_DEATH_FLOWER_WITHER"
			
			private const val LAST_TELEPORT_TIME_TAG = "LastTeleportTime"
			private const val LAST_WITHER_DAY_TAG = "LastWitherDay"
		}
		
		private var lastTeleportTime = -24000L
		private var lastWitherDay = -1L
		
		fun onTeleportTriggered(world: World): Boolean {
			val currentTime = world.dayTime
			
			if (currentTime < lastTeleportTime + 21600L) {
				return false
			}
			
			lastTeleportTime = currentTime
			markDirty()
			return true
		}
		
		fun onWitherTriggered(world: World): Boolean {
			val currentDay = world.dayTime / 24000L
			
			if (currentDay == lastWitherDay) {
				return false
			}
			
			lastWitherDay = currentDay
			markDirty()
			return true
		}
		
		override fun write(nbt: TagCompound) = nbt.apply {
			putLong(LAST_TELEPORT_TIME_TAG, lastTeleportTime)
			putLong(LAST_WITHER_DAY_TAG, lastWitherDay)
		}
		
		override fun read(nbt: TagCompound) = nbt.use {
			lastTeleportTime = getLong(LAST_TELEPORT_TIME_TAG)
			lastWitherDay = getLong(LAST_WITHER_DAY_TAG)
		}
	}
}
