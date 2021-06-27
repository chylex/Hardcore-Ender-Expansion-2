package chylex.hee.game.entity.living.behavior

import chylex.hee.HEE
import chylex.hee.game.entity.isAnyVulnerablePlayerWithinRange
import chylex.hee.game.entity.isInOverworldDimension
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.entity.living.ai.AIPickUpBlock.IBlockPickUpHandler
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler.TargetBlockType.FULL
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler.TargetBlockType.NONE
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler.TargetBlockType.TRANSPARENT
import chylex.hee.game.entity.lookPosVec
import chylex.hee.game.entity.posVec
import chylex.hee.game.world.Pos
import chylex.hee.game.world.center
import chylex.hee.game.world.getMaterial
import chylex.hee.game.world.getState
import chylex.hee.game.world.isAir
import chylex.hee.game.world.isFullBlock
import chylex.hee.game.world.playServer
import chylex.hee.game.world.setState
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.addY
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.random.nextFloat
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.tags.BlockTags
import net.minecraft.tags.Tag
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceContext.BlockMode
import net.minecraft.util.math.RayTraceContext.FluidMode
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.World
import net.minecraftforge.event.TagsUpdatedEvent

class EndermanBlockHandler(private val enderman: EntityMobAbstractEnderman) : IBlockPickUpHandler {
	@SubscribeAllEvents(modid = HEE.ID)
	companion object {
		private val SEARCH_AREA = AxisAlignedBB(-15.0, -2.0, -15.0, 15.0, 1.0, 15.0)
		
		private lateinit var GENERIC_CARRIABLE_BLOCKS: Tag<Block>
		
		@SubscribeEvent(priority = EventPriority.LOWEST)
		fun onTagsUpdated(e: TagsUpdatedEvent) {
			GENERIC_CARRIABLE_BLOCKS = Tag.getTagFromContents(setOf(
				*BlockTags.SAND.allElements.toTypedArray(),
				*BlockTags.SAPLINGS.allElements.toTypedArray(),
				*BlockTags.SMALL_FLOWERS.allElements.toTypedArray(),
				Blocks.CLAY,
				Blocks.COARSE_DIRT,
				Blocks.DIRT,
				Blocks.GRASS_BLOCK,
				Blocks.LARGE_FERN,
				Blocks.LILAC,
				Blocks.MELON,
				Blocks.PEONY,
				Blocks.PODZOL,
				Blocks.PUMPKIN,
				Blocks.ROSE_BUSH,
				Blocks.SUNFLOWER,
				Blocks.TALL_GRASS,
			))
		}
	}
	
	private enum class TargetBlockType {
		NONE        { override fun isValid(world: World, pos: BlockPos) = false },
		FULL        { override fun isValid(world: World, pos: BlockPos) = pos.isFullBlock(world) },
		TRANSPARENT { override fun isValid(world: World, pos: BlockPos) = !pos.isFullBlock(world) };
		
		abstract fun isValid(world: World, pos: BlockPos): Boolean
	}
	
	private val world = enderman.world
	private val rand = enderman.rng
	
	override val canBeginSearch
		get() = enderman.heldBlockState == null
	
	private fun isPlayerInProximity(): Boolean {
		return enderman.isAnyVulnerablePlayerWithinRange(14.0)
	}
	
	override fun onBeginSearch(): BlockPos? {
		if (!enderman.isInOverworldDimension || isPlayerInProximity()) {
			return null
		}
		
		val targetType = when(rand.nextInt(100)) {
			0       -> FULL
			in 1..5 -> TRANSPARENT
			else    -> NONE
		}
		
		if (targetType == NONE) {
			return null
		}
		
		val searchArea = SEARCH_AREA.offset(enderman.posVec)
		
		for(attempt in 1..75) {
			val pos = Pos(
				rand.nextFloat(searchArea.minX, searchArea.maxX),
				rand.nextFloat(searchArea.minY, searchArea.maxY),
				rand.nextFloat(searchArea.minZ, searchArea.maxZ)
			)
			
			if (canPickUp(pos, targetType)) {
				return pos
			}
		}
		
		return null
	}
	
	private fun canPickUp(pos: BlockPos, targetType: TargetBlockType): Boolean {
		if (!targetType.isValid(world, pos)) {
			return false
		}
		
		if (pos.up().getMaterial(world).blocksMovement()) {
			return false
		}
		
		if (!FULL.isValid(world, pos) && !FULL.isValid(world, pos.down())) { // make sure it can only pick the bottom block of a double plant
			return false
		}
		
		val state = pos.getState(world)
		
		if (!state.isIn(GENERIC_CARRIABLE_BLOCKS) && state !== world.getBiome(pos).generationSettings.surfaceBuilderConfig.top) {
			return false
		}
		
		if (world.getLightFor(BLOCK, pos) > 1) {
			return false
		}
		
		val result = world.rayTraceBlocks(RayTraceContext(enderman.lookPosVec, pos.center, BlockMode.COLLIDER, FluidMode.ANY, enderman))
		return result.type == Type.MISS || result.pos == pos
	}
	
	override fun onBlockReached(state: BlockState) {
		enderman.heldBlockState = state
	}
	
	fun tryPlaceBlock(allowPlayerProximity: Boolean): Boolean {
		if (!allowPlayerProximity && isPlayerInProximity()) {
			return false
		}
		
		val state = enderman.heldBlockState ?: return true
		val block = state.block
		
		val endermanVec = enderman.posVec
		val endermanPos = Pos(endermanVec)
		
		for(attempt in 1..20) {
			val dir = Vec3.fromYaw(enderman.rotationYaw + rand.nextFloat(-60F, 60F))
			val pos = Pos(endermanVec.add(dir.scale(rand.nextFloat(0.5, 2.0))))
			
			if (pos != endermanPos && pos.isAir(world) && pos.down().isFullBlock(world) && state.isValidPosition(world, pos)) {
				pos.setState(world, state)
				block.onBlockPlacedBy(world, pos, state, enderman, ItemStack(block))
				
				block.getSoundType(state, world, pos, enderman).let {
					it.placeSound.playServer(world, pos, SoundCategory.BLOCKS, volume = (it.getVolume() + 1F) / 2F, pitch = it.getPitch() * 0.8F)
				}
				
				enderman.heldBlockState = null
				return true
			}
		}
		
		return false
	}
	
	fun dropBlockAsItem() {
		val state = enderman.heldBlockState ?: return
		val block = state.block
		
		val (x, y, z) = enderman.posVec.addY(0.55).add(Vec3.fromYaw(enderman.rotationYaw).scale(0.8))
		
		EntityItem(world, x, y, z, ItemStack(block)).apply {
			motion = Vec3.ZERO
			setDefaultPickupDelay()
			world.addEntity(this)
		}
		
		enderman.heldBlockState = null
	}
}
