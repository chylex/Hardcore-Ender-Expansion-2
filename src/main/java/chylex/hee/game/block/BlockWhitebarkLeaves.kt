package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.vanilla.BlockLeaves
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.facades.Stats
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class BlockWhitebarkLeaves(builder: BlockBuilder, private val sapling: BlockWhitebarkSapling) : BlockLeaves(builder.p){
	/* UPDATE
	override fun getSaplingDropChance(state: BlockState): Int{
		return 20
	}
	
	override fun getItemDropped(state: BlockState, rand: Random, fortune: Int): Item{
		return Item.getItemFromBlock(sapling)
	}
	
	override fun quantityDropped(rand: Random): Int{
		return if (rand.nextInt(getSaplingDropChance(defaultState)) == 0) 1 else 0
	}
	
	override fun onSheared(item: ItemStack, world: IBlockReader, pos: BlockPos, fortune: Int): MutableList<ItemStack>{
		return mutableListOf(ItemStack(this))
	}*/
	
	override fun harvestBlock(world: World, player: EntityPlayer, pos: BlockPos, state: BlockState, tile: TileEntity?, stack: ItemStack){
		if (!world.isRemote && stack.item === Items.SHEARS){
			player.addStat(Stats.harvestBlock(this))
		}
		else{
			super.harvestBlock(world, player, pos, state, tile, stack)
		}
	}
	
	override fun canEntitySpawn(state: BlockState, world: IBlockReader, pos: BlockPos, entity: EntityType<*>): Boolean{
		return false
	}
	
	override fun getRenderLayer(): BlockRenderLayer{
		return CUTOUT_MIPPED
	}
}
