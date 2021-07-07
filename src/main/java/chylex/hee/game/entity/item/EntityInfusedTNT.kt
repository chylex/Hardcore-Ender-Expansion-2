package chylex.hee.game.entity.item

import chylex.hee.game.Environment
import chylex.hee.game.block.properties.BlockBuilder.Companion.INDESTRUCTIBLE_HARDNESS
import chylex.hee.game.entity.util.motionY
import chylex.hee.game.item.ItemFlintAndInfernium
import chylex.hee.game.item.infusion.Infusion.FIRE
import chylex.hee.game.item.infusion.Infusion.HARMLESS
import chylex.hee.game.item.infusion.Infusion.MINING
import chylex.hee.game.item.infusion.Infusion.PHASING
import chylex.hee.game.item.infusion.Infusion.POWER
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.explosion.ExplosionBuilder
import chylex.hee.game.world.util.allInCenteredSphereMutable
import chylex.hee.game.world.util.getMaterial
import chylex.hee.game.world.util.getState
import chylex.hee.init.ModEntities
import chylex.hee.system.heeTag
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextItem
import chylex.hee.system.random.nextRounded
import chylex.hee.util.math.Vec
import chylex.hee.util.math.center
import chylex.hee.util.math.remapRange
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MoverType
import net.minecraft.entity.MoverType.SELF
import net.minecraft.entity.item.ItemEntity
import net.minecraft.entity.item.TNTEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.crafting.IRecipeType.SMELTING
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameterSets
import net.minecraft.loot.LootParameters
import net.minecraft.loot.LootTables
import net.minecraft.network.IPacket
import net.minecraft.particles.ParticleTypes.SMOKE
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.Explosion
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkStatus
import net.minecraft.world.chunk.IChunk
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.fml.network.NetworkHooks
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class EntityInfusedTNT : TNTEntity {
	private companion object {
		private const val WATER_CHECK_RADIUS = 4
		private const val PHASING_INSTANT_FUSE_TICKS = 3
		
		private const val HAS_INFERNIUM_TAG = "HasInfernium"
		private const val HAS_PHASED_TAG = "HasPhased"
		
		private val PARTICLE_TICK = ParticleSpawnerVanilla(SMOKE)
		
		// EntityItem construction
		
		private val constructRawItem: (World, Vector3d, ItemStack) -> ItemEntity = { world, pos, stack ->
			ItemEntity(world, pos.x, pos.y, pos.z, stack)
		}
		
		private val constructCookedItem: (World, Vector3d, ItemStack) -> ItemEntity = { world, pos, stack ->
			world.recipeManager.getRecipe(SMELTING, Inventory(stack), world).orElse(null).let {
				if (it == null)
					constructRawItem(world, pos, stack)
				else
					EntityItemFreshlyCooked(world, pos, it.recipeOutput.copy())
			}
		}
	}
	
	private var igniter: LivingEntity? = null
	private var infusions = InfusionList.EMPTY
	private var hasInferniumPower = false
	private var hasPhasedIntoWall = false
	
	@Suppress("unused")
	constructor(type: EntityType<EntityInfusedTNT>, world: World) : super(type, world)
	
	constructor(world: World, pos: BlockPos, infusions: InfusionList, igniter: LivingEntity?, infernium: Boolean) : this(ModEntities.INFUSED_TNT, world) {
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
	
	constructor(world: World, pos: BlockPos, infusions: InfusionList, explosion: Explosion) : this(world, pos, infusions, explosion.explosivePlacedBy, infernium = false) {
		this.fuse = rand.nextInt(fuse / 4) + (fuse / 8)
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun getIgniter(): LivingEntity? {
		return igniter
	}
	
	private fun loadInfusions(infusions: InfusionList) {
		this.infusions = infusions
		this.noClip = infusions.has(PHASING)
	}
	
	override fun tick() {
		prevPosX = posX
		prevPosY = posY
		prevPosZ = posZ
		
		if (!hasNoGravity()) {
			motionY -= 0.04
		}
		
		move(SELF, motion)
		motion = motion.scale(0.98)
		
		if (onGround) {
			motion = motion.mul(0.7, -0.5, 0.7)
		}
		
		if (--fuse <= 0) {
			remove()
			blowUp()
		}
		else {
			func_233566_aG_() // RENAME handleWaterMovement
			PARTICLE_TICK.spawn(Point(posX, posY + 0.5, posZ, 1), rand)
		}
	}
	
	// Phasing
	
	override fun move(type: MoverType, by: Vector3d) {
		val wasNoclip = noClip
		
		noClip = false
		super.move(type, by)
		noClip = wasNoclip
		
		if (noClip && type == SELF) {
			if (!world.hasNoCollisions(boundingBox)) {
				hasPhasedIntoWall = true
			}
			else if (hasPhasedIntoWall && fuse > PHASING_INSTANT_FUSE_TICKS) {
				fuse = PHASING_INSTANT_FUSE_TICKS
			}
		}
	}
	
	fun getWorldReaderForCollisions(): IWorldReader {
		if (!infusions.has(PHASING)) {
			return world
		}
		
		return object : IWorldReader by world {
			private fun returnOnlyIndestructible(state: BlockState, pos: BlockPos): BlockState {
				return if (state.getBlockHardness(world, pos) == INDESTRUCTIBLE_HARDNESS)
					state
				else
					Blocks.AIR.defaultState
			}
			
			override fun getBlockState(pos: BlockPos): BlockState {
				return returnOnlyIndestructible(pos.getState(world), pos)
			}
			
			override fun getChunk(x: Int, z: Int, requiredStatus: ChunkStatus, nonNull: Boolean): IChunk? {
				val chunk = world.getChunk(x, z, requiredStatus, nonNull) ?: return null
				
				return object : IChunk by chunk {
					override fun getBlockState(pos: BlockPos): BlockState {
						return returnOnlyIndestructible(chunk.getBlockState(pos), pos)
					}
				}
			}
		}
	}
	
	// Explosion handling
	
	private fun blowUp() {
		if (world.isRemote) {
			return
		}
		
		val strength = (if (infusions.has(POWER)) 6F else 4F) * (if (hasInferniumPower) ItemFlintAndInfernium.EXPLOSION_MULTIPLIER else 1F)
		
		val isFiery = infusions.has(FIRE)
		val isHarmless = infusions.has(HARMLESS)
		val isMining = infusions.has(MINING)
		
		val dropRateMultiplier: Float
		val dropFortune: Int
		
		if (isMining) {
			dropRateMultiplier = 3F
			dropFortune = 1
		}
		else {
			dropRateMultiplier = 1F
			dropFortune = 0
		}
		
		with(ExplosionBuilder()) {
			this.destroyBlocks = !isHarmless
			this.damageEntities = !isHarmless
			
			this.spawnFire = isFiery
			this.blockDropRateMultiplier = dropRateMultiplier
			this.blockDropFortune = dropFortune
			
			trigger(world, this@EntityInfusedTNT, posX, posY + (height / 16.0), posZ, strength)
		}
		
		if (isMining && isInWater) {
			performFishing(isFiery)
		}
	}
	
	private fun performFishing(cook: Boolean) {
		var totalCountedBlocks = 0
		val foundWaterBlocks = mutableListOf<BlockPos>()
		
		for (pos in position.allInCenteredSphereMutable(WATER_CHECK_RADIUS)) {
			++totalCountedBlocks
			
			if (pos.getMaterial(world) === Material.WATER) {
				foundWaterBlocks.add(pos.toImmutable())
			}
		}
		
		if (foundWaterBlocks.isNotEmpty()) {
			val waterRatio = foundWaterBlocks.size.toFloat() / totalCountedBlocks
			
			val dropAmount = when {
				waterRatio < 0.1 -> remapRange(waterRatio, (0.0F)..(0.1F), (1.0F)..(1.6F))
				waterRatio < 0.4 -> remapRange(waterRatio, (0.1F)..(0.4F), (1.6F)..(4.0F))
				else             -> remapRange(waterRatio, (0.4F)..(1.0F), (4.0F)..(5.8F))
			}
			
			val lootTable = Environment.getLootTable(LootTables.GAMEPLAY_FISHING)
			val lootContext = LootContext.Builder(world as ServerWorld)
				.withRandom(rand)
				.withParameter(LootParameters.ORIGIN, position.center)
				.withParameter(LootParameters.TOOL, ItemStack(Items.FISHING_ROD))
				.build(LootParameterSets.FISHING)
			
			val constructItemEntity = if (cook) constructCookedItem else constructRawItem
			
			repeat(rand.nextRounded(dropAmount)) {
				for (droppedItem in lootTable.generate(lootContext)) { // there's ItemFishedEvent but it needs the hook entity...
					val dropPos = rand.nextItem(foundWaterBlocks).center.add(
						rand.nextFloat(-0.25, 0.25),
						rand.nextFloat(-0.25, 0.25),
						rand.nextFloat(-0.25, 0.25)
					)
					
					constructItemEntity(world, dropPos, droppedItem).apply {
						motion = Vec(
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
