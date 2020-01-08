package chylex.hee.game.entity.item
import chylex.hee.game.item.ItemFlintAndInfernium
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
import chylex.hee.proxy.Environment
import chylex.hee.system.migration.vanilla.EntityItem
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityTNTPrimed
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.allInCenteredSphereMutable
import chylex.hee.system.util.center
import chylex.hee.system.util.getMaterial
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.motionY
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.nextRounded
import chylex.hee.system.util.remapRange
import chylex.hee.system.util.use
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityType
import net.minecraft.entity.MoverType
import net.minecraft.entity.MoverType.SELF
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeType.SMELTING
import net.minecraft.network.IPacket
import net.minecraft.particles.ParticleTypes.SMOKE
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameterSets
import net.minecraft.world.storage.loot.LootParameters
import net.minecraft.world.storage.loot.LootTables
import net.minecraftforge.fml.network.NetworkHooks

class EntityInfusedTNT : EntityTNTPrimed{
	private companion object{
		private const val WATER_CHECK_RADIUS = 4
		private const val PHASING_INSTANT_FUSE_TICKS = 3
		
		private const val HAS_INFERNIUM_TAG = "HasInfernium"
		private const val HAS_PHASED_TAG = "HasPhased"
		
		private val PARTICLE_TICK = ParticleSpawnerVanilla(SMOKE)
		
		// EntityItem construction
		
		private val constructRawItem: (World, Vec3d, ItemStack) -> EntityItem = { world, pos, stack ->
			EntityItem(world, pos.x, pos.y, pos.z, stack)
		}
		
		private val constructCookedItem: (World, Vec3d, ItemStack) -> EntityItem = { world, pos, stack ->
			world.recipeManager.getRecipe(SMELTING, Inventory(stack), world).orElse(null).let {
				if (it == null)
					constructRawItem(world, pos, stack)
				else
					EntityItemFreshlyCooked(world, pos, it.recipeOutput.copy())
			}
		}
	}
	
	private var infusions = InfusionList.EMPTY
	private var hasInferniumPower = false
	private var hasPhasedIntoWall = false
	
	@Suppress("unused")
	constructor(type: EntityType<EntityInfusedTNT>, world: World) : super(type, world)
	
	@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
	constructor(world: World, pos: BlockPos, infusions: InfusionList, igniter: EntityLivingBase?, infernium: Boolean) : super(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, igniter){
		// UPDATE
		loadInfusions(infusions)
		this.hasInferniumPower = infernium
	}
	
	constructor(world: World, pos: BlockPos, infusions: InfusionList, explosion: Explosion) : this(world, pos, infusions, explosion.explosivePlacedBy, infernium = false){
		fuse = rand.nextInt(fuse / 4) + (fuse / 8)
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	private fun loadInfusions(infusions: InfusionList){
		this.infusions = infusions
		this.noClip = infusions.has(PHASING)
	}
	
	override fun tick(){
		prevPosX = posX
		prevPosY = posY
		prevPosZ = posZ
		
		if (!hasNoGravity()){
			motionY -= 0.04
		}
		
		move(SELF, motion)
		motion = motion.scale(0.98)
		
		if (onGround){
			motion = motion.mul(0.7, -0.5, 0.7)
		}
		
		if (--fuse <= 0){
			remove()
			blowUp()
		}
		else{
			handleWaterMovement()
			PARTICLE_TICK.spawn(Point(posX, posY + 0.5, posZ, 1), rand)
		}
	}
	
	// Phasing
	
	override fun move(type: MoverType, by: Vec3d){
		if (noClip && type == SELF){
			collideWithIndestructibleBlocks()
			
			if (!world.areCollisionShapesEmpty(boundingBox)){
				hasPhasedIntoWall = true
			}
			else if (hasPhasedIntoWall && fuse > PHASING_INSTANT_FUSE_TICKS){
				fuse = PHASING_INSTANT_FUSE_TICKS
			}
		}
		else{
			super.move(type, by)
		}
	}
	
	private fun collideWithIndestructibleBlocks(){
		/* UPDATE
		val (prevMotX, prevMotY, prevMotZ) = motionVec
		
		val collisionBoxes = getCollisionBoxesForIndestructibleBlocks(boundingBox.expand(motionX, motionY, motionZ))
		
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
		}*/
	}
	
	/* UPDATE
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
	
	private inline fun moveWithCollisionCheck(collisionBoxes: List<AxisAlignedBB>, initialMotion: Double, calculateFunc: AxisAlignedBB.(AxisAlignedBB, Double) -> Double, offsetFunc: AxisAlignedBB.(Double) -> AxisAlignedBB): Double{
		if (initialMotion == 0.0){
			return initialMotion
		}
		
		val boundingBox2 = boundingBox // UPDATE
		val finalOffset = collisionBoxes.fold(initialMotion){ acc, box -> box.calculateFunc(boundingBox2, acc) }
		
		if (finalOffset != 0.0){
			boundingBox = boundingBox2.offsetFunc(finalOffset)
		}
		
		return finalOffset
	}*/
	
	// Explosion handling
	
	private fun blowUp(){
		if (world.isRemote){
			return
		}
		
		val strength = (if (infusions.has(POWER)) 6F else 4F) * (if (hasInferniumPower) ItemFlintAndInfernium.EXPLOSION_MULTIPLIER else 1F)
		
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
		
		for(pos in position.allInCenteredSphereMutable(WATER_CHECK_RADIUS)){
			++totalCountedBlocks
			
			if (pos.getMaterial(world) === Material.WATER){
				foundWaterBlocks.add(pos.toImmutable())
			}
		}
		
		if (foundWaterBlocks.isNotEmpty()){
			val waterRatio = foundWaterBlocks.size.toFloat() / totalCountedBlocks
			
			val dropAmount = when{
				waterRatio < 0.1 -> remapRange(waterRatio, (0.0F)..(0.1F), (1.0F)..(1.6F))
				waterRatio < 0.4 -> remapRange(waterRatio, (0.1F)..(0.4F), (1.6F)..(4.0F))
				else             -> remapRange(waterRatio, (0.4F)..(1.0F), (4.0F)..(5.8F))
			}
			
			val lootTable = Environment.getServer().lootTableManager.getLootTableFromLocation(LootTables.GAMEPLAY_FISHING)
			val lootContext = LootContext.Builder(world as ServerWorld)
				.withRandom(rand)
				.withParameter(LootParameters.POSITION, position)
				.withParameter(LootParameters.TOOL, ItemStack(Items.FISHING_ROD))
				.build(LootParameterSets.FISHING)
			
			val constructItemEntity = if (cook) constructCookedItem else constructRawItem
			
			repeat(rand.nextRounded(dropAmount)){
				for(droppedItem in lootTable.generate(lootContext)){ // there's ItemFishedEvent but it needs the hook entity...
					val dropPos = rand.nextItem(foundWaterBlocks).center.add(
						rand.nextFloat(-0.25, 0.25),
						rand.nextFloat(-0.25, 0.25),
						rand.nextFloat(-0.25, 0.25)
					)
					
					constructItemEntity(world, dropPos, droppedItem).apply {
						motion = Vec3d(
							rand.nextFloat(-0.25, 0.25),
							rand.nextFloat(1.0, 1.2), // UPDATE: 1.13 makes items float upwards, review this
							rand.nextFloat(-0.25, 0.25)
						)
						
						world.addEntity(this)
					}
				}
			}
		}
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		InfusionTag.setList(this, infusions)
		putBoolean(HAS_INFERNIUM_TAG, hasInferniumPower)
		putBoolean(HAS_PHASED_TAG, hasPhasedIntoWall)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		loadInfusions(InfusionTag.getList(this))
		hasInferniumPower = getBoolean(HAS_INFERNIUM_TAG)
		hasPhasedIntoWall = getBoolean(HAS_PHASED_TAG)
	}
}
