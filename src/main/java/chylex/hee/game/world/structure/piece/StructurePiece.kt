package chylex.hee.game.world.structure.piece
import chylex.hee.game.world.structure.IStructureGenerator
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.world.RotatedStructureWorld
import net.minecraft.block.BlockColored
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.EnumFacing
import net.minecraft.util.Rotation

abstract class StructurePiece : IStructureGenerator{
	protected abstract val connections: Array<IStructurePieceConnection>
	
	final override fun generate(world: IStructureWorld){
		generate(world, Instance(Rotation.NONE).apply { connections.forEach(::useConnection) })
		
		for(connection in connections){
			world.setState(connection.offset, Blocks.WOOL.defaultState.withProperty(BlockColored.COLOR, EnumDyeColor.values()[connection.facing.index - 2]))
		}
	}
	
	protected abstract fun generate(world: IStructureWorld, instance: Instance)
	
	inner class Instance(private val rotation: Rotation) : IStructureGenerator{
		override val size = this@StructurePiece.size.rotate(rotation)
		
		private inner class RotatedStructurePieceConnection(val original: IStructurePieceConnection) : IStructurePieceConnection{
			override val offset = RotatedStructureWorld.rotatePos(original.offset, this@StructurePiece.size, rotation)
			override val facing = rotation.rotate(original.facing)!!
			
			override fun canConnectWith(other: IStructurePieceConnection): Boolean{
				return original.canConnectWith((other as? RotatedStructurePieceConnection)?.original ?: other)
			}
			
			fun matches(other: IStructurePieceConnection): Boolean{
				return this === other || original === other
			}
		}
		
		private val availableConnections = this@StructurePiece.connections.map(::RotatedStructurePieceConnection).toMutableList()
		
		fun findValidConnections(facing: EnumFacing): List<IStructurePieceConnection>{
			return availableConnections.filter { it.facing == facing }
		}
		
		fun findValidConnections(facing: EnumFacing, with: IStructurePieceConnection): List<IStructurePieceConnection>{
			return availableConnections.filter { it.facing == facing && it.canConnectWith(with) }
		}
		
		fun isConnectionUsed(connection: IStructurePieceConnection): Boolean{
			return availableConnections.none { it.matches(connection) }
		}
		
		fun useConnection(connection: IStructurePieceConnection){
			availableConnections.removeIf { it.matches(connection) }
		}
		
		override fun generate(world: IStructureWorld){
			this@StructurePiece.generate(RotatedStructureWorld(world, this@StructurePiece.size, rotation), this)
		}
	}
}
