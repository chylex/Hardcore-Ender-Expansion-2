package chylex.hee.game.entity.technical
import chylex.hee.game.block.BlockPuzzleLogic
import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.vanilla.EntityItem
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.getPos
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.max
import chylex.hee.system.util.min
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.putEnum
import chylex.hee.system.util.putPos
import chylex.hee.system.util.selectEntities
import chylex.hee.system.util.setState
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.use
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class EntityTechnicalPuzzle(type: EntityType<EntityTechnicalPuzzle>, world: World) : EntityTechnicalBase(type, world){
	constructor(world: World) : this(ModEntities.TECHNICAL_PUZZLE, world)
	
	private companion object{
		private const val START_POS_TAG = "StartPos"
		private const val FACING_TAG = "Facing"
	}
	
	private var startPos = BlockPos.ZERO
	private var facing = DOWN
	
	override fun registerData(){}
	
	override fun tick(){
		super.tick()
		
		if (world.isRemote){
			return
		}
		
		if (ticksExisted == 1 && hasConflict()){
			remove()
			return
		}
		
		val pos = Pos(this)
		
		if (world.isAreaLoaded(pos, BlockPuzzleLogic.MAX_SIZE) && ticksExisted > 5 && world.totalTime % BlockPuzzleLogic.UPDATE_RATE == 0L){
			moveToBlockAndToggle(pos)
		}
	}
	
	private fun hasConflict(): Boolean{
		return world.selectEntities.inBox<EntityTechnicalPuzzle>(AxisAlignedBB(Pos(this))).any { it !== this && it.facing == facing && it.isAlive }
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
					remove()
				}
				
				for(index in 1 until nextChains.size){
					EntityTechnicalPuzzle(world).apply {
						if (startChain(nextChains[index].first, nextChains[index].second)){
							world.addEntity(this)
						}
					}
				}
			}
		}
		else{
			endChain()
		}
	}
	
	private fun isBlockingSolution(allBlocks: List<BlockPos>, other: EntityTechnicalPuzzle): Boolean{
		if (!other.isAlive){
			return false
		}
		
		val entityPos = Pos(other)
		return allBlocks.any { it == entityPos }
	}
	
	fun startChain(pos: BlockPos, facing: Direction): Boolean{
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
		remove()
		
		val allBlocks = BlockPuzzleLogic.findAllBlocks(world, startPos)
		val entityArea = BlockPuzzleLogic.MAX_SIZE.toDouble().let { AxisAlignedBB(startPos).grow(it, 0.0, it) }
		
		if (allBlocks.isEmpty() || world.selectEntities.inBox<EntityTechnicalPuzzle>(entityArea).any { isBlockingSolution(allBlocks, it) }){
			return
		}
		
		if (allBlocks.all { it.getState(world)[BlockPuzzleLogic.STATE] == BlockPuzzleLogic.State.ACTIVE }){
			allBlocks.forEach { it.setState(world, it.getState(world).with(BlockPuzzleLogic.STATE, BlockPuzzleLogic.State.DISABLED)) }
			
			val min = allBlocks.reduce(BlockPos::min)
			val max = allBlocks.reduce(BlockPos::max)
			
			// TODO will need to be adjusted for different shapes
			// TODO fx
			
			EntityItem(world, (min.x + max.x + 1) / 2.0, posY + 1.5, (min.z + max.z + 1) / 2.0, ItemStack(ModItems.PUZZLE_MEDALLION)).apply {
				motionVec = Vec3d.ZERO
				world.addEntity(this)
			}
		}
	}
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		putPos(START_POS_TAG, startPos)
		putEnum(FACING_TAG, facing)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		startPos = getPos(START_POS_TAG)
		facing = getEnum<Direction>(FACING_TAG) ?: facing
	}
}
