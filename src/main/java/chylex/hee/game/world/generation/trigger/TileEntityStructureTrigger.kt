package chylex.hee.game.world.generation.trigger

import chylex.hee.game.world.generation.structure.IStructureTrigger
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import chylex.hee.game.world.util.getBlock
import chylex.hee.util.nbt.TagCompound
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IServerWorld
import net.minecraft.world.IWorld
import net.minecraft.world.World

class TileEntityStructureTrigger(private val state: BlockState, private val nbt: TagCompound) : IStructureTrigger {
	constructor(state: BlockState, tile: TileEntity) : this(state, tile.serializeNBT())
	constructor(block: Block, nbt: TagCompound) : this(block.defaultState, nbt)
	constructor(block: Block, tile: TileEntity) : this(block, tile.serializeNBT())
	
	companion object {
		fun addTileSafe(world: IWorld, pos: BlockPos, tile: TileEntity) {
			world.getChunk(pos).addTileEntity(pos, tile)
			
			if (world is World) {
				world.addTileEntity(tile)
			}
		}
	}
	
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform) {
		world.setState(pos, transform(state))
	}
	
	override fun realize(world: IServerWorld, pos: BlockPos, transform: Transform) {
		if (pos.getBlock(world) !== state.block) {
			return
		}
		
		addTileSafe(world, pos, state.createTileEntity(world)!!.also {
			it.read(state, nbt)
			it.pos = pos
			transform(it)
		})
	}
}
