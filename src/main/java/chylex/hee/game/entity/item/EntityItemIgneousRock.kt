package chylex.hee.game.entity.item
import chylex.hee.game.block.BlockPuzzleLogic
import chylex.hee.game.block.with
import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.selectVulnerableEntities
import chylex.hee.game.entity.setFireTicks
import chylex.hee.game.entity.technical.EntityTechnicalPuzzle
import chylex.hee.game.inventory.isNotEmpty
import chylex.hee.game.inventory.setStack
import chylex.hee.game.inventory.size
import chylex.hee.game.particle.ParticleFlameCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.Pos
import chylex.hee.game.world.breakBlock
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.getFluidState
import chylex.hee.game.world.getMaterial
import chylex.hee.game.world.getState
import chylex.hee.game.world.isAir
import chylex.hee.game.world.isFullBlock
import chylex.hee.game.world.isTopSolid
import chylex.hee.game.world.offsetUntil
import chylex.hee.game.world.offsetUntilExcept
import chylex.hee.game.world.playClient
import chylex.hee.game.world.setBlock
import chylex.hee.game.world.setState
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxBlockData
import chylex.hee.network.fx.FxBlockHandler
import chylex.hee.network.fx.FxEntityData
import chylex.hee.network.fx.FxEntityHandler
import chylex.hee.system.facades.Facing4
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.math.floorToInt
import chylex.hee.system.math.scaleXZ
import chylex.hee.system.migration.BlockCarpet
import chylex.hee.system.migration.BlockDoublePlant
import chylex.hee.system.migration.BlockFlower
import chylex.hee.system.migration.BlockFlowingFluid
import chylex.hee.system.migration.BlockLeaves
import chylex.hee.system.migration.BlockSkull
import chylex.hee.system.migration.BlockSkullWall
import chylex.hee.system.migration.BlockStainedGlass
import chylex.hee.system.migration.BlockStainedGlassPane
import chylex.hee.system.migration.Blocks
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.ItemBlock
import chylex.hee.system.migration.Sounds
import chylex.hee.system.random.nextBiasedFloat
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextVector
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getEnum
import chylex.hee.system.serialization.heeTag
import chylex.hee.system.serialization.putEnum
import chylex.hee.system.serialization.use
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MoverType
import net.minecraft.inventory.Inventory
import net.minecraft.item.DirectionalPlaceContext
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeType
import net.minecraft.particles.ParticleTypes.LAVA
import net.minecraft.tags.FluidTags
import net.minecraft.util.DamageSource
import net.minecraft.util.Direction
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random
import kotlin.math.log2

class EntityItemIgneousRock : EntityItemNoBob{
	@Suppress("unused")
	constructor(type: EntityType<EntityItemIgneousRock>, world: World) : super(type, world)
	
	constructor(world: World, stack: ItemStack, replacee: Entity) : super(ModEntities.ITEM_IGNEOUS_ROCK, world, stack, replacee){
		lifespan = 1200 + (world.rand.nextBiasedFloat(3F) * 1200F).floorToInt()
		throwFacing = Facing4.fromDirection(motion)
	}
	
	companion object{
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
		
		val FX_BLOCK_SMELT = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				PARTICLE_SMELT.spawn(Point(pos, 20), rand)
				Sounds.ENTITY_GENERIC_BURN.playClient(pos, SoundCategory.NEUTRAL, volume = 0.3F, pitch = rand.nextFloat(1F, 2F))
			}
		}
		
		val FX_ENTITY_BURN = object : FxEntityHandler(){
			override fun handle(entity: Entity, rand: Random){
				PARTICLE_BURN(entity).spawn(Point(entity, heightMp = 0.5F, amount = 24), rand)
				Sounds.ENTITY_GENERIC_BURN.playClient(entity.posVec, SoundCategory.NEUTRAL, volume = 0.3F, pitch = rand.nextFloat(1F, 2F))
			}
		}
	}
	
	private var throwFacing = DOWN
	private var prevMotion = Vec3d.ZERO
	
	private val smeltingInventory = Inventory(1)
	
	override fun tick(){
		prevMotion = motion
		super.tick()
		
		val currentPos = Pos(this)
		
		if (!world.isRemote && age > 4 && (world.totalTime - 1L) % BlockPuzzleLogic.UPDATE_RATE == 0L){
			val posBelow = currentPos.down()
			
			if (posBelow.getBlock(world) is BlockPuzzleLogic){
				val entity = EntityTechnicalPuzzle(world)
				
				if (entity.startChain(posBelow, throwFacing)){
					world.addEntity(entity)
					
					val reducedStack = item.apply { shrink(1) }.takeIf { it.isNotEmpty }
					
					if (reducedStack == null){
						remove()
						return
					}
					else{
						item = reducedStack
					}
				}
			}
		}
		
		if (!world.isRemote && age >= ACTIVITY_DELAY_TICKS){
			if (ticksExisted % 5 == 0){
				updateBurnNearbyEntities()
			}
			
			var hasChangedAnyBlock = false
			
			val stack = item
			val count = stack.size
			
			repeat(count){
				if (rand.nextInt(6) == 0 || age < INITIAL_FIRE_UNTIL_TICKS){
					val checkRange = (BURN_DISTANCE * 2).ceilToInt()
					
					val randomTopBlock = getRandomBlock().let { randomPos ->
						if (randomPos.isAir(world))
							randomPos.offsetUntilExcept(DOWN, 1..checkRange){ !it.isAir(world) || it.distanceSqTo(this) > BURN_DISTANCE_SQ }
						else
							randomPos.offsetUntil(UP, 0..checkRange){ it.isAir(world) || it.distanceSqTo(this) > BURN_DISTANCE_SQ }
					}
					
					if (randomTopBlock != null && randomTopBlock.isAir(world) && randomTopBlock.down().isTopSolid(world)){
						randomTopBlock.setBlock(world, Blocks.FIRE)
						hasChangedAnyBlock = true
					}
				}
				else if (rand.nextBoolean()){
					getRandomBlock().takeUnless { it.isAir(world) }?.let {
						updateBurnBlock(it)
						hasChangedAnyBlock = true
					}
				}
			}
			
			if (hasChangedAnyBlock && count > 1 && rand.nextInt(100) < (count * 4) / 10){
				item = stack.apply { shrink(1) }
			}
		}
		
		if (currentPos != Pos(prevPosX, prevPosY, prevPosZ) || ticksExisted % 25 == 0){
			if (currentPos.getMaterial(world) === Material.WATER){
				setMotion(rand.nextFloat(-0.2, 0.2), 0.2, rand.nextFloat(-0.2, 0.2))
				// spawns bubble particles via Entity.doWaterSplashEffect
				// plays hissing sound (ENTITY_GENERIC_EXTINGUISH_FIRE) via Entity.move, as the entity is on fire
			}
		}
		
		if (isInLava){
			lifespan -= 3
			
			handleFluidAcceleration(FluidTags.LAVA)
			motion = motion.scaleXZ(0.9)
		}
		
		if (world.isRemote){
			val stackSize = item.size.toFloat()
			val particleChance = if (stackSize < 1F) 0F else 0.13F + (stackSize / 110F) + (log2(stackSize) / 18F)
			
			if (rand.nextFloat() < particleChance){
				PARTICLE_TICK.spawn(Point(this, heightMp = 0.5F, amount = 1), rand)
			}
		}
	}
	
	override fun move(type: MoverType, by: Vec3d){
		if (isInLava){
			super.move(type, by.mul(0.2, 0.01, 0.2))
		}
		else{
			super.move(type, by)
		}
	}
	
	override fun playSound(sound: SoundEvent, volume: Float, pitch: Float){
		if (sound === Sounds.ENTITY_GENERIC_BURN && volume == 0.4F && pitch >= 2.0F){ // UPDATE 1.15 (check if this still applies, or find a better way)
			motion = prevMotion // this disables vanilla lava handling, but also breaks hasNoGravity
		}
		else{
			super.playSound(sound, volume, pitch)
		}
	}
	
	override fun isInvulnerableTo(source: DamageSource): Boolean{
		return super.isInvulnerableTo(source) || source.isFireDamage
	}
	
	override fun isBurning() = true
	
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
		val world = world as? ServerWorld ?: return
		
		val sourceState = pos.getState(world)
		var targetState: BlockState? = null
		
		// primary transformations
		
		smeltingInventory.setStack(0, ItemStack(sourceState.block))
		val output = world.recipeManager.getRecipe(IRecipeType.SMELTING, smeltingInventory, world).orElse(null)?.recipeOutput
		
		if (output != null && output.isNotEmpty){
			targetState = (output.item as? ItemBlock)?.block?.getStateForPlacement(object : DirectionalPlaceContext(world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP){
				override fun canPlace(): Boolean{
					return true
				}
				
				override fun replacingClickedOnBlock(): Boolean{
					return true
				}
			})
		}
		
		// secondary transformations
		
		if (targetState == null){
			val fluid = pos.getFluidState(world)
			
			if (fluid.isTagged(FluidTags.WATER) && !fluid.isSource){
				targetState = Blocks.AIR.defaultState
			}
		}
		
		if (targetState == null){
			targetState = when(sourceState.block){
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
				is BlockCarpet -> Blocks.FIRE
				
				is BlockFlower -> Blocks.DEAD_BUSH
				
				is BlockDoublePlant -> {
					if (pos.up().getBlock(world) !== sourceState.block){
						return
					}
					
					Blocks.DEAD_BUSH
				}
				
				is BlockLeaves -> {
					val posAbove = pos.up()
					
					if (posAbove.isAir(world)){
						posAbove.setBlock(world, Blocks.FIRE)
						return
					}
					
					Blocks.FIRE
				}
				
				Blocks.TNT,
				ModBlocks.INFUSED_TNT -> {
					ModItems.FLINT_AND_INFERNIUM.igniteTNT(world, pos, null, ignoreTrap = true)
					return
				}
				
				else -> null
			}?.defaultState
		}
		
		if (targetState == null){
			targetState = when(sourceState.block){
				Blocks.ZOMBIE_HEAD,
				Blocks.PLAYER_HEAD -> Blocks.SKELETON_SKULL.with(BlockSkull.ROTATION, sourceState[BlockSkull.ROTATION])
				
				Blocks.ZOMBIE_WALL_HEAD,
				Blocks.PLAYER_WALL_HEAD -> Blocks.SKELETON_WALL_SKULL.with(BlockSkullWall.FACING, sourceState[BlockSkullWall.FACING])
				
				Blocks.SNOW -> {
					if (rand.nextInt(4) == 0)
						Blocks.WATER.with(BlockFlowingFluid.LEVEL, rand.nextInt(5, 6)).also { spreadFluidToNeighbors(pos, it.block, it[BlockFlowingFluid.LEVEL] + 1) }
					else
						Blocks.AIR.defaultState
				}
				
				Blocks.ICE,
				Blocks.FROSTED_ICE,
				Blocks.SNOW_BLOCK -> {
					if (Facing4.any { pos.offset(it).getBlock(world) === Blocks.WATER } || Facing4.all { pos.offset(it).isFullBlock(world) })
						Blocks.WATER.defaultState
					else
						Blocks.WATER.with(BlockFlowingFluid.LEVEL, 1).also { spreadFluidToNeighbors(pos, it.block, 2) }
				}
				
				else -> null
			}
		}
		
		// ternary transformations
		
		if (targetState == null && rand.nextInt(100) < 18){
			targetState = when(sourceState.block){
				Blocks.WATER -> Blocks.COBBLESTONE // flowing water is already covered above
				Blocks.DEAD_BUSH -> Blocks.AIR
				
				Blocks.GLASS,
				Blocks.GLASS_PANE,
				is BlockStainedGlass,
				is BlockStainedGlassPane -> {
					pos.breakBlock(world, false)
					return
				}
				
				else -> null
			}?.defaultState
		}
		
		// final handling
		
		if (targetState != null){
			pos.setState(world, targetState)
			PacketClientFX(FX_BLOCK_SMELT, FxBlockData(pos)).sendToAllAround(this, 32.0)
		}
	}
	
	private fun spreadFluidToNeighbors(pos: BlockPos, block: Block, level: Int){
		val state = block.with(BlockFlowingFluid.LEVEL, level)
		
		for(facing in Facing4){
			val offset = pos.offset(facing)
			
			if (offset.isAir(world) || offset.getBlock(world) === Blocks.SNOW){
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
