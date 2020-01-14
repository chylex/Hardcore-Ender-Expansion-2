package chylex.hee.game.block
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.LEVEL
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.item.ItemDeathFlower
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.setState
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import net.minecraft.world.storage.loot.LootContext
import java.util.Random

class BlockFlowerPotDeathFlowerDecaying(builder: BlockBuilder, private val flower: Block) : BlockFlowerPotCustom(builder, flower), IBlockDeathFlowerDecaying{
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(LEVEL)
	}
	
	override val thisAsBlock
		get() = this
	
	override val healedFlowerBlock
		get() = ModBlocks.POTTED_DEATH_FLOWER_HEALED
	
	override val witheredFlowerBlock
		get() = ModBlocks.POTTED_DEATH_FLOWER_WITHERED
	
	override fun tickRate(world: IWorldReader): Int{
		return implTickRate()
	}
	
	override fun onBlockPlacedBy(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack){
		pos.setState(world, state.with(LEVEL, ItemDeathFlower.getDeathLevel(stack))) // UPDATE broken
	}
	
	private fun getDrop(state: BlockState): ItemStack{
		return ItemStack(flower).also { ItemDeathFlower.setDeathLevel(it, state[LEVEL]) }
	}
	
	override fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack>{
		return mutableListOf(ItemStack(Blocks.FLOWER_POT), getDrop(state))
	}
	
	override fun getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: EntityPlayer): ItemStack{
		return getDrop(state)
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean){
		super.onBlockAdded(state, world, pos, oldState, isMoving)
		implOnBlockAdded(world, pos)
	}
	
	override fun tick(state: BlockState, world: World, pos: BlockPos, rand: Random){
		super.tick(state, world, pos, rand)
		implUpdateTick(world, pos, state, rand)
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, hand: Hand, hit: BlockRayTraceResult): Boolean{
		if (player.getHeldItem(hand).item === ModItems.END_POWDER){
			return false
		}
		
		return super.onBlockActivated(state, world, pos, player, hand, hit)
	}
}
