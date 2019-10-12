package chylex.hee.game.entity.technical
import chylex.hee.game.block.BlockPuzzleLogic
import chylex.hee.init.ModItems
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.getPos
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.max
import chylex.hee.system.util.min
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.selectEntities
import chylex.hee.system.util.setEnum
import chylex.hee.system.util.setPos
import chylex.hee.system.util.setState
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.with
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class EntityTechnicalPuzzle(world: World) : EntityTechnicalBase(world){
	private companion object{
		private const val START_POS_TAG = "StartPos"
		private const val FACING_TAG = "Facing"
	}
	
	private var startPos = BlockPos.ORIGIN
	private var facing = DOWN
	
	override fun entityInit(){}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (world.isRemote){
			return
		}
		
		if (ticksExisted == 1 && hasConflict()){
			setDead()
			return
		}
		
		val pos = Pos(this)
		
		if (world.isAreaLoaded(pos, BlockPuzzleLogic.MAX_SIZE) && ticksExisted > 5 && world.totalTime % BlockPuzzleLogic.UPDATE_RATE == 0L){
			moveToBlockAndToggle(pos)
		}
	}
	
	private fun hasConflict(): Boolean{
		return world.selectEntities.inBox<EntityTechnicalPuzzle>(AxisAlignedBB(Pos(this))).any { it !== this && it.facing == facing && !it.isDead }
	}
	
	private fun setPosition(pos: BlockPos){
		setPosition(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
	}
	
	private fun moveToBlockAndToggle(pos: BlockPos){
		val block = pos.getBlock(world)
		
		if (block is BlockPuzzleLogic){
			setPosition(pos)
			
			val nextChains = block.onToggled(world, pos, facing)
			
			if (nextChains.isEmpty()){
				endChain()
			}
			else{
				setPosition(nextChains[0].first)
				facing = nextChains[0].second
				
				if (hasConflict()){
					setDead()
				}
				
				for(index in 1 until nextChains.size){
					EntityTechnicalPuzzle(world).apply {
						if (startChain(nextChains[index].first, nextChains[index].second)){
							world.spawnEntity(this)
						}
					}
				}
			}
		}
		else{
			endChain()
		}
	}
	
	private fun isBlockingSolution(allBlocks: List<Pair<BlockPos, IBlockState>>, other: EntityTechnicalPuzzle): Boolean{
		if (other.isDead){
			return false
		}
		
		val entityPos = Pos(other)
		return allBlocks.any { (pos, _) -> pos == entityPos }
	}
	
	fun startChain(pos: BlockPos, facing: EnumFacing): Boolean{
		val state = pos.getState(world)
		val block = state.block
		
		if (block !is BlockPuzzleLogic || state[BlockPuzzleLogic.STATE] == BlockPuzzleLogic.State.DISABLED){
			return false
		}
		
		this.startPos = pos
		this.facing = facing
		setPosition(pos)
		return !hasConflict()
	}
	
	private fun endChain(){
		setDead()
		
		val allBlocks = BlockPuzzleLogic.findAllBlocks(world, startPos)
		val entityArea = BlockPuzzleLogic.MAX_SIZE.toDouble().let { AxisAlignedBB(startPos).grow(it, 0.0, it) }
		
		if (world.selectEntities.inBox<EntityTechnicalPuzzle>(entityArea).any { isBlockingSolution(allBlocks, it) }){
			return
		}
		
		if (allBlocks.isNotEmpty() && allBlocks.all { it.second[BlockPuzzleLogic.STATE] == BlockPuzzleLogic.State.ACTIVE }){
			allBlocks.forEach { it.first.setState(world, it.second.with(BlockPuzzleLogic.STATE, BlockPuzzleLogic.State.DISABLED)) }
			
			val min = allBlocks.fold(allBlocks[0].first){ acc, (pos, _) -> acc.min(pos) }
			val max = allBlocks.fold(allBlocks[0].first){ acc, (pos, _) -> acc.max(pos) }
			
			// TODO will need to be adjusted for different shapes
			// TODO fx
			
			EntityItem(world, (min.x + max.x + 1) / 2.0, posY + 1.5, (min.z + max.z + 1) / 2.0, ItemStack(ModItems.PUZZLE_MEDALLION)).apply {
				motionVec = Vec3d.ZERO
				world.spawnEntity(this)
			}
		}
	}
	
	override fun writeEntityToNBT(nbt: TagCompound) = with(nbt.heeTag){
		setPos(START_POS_TAG, startPos)
		setEnum(FACING_TAG, facing)
	}
	
	override fun readEntityFromNBT(nbt: TagCompound) = with(nbt.heeTag){
		startPos = getPos(START_POS_TAG)
		facing = getEnum<EnumFacing>(FACING_TAG) ?: facing
	}
}
