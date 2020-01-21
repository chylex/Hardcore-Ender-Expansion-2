package chylex.hee.game.entity.item
import chylex.hee.game.block.info.BlockBuilder.Companion.INDESTRUCTIBLE_HARDNESS
import chylex.hee.game.item.ItemFlintAndInfernium
import chylex.hee.game.item.infusion.Infusion.FIRE
import chylex.hee.game.item.infusion.Infusion.HARMLESS
import chylex.hee.game.item.infusion.Infusion.MINING
import chylex.hee.game.item.infusion.Infusion.PHASING
import chylex.hee.game.item.infusion.Infusion.POWER
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.mechanics.explosion.ExplosionBuilder
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModEntities
import chylex.hee.proxy.Environment
import chylex.hee.system.migration.vanilla.EntityItem
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityTNTPrimed
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.allInCenteredSphereMutable
import chylex.hee.system.util.asVoxelShape
import chylex.hee.system.util.center
import chylex.hee.system.util.getMaterial
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.motionY
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.nextRounded
import chylex.hee.system.util.remapRange
import chylex.hee.system.util.use
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MoverType
import net.minecraft.entity.MoverType.SELF
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeType.SMELTING
import net.minecraft.network.IPacket
import net.minecraft.particles.ParticleTypes.SMOKE
import net.minecraft.util.ReuseableStream
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.shapes.IBooleanFunction
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.Explosion
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkStatus
import net.minecraft.world.chunk.IChunk
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameterSets
import net.minecraft.world.storage.loot.LootParameters
import net.minecraft.world.storage.loot.LootTables
import net.minecraftforge.fml.network.NetworkHooks
import java.util.stream.Stream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
	
	private var igniter: EntityLivingBase? = null
	private var infusions = InfusionList.EMPTY
	private var hasInferniumPower = false
	private var hasPhasedIntoWall = false
	
	@Suppress("unused")
	constructor(type: EntityType<EntityInfusedTNT>, world: World) : super(type, world)
	
	constructor(world: World, pos: BlockPos, infusions: InfusionList, igniter: EntityLivingBase?, infernium: Boolean) : this(ModEntities.INFUSED_TNT, world){
		setPosition(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
		prevPosX = posX
		prevPosY = posY
		prevPosZ = posZ
		
		val angle = rand.nextFloat(0.0, 2.0 * PI)
		setMotion(-sin(angle) * 0.02, 0.2, -cos(angle) * 0.02)
		
		loadInfusions(infusions)
		
		this.fuse = 80
		this.igniter = igniter
		this.hasInferniumPower = infernium
	}
	
	constructor(world: World, pos: BlockPos, infusions: InfusionList, explosion: Explosion) : this(world, pos, infusions, explosion.explosivePlacedBy, infernium = false){
		this.fuse = rand.nextInt(fuse / 4) + (fuse / 8)
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun getTntPlacedBy(): EntityLivingBase?{
		return igniter
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
		val wasNoclip = noClip
		
		noClip = false
		super.move(type, by)
		noClip = wasNoclip
		
		if (noClip && type == SELF){
			if (!world.areCollisionShapesEmpty(boundingBox)){
				hasPhasedIntoWall = true
			}
			else if (hasPhasedIntoWall && fuse > PHASING_INSTANT_FUSE_TICKS){
				fuse = PHASING_INSTANT_FUSE_TICKS
			}
		}
	}
	
	override fun getAllowedMovement(motion: Vec3d): Vec3d{
		if (infusions.has(PHASING)){
			if (motion.lengthSquared() == 0.0){
				return motion
			}
			
			val aabb = boundingBox
			val context = ISelectionContext.forEntity(this)
			
			val borderShape = world.worldBorder.shape
			val borderStream = if (VoxelShapes.compare(borderShape, aabb.shrink(1.0E-7).asVoxelShape, IBooleanFunction.AND))
				Stream.empty()
			else
				Stream.of(borderShape)
			
			val shapeStream = world.getEmptyCollisionShapes(this, aabb.expand(motion), emptySet())
			val stream = ReuseableStream(Stream.concat(shapeStream, borderStream))
			
			val indestructibleOnlyWorld = object : IWorldReader by world{
				private fun returnOnlyIndestructible(state: BlockState, pos: BlockPos): BlockState{
					return if (state.getBlockHardness(world, pos) == INDESTRUCTIBLE_HARDNESS)
						state
					else
						Blocks.AIR.defaultState
				}
				
				override fun getBlockState(pos: BlockPos): BlockState{
					return returnOnlyIndestructible(pos.getState(world), pos)
				}
				
				override fun getChunk(x: Int, z: Int, requiredStatus: ChunkStatus, nonNull: Boolean): IChunk?{
					val chunk = world.getChunk(x, z, requiredStatus, nonNull) ?: return null
					
					return object : IChunk by chunk{
						override fun getBlockState(pos: BlockPos): BlockState{
							return returnOnlyIndestructible(chunk.getBlockState(pos), pos)
						}
					}
				}
			}
			
			val movingX = motion.x == 0.0
			val movingY = motion.y == 0.0
			val movingZ = motion.z == 0.0
			
			return if ((!movingX || !movingY) && (!movingX || !movingZ) && (!movingY || !movingZ))
				Entity.collideBoundingBox(motion, aabb, ReuseableStream(Stream.concat(stream.createStream(), indestructibleOnlyWorld.getCollisionShapes(this, aabb.expand(motion)))))
			else
				Entity.getAllowedMovement(motion, aabb, indestructibleOnlyWorld, context, stream)
		}
		
		return super.getAllowedMovement(motion)
	}
	
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
			this.blockDropRateMultiplier = dropRateMultiplier
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
							rand.nextFloat(1.0, 1.2),
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
