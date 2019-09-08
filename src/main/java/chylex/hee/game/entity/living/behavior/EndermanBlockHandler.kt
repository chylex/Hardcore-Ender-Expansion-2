package chylex.hee.game.entity.living.behavior
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.entity.living.ai.AIPickUpBlock.IBlockPickUpHandler
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler.TargetBlockType.FULL
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler.TargetBlockType.NONE
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler.TargetBlockType.TRANSPARENT
import chylex.hee.game.world.util.RayTracer
import chylex.hee.system.util.Pos
import chylex.hee.system.util.Vec3
import chylex.hee.system.util.addY
import chylex.hee.system.util.center
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.getMaterial
import chylex.hee.system.util.getState
import chylex.hee.system.util.isAir
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.setState
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.EnumSkyBlock.BLOCK
import net.minecraft.world.biome.Biome

class EndermanBlockHandler(private val enderman: EntityMobAbstractEnderman) : IBlockPickUpHandler{
	companion object{
		private val SEARCH_AREA = AxisAlignedBB(-15.0, -2.0, -15.0, 15.0, 1.0, 15.0)
		
		private val RAY_TRACE_CHECK = RayTracer(
			canCollideCheck = { _, _, state -> state.block.canCollideCheck(state, true) }
		)
		
		private val BIOME_CARRIABLE_BLOCKS = mutableMapOf<Biome, IBlockState>()
		
		fun setupCarriableBlocks(){
			with(EntityEnderman.CARRIABLE_BLOCKS){
				clear()
				add(Blocks.GRASS)
				add(Blocks.DIRT)
				add(Blocks.SAND)
				add(Blocks.CLAY)
				add(Blocks.PUMPKIN)
				add(Blocks.MELON_BLOCK)
				add(Blocks.RED_FLOWER)
				add(Blocks.YELLOW_FLOWER)
				add(Blocks.DOUBLE_PLANT)
				add(Blocks.SAPLING)
			}
			
			BIOME_CARRIABLE_BLOCKS.clear()
			Biome.REGISTRY.associateWithTo(BIOME_CARRIABLE_BLOCKS){ it.topBlock }
		}
	}
	
	private enum class TargetBlockType{
		NONE        { override fun isValid(state: IBlockState) = false },
		FULL        { override fun isValid(state: IBlockState) = state.isFullBlock },
		TRANSPARENT { override fun isValid(state: IBlockState) = !state.isFullBlock };
		
		abstract fun isValid(state: IBlockState): Boolean
	}
	
	private val world = enderman.world
	private val rand = enderman.rng
	
	override val canBeginSearch
		get() = enderman.heldBlockState == null
	
	private fun isPlayerInProximity(): Boolean{
		return world.selectVulnerableEntities.inRange<EntityPlayer>(enderman.posVec, 14.0).isNotEmpty()
	}
	
	override fun onBeginSearch(): BlockPos?{
		if (enderman.dimension != 0 || isPlayerInProximity()){
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
		val state = pos.getState(world)
		
		if (!targetType.isValid(state)){
			return false
		}
		
		if (pos.up().getMaterial(world).blocksMovement()){
			return false
		}
		
		if (!state.isFullBlock && !pos.down().getState(world).isFullBlock){ // make sure it can only pick the bottom block of a double plant
			return false
		}
		
		if (!EntityEnderman.getCarriable(state.block) && state !== BIOME_CARRIABLE_BLOCKS[world.getBiome(pos)]){
			return false
		}
		
		if (world.getLightFor(BLOCK, pos) > 1){
			return false
		}
		
		val result = RAY_TRACE_CHECK.traceBlocksBetweenVectors(world, enderman.lookPosVec, pos.center)
		return result == null || result.blockPos == pos
	}
	
	override fun onBlockReached(state: IBlockState){
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
			
			if (pos != endermanPos && pos.isAir(world) && pos.down().getState(world).isFullBlock && block.canPlaceBlockAt(world, pos)){
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
		
		val item = Item.getItemFromBlock(block)
		val meta = if (item.hasSubtypes) block.getMetaFromState(state) else 0
		
		val (x, y, z) = enderman.posVec.addY(0.55).add(Vec3.fromYaw(enderman.rotationYaw).scale(0.8))
		
		EntityItem(world, x, y, z, ItemStack(item, 1, meta)).apply {
			motionVec = Vec3d.ZERO
			setDefaultPickupDelay()
			world.spawnEntity(this)
		}
		
		enderman.heldBlockState = null
	}
}
