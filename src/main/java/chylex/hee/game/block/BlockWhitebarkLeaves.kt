package chylex.hee.game.block
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.facades.Stats
import chylex.hee.system.util.get
import chylex.hee.system.util.with
import net.minecraft.block.BlockLeaves
import net.minecraft.block.BlockPlanks.EnumType
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.Random

class BlockWhitebarkLeaves(private val sapling: BlockWhitebarkSapling) : BlockLeaves(){
	val worldgenState: IBlockState
	
	init{
		defaultState = blockState.baseState.with(CHECK_DECAY, false).with(DECAYABLE, false)
		worldgenState = defaultState.with(DECAYABLE, true)
		
		leavesFancy = true
	}
	
	override fun createBlockState() = BlockStateContainer(this, CHECK_DECAY, DECAYABLE)
	
	override fun getMetaFromState(state: IBlockState): Int{
		var meta = 0
		
		if (state[CHECK_DECAY]){
			meta += 1
		}
		
		if (state[DECAYABLE]){
			meta += 2
		}
		
		return meta
	}
	
	override fun getStateFromMeta(meta: Int): IBlockState{
		return defaultState
			.with(CHECK_DECAY, (meta and 1) != 0)
			.with(DECAYABLE, (meta and 2) != 0)
	}
	
	override fun getWoodType(meta: Int): EnumType?{
		return null // TODO bad idea?
	}
	
	// Leaf behavior
	
	override fun getSaplingDropChance(state: IBlockState): Int{
		return 20
	}
	
	override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item{
		return Item.getItemFromBlock(sapling)
	}
	
	override fun quantityDropped(rand: Random): Int{
		return if (rand.nextInt(getSaplingDropChance(defaultState)) == 0) 1 else 0
	}
	
	override fun onSheared(item: ItemStack, world: IBlockAccess, pos: BlockPos, fortune: Int): MutableList<ItemStack>{
		return mutableListOf(ItemStack(this))
	}
	
	override fun harvestBlock(world: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, tile: TileEntity?, stack: ItemStack){
		if (!world.isRemote && stack.item === Items.SHEARS){
			player.addStat(Stats.harvestBlock(this))
		}
		else{
			super.harvestBlock(world, player, pos, state, tile, stack)
		}
	}
}
