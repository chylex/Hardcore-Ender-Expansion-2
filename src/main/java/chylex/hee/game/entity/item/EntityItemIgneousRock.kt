package chylex.hee.game.entity.item
import chylex.hee.HEE
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.fx.FxEntityData
import chylex.hee.game.fx.FxEntityHandler
import chylex.hee.game.particle.ParticleFlameCustom
import chylex.hee.game.particle.ParticleFlameCustom.Data
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.util.Pos
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getFaceShape
import chylex.hee.system.util.getMaterial
import chylex.hee.system.util.getState
import chylex.hee.system.util.isAir
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextBiasedFloat
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextVector
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.setBlock
import chylex.hee.system.util.setFireTicks
import chylex.hee.system.util.setState
import chylex.hee.system.util.size
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockFaceShape.SOLID
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.MoverType
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.init.SoundEvents.ENTITY_GENERIC_BURN
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumParticleTypes.LAVA
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.Random
import kotlin.collections.Map.Entry
import kotlin.math.log2

class EntityItemIgneousRock : EntityItemNoBob{
	companion object{
		private const val BURN_DISTANCE = 3.5
		private const val BURN_DISTANCE_SQ = BURN_DISTANCE * BURN_DISTANCE
		
		private const val ACTIVITY_DELAY_TICKS = 25
		private const val INITIAL_FIRE_UNTIL_TICKS = ACTIVITY_DELAY_TICKS + 10
		
		private const val MIN_ENTITY_BURN_DURATION_TICKS = 40
		
		private val PARTICLE_TICK = ParticleSpawnerVanilla(
			type = LAVA,
			pos = InBox(0.1F)
		)
		
		private val PARTICLE_FLAME_MOT = Constant(0.008F, UP) + InBox(0.012F, 0.014F, 0.012F)
		
		private val PARTICLE_SMELT = ParticleSpawnerCustom(
			type = ParticleFlameCustom,
			data = Data(maxAge = 16),
			pos = InBox(0.85F),
			mot = PARTICLE_FLAME_MOT
		)
		
		private fun PARTICLE_BURN(target: Entity) = ParticleSpawnerCustom(
			type = ParticleFlameCustom,
			data = Data(maxAge = 8),
			pos = Constant(0.2F, UP) + InBox(target, 0.4F),
			mot = PARTICLE_FLAME_MOT
		)
		
		@JvmStatic
		val FX_BLOCK_SMELT = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				PARTICLE_SMELT.spawn(Point(pos, 20), rand)
				SoundEvents.ENTITY_GENERIC_BURN.playClient(pos, SoundCategory.NEUTRAL, volume = 0.3F, pitch = rand.nextFloat(1F, 2F))
			}
		}
		
		@JvmStatic
		val FX_ENTITY_BURN = object : FxEntityHandler(){
			override fun handle(entity: Entity, rand: Random){
				PARTICLE_BURN(entity).spawn(Point(entity, 0.5F, 24), rand)
				SoundEvents.ENTITY_GENERIC_BURN.playClient(entity.posVec, SoundCategory.NEUTRAL, volume = 0.3F, pitch = rand.nextFloat(1F, 2F))
			}
		}
		
		private lateinit var smeltingTransformations: Map<IBlockState, IBlockState>
		
		fun setupSmeltingTransformations(){
			smeltingTransformations = FurnaceRecipes.instance().smeltingList.map(::convertToBlockStatePair).filterNotNull().toMap()
			
			for((from, to) in smeltingTransformations){
				HEE.log.info("[EntityItemIgneousRock] found smelting transformation: $from -> $to")
			}
		}
		
		private fun convertToBlockState(stack: ItemStack): IBlockState?{
			val item = stack.item as? ItemBlock ?: return null
			val meta = if (stack.itemDamage == 32767) 0 else stack.itemDamage // UPDATE: rewrite this too
			
			return try{
				item.block.getStateForPlacement(null, null, UP, 0F, 0F, 0F, meta, null, null) // UPDATE: rewrite once block states are flattened and sane
			}catch(e: Exception){
				HEE.log.warn("[EntityItemIgneousRock] faking getStateForPlacement to retrieve IBlockState from ItemStack caused a crash: ${e.stackTrace[0]}") // UPDATE: currently crashes on BlockGlazedTerracotta
				null
			}
		}
		
		private fun convertToBlockStatePair(entry: Entry<ItemStack, ItemStack>): Pair<IBlockState, IBlockState>?{
			return (convertToBlockState(entry.key) ?: return null) to (convertToBlockState(entry.value) ?: return null)
		}
	}
	
	@Suppress("unused")
	constructor(world: World) : super(world)
	
	constructor(world: World, stack: ItemStack, replacee: Entity) : super(world, stack, replacee){
		lifespan = 1200 + (world.rand.nextBiasedFloat(3F) * 1200F).floorToInt()
	}
	
	private var prevMotionVec = Vec3d.ZERO
	
	init{
		isImmuneToFire = true
	}
	
	override fun onUpdate(){
		prevMotionVec = motionVec
		super.onUpdate()
		
		if (!world.isRemote && age >= ACTIVITY_DELAY_TICKS){
			if (ticksExisted % 5 == 0){
				updateBurnNearbyEntities()
			}
			
			repeat(item.size){ // TODO maybe figure out some system to decrease the stack count after changing lots of blocks, to make big stacks lossy
				if (rand.nextInt(6) == 0 || age < INITIAL_FIRE_UNTIL_TICKS){
					val checkRange = (BURN_DISTANCE * 2).ceilToInt()
					
					val randomTopBlock = getRandomBlock().let {
						if (it.isAir(world))
							it.offsetUntil(DOWN, 1..checkRange){ !it.isAir(world) || it.distanceSqTo(this) > BURN_DISTANCE_SQ }?.up()
						else
							it.offsetUntil(UP, 0..checkRange){ it.isAir(world) || it.distanceSqTo(this) > BURN_DISTANCE_SQ }
					}
					
					if (randomTopBlock != null && randomTopBlock.isAir(world) && randomTopBlock.down().getFaceShape(world, UP) == SOLID){
						randomTopBlock.setBlock(world, Blocks.FIRE)
					}
				}
				else if (rand.nextBoolean()){
					getRandomBlock().takeUnless { it.isAir(world) }?.let(::updateBurnBlock)
				}
			}
		}
		
		val currentPos = Pos(this)
		
		if (currentPos != Pos(prevPosX, prevPosY, prevPosZ) || ticksExisted % 25 == 0){
			if (currentPos.getMaterial(world) === Material.WATER){
				motionY = 0.2
				motionX = rand.nextFloat(-0.2, 0.2)
				motionZ = rand.nextFloat(-0.2, 0.2)
				// spawns bubble particles via Entity.doWaterSplashEffect
				// plays hissing sound (ENTITY_GENERIC_EXTINGUISH_FIRE) via Entity.move, as the entity is on fire
			}
		}
		
		if (isInLava){
			lifespan -= 3
			
			world.handleMaterialAcceleration(entityBoundingBox, Material.LAVA, this)
			motionX *= 0.9
			motionZ *= 0.9
		}
		
		if (world.isRemote){
			val stackSize = item.size.toFloat()
			val particleChance = if (stackSize < 1F) 0F else 0.13F + (stackSize / 110F) + (log2(stackSize) / 18F)
			
			if (rand.nextFloat() < particleChance){
				PARTICLE_TICK.spawn(Point(this, heightMp = 0.5F, amount = 1), rand)
			}
		}
	}
	
	override fun move(type: MoverType, x: Double, y: Double, z: Double){
		if (isInLava){
			super.move(type, x * 0.2, y * 0.01, z * 0.2)
		}
		else{
			super.move(type, x, y, z)
		}
	}
	
	override fun playSound(sound: SoundEvent, volume: Float, pitch: Float){
		if (sound === ENTITY_GENERIC_BURN && volume == 0.4F && pitch >= 2.0F){ // UPDATE: find a better way, all item handling has changed anyway
			motionVec = prevMotionVec // this disables vanilla lava handling, but also breaks hasNoGravity
		}
		else{
			super.playSound(sound, volume, pitch)
		}
	}
	
	override fun isEntityInvulnerable(source: DamageSource): Boolean{
		return super.isEntityInvulnerable(source) || source.isFireDamage
	}
	
	override fun isBurning(): Boolean = true
	
	// In-world behavior
	
	private fun getRandomBlock(): BlockPos{
		val randomOffset = rand.nextVector(rand.nextBiasedFloat(6F) * BURN_DISTANCE)
		return Pos(posVec.add(randomOffset))
	}
	
	private fun updateBurnNearbyEntities(){
		val pos = posVec
		
		for(entity in world.selectVulnerableEntities.allInRange(pos, BURN_DISTANCE).filter { !it.isImmuneToFire && it.fire < MIN_ENTITY_BURN_DURATION_TICKS }){
			val distanceMp = 1F - (pos.squareDistanceTo(entity.posVec) / BURN_DISTANCE_SQ)
			val extraDuration = (distanceMp * 40F) + rand.nextInt(20)
			
			entity.setFireTicks(MIN_ENTITY_BURN_DURATION_TICKS + extraDuration.floorToInt()) // about 2-5 seconds
			PacketClientFX(FX_ENTITY_BURN, FxEntityData(entity)).sendToAllAround(this, 32.0)
		}
	}
	
	private fun updateBurnBlock(pos: BlockPos){
		val sourceState = pos.getState(world)
		var targetState: IBlockState?
		
		// primary transformations
		
		targetState = smeltingTransformations[sourceState]
		
		// secondary transformations
		
		if (targetState == null){
			// TODO 1.13
		}
		
		// ternary transformations
		
		if (targetState == null && rand.nextInt(5) == 0){
			// TODO 1.13
		}
		
		// final handling
		
		if (targetState != null){
			pos.setState(world, targetState)
			PacketClientFX(FX_BLOCK_SMELT, FxBlockData(pos)).sendToAllAround(this, 32.0)
		}
	}
}
