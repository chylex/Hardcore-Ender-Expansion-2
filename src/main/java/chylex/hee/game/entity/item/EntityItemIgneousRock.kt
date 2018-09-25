package chylex.hee.game.entity.item
import chylex.hee.HEE
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
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.setBlock
import chylex.hee.system.util.setFireTicks
import chylex.hee.system.util.setState
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockFaceShape.SOLID
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.MoverType
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents.ENTITY_GENERIC_BURN
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.collections.Map.Entry

class EntityItemIgneousRock : EntityItemNoBob{
	companion object{
		private const val BURN_DISTANCE = 3.5
		private const val BURN_DISTANCE_SQ = BURN_DISTANCE * BURN_DISTANCE
		
		private const val ACTIVITY_DELAY_TICKS = 25
		private const val INITIAL_FIRE_UNTIL_TICKS = ACTIVITY_DELAY_TICKS + 10
		
		private const val MIN_ENTITY_BURN_DURATION_TICKS = 40
		
		private lateinit var smeltingTransformations: Map<IBlockState, IBlockState>
		
		fun setupSmeltingTransformations(){
			smeltingTransformations = FurnaceRecipes.instance().smeltingList.map(::convertToBlockStatePair).filterNotNull().toMap()
			HEE.log.info(smeltingTransformations)
		}
		
		private fun convertToBlockState(stack: ItemStack): IBlockState?{
			val item = stack.item as? ItemBlock ?: return null
			val meta = if (stack.itemDamage == 32767) 0 else stack.itemDamage // UPDATE: rewrite this too
			
			return try{
				item.block.getStateForPlacement(null, null, UP, 0F, 0F, 0F, meta, null, null) // UPDATE: rewrite once block states are flattened and sane
			}catch(e: Exception){
				HEE.log.warn("Faking getStateForPlacement to retrieve IBlockState from ItemStack caused a crash:", e) // UPDATE: currently crashes on BlockGlazedTerracotta
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
		
		if (age >= ACTIVITY_DELAY_TICKS && !world.isRemote){
			if (ticksExisted % 5 == 0){
				updateBurnNearbyEntities()
			}
			
			repeat(item.count){ // TODO maybe figure out some system to decrease the stack count after changing lots of blocks, to make big stacks lossy
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
		
		val currentPos = Pos(posX, posY, posZ)
		
		if (Pos(prevPosX, prevPosY, prevPosZ) != currentPos || ticksExisted % 25 == 0){
			if (currentPos.getMaterial(world) === Material.WATER){
				motionY = 0.2
				motionX = rand.nextFloat(-0.2F, 0.2F).toDouble()
				motionZ = rand.nextFloat(-0.2F, 0.2F).toDouble()
				super.playSound(ENTITY_GENERIC_BURN, 0.4F, rand.nextFloat(2.0F, 2.4F))
			}
		}
		
		if (isInLava){
			lifespan -= 3
			
			world.handleMaterialAcceleration(entityBoundingBox, Material.LAVA, this)
			motionX *= 0.9
			motionZ *= 0.9
		}
		
		// TODO FX
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
			return
		}
		
		super.playSound(sound, volume, pitch)
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
			// TODO FX
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
			// TODO FX
		}
	}
}
