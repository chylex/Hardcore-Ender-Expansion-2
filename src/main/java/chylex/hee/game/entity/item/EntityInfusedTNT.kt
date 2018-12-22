package chylex.hee.game.entity.item
import chylex.hee.game.block.BlockSimple.Builder.Companion.INDESTRUCTIBLE_HARDNESS
import chylex.hee.game.item.infusion.Infusion.FIRE
import chylex.hee.game.item.infusion.Infusion.HARMLESS
import chylex.hee.game.item.infusion.Infusion.MINING
import chylex.hee.game.item.infusion.Infusion.PHASING
import chylex.hee.game.item.infusion.Infusion.POWER
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.util.ExplosionBuilder
import chylex.hee.system.util.allInCenteredBoxMutable
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getMaterial
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.nextRounded
import chylex.hee.system.util.remapRange
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.MoverType
import net.minecraft.entity.MoverType.SELF
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityTNTPrimed
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Explosion
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootTableList

class EntityInfusedTNT : EntityTNTPrimed{
	private companion object{
		private const val WATER_CHECK_RADIUS = 4
		private const val WATER_CHECK_RADIUS_SQ = WATER_CHECK_RADIUS * WATER_CHECK_RADIUS
		
		private const val PHASING_INSTANT_FUSE_TICKS = 3
		
		private val PARTICLE_TICK = ParticleSpawnerVanilla(SMOKE_NORMAL)
		
		// EntityItem construction
		
		private val constructRawItem: (World, Double, Double, Double, ItemStack) -> EntityItem = ::EntityItem
		
		private val constructCookedItem: (World, Double, Double, Double, ItemStack) -> EntityItem = { world, x, y, z, stack ->
			FurnaceRecipes.instance().getSmeltingResult(stack).let {
				if (it.isEmpty)
					constructRawItem(world, x, y, z, stack)
				else
					EntityItemFreshlyCooked(world, x, y, z, it)
			}
		}
	}
	
	@Suppress("unused")
	constructor(world: World) : super(world)
	
	@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") // UPDATE
	constructor(world: World, pos: BlockPos, infusions: InfusionList, igniter: EntityLivingBase?) : super(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, igniter){
		loadInfusions(infusions)
	}
	
	constructor(world: World, pos: BlockPos, infusions: InfusionList, explosion: Explosion) : this(world, pos, infusions, explosion.explosivePlacedBy){
		fuse = rand.nextInt(fuse / 4) + (fuse / 8)
	}
	
	private var infusions = InfusionList.EMPTY
	private var hasPhasedIntoWall = false
	
	private fun loadInfusions(infusions: InfusionList){
		this.infusions = infusions
		this.noClip = infusions.has(PHASING)
	}
	
	override fun onUpdate(){
		prevPosX = posX
		prevPosY = posY
		prevPosZ = posZ
		
		if (!hasNoGravity()){
			motionY -= 0.04
		}
		
		move(SELF, motionX, motionY, motionZ)
		motionX *= 0.98
		motionY *= 0.98
		motionZ *= 0.98
		
		if (onGround){
			motionX *= 0.7
			motionZ *= 0.7
			motionY *= -0.5
		}
		
		if (--fuse <= 0){
			setDead()
			blowUp()
		}
		else{
			handleWaterMovement()
			PARTICLE_TICK.spawn(Point(posX, posY + 0.5, posZ, 1), rand)
		}
	}
	
	// Phasing
	
	override fun move(type: MoverType, x: Double, y: Double, z: Double){
		if (noClip && type == SELF){
			collideWithIndestructibleBlocks()
			
			if (world.collidesWithAnyBlock(entityBoundingBox)){
				hasPhasedIntoWall = true
			}
			else if (hasPhasedIntoWall && fuse > PHASING_INSTANT_FUSE_TICKS){
				fuse = PHASING_INSTANT_FUSE_TICKS
			}
		}
		else{
			super.move(type, x, y, z)
		}
	}
	
	private fun collideWithIndestructibleBlocks(){
		val (prevMotX, prevMotY, prevMotZ) = motionVec
		
		val collisionBoxes = getCollisionBoxesForIndestructibleBlocks(entityBoundingBox.expand(motionX, motionY, motionZ))
		
		val newMotY = moveWithCollisionCheck(collisionBoxes, motionY, AxisAlignedBB::calculateYOffset){ offset(0.0, it, 0.0) }
		val newMotX = moveWithCollisionCheck(collisionBoxes, motionX, AxisAlignedBB::calculateXOffset){ offset(it, 0.0, 0.0) }
		val newMotZ = moveWithCollisionCheck(collisionBoxes, motionZ, AxisAlignedBB::calculateZOffset){ offset(0.0, 0.0, it) }
		
		val changedMotX = newMotX != prevMotX
		val changedMotY = newMotY != prevMotY
		val changedMotZ = newMotZ != prevMotZ
		
		resetPositionToBB()
		
		collidedHorizontally = changedMotX || changedMotZ
		collidedVertically = changedMotY
		
		onGround = collidedVertically && motionY < 0.0
		collided = collidedHorizontally || collidedVertically
		
		if (changedMotX){
			motionX = 0.0
		}
		
		if (changedMotZ){
			motionZ = 0.0
		}
		
		if (changedMotY){
			motionY = 0.0
		}
	}
	
	private fun getCollisionBoxesForIndestructibleBlocks(box: AxisAlignedBB): List<AxisAlignedBB>{
		val minX = box.minX.floorToInt() - 1
		val minY = box.minY.floorToInt() - 1
		val minZ = box.minZ.floorToInt() - 1
		
		val maxX = box.maxX.ceilToInt()
		val maxY = box.maxY.ceilToInt()
		val maxZ = box.maxZ.ceilToInt()
		
		val collisionBoxes = mutableListOf<AxisAlignedBB>()
		val testPos = BlockPos.PooledMutableBlockPos.retain()
		
		try{
			for(pX in minX..maxX){
				for(pZ in minZ..maxZ){
					val isEdgeX = pX == minX || pX == maxX
					val isEdgeZ = pZ == minZ || pZ == maxZ
					
					if ((!isEdgeX || !isEdgeZ) && world.isBlockLoaded(testPos.setPos(pX, 64, pZ))){
						for(pY in minY..maxY){
							if ((!isEdgeX && !isEdgeZ) || pY != maxY){
								val state = testPos.setPos(pX, pY, pZ).getState(world)
								
								if (state.getBlockHardness(world, testPos) == INDESTRUCTIBLE_HARDNESS){
									state.addCollisionBoxToList(world, testPos, box, collisionBoxes, this, false)
								}
							}
						}
					}
				}
			}
		}finally{
			testPos.release()
		}
		
		return collisionBoxes
	}
	
	private fun moveWithCollisionCheck(collisionBoxes: List<AxisAlignedBB>, initialMotion: Double, calculateFunc: AxisAlignedBB.(AxisAlignedBB, Double) -> Double, offsetFunc: AxisAlignedBB.(Double) -> AxisAlignedBB): Double{
		if (initialMotion == 0.0){
			return initialMotion
		}
		
		val boundingBox = entityBoundingBox
		val finalOffset = collisionBoxes.fold(initialMotion){ acc, box -> box.calculateFunc(boundingBox, acc) }
		
		if (finalOffset != 0.0){
			entityBoundingBox = boundingBox.offsetFunc(finalOffset)
		}
		
		return finalOffset
	}
	
	// Explosion handling
	
	private fun blowUp(){
		if (world.isRemote){
			return
		}
		
		val strength = if (infusions.has(POWER)) 6F else 4F
		
		val isFiery = infusions.has(FIRE)
		val isHarmless = infusions.has(HARMLESS)
		val isMining = infusions.has(MINING)
		
		val dropRateMultiplier: Float
		val dropFortune: Int
		
		if (isMining){
			dropRateMultiplier = 3F
			dropFortune = 1
		}
		else{
			dropRateMultiplier = 1F
			dropFortune = 0
		}
		
		with(ExplosionBuilder()){
			this.destroyBlocks = !isHarmless
			this.damageEntities = !isHarmless
			
			this.spawnFire = isFiery
			this.blockDropRate = dropRateMultiplier / strength
			this.blockDropFortune = dropFortune
			
			trigger(world, this@EntityInfusedTNT, posX, posY + (height / 16.0), posZ, strength)
		}
		
		if (isMining && isInWater){
			performFishing(isFiery)
		}
	}
	
	private fun performFishing(cook: Boolean){
		var totalCountedBlocks = 0
		val foundWaterBlocks = mutableListOf<BlockPos>()
		
		for(pos in position.allInCenteredBoxMutable(WATER_CHECK_RADIUS, WATER_CHECK_RADIUS, WATER_CHECK_RADIUS)){
			if (pos.distanceSqTo(this) <= WATER_CHECK_RADIUS_SQ){
				++totalCountedBlocks
				
				if (pos.getMaterial(world) === Material.WATER){
					foundWaterBlocks.add(pos.toImmutable())
				}
			}
		}
		
		if (foundWaterBlocks.isNotEmpty()){
			val waterRatio = foundWaterBlocks.size.toFloat() / totalCountedBlocks
			
			val dropAmount = when{
				waterRatio < 0.1 -> remapRange(waterRatio, (0.0F)..(0.1F), (1.0F)..(1.6F))
				waterRatio < 0.4 -> remapRange(waterRatio, (0.1F)..(0.4F), (1.6F)..(4.0F))
				else             -> remapRange(waterRatio, (0.4F)..(1.0F), (4.0F)..(5.8F))
			}
			
			val lootContext = LootContext.Builder(world as WorldServer).build()
			val lootTable = world.lootTableManager.getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING)
			
			val constructItemEntity = if (cook) constructCookedItem else constructRawItem
			
			repeat(rand.nextRounded(dropAmount)){
				for(droppedItem in lootTable.generateLootForPools(rand, lootContext)){ // there's ItemFishedEvent but it needs the hook entity...
					val dropPos = rand.nextItem(foundWaterBlocks)!!
					
					constructItemEntity(world, dropPos.x + rand.nextFloat(0.25, 0.75), dropPos.y + rand.nextFloat(0.25, 0.75), dropPos.z + rand.nextFloat(0.25, 0.75), droppedItem).apply {
						motionX = rand.nextFloat(-0.25, 0.25)
						motionZ = rand.nextFloat(-0.25, 0.25)
						motionY = rand.nextFloat(1.0, 1.2) // UPDATE: 1.13 makes items float upwards, review this
						
						world.spawnEntity(this)
					}
				}
			}
		}
	}
	
	// Serialization
	
	override fun writeEntityToNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		super.writeEntityToNBT(nbt)
		
		InfusionTag.setList(this, infusions)
		setBoolean("HasPhased", hasPhasedIntoWall)
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		super.readEntityFromNBT(nbt)
		
		loadInfusions(InfusionTag.getList(this))
		hasPhasedIntoWall = getBoolean("HasPhased")
	}
}
