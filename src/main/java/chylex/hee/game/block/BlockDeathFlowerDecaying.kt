package chylex.hee.game.block
import chylex.hee.game.block.util.Property
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.particle.ParticleDeathFlowerHeal
import chylex.hee.game.particle.ParticleDeathFlowerHeal.Data
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.Gaussian
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.util.Teleporter
import chylex.hee.game.world.util.Teleporter.FxRange.Extended
import chylex.hee.init.ModBlocks
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.util.allInCenteredBox
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.center
import chylex.hee.system.util.distanceTo
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.perDimensionData
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readPos
import chylex.hee.system.util.removeItem
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.setBlock
import chylex.hee.system.util.setState
import chylex.hee.system.util.use
import chylex.hee.system.util.with
import chylex.hee.system.util.writePos
import io.netty.buffer.ByteBuf
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects.WITHER
import net.minecraft.init.SoundEvents
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.potion.PotionEffect
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.EnumDifficulty.PEACEFUL
import net.minecraft.world.World
import net.minecraft.world.storage.WorldSavedData
import java.util.Random
import kotlin.math.min
import kotlin.math.roundToInt

class BlockDeathFlowerDecaying : BlockEndPlant(){
	companion object{
		const val MIN_LEVEL = 1
		const val MAX_LEVEL = 14
		
		val LEVEL = Property.int("level", MIN_LEVEL..MAX_LEVEL)
		
		private const val WITHER_FLOWER_RADIUS = 4
		private const val WITHER_FLOWER_RADIUS_SQ = WITHER_FLOWER_RADIUS * WITHER_FLOWER_RADIUS
		
		private const val WITHER_PLAYER_RADIUS = 1024.0
		
		private val TELEPORT = Teleporter(causedInstability = 15u, effectRange = Extended(8F))
		
		private val PARTICLE_POS = Constant(0.5F, DOWN) + InBox(-0.5F, 0.5F, 0F, BUSH_AABB.maxY.toFloat(), -0.5F, 0.5F)
		private val PARTICLE_MOT = Gaussian(0.02F)
		
		class FxHealData(private val pos: BlockPos, private val newLevel: Int) : IFxData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writePos(pos)
				writeByte(newLevel)
			}
		}
		
		@JvmStatic
		val FX_HEAL = object : IFxHandler<FxHealData>{
			override fun handle(buffer: ByteBuf, world: World, rand: Random){
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
					data = Data(healLevel),
					pos = PARTICLE_POS,
					mot = PARTICLE_MOT
				).spawn(Point(pos, particleAmount), rand)
			}
		}
	}
	
	class DimensionWitherData(name: String) : WorldSavedData(name){ // must be public for reflection
		companion object{
			fun get(world: World) = world.perDimensionData("HEE_DEATH_FLOWER_WITHER", ::DimensionWitherData)
		}
		
		private var lastTeleportTime = -24000L
		private var lastWitherDay = -1L
		
		fun onTeleportTriggered(world: World): Boolean{
			val currentTime = world.worldTime
			
			if (currentTime < lastTeleportTime + 21600L){
				return false
			}
			
			lastTeleportTime = currentTime
			markDirty()
			return true
		}
		
		fun onWitherTriggered(world: World): Boolean{
			val currentDay = world.worldTime / 24000L
			
			if (currentDay == lastWitherDay){
				return false
			}
			
			lastWitherDay = currentDay
			markDirty()
			return true
		}
		
		override fun writeToNBT(nbt: NBTTagCompound) = nbt.apply {
			setLong("LastTeleportTime", lastTeleportTime)
			setLong("LastWitherDay", lastWitherDay)
		}
		
		override fun readFromNBT(nbt: NBTTagCompound) = with(nbt){
			lastTeleportTime = getLong("LastTeleportTime")
			lastWitherDay = getLong("LastWitherDay")
		}
	}
	
	override fun createBlockState() = BlockStateContainer(this, LEVEL)
	
	// Healing
	
	fun healDeathFlower(world: World, pos: BlockPos){
		val state = pos.getState(world)
		val newDecayLevel = state[LEVEL] - world.rand.nextInt(1, 2)
		
		if (newDecayLevel < MIN_LEVEL){
			pos.setBlock(world, ModBlocks.DEATH_FLOWER_HEALED)
		}
		else{
			pos.setState(world, state.with(LEVEL, newDecayLevel))
		}
		
		PacketClientFX(FX_HEAL, FxHealData(pos, newDecayLevel)).sendToAllAround(world, pos, 64.0)
	}
	
	// Behavior
	
	override fun tickRate(world: World): Int{
		return 1600
	}
	
	override fun onBlockAdded(world: World, pos: BlockPos, state: IBlockState){
		super.onBlockAdded(world, pos, state)
		
		val tickRate = tickRate(world)
		world.scheduleUpdate(pos, this, world.rand.nextInt(tickRate / 4, tickRate))
	}
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		super.updateTick(world, pos, state, rand)
		
		if (pos.getBlock(world) !== this){
			return
		}
		
		world.scheduleUpdate(pos, this, tickRate(world))
		
		if (world.difficulty == PEACEFUL){
			return
		}
		
		val currentDecayLevel = state[LEVEL]
		
		if (currentDecayLevel < MAX_LEVEL && rand.nextInt(45) == 0){
			pos.setState(world, state.with(LEVEL, currentDecayLevel + 1))
		}
		else if (currentDecayLevel == MAX_LEVEL && rand.nextInt(8) == 0 && (world.worldTime % 24000L) in 15600..20400){
			for(testPos in pos.allInCenteredBox(WITHER_FLOWER_RADIUS, WITHER_FLOWER_RADIUS, WITHER_FLOWER_RADIUS)){
				if (testPos.distanceTo(pos) <= WITHER_FLOWER_RADIUS_SQ && testPos.getBlock(world) === this){
					testPos.setBlock(world, ModBlocks.DEATH_FLOWER_WITHERED)
				}
			}
			
			if (DimensionWitherData.get(world).onWitherTriggered(world)){
				val center = pos.center
				
				repeat(rand.nextInt(4, 5)){
					val enderman = EntityEnderman(world) // TODO use angry endermen once implemented
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
							
							if (world.getCollisionBoxes(enderman, enderman.entityBoundingBox.grow(0.2, 0.0, 0.2)).isEmpty()){
								world.spawnEntity(enderman)
								break
							}
						}
					}
				}
				
				SoundEvents.ENTITY_ENDERMEN_TELEPORT.playServer(world, center, SoundCategory.HOSTILE, volume = 1.25F)
				
				for(player in world.selectExistingEntities.inRange<EntityPlayer>(center, WITHER_PLAYER_RADIUS)){
					val distanceMp = center.distanceTo(player.posVec) / WITHER_PLAYER_RADIUS
					val witherSeconds = 30 - (25 * distanceMp).roundToInt()
					
					player.addPotionEffect(PotionEffect(WITHER, 20 * witherSeconds, 0))
					// TODO broadcast
				}
			}
		}
		else if (rand.nextInt(18) == 0){ // TODO use EntityMobEnderman once implemented?
			val center = pos.center
			val nearbyEndermen = world.selectExistingEntities.inRange<EntityEnderman>(center, 128.0).filter { it.attackTarget == null }.toMutableList()
			
			if (nearbyEndermen.any() && DimensionWitherData.get(world).onTeleportTriggered(world)){
				repeat(min(rand.nextInt(2, 3), nearbyEndermen.size)){
					TELEPORT.nearLocation(rand.removeItem(nearbyEndermen), rand, center, distance = (1.5)..(5.0), attempts = 64)
				}
			}
		}
	}
	
	// General
	
	override fun getMetaFromState(state: IBlockState) = state[LEVEL] - MIN_LEVEL
	override fun getStateFromMeta(meta: Int) = this.with(LEVEL, meta + MIN_LEVEL)
	
	override fun damageDropped(state: IBlockState) = state[LEVEL] - MIN_LEVEL
}
