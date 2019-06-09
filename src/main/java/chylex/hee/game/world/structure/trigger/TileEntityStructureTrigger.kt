package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.util.Transform
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setState
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TileEntityStructureTrigger(private val state: IBlockState, private val nbt: NBTTagCompound) : IStructureTrigger{
	constructor(state: IBlockState, tile: TileEntity) : this(state, tile.serializeNBT())
	constructor(block: Block, nbt: NBTTagCompound) : this(block.defaultState, nbt)
	constructor(block: Block, tile: TileEntity) : this(block, tile.serializeNBT())
	
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		pos.setState(world, transform(state))
		
		pos.getTile<TileEntity>(world)?.let {
			it.readFromNBT(nbt)
			it.pos = pos
			transform(it)
		}
	}
}
