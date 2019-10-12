package chylex.hee.game.entity.item
import chylex.hee.HEE
import chylex.hee.game.block.BlockPuzzleLogic
import chylex.hee.game.entity.technical.EntityTechnicalPuzzle
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
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.MagicValues
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.facades.Facing4
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.getFaceShape
import chylex.hee.system.util.getMaterial
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.isAir
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextBiasedFloat
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextVector
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.setBlock
import chylex.hee.system.util.setEnum
import chylex.hee.system.util.setFireTicks
import chylex.hee.system.util.setState
import chylex.hee.system.util.size
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.with
import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockSilverfish
import net.minecraft.block.BlockSponge
import net.minecraft.block.BlockStoneBrick
import net.minecraft.block.BlockWall
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockFaceShape.SOLID
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.MoverType
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumFacing
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
		
		private const val FACING_TAG = "Facing"
		
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
		
		private lateinit var smeltingTransformations: Map<IBlockState, IBlockState>
		
		fun setupSmeltingTransformations(){
			smeltingTransformations = FurnaceRecipes.instance().smeltingList.map(::convertToBlockStatePair).filterNotNull().toMap()
			
			for((from, to) in smeltingTransformations){
				HEE.log.info("[EntityItemIgneousRock] found smelting transformation: $from -> $to")
			}
		}
		
		@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
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
		throwFacing = Facing4.fromDirection(motionVec)
	}
	
	private var throwFacing = DOWN
	private var prevMotionVec = Vec3d.ZERO
	
	init{
		isImmuneToFire = true
	}
	
	override fun onUpdate(){
		prevMotionVec = motionVec
		super.onUpdate()
		
		val currentPos = Pos(this)
		
		if (!world.isRemote && age > 4 && (world.totalTime - 1L) % BlockPuzzleLogic.UPDATE_RATE == 0L){
			val posBelow = currentPos.down()
			
			if (posBelow.getBlock(world) is BlockPuzzleLogic){
				val entity = EntityTechnicalPuzzle(world)
				
				if (entity.startChain(posBelow, throwFacing)){
					world.spawnEntity(entity)
					
					val reducedStack = item.apply { shrink(1) }.takeIf { it.isNotEmpty }
					
					if (reducedStack == null){
						setDead()
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
					
					if (randomTopBlock != null && randomTopBlock.isAir(world) && randomTopBlock.down().getFaceShape(world, UP) == SOLID){
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
		if (sound === Sounds.ENTITY_GENERIC_BURN && volume == 0.4F && pitch >= 2.0F){ // UPDATE: find a better way, all item handling has changed anyway
			motionVec = prevMotionVec // this disables vanilla lava handling, but also breaks hasNoGravity
		}
		else{
			super.playSound(sound, volume, pitch)
		}
	}
	
	override fun isEntityInvulnerable(source: DamageSource): Boolean{
		return super.isEntityInvulnerable(source) || source.isFireDamage
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
		var targetState: IBlockState?
		
		// primary transformations
		
		targetState = smeltingTransformations[sourceState]
		
		// secondary transformations
		
		if (targetState == null){
			targetState = when(sourceState.block){
				Blocks.MOSSY_COBBLESTONE -> Blocks.COBBLESTONE
				Blocks.PACKED_ICE -> Blocks.ICE
				Blocks.GRASS -> Blocks.DIRT
				Blocks.FARMLAND -> Blocks.DIRT
				Blocks.DIRT -> Blocks.DIRT.takeIf { sourceState[BlockDirt.VARIANT] == BlockDirt.DirtType.PODZOL }
				Blocks.STONEBRICK -> Blocks.STONEBRICK.takeIf { sourceState[BlockStoneBrick.VARIANT] == BlockStoneBrick.EnumType.MOSSY }
				Blocks.COBBLESTONE_WALL -> Blocks.COBBLESTONE_WALL.takeIf { sourceState[BlockWall.VARIANT] == BlockWall.EnumType.MOSSY }
				Blocks.SPONGE -> Blocks.SPONGE.takeIf { sourceState[BlockSponge.WET] }
				
				Blocks.FLOWING_WATER,
				Blocks.TRIPWIRE,
				Blocks.BROWN_MUSHROOM,
				Blocks.RED_MUSHROOM -> Blocks.AIR
				
				Blocks.CARPET,
				Blocks.VINE,
				Blocks.WEB,
				ModBlocks.DRY_VINES,
				ModBlocks.ANCIENT_COBWEB -> Blocks.FIRE
				
				Blocks.RED_FLOWER,
				Blocks.YELLOW_FLOWER -> Blocks.DEADBUSH
				
				Blocks.DOUBLE_PLANT -> {
					if (pos.up().getBlock(world) !== Blocks.DOUBLE_PLANT){
						return
					}
					
					Blocks.DEADBUSH
				}
				
				Blocks.LEAVES,
				Blocks.LEAVES2 -> {
					val posAbove = pos.up()
					
					if (posAbove.isAir(world)){
						posAbove.setBlock(world, Blocks.FIRE)
						return
					}
					
					Blocks.FIRE
				}
				
				Blocks.SNOW_LAYER -> {
					if (rand.nextInt(4) == 0)
						Blocks.FLOWING_WATER
					else
						Blocks.AIR
				}
				
				Blocks.ICE,
				Blocks.FROSTED_ICE,
				Blocks.SNOW -> {
					if (Facing4.any { pos.offset(it).getBlock(world) === Blocks.WATER } || Facing4.all { pos.offset(it).getState(world).isFullBlock })
						Blocks.WATER
					else
						Blocks.FLOWING_WATER
				}
				
				Blocks.SKULL -> {
					pos.getTile<TileEntitySkull>(world)?.takeIf { it.skullType == MagicValues.SKULL_TYPE_ZOMBIE || (it.skullType == MagicValues.SKULL_TYPE_PLAYER && it.playerProfile == null) }?.let {
						it.setType(MagicValues.SKULL_TYPE_SKELETON)
						it.markDirty()
						world.notifyBlockUpdate(pos, sourceState, sourceState, FLAG_SYNC_CLIENT)
					}
					
					return
				}
				
				Blocks.TNT,
				ModBlocks.INFUSED_TNT -> {
					ModItems.FLINT_AND_INFERNIUM.igniteTNT(world, pos, null, ignoreTrap = true)
					return
				}
				
				else -> null
			}?.defaultState
		}
		
		if (targetState == null && sourceState.block === Blocks.MONSTER_EGG && sourceState[BlockSilverfish.VARIANT] == BlockSilverfish.EnumType.MOSSY_STONEBRICK){
			targetState = Blocks.MONSTER_EGG.with(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONEBRICK)
		}
		
		// ternary transformations
		
		if (targetState == null && rand.nextInt(100) < 18){
			targetState = when(sourceState.block){
				Blocks.WATER -> Blocks.COBBLESTONE
				Blocks.DEADBUSH -> Blocks.AIR
				
				Blocks.GLASS,
				Blocks.GLASS_PANE,
				Blocks.STAINED_GLASS,
				Blocks.STAINED_GLASS_PANE -> {
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
	
	override fun writeEntityToNBT(nbt: TagCompound) = with(nbt.heeTag){
		setEnum(FACING_TAG, throwFacing)
	}
	
	override fun readEntityFromNBT(nbt: TagCompound) = with(nbt.heeTag){
		throwFacing = getEnum<EnumFacing>(FACING_TAG) ?: throwFacing
	}
}
