package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityInfusedTNT
import chylex.hee.game.block.util.Property
import chylex.hee.game.entity.item.EntityInfusedTNT
import chylex.hee.game.item.infusion.Infusion.TRAP
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.system.migration.vanilla.BlockTNT
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.getTile
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import chylex.hee.system.util.setAir
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.state.StateContainer.Builder
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameters
import java.util.Random

class BlockInfusedTNT : BlockTNT(Properties.from(Blocks.TNT)){
	companion object{
		val INFERNIUM = Property.bool("infernium")
	}
	
	init{
		defaultState = defaultState.with(INFERNIUM, false)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		super.fillStateContainer(container)
		container.add(INFERNIUM)
	}
	
	override fun hasTileEntity(state: BlockState): Boolean{
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return TileEntityInfusedTNT()
	}
	
	override fun getTranslationKey(): String{
		return Blocks.TNT.translationKey
	}
	
	// Placement & drops
	
	override fun onBlockPlacedBy(world: World, pos: BlockPos, state: BlockState, placer: EntityLivingBase?, stack: ItemStack){
		pos.getTile<TileEntityInfusedTNT>(world)?.infusions = InfusionTag.getList(stack)
	}
	
	private fun getDrop(tile: TileEntityInfusedTNT): ItemStack?{
		return ItemStack(this).also { InfusionTag.setList(it, tile.infusions) }
	}
	
	override fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack>{
		val drop = (context.get(LootParameters.BLOCK_ENTITY) as? TileEntityInfusedTNT)?.let(::getDrop)
		
		return if (drop != null)
			mutableListOf(drop)
		else
			mutableListOf()
	}
	
	override fun getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: EntityPlayer): ItemStack{
		return pos.getTile<TileEntityInfusedTNT>(world)?.let(::getDrop) ?: ItemStack(this)
	}
	
	// Delay block addition/deletion
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean){
		world.pendingBlockTicks.scheduleTick(pos, this, 1)
	}
	
	override fun tick(state: BlockState, world: World, pos: BlockPos, rand: Random){
		super.onBlockAdded(state, world, pos, Blocks.AIR.defaultState, false)
	}
	
	/* UPDATE
	override fun removedByPlayer(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, willHarvest: Boolean): Boolean{
		if (pos.getTile<TileEntityInfusedTNT>(world)?.infusions?.has(TRAP) == true && !player.isCreative){
			explode(world, pos, state.with(EXPLODE, true), player)
			pos.setAir(world)
			return false
		}
		
		if (willHarvest){
			return true // skip super call before drops
		}
		
		return super.removedByPlayer(state, world, pos, player, willHarvest)
	}*/
	
	override fun harvestBlock(world: World, player: EntityPlayer, pos: BlockPos, state: BlockState, tile: TileEntity?, stack: ItemStack){
		super.harvestBlock(world, player, pos, state, tile, stack)
		pos.setAir(world)
	}
	
	// TNT overrides
	
	override fun onExplosionDestroy(world: World, pos: BlockPos, explosion: Explosion){
		if (world.isRemote){
			return
		}
		
		val infusions = pos.getTile<TileEntityInfusedTNT>(world)?.infusions
		
		if (infusions != null){
			world.addEntity(EntityInfusedTNT(world, pos, infusions, explosion))
		}
	}
	
	/* UPDATE
	override fun explode(world: World, pos: BlockPos, state: BlockState, igniter: EntityLivingBase?){
		if (world.isRemote){
			return
		}
		
		igniteTNT(world, pos, state, igniter, ignoreTrap = false)
	}*/
	
	fun igniteTNT(world: World, pos: BlockPos, state: BlockState, igniter: EntityLivingBase?, ignoreTrap: Boolean){
		val infusions = pos.getTile<TileEntityInfusedTNT>(world)?.infusions
		
		if (infusions != null){
			if (infusions.has(TRAP) && world.isBlockPowered(pos) && !ignoreTrap){
				pos.breakBlock(world, true) // UPDATE test
			}
			else{
				EntityInfusedTNT(world, pos, infusions, igniter, state[INFERNIUM]).apply {
					Sounds.ENTITY_TNT_PRIMED.playServer(world, posVec, SoundCategory.BLOCKS)
					world.addEntity(this)
				}
			}
		}
	}
}
