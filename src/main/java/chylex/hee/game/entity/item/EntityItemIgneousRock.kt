package chylex.hee.game.entity.item

import chylex.hee.game.block.BlockPuzzleLogic
import chylex.hee.game.block.util.FLUID_LEVEL
import chylex.hee.game.block.util.SKULL_ROTATION
import chylex.hee.game.block.util.SKULL_WALL_FACING
import chylex.hee.game.block.util.with
import chylex.hee.game.entity.technical.EntityTechnicalPuzzle
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectVulnerableEntities
import chylex.hee.game.entity.util.setFireTicks
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.fx.FxEntityData
import chylex.hee.game.fx.FxEntityHandler
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.inventory.util.setStack
import chylex.hee.game.item.ItemFlintAndInfernium
import chylex.hee.game.item.util.isNotEmpty
import chylex.hee.game.item.util.size
import chylex.hee.game.particle.ParticleFlameCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.distanceSqTo
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getFluidState
import chylex.hee.game.world.util.getMaterial
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.isAir
import chylex.hee.game.world.util.isFullBlock
import chylex.hee.game.world.util.isTopSolid
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.game.world.util.offsetUntilExcept
import chylex.hee.game.world.util.setBlock
import chylex.hee.game.world.util.setState
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.heeTag
import chylex.hee.util.math.Pos
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.scaleXZ
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getEnum
import chylex.hee.util.nbt.putEnum
import chylex.hee.util.nbt.use
import chylex.hee.util.random.nextBiasedFloat
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextVector
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.CarpetBlock
import net.minecraft.block.DoublePlantBlock
import net.minecraft.block.FlowerBlock
import net.minecraft.block.LeavesBlock
import net.minecraft.block.StainedGlassBlock
import net.minecraft.block.StainedGlassPaneBlock
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MoverType
import net.minecraft.inventory.Inventory
import net.minecraft.item.BlockItem
import net.minecraft.item.DirectionalPlaceContext
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeType
import net.minecraft.particles.ParticleTypes.LAVA
import net.minecraft.tags.FluidTags
import net.minecraft.util.DamageSource
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.UP
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random
import kotlin.math.log2

class EntityItemIgneousRock : EntityItemNoBob {
	@Suppress("unused")
	constructor(type: EntityType<EntityItemIgneousRock>, world: World) : super(type, world)
	
	constructor(world: World, stack: ItemStack, replacee: Entity) : super(ModEntities.ITEM_IGNEOUS_ROCK, world, stack, replacee) {
		lifespan = 1200 + (world.rand.nextBiasedFloat(3F) * 1200F).floorToInt()
		throwFacing = Facing4.fromDirection(motion)
	}
	
	object Type : BaseType<EntityItemIgneousRock>() {
		override val isImmuneToFire
			get() = true
	}
	
	companion object {
		private const val BURN_DISTANCE = 3.5
		private const val BURN_DISTANCE_SQ = BURN_DISTANCE * BURN_DISTANCE
		
		private const val ACTIVITY_DELAY_TICKS = 25
		private const val INITIAL_FIRE_UNTIL_TICKS = ACTIVITY_DELAY_TICKS + 10
		
		private const val MIN_ENTITY_BURN_DURATION_TICKS = 40
		
		private const val FACING_TAG = "Facing"
		
		private val PARTICLE_TICK = ParticleSpawnerVanilla(
			type = LAVA,
			pos = InBox(0.1F)
		)
		
		private val PARTICLE_FLAME_MOT = Constant(0.008F, UP) + InBox(0.012F, 0.014F, 0.012F)
		
		private val PARTICLE_SMELT = ParticleSpawnerCustom(
			type = ParticleFlameCustom,
			data = ParticleFlameCustom.Data(maxAge = 16),
			pos = InBox(0.85F),
			mot = PARTICLE_FLAME_MOT
		)
		
		private fun PARTICLE_BURN(target: Entity) = ParticleSpawnerCustom(
			type = ParticleFlameCustom,
			data = ParticleFlameCustom.Data(maxAge = 8),
			pos = Constant(0.2F, UP) + InBox(target, 0.4F),
			mot = PARTICLE_FLAME_MOT
		)
		
		val FX_BLOCK_SMELT = object : FxBlockHandler() {
			override fun handle(pos: BlockPos, world: World, rand: Random) {
				PARTICLE_SMELT.spawn(Point(pos, 20), rand)
				ModSounds.ENTITY_IGNEOUS_ROCK_BURN.playClient(pos, SoundCategory.NEUTRAL, volume = 0.3F, pitch = rand.nextFloat(1F, 2F))
			}
		}
		
		val FX_ENTITY_BURN = object : FxEntityHandler() {
			override fun handle(entity: Entity, rand: Random) {
				PARTICLE_BURN(entity).spawn(Point(entity, heightMp = 0.5F, amount = 24), rand)
				ModSounds.ENTITY_IGNEOUS_ROCK_BURN.playClient(entity.posVec, SoundCategory.NEUTRAL, volume = 0.3F, pitch = rand.nextFloat(1F, 2F))
			}
		}
	}
	
	private var throwFacing = DOWN
	
	private val smeltingInventory = Inventory(1)
	
	override fun tick() {
		super.tick()
		
		val currentPos = Pos(this)
		
		if (!world.isRemote && age > 4 && (world.gameTime - 1L) % BlockPuzzleLogic.UPDATE_RATE == 0L) {
			val posBelow = currentPos.down()
			
			if (BlockPuzzleLogic.isPuzzleBlock(posBelow.getBlock(world))) {
				val entity = EntityTechnicalPuzzle(world)
				
				if (entity.startChain(posBelow, throwFacing)) {
					world.addEntity(entity)
					
					val reducedStack = item.apply { shrink(1) }.takeIf { it.isNotEmpty }
					
					if (reducedStack == null) {
						remove()
						return
					}
					else {
						item = reducedStack
					}
				}
			}
		}
		
		if (!world.isRemote && age >= ACTIVITY_DELAY_TICKS) {
			if (ticksExisted % 5 == 0) {
				updateBurnNearbyEntities()
			}
			
			var hasChangedAnyBlock = false
			
			val stack = item
			val count = stack.size
			
			repeat(count) {
				if (rand.nextInt(6) == 0 || age < INITIAL_FIRE_UNTIL_TICKS) {
					val checkRange = (BURN_DISTANCE * 2).ceilToInt()
					
					val randomTopBlock = getRandomBlock().let { randomPos ->
						if (randomPos.isAir(world))
							randomPos.offsetUntilExcept(DOWN, 1..checkRange) { !it.isAir(world) || it.distanceSqTo(this) > BURN_DISTANCE_SQ }
						else
							randomPos.offsetUntil(UP, 0..checkRange) { it.isAir(world) || it.distanceSqTo(this) > BURN_DISTANCE_SQ }
					}
					
					if (randomTopBlock != null && randomTopBlock.isAir(world) && randomTopBlock.down().isTopSolid(world)) {
						randomTopBlock.setBlock(world, Blocks.FIRE)
						hasChangedAnyBlock = true
					}
				}
				else if (rand.nextBoolean()) {
					getRandomBlock().takeUnless { it.isAir(world) }?.let {
						updateBurnBlock(it)
						hasChangedAnyBlock = true
					}
				}
			}
			
			if (hasChangedAnyBlock && count > 1 && rand.nextInt(100) < (count * 4) / 10) {
				item = stack.apply { shrink(1) }
			}
		}
		
		if (currentPos != Pos(prevPosX, prevPosY, prevPosZ) || ticksExisted % 25 == 0) {
			if (currentPos.getMaterial(world) === Material.WATER) {
				setMotion(rand.nextFloat(-0.2, 0.2), 0.2, rand.nextFloat(-0.2, 0.2))
				// spawns bubble particles via Entity.doWaterSplashEffect
				// plays hissing sound (ENTITY_GENERIC_EXTINGUISH_FIRE) via Entity.move, as the entity is on fire
			}
		}
		
		if (isInLava) {
			lifespan -= 3
			
			handleFluidAcceleration(FluidTags.LAVA, 0.014)
			motion = motion.scaleXZ(0.9)
		}
		
		if (world.isRemote) {
			val stackSize = item.size.toFloat()
			val particleChance = if (stackSize < 1F) 0F else 0.13F + (stackSize / 110F) + (log2(stackSize) / 18F)
			
			if (rand.nextFloat() < particleChance) {
				PARTICLE_TICK.spawn(Point(this, heightMp = 0.5F, amount = 1), rand)
			}
		}
	}
	
	override fun move(type: MoverType, by: Vector3d) {
		if (isInLava) {
			super.move(type, by.mul(0.2, 0.01, 0.2))
		}
		else {
			super.move(type, by)
		}
	}
	
	override fun isInvulnerableTo(source: DamageSource): Boolean {
		return super.isInvulnerableTo(source) || source.isFireDamage
	}
	
	override fun isBurning() = true
	
	// In-world behavior
	
	private fun getRandomBlock(): BlockPos {
		val randomOffset = rand.nextVector(rand.nextBiasedFloat(6F) * BURN_DISTANCE)
		return Pos(posVec.add(randomOffset))
	}
	
	private fun updateBurnNearbyEntities() {
		val pos = posVec
		
		for (entity in world.selectVulnerableEntities.allInRange(pos, BURN_DISTANCE).filter { !it.isImmuneToFire && it.fireTimer < MIN_ENTITY_BURN_DURATION_TICKS }) {
			val distanceMp = 1F - (pos.squareDistanceTo(entity.posVec) / BURN_DISTANCE_SQ)
			val extraDuration = (distanceMp * 40F) + rand.nextInt(20)
			
			entity.setFireTicks(MIN_ENTITY_BURN_DURATION_TICKS + extraDuration.floorToInt()) // about 2-5 seconds
			PacketClientFX(FX_ENTITY_BURN, FxEntityData(entity)).sendToAllAround(this, 32.0)
		}
	}
	
	private fun updateBurnBlock(pos: BlockPos) {
		val world = world as? ServerWorld ?: return
		
		val sourceState = pos.getState(world)
		var targetState: BlockState? = null
		
		// primary transformations
		
		smeltingInventory.setStack(0, ItemStack(sourceState.block))
		val output = world.recipeManager.getRecipe(IRecipeType.SMELTING, smeltingInventory, world).orElse(null)?.recipeOutput
		
		if (output != null && output.isNotEmpty) {
			targetState = (output.item as? BlockItem)?.block?.getStateForPlacement(object : DirectionalPlaceContext(world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP) {
				override fun canPlace(): Boolean {
					return true
				}
				
				override fun replacingClickedOnBlock(): Boolean {
					return true
				}
			})
		}
		
		// secondary transformations
		
		if (targetState == null) {
			val fluid = pos.getFluidState(world)
			
			if (fluid.isTagged(FluidTags.WATER) && !fluid.isSource) {
				targetState = Blocks.AIR.defaultState
			}
		}
		
		if (targetState == null) {
			targetState = when (sourceState.block) {
				Blocks.MOSSY_COBBLESTONE -> Blocks.COBBLESTONE
				Blocks.PACKED_ICE -> Blocks.ICE
				Blocks.GRASS_BLOCK -> Blocks.DIRT
				Blocks.FARMLAND -> Blocks.DIRT
				Blocks.PODZOL -> Blocks.DIRT
				
				Blocks.MOSSY_COBBLESTONE -> Blocks.COBBLESTONE
				Blocks.MOSSY_COBBLESTONE_SLAB -> Blocks.COBBLESTONE_SLAB
				Blocks.MOSSY_COBBLESTONE_STAIRS -> Blocks.COBBLESTONE_STAIRS
				Blocks.MOSSY_COBBLESTONE_WALL -> Blocks.COBBLESTONE_WALL
				
				Blocks.MOSSY_STONE_BRICKS -> Blocks.STONE_BRICKS
				Blocks.MOSSY_STONE_BRICK_SLAB -> Blocks.STONE_BRICK_SLAB
				Blocks.MOSSY_STONE_BRICK_STAIRS -> Blocks.STONE_BRICK_STAIRS
				Blocks.MOSSY_STONE_BRICK_WALL -> Blocks.STONE_BRICK_WALL
				
				Blocks.INFESTED_MOSSY_STONE_BRICKS -> Blocks.INFESTED_STONE_BRICKS
				
				Blocks.WET_SPONGE -> Blocks.SPONGE
				
				Blocks.TRIPWIRE,
				Blocks.BROWN_MUSHROOM,
				Blocks.RED_MUSHROOM -> Blocks.AIR
				
				Blocks.VINE,
				Blocks.COBWEB,
				ModBlocks.DRY_VINES,
				ModBlocks.ANCIENT_COBWEB,
				is CarpetBlock -> Blocks.FIRE
				
				is FlowerBlock -> Blocks.DEAD_BUSH
				
				is DoublePlantBlock -> {
					if (pos.up().getBlock(world) !== sourceState.block) {
						return
					}
					
					Blocks.DEAD_BUSH
				}
				
				is LeavesBlock -> {
					val posAbove = pos.up()
					
					if (posAbove.isAir(world)) {
						posAbove.setBlock(world, Blocks.FIRE)
						return
					}
					
					Blocks.FIRE
				}
				
				Blocks.TNT,
				ModBlocks.INFUSED_TNT -> {
					ItemFlintAndInfernium.igniteTNT(world, pos, null, ignoreTrap = true)
					return
				}
				
				else -> null
			}?.defaultState
		}
		
		if (targetState == null) {
			targetState = when (sourceState.block) {
				Blocks.ZOMBIE_HEAD,
				Blocks.PLAYER_HEAD -> Blocks.SKELETON_SKULL.with(SKULL_ROTATION, sourceState[SKULL_ROTATION])
				
				Blocks.ZOMBIE_WALL_HEAD,
				Blocks.PLAYER_WALL_HEAD -> Blocks.SKELETON_WALL_SKULL.with(SKULL_WALL_FACING, sourceState[SKULL_WALL_FACING])
				
				Blocks.SNOW -> {
					if (rand.nextInt(4) == 0)
						Blocks.WATER.with(FLUID_LEVEL, rand.nextInt(5, 6)).also { spreadFluidToNeighbors(pos, it.block, it[FLUID_LEVEL] + 1) }
					else
						Blocks.AIR.defaultState
				}
				
				Blocks.ICE,
				Blocks.FROSTED_ICE,
				Blocks.SNOW_BLOCK -> {
					if (Facing4.any { pos.offset(it).getBlock(world) === Blocks.WATER } || Facing4.all { pos.offset(it).isFullBlock(world) })
						Blocks.WATER.defaultState
					else
						Blocks.WATER.with(FLUID_LEVEL, 1).also { spreadFluidToNeighbors(pos, it.block, 2) }
				}
				
				else -> null
			}
		}
		
		// ternary transformations
		
		if (targetState == null && rand.nextInt(100) < 18) {
			targetState = when (sourceState.block) {
				Blocks.WATER     -> Blocks.COBBLESTONE // flowing water is already covered above
				Blocks.DEAD_BUSH -> Blocks.AIR
				
				Blocks.GLASS,
				Blocks.GLASS_PANE,
				is StainedGlassBlock,
				is StainedGlassPaneBlock -> {
					pos.breakBlock(world, false)
					return
				}
				
				else -> null
			}?.defaultState
		}
		
		// final handling
		
		if (targetState != null) {
			pos.setState(world, targetState)
			PacketClientFX(FX_BLOCK_SMELT, FxBlockData(pos)).sendToAllAround(this, 32.0)
		}
	}
	
	private fun spreadFluidToNeighbors(pos: BlockPos, block: Block, level: Int) {
		val state = block.with(FLUID_LEVEL, level)
		
		for (facing in Facing4) {
			val offset = pos.offset(facing)
			
			if (offset.isAir(world) || offset.getBlock(world) === Blocks.SNOW) {
				offset.setState(world, state) // forces non-statinary fluids to spread out a bit
			}
		}
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		putEnum(FACING_TAG, throwFacing)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		throwFacing = getEnum<Direction>(FACING_TAG) ?: throwFacing
	}
}
