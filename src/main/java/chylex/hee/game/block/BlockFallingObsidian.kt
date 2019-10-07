package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.entity.item.EntityFallingBlockHeavy
import chylex.hee.game.entity.item.EntityFallingObsidian
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.setAir
import chylex.hee.system.util.setBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockFalling
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class BlockFallingObsidian(builder: BlockBuilder) : BlockSimple(builder){
	override fun tickRate(world: World): Int{
		return 2
	}
	
	override fun onBlockAdded(world: World, pos: BlockPos, state: IBlockState){
		world.scheduleUpdate(pos, this, tickRate(world))
	}
	
	override fun neighborChanged(state: IBlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos){
		world.scheduleUpdate(pos, this, tickRate(world))
	}
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		if (world.isRemote){
			return
		}
		
		if (EntityFallingBlockHeavy.canFallThrough(world, pos.down()) && pos.y >= 0){
			if (!BlockFalling.fallInstantly && world.isAreaLoaded(pos, 32)){
				world.spawnEntity(EntityFallingObsidian(world, pos, defaultState))
			}
			else{
				pos.setAir(world)
				pos.offsetUntil(DOWN, 2..(pos.y)){ !EntityFallingBlockHeavy.canFallThrough(world, it) }?.up()?.setBlock(world, this)
			}
		}
	}
	
	override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item{
		return Item.getItemFromBlock(Blocks.OBSIDIAN)
	}
	
	override fun canSilkHarvest(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer): Boolean{
		return false
	}
}
