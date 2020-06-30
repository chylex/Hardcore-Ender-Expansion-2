package chylex.hee.game.block
import chylex.hee.game.block.util.Property
import chylex.hee.game.entity.living.EntityMobAngryEnderman
import chylex.hee.game.entity.living.EntityMobEnderman
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.particle.ParticleDeathFlowerHeal
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.Gaussian
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.util.Teleporter
import chylex.hee.game.world.util.Teleporter.FxRange.Extended
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.allInCenteredSphereMutable
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.center
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.isPeaceful
import chylex.hee.system.util.makeEffect
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.perDimensionData
import chylex.hee.system.util.playClient
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readPos
import chylex.hee.system.util.removeItem
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.setBlock
import chylex.hee.system.util.setState
import chylex.hee.system.util.use
import chylex.hee.system.util.writePos
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.network.PacketBuffer
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.WorldSavedData
import java.util.Random
import kotlin.math.min
import kotlin.math.roundToInt

interface IBlockDeathFlowerDecaying{
	val thisAsBlock: Block
	
	val healedFlowerBlock: Block
	val witheredFlowerBlock: Block
	
	@JvmDefault
	fun healDeathFlower(world: World, pos: BlockPos){
		val state = pos.getState(world)
		val newDecayLevel = state[LEVEL] - world.rand.nextInt(1, 2)
		
		if (newDecayLevel < MIN_LEVEL){
			pos.setBlock(world, healedFlowerBlock)
		}
		else{
			pos.setState(world, state.with(LEVEL, newDecayLevel))
		}
		
		PacketClientFX(FX_HEAL, FxHealData(pos, newDecayLevel)).sendToAllAround(world, pos, 64.0)
	}
	
	@JvmDefault
	fun implTickRate(): Int{
		return 1600
	}
	
	@JvmDefault
	fun implOnBlockAdded(world: World, pos: BlockPos){
		val tickRate = implTickRate()
		world.pendingBlockTicks.scheduleTick(pos, thisAsBlock, world.rand.nextInt(tickRate / 4, tickRate))
	}
	
	@JvmDefault
	fun implUpdateTick(world: World, pos: BlockPos, state: BlockState, rand: Random){
		if (pos.getBlock(world) !== thisAsBlock || world !is ServerWorld){
			return
		}
		
		world.pendingBlockTicks.scheduleTick(pos, thisAsBlock, implTickRate())
		
		if (world.isPeaceful){
			return
		}
		
		val currentDecayLevel = state[LEVEL]
		
		if (currentDecayLevel < MAX_LEVEL && rand.nextInt(45) == 0){
			pos.setState(world, state.with(LEVEL, currentDecayLevel + 1))
		}
		else if (currentDecayLevel == MAX_LEVEL && rand.nextInt(8) == 0 && (world.dayTime % 24000L) in 15600..20400){
			for(testPos in pos.allInCenteredSphereMutable(WITHER_FLOWER_RADIUS, avoidNipples = true)){
				val block = testPos.getBlock(world)
				
				if (block is IBlockDeathFlowerDecaying){
					testPos.setBlock(world, block.witheredFlowerBlock)
				}
			}
			
			if (DimensionWitherData.get(world).onWitherTriggered(world)){
				val center = pos.center
				
				repeat(rand.nextInt(4, 5)){
					val enderman = EntityMobAngryEnderman(world)
					val yaw = rand.nextFloat(0F, 360F)
					
					for(attempt in 1..64){
						val testPos = pos.add(
							rand.nextInt(1, 6) * (if (rand.nextBoolean()) 1 else -1),
							rand.nextInt(1, 4),
							rand.nextInt(1, 6) * (if (rand.nextBoolean()) 1 else -1)
						).offsetUntil(DOWN, 1..8){
							it.blocksMovement(world)
						}?.up()
						
						if (testPos != null){
							enderman.setLocationAndAngles(testPos.x + rand.nextFloat(-0.5, 0.5), testPos.y + 0.01, testPos.z + rand.nextFloat(-0.5, 0.5), yaw, 0F)
							
							if (world.hasNoCollisions(enderman, enderman.boundingBox.grow(0.2, 0.0, 0.2))){
								world.addEntity(enderman)
								break
							}
						}
					}
				}
				
				Sounds.ENTITY_ENDERMAN_TELEPORT.playServer(world, center, SoundCategory.HOSTILE, volume = 1.25F)
				
				val broadcastSoundPacket = PacketClientFX(FX_WITHER, FxBlockData(pos))
				
				for(player in world.selectExistingEntities.inRange<EntityPlayer>(center, WITHER_PLAYER_RADIUS)){
					val distanceMp = center.distanceTo(player.posVec) / WITHER_PLAYER_RADIUS
					val witherSeconds = 30 - (25 * distanceMp).roundToInt()
					
					player.addPotionEffect(Potions.WITHER.makeEffect(20 * witherSeconds))
					broadcastSoundPacket.sendToPlayer(player)
				}
			}
		}
		else if (rand.nextInt(18) == 0){
			val center = pos.center
			val nearbyEndermen = world.selectExistingEntities.inRange<EntityMobEnderman>(center, 128.0).filter { it.attackTarget == null }.toMutableList()
			
			if (nearbyEndermen.isNotEmpty() && DimensionWitherData.get(world).onTeleportTriggered(world)){
				repeat(min(rand.nextInt(2, 3), nearbyEndermen.size)){
					TELEPORT.nearLocation(rand.removeItem(nearbyEndermen), rand, center, distance = (1.5)..(5.0), attempts = 64)
				}
			}
		}
	}
	
	companion object{
		const val MIN_LEVEL = 1
		const val MAX_LEVEL = 14
		
		val LEVEL = Property.int("level", MIN_LEVEL..MAX_LEVEL)
		
		private const val WITHER_FLOWER_RADIUS = 4
		private const val WITHER_PLAYER_RADIUS = 1024.0
		
		private val TELEPORT = Teleporter(causedInstability = 15u, effectRange = Extended(8F))
		
		private val PARTICLE_POS = Constant(0.5F, DOWN) + InBox(-0.5F, 0.5F, 0F, 0.6F, -0.5F, 0.5F)
		private val PARTICLE_MOT = Gaussian(0.02F)
		
		class FxHealData(private val pos: BlockPos, private val newLevel: Int) : IFxData{
			override fun write(buffer: PacketBuffer) = buffer.use {
				writePos(pos)
				writeByte(newLevel)
			}
		}
		
		val FX_HEAL = object : IFxHandler<FxHealData>{
			override fun handle(buffer: PacketBuffer, world: World, rand: Random){
				val pos = buffer.readPos()
				val newLevel = buffer.readByte()
				
				val healLevel: Float
				val particleAmount: Int
				
				if (newLevel < MIN_LEVEL){
					healLevel = 1F
					particleAmount = 30
				}
				else{
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
		
		val FX_WITHER = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				repeat(2){
					ModSounds.BLOCK_DEATH_FLOWER_WITHER.playClient(pos, SoundCategory.BLOCKS)
				}
			}
		}
	}
	
	class DimensionWitherData private constructor() : WorldSavedData(NAME){
		companion object{
			fun get(world: ServerWorld) = world.perDimensionData(NAME, ::DimensionWitherData)
			
			private const val NAME = "HEE_DEATH_FLOWER_WITHER"
			
			private const val LAST_TELEPORT_TIME_TAG = "LastTeleportTime"
			private const val LAST_WITHER_DAY_TAG = "LastWitherDay"
		}
		
		private var lastTeleportTime = -24000L
		private var lastWitherDay = -1L
		
		fun onTeleportTriggered(world: World): Boolean{
			val currentTime = world.dayTime
			
			if (currentTime < lastTeleportTime + 21600L){
				return false
			}
			
			lastTeleportTime = currentTime
			markDirty()
			return true
		}
		
		fun onWitherTriggered(world: World): Boolean{
			val currentDay = world.dayTime / 24000L
			
			if (currentDay == lastWitherDay){
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
