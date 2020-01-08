package chylex.hee.game.entity.item
import chylex.hee.game.block.BlockPuzzleLogic
import chylex.hee.game.entity.technical.EntityTechnicalPuzzle
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.fx.FxEntityData
import chylex.hee.game.fx.FxEntityHandler
import chylex.hee.game.particle.ParticleFlameCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.vanilla.BlockCarpet
import chylex.hee.system.migration.vanilla.BlockDoublePlant
import chylex.hee.system.migration.vanilla.BlockFlower
import chylex.hee.system.migration.vanilla.BlockLeaves
import chylex.hee.system.migration.vanilla.BlockSkull
import chylex.hee.system.migration.vanilla.BlockSkullWall
import chylex.hee.system.migration.vanilla.BlockStainedGlass
import chylex.hee.system.migration.vanilla.BlockStainedGlassPane
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.migration.vanilla.ItemBlock
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.facades.Facing4
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.getMaterial
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.isAir
import chylex.hee.system.util.isFullBlock
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.isTopSolid
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextBiasedFloat
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextVector
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.putEnum
import chylex.hee.system.util.scaleXZ
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.setBlock
import chylex.hee.system.util.setFireTicks
import chylex.hee.system.util.setStack
import chylex.hee.system.util.setState
import chylex.hee.system.util.size
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.use
import chylex.hee.system.util.with
import net.minecraft.block.BlockState
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MoverType
import net.minecraft.inventory.Inventory
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
import java.util.Random
import kotlin.math.log2

class EntityItemIgneousRock : EntityItemNoBob{
	@Suppress("unused")
	constructor(type: EntityType<EntityItemIgneousRock>, world: World) : super(type, world)
	
	constructor(world: World, stack: ItemStack, replacee: Entity) : super(ModEntities.ITEM_IGNEOUS_ROCK, world, stack, replacee){
		lifespan = 1200 + (world.rand.nextBiasedFloat(3F) * 1200F).floorToInt()
		throwFacing = Facing4.fromDirection(motionVec)
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
	private var prevMotionVec = Vec3d.ZERO
	
	private val smeltingInventory = Inventory(1)
	
	override fun tick(){
		prevMotionVec = motionVec
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
							randomPos.offsetUntil(DOWN, 1..checkRange){ !it.isAir(world) || it.distanceSqTo(this) > BURN_DISTANCE_SQ }?.up()
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
		if (sound === Sounds.ENTITY_GENERIC_BURN && volume == 0.4F && pitch >= 2.0F){ // UPDATE: find a better way, all item handling has changed anyway
			motionVec = prevMotionVec // this disables vanilla lava handling, but also breaks hasNoGravity
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
		val sourceState = pos.getState(world)
		var targetState: BlockState? = null
		
		// primary transformations
		
		smeltingInventory.setStack(0, ItemStack(sourceState.block))
		val output = world.recipeManager.getRecipe(IRecipeType.SMELTING, smeltingInventory, world).orElse(null)?.recipeOutput
		
		if (output != null && output.isNotEmpty){
			targetState = (output.item as? ItemBlock)?.block?.defaultState // UPDATE test, maybe attempt to clone state where possible
		}
		
		// secondary transformations
		
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
				
				// UPDATE Blocks.FLOWING_WATER,
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
				
				Blocks.SNOW -> {
					if (rand.nextInt(4) == 0)
						Blocks.AIR // UPDATE flowing water
					else
						Blocks.AIR
				}
				
				Blocks.ICE,
				Blocks.FROSTED_ICE,
				Blocks.SNOW -> {
					if (Facing4.any { pos.offset(it).getBlock(world) === Blocks.WATER } || Facing4.all { pos.offset(it).isFullBlock(world) })
						Blocks.WATER
					else
						Blocks.AIR // UPDATE flowing water
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
				
				else -> null
			}
		}
		
		// ternary transformations
		
		if (targetState == null && rand.nextInt(100) < 18){
			targetState = when(sourceState.block){
				Blocks.WATER -> Blocks.COBBLESTONE
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
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		putEnum(FACING_TAG, throwFacing)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		throwFacing = getEnum<Direction>(FACING_TAG) ?: throwFacing
	}
}
