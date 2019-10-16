package chylex.hee.game.world.structure.world
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.BlockUpdateStructureTrigger
import chylex.hee.game.world.util.Transform
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.getState
import chylex.hee.system.util.setState
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class WorldToStructureWorldAdapter(private val world: World, override val rand: Random, private val offset: BlockPos) : IStructureWorld{
	private companion object{
		private val REQUIRE_SECOND_PASS = setOf<Block>(
			Blocks.WOODEN_BUTTON,
			Blocks.STONE_BUTTON,
			Blocks.TORCH,
			Blocks.REDSTONE_TORCH,
			Blocks.UNLIT_REDSTONE_TORCH,
			Blocks.REDSTONE_WIRE,
			Blocks.VINE,
			ModBlocks.DRY_VINES
		)
		
		private val REQUIRE_IMMEDIATE_UPDATE = setOf<Block>(
			Blocks.LAVA,
			Blocks.WATER,
			ModBlocks.ENDER_GOO,
			ModBlocks.PURIFIED_ENDER_GOO
		)
	}
	
	private val secondPass = mutableMapOf<BlockPos, IBlockState>()
	
	override fun getState(pos: BlockPos): IBlockState{
		return secondPass[pos] ?: pos.add(offset).getState(world)
	}
	
	override fun setState(pos: BlockPos, state: IBlockState){
		val block = state.block
		
		if (REQUIRE_SECOND_PASS.contains(block)){
			secondPass[pos] = state
			return
		}
		
		val worldPos = pos.add(offset)
		worldPos.setState(world, state, FLAG_SYNC_CLIENT)
		
		if (REQUIRE_IMMEDIATE_UPDATE.contains(block)){
			BlockUpdateStructureTrigger(state).realize(world, worldPos, Transform.NONE)
		}
	}
	
	override fun addTrigger(pos: BlockPos, trigger: IStructureTrigger){
		trigger.setup(this, pos, Transform.NONE)
		trigger.realize(world, pos.add(offset), Transform.NONE)
	}
	
	override fun finalize(){
		for((pos, state) in secondPass){
			pos.add(offset).setState(world, state, FLAG_SYNC_CLIENT)
		}
		
		secondPass.clear()
	}
}
