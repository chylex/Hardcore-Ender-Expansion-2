package chylex.hee.game.block
import chylex.hee.client.util.MC
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.init.ModBlocks
import chylex.hee.proxy.Environment
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.BlockFire
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.center
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.isPeaceful
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.playClient
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import chylex.hee.system.util.toYaw
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateContainer.Builder
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.DamageSource
import net.minecraft.util.Direction
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.Explosion
import net.minecraft.world.GameRules.DO_MOB_LOOT
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameterSets
import net.minecraft.world.storage.loot.LootParameters
import java.util.Random

open class BlockGraveDirt(builder: BlockBuilder) : BlockSimpleShaped(builder, AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0)){
	companion object{
		val FULL = Property.bool("full")
		
		private fun makeSpiderling(world: World, pos: BlockPos, yaw: Float): EntityMobSpiderling{
			return EntityMobSpiderling(world).apply {
				setLocationAndAngles(pos.x + 0.5, pos.y + 0.01, pos.z + 0.5, yaw, 0F)
			}
		}
	}
	
	// Instance
	
	init{
		defaultState = stateContainer.baseState.with(FULL, true)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(FULL)
	}
	
	// Bounding box
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState{
		return this.with(FULL, context.pos.up().getBlock(context.world) is BlockGraveDirt)
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState{
		return if (facing == UP)
			state.with(FULL, pos.up().getBlock(world) is BlockGraveDirt)
		else
			state
	}
	
	override fun getShape(state: BlockState, source: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape{
		return if (state[FULL])
			VoxelShapes.fullCube()
		else
			super.getShape(state, source, pos, context)
	}
	
	// Explosions
	
	override fun canDropFromExplosion(explosion: Explosion): Boolean{
		return false
	}
	
	override fun onBlockExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion){
		super.onBlockExploded(state, world, pos, explosion)
		
		if (world is ServerWorld){
			LootContext.Builder(world)
				.withRandom(world.rand)
				.withParameter(LootParameters.POSITION, pos)
				.withParameter(LootParameters.EXPLOSION_RADIUS, explosion.size)
				.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
				.withNullableParameter(LootParameters.BLOCK_ENTITY, null)
				.let { spawnDrops(state, it) }
		}
	}
	
	// Variations
	
	class Spiderling(builder: BlockBuilder) : BlockGraveDirt(builder){
		private var clientLastSpiderlingSound = 0L
		
		override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState{
			if (!world.isRemote && !world.isPeaceful && facing == UP && neighborState.block is BlockFire){
				val wrld = world.world
				
				makeSpiderling(wrld, neighborPos, yaw = wrld.rand.nextFloat()).apply {
					health = maxHealth * rng.nextFloat(0.5F, 1F)
					
					setFire(rng.nextInt(6, 7))
					getHurtSound(DamageSource.IN_FIRE).playServer(wrld, neighborPos, soundCategory, volume = 1.2F, pitch = soundPitch)
					
					wrld.addEntity(this)
				}
				
				return ModBlocks.GRAVE_DIRT_PLAIN.defaultState
			}
			
			return super.updatePostPlacement(state, facing, neighborState, world, pos, neighborPos)
		}
		
		override fun harvestBlock(world: World, player: EntityPlayer, pos: BlockPos, state: BlockState, tile: TileEntity?, stack: ItemStack){
			super.harvestBlock(world, player, pos, state, tile, stack)
			
			if (!world.isPeaceful){
				makeSpiderling(world, pos, player.posVec.subtract(pos.center).toYaw()).apply {
					world.addEntity(this)
					attackTarget = player
				}
			}
		}
		
		override fun onExplosionDestroy(world: World, pos: BlockPos, explosion: Explosion){
			super.onExplosionDestroy(world, pos, explosion)
			
			if (world.isRemote){
				makeSpiderling(world, pos, yaw = 0F).apply {
					spawnExplosionParticle()
					deathSound.playClient(pos, soundCategory, volume = 0.8F, pitch = soundPitch)
				}
			}
			else if (world is ServerWorld && world.gameRules.getBoolean(DO_MOB_LOOT)){
				val lootContext = LootContext.Builder(world).withRandom(world.rand).build(LootParameterSets.EMPTY)
				
				for(drop in Environment.getServer().lootTableManager.getLootTableFromLocation(EntityMobSpiderling.LOOT_TABLE).generate(lootContext)){
					spawnAsEntity(world, pos, drop)
				}
			}
		}
		
		@Sided(Side.CLIENT)
		override fun animateTick(state: BlockState, world: World, pos: BlockPos, rand: Random){
			if (!world.isPeaceful && world.totalTime - clientLastSpiderlingSound > 35L){
				val distanceSq = MC.player?.getDistanceSq(pos.center) ?: 0.0
				
				if (rand.nextInt(3 + (distanceSq.floorToInt() / 5)) == 0){
					clientLastSpiderlingSound = world.totalTime
					
					makeSpiderling(world, pos, yaw = 0F).apply {
						ambientSound.playClient(pos, soundCategory, volume = 0.35F, pitch = rand.nextFloat(0.4F, 0.6F))
					}
				}
			}
		}
	}
}
