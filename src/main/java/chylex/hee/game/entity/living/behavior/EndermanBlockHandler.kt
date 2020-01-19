package chylex.hee.game.entity.living.behavior
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.entity.living.ai.AIPickUpBlock.IBlockPickUpHandler
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler.TargetBlockType.FULL
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler.TargetBlockType.NONE
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler.TargetBlockType.TRANSPARENT
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.migration.vanilla.EntityItem
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.Pos
import chylex.hee.system.util.Vec3
import chylex.hee.system.util.addY
import chylex.hee.system.util.center
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.getMaterial
import chylex.hee.system.util.getState
import chylex.hee.system.util.isAir
import chylex.hee.system.util.isFullBlock
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.setState
import net.minecraft.block.Block
import net.minecraft.block.BlockState
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
import net.minecraft.util.math.Vec3d
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.dimension.DimensionType
import net.minecraftforge.registries.ForgeRegistries

class EndermanBlockHandler(private val enderman: EntityMobAbstractEnderman) : IBlockPickUpHandler{
	companion object{
		private val SEARCH_AREA = AxisAlignedBB(-15.0, -2.0, -15.0, 15.0, 1.0, 15.0)
		
		private lateinit var GENERIC_CARRIABLE_BLOCKS: Tag<Block>
		private val BIOME_CARRIABLE_BLOCKS = mutableMapOf<Biome, BlockState>()
		
		fun setupCarriableBlocks(){
			GENERIC_CARRIABLE_BLOCKS = Tag.Builder<Block>()
				.add(BlockTags.SMALL_FLOWERS)
				.add(Blocks.GRASS_BLOCK)
				.add(Blocks.DIRT)
				.add(Blocks.COARSE_DIRT)
				.add(Blocks.PODZOL)
				.add(BlockTags.SAND)
				.add(Blocks.CLAY)
				.add(Blocks.PUMPKIN)
				.add(Blocks.MELON)
				.add(BlockTags.SAPLINGS)
				.add(Blocks.TALL_GRASS)
				.add(Blocks.LARGE_FERN)
				.add(Blocks.SUNFLOWER)
				.add(Blocks.LILAC)
				.add(Blocks.ROSE_BUSH)
				.add(Blocks.PEONY)
				.build(Resource.Custom("enderman_carriable"))
			
			BIOME_CARRIABLE_BLOCKS.clear()
			ForgeRegistries.BIOMES.associateWithTo(BIOME_CARRIABLE_BLOCKS){ it.surfaceBuilderConfig.top }
		}
	}
	
	private enum class TargetBlockType{
		NONE        { override fun isValid(world: World, pos: BlockPos) = false },
		FULL        { override fun isValid(world: World, pos: BlockPos) = pos.isFullBlock(world) },
		TRANSPARENT { override fun isValid(world: World, pos: BlockPos) = !pos.isFullBlock(world) };
		
		abstract fun isValid(world: World, pos: BlockPos): Boolean
	}
	
	private val world = enderman.world
	private val rand = enderman.rng
	
	override val canBeginSearch
		get() = enderman.heldBlockState == null
	
	private fun isPlayerInProximity(): Boolean{
		return world.selectVulnerableEntities.inRange<EntityPlayer>(enderman.posVec, 14.0).isNotEmpty()
	}
	
	override fun onBeginSearch(): BlockPos?{
		if (enderman.dimension != DimensionType.OVERWORLD || isPlayerInProximity()){
			return null
		}
		
		val targetType = when(rand.nextInt(100)){
			0       -> FULL
			in 1..5 -> TRANSPARENT
			else    -> NONE
		}
		
		if (targetType == NONE){
			return null
		}
		
		val searchArea = SEARCH_AREA.offset(enderman.posVec)
		
		for(attempt in 1..75){
			val pos = Pos(
				rand.nextFloat(searchArea.minX, searchArea.maxX),
				rand.nextFloat(searchArea.minY, searchArea.maxY),
				rand.nextFloat(searchArea.minZ, searchArea.maxZ)
			)
			
			if (canPickUp(pos, targetType)){
				return pos
			}
		}
		
		return null
	}
	
	private fun canPickUp(pos: BlockPos, targetType: TargetBlockType): Boolean{
		if (!targetType.isValid(world, pos)){
			return false
		}
		
		if (pos.up().getMaterial(world).blocksMovement()){
			return false
		}
		
		if (!FULL.isValid(world, pos) && !FULL.isValid(world, pos.down())){ // make sure it can only pick the bottom block of a double plant
			return false // UPDATE test
		}
		
		val state = pos.getState(world)
		
		if (!state.isIn(GENERIC_CARRIABLE_BLOCKS) && state !== BIOME_CARRIABLE_BLOCKS[world.getBiome(pos)]){
			return false
		}
		
		if (world.getLightFor(BLOCK, pos) > 1){
			return false
		}
		
		val result = world.rayTraceBlocks(RayTraceContext(enderman.lastPortalVec, pos.center, BlockMode.COLLIDER, FluidMode.ANY, enderman))
		return result.type == Type.MISS || result.pos == pos
	}
	
	override fun onBlockReached(state: BlockState){
		enderman.heldBlockState = state
	}
	
	fun tryPlaceBlock(allowPlayerProximity: Boolean): Boolean{
		if (!allowPlayerProximity && isPlayerInProximity()){
			return false
		}
		
		val state = enderman.heldBlockState ?: return true
		val block = state.block
		
		val endermanVec = enderman.posVec
		val endermanPos = Pos(endermanVec)
		
		for(attempt in 1..20){
			val dir = Vec3.fromYaw(enderman.rotationYaw + rand.nextFloat(-60F, 60F))
			val pos = Pos(endermanVec.add(dir.scale(rand.nextFloat(0.5, 2.0))))
			
			if (pos != endermanPos && pos.isAir(world) && pos.down().isFullBlock(world) && state.isValidPosition(world, pos)){
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
	
	fun dropBlockAsItem(){
		val state = enderman.heldBlockState ?: return
		val block = state.block
		
		val (x, y, z) = enderman.posVec.addY(0.55).add(Vec3.fromYaw(enderman.rotationYaw).scale(0.8))
		
		EntityItem(world, x, y, z, ItemStack(block)).apply {
			motion = Vec3d.ZERO
			setDefaultPickupDelay()
			world.addEntity(this)
		}
		
		enderman.heldBlockState = null
	}
}
