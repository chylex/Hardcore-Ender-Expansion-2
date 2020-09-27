package chylex.hee.game.entity.technical
import chylex.hee.game.block.BlockPuzzleLogic
import chylex.hee.game.entity.lookDirVec
import chylex.hee.game.entity.lookPosVec
import chylex.hee.game.entity.selectEntities
import chylex.hee.game.world.Pos
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.getState
import chylex.hee.game.world.setState
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.system.math.square
import chylex.hee.system.math.toRadians
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.random.nextItem
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getEnum
import chylex.hee.system.serialization.getPos
import chylex.hee.system.serialization.heeTag
import chylex.hee.system.serialization.putEnum
import chylex.hee.system.serialization.putPos
import chylex.hee.system.serialization.use
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceContext.BlockMode
import net.minecraft.util.math.RayTraceContext.FluidMode
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.cos

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
			val candidatesInitial = BlockPuzzleLogic.findAllRectangles(world, allBlocks)
				.map { box -> Vec3d((box.min.x + box.max.x + 1) * 0.5, posY + 1.5, (box.min.z + box.max.z + 1) * 0.5) }
			
			val candidatesWithPlayerVisibility = candidatesInitial
				.filter(::isPointInPlayerView)
				.ifEmpty { candidatesInitial }
			
			val candidatesOutsidePickupRange = candidatesInitial
				.filter { center -> world.players.none { it.getDistanceSq(center) < square(5.0) } }
				.ifEmpty { candidatesWithPlayerVisibility }
			
			val pickedCandidate = rand.nextItem(candidatesOutsidePickupRange)
			
			// TODO fx
			
			EntityItem(world, pickedCandidate.x, pickedCandidate.y, pickedCandidate.z, ItemStack(ModItems.PUZZLE_MEDALLION)).apply {
				motion = Vec3d.ZERO
				world.addEntity(this)
			}
			
			allBlocks.forEach { it.setState(world, it.getState(world).with(BlockPuzzleLogic.STATE, BlockPuzzleLogic.State.DISABLED)) }
		}
	}
	
	private fun isPointInPlayerView(point: Vec3d): Boolean{
		return world.players.any {
			val lookPos = it.lookPosVec
			val lookDir = it.lookDirVec
			val pointDir = point.subtract(lookPos).normalize()
			
			lookDir.dotProduct(pointDir) > cos(80.0.toRadians()) &&
			world.rayTraceBlocks(RayTraceContext(lookPos, point, BlockMode.COLLIDER, FluidMode.NONE, this)).type == Type.MISS
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
