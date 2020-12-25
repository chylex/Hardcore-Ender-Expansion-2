package chylex.hee.game.block

import chylex.hee.game.block.entity.TileEntityInfusedTNT
import chylex.hee.game.block.logic.IBlockFireCatchOverride
import chylex.hee.game.block.properties.Property
import chylex.hee.game.entity.item.EntityInfusedTNT
import chylex.hee.game.entity.posVec
import chylex.hee.game.item.infusion.Infusion.TRAP
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.world.breakBlock
import chylex.hee.game.world.getState
import chylex.hee.game.world.getTile
import chylex.hee.game.world.playServer
import chylex.hee.game.world.removeBlock
import chylex.hee.system.migration.BlockTNT
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Sounds
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.fluid.IFluidState
import net.minecraft.item.ItemStack
import net.minecraft.state.StateContainer.Builder
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.Direction.UP
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameters
import java.util.Random

class BlockInfusedTNT : BlockTNT(Properties.from(Blocks.TNT)), IBlockFireCatchOverride {
	companion object {
		val INFERNIUM = Property.bool("infernium")
	}
	
	init {
		defaultState = defaultState.with(INFERNIUM, false)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		super.fillStateContainer(container)
		container.add(INFERNIUM)
	}
	
	override fun hasTileEntity(state: BlockState): Boolean {
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityInfusedTNT()
	}
	
	override fun getTranslationKey(): String {
		return Blocks.TNT.translationKey
	}
	
	// Placement & drops
	
	override fun onBlockPlacedBy(world: World, pos: BlockPos, state: BlockState, placer: EntityLivingBase?, stack: ItemStack) {
		pos.getTile<TileEntityInfusedTNT>(world)?.infusions = InfusionTag.getList(stack)
	}
	
	private fun getDrop(tile: TileEntityInfusedTNT): ItemStack? {
		return ItemStack(this).also { InfusionTag.setList(it, tile.infusions) }
	}
	
	override fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack> {
		val drop = (context.get(LootParameters.BLOCK_ENTITY) as? TileEntityInfusedTNT)?.let(::getDrop)
		
		return if (drop != null)
			mutableListOf(drop)
		else
			mutableListOf()
	}
	
	override fun getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: EntityPlayer): ItemStack {
		return pos.getTile<TileEntityInfusedTNT>(world)?.let(::getDrop) ?: ItemStack(this)
	}
	
	// Delay block addition/deletion
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
		world.pendingBlockTicks.scheduleTick(pos, this, 1)
	}
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random) {
		super.onBlockAdded(state, world, pos, Blocks.AIR.defaultState, false)
	}
	
	override fun removedByPlayer(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, willHarvest: Boolean, fluid: IFluidState): Boolean {
		if (pos.getTile<TileEntityInfusedTNT>(world)?.infusions?.has(TRAP) == true && !player.isCreative) {
			catchFire(state, world, pos, null, player)
			pos.removeBlock(world)
			return false
		}
		
		return super.removedByPlayer(state, world, pos, player, willHarvest, fluid)
	}
	
	// TNT overrides
	
	override fun tryCatchFire(world: World, pos: BlockPos, chance: Int, rand: Random) {
		val state = pos.getState(world)
		
		if (rand.nextInt(chance) < state.getFlammability(world, pos, UP)) {
			igniteTNT(world, pos, state, null, ignoreTrap = true)
			pos.removeBlock(world)
		}
	}
	
	override fun catchFire(state: BlockState, world: World, pos: BlockPos, face: Direction?, igniter: EntityLivingBase?) {
		if (world.isRemote) {
			return
		}
		
		// UPDATE 1.15 check if FireBlock still removes the TNT block and tile entity before calling this
		igniteTNT(world, pos, state, igniter, ignoreTrap = false)
	}
	
	override fun onBlockExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion) {
		if (world.isRemote) {
			return
		}
		
		val infusions = pos.getTile<TileEntityInfusedTNT>(world)?.infusions
		
		if (infusions != null) {
			world.addEntity(EntityInfusedTNT(world, pos, infusions, explosion))
		}
		
		super.onBlockExploded(state, world, pos, explosion)
	}
	
	override fun onExplosionDestroy(world: World, pos: BlockPos, explosion: Explosion) {
		// disable super call and spawn TNT in onBlockExploded before the tile entity is removed
	}
	
	fun igniteTNT(world: World, pos: BlockPos, state: BlockState, igniter: EntityLivingBase?, ignoreTrap: Boolean) {
		val infusions = pos.getTile<TileEntityInfusedTNT>(world)?.infusions
		
		if (infusions != null) {
			if (infusions.has(TRAP) && world.isBlockPowered(pos) && !ignoreTrap) {
				pos.breakBlock(world, true)
			}
			else {
				EntityInfusedTNT(world, pos, infusions, igniter, state[INFERNIUM]).apply {
					Sounds.ENTITY_TNT_PRIMED.playServer(world, posVec, SoundCategory.BLOCKS)
					world.addEntity(this)
				}
			}
		}
	}
}
