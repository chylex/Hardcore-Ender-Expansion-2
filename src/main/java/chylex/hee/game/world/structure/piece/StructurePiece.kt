package chylex.hee.game.world.structure.piece
import chylex.hee.game.block.util.ColoredBlocks
import chylex.hee.game.world.structure.IStructureGenerator
import chylex.hee.game.world.structure.IStructurePiece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.world.TransformedStructureWorld
import chylex.hee.game.world.util.Size
import chylex.hee.game.world.util.Transform
import net.minecraft.item.DyeColor

abstract class StructurePiece<T> : IStructurePiece, IStructureGenerator{
	protected abstract val connections: Array<IStructurePieceConnection>
	
	final override fun generate(world: IStructureWorld){
		generate(world, MutableInstance(null, Transform.NONE).apply { connections.forEach { useConnection(it, MutableInstance(null, Transform.NONE)) } })
		
		for(connection in connections){
			world.setBlock(connection.offset, ColoredBlocks.WOOL.getValue(DyeColor.values()[connection.facing.index - 2]))
		}
	}
	
	protected abstract fun generate(world: IStructureWorld, instance: Instance)
	
	protected fun placeConnections(world: IStructureWorld, instance: Instance){
		for(connection in connections){
			if (instance.isConnectionUsed(connection)){
				connection.type.placeConnection(world, connection)
			}
		}
	}
	
	// Transformed connection
	
	private class TransformedStructurePieceConnection(private val original: IStructurePieceConnection, size: Size, transform: Transform) : IStructurePieceConnection{
		override val type = original.type
		
		override val offset = transform(original.offset, size)
		override val facing = transform(original.facing)
		override val alignment = if (transform.mirror) original.alignment.mirrored else original.alignment
		
		fun matches(other: IStructurePieceConnection): Boolean{
			return this === other || original === other
		}
		
		override fun toString(): String{
			return original.toString()
		}
	}
	
	// Frozen instance
	
	open inner class Instance private constructor(val context: T?, private val transform: Transform, private val availableConnections: List<TransformedStructurePieceConnection>) : IStructureGenerator{
		protected constructor(context: T?, transform: Transform) : this(context, transform, this@StructurePiece.connections.map { TransformedStructurePieceConnection(it, this@StructurePiece.size, transform) }.toMutableList())
		
		final override val size = transform(this@StructurePiece.size)
		
		val hasAvailableConnections
			get() = availableConnections.isNotEmpty()
		
		fun findAvailableConnections(): List<IStructurePieceConnection>{
			return availableConnections
		}
		
		fun findAvailableConnections(targetConnection: IStructurePieceConnection): List<IStructurePieceConnection>{
			val targetFacing = targetConnection.facing.opposite
			return availableConnections.filter { it.facing == targetFacing && it.type.canBeAttachedTo(targetConnection.type) }
		}
		
		fun isConnectionUsed(connection: IStructurePieceConnection): Boolean{
			return availableConnections.none { it.matches(connection) }
		}
		
		fun freeze(): Instance{
			return Instance(context, transform, availableConnections.toList())
		}
		
		final override fun generate(world: IStructureWorld){
			this@StructurePiece.generate(TransformedStructureWorld(world, this@StructurePiece.size, transform), this)
		}
	}
	
	// Mutable instance
	
	@Suppress("RemoveRedundantQualifierName")
	open inner class MutableInstance(context: T?, transform: Transform) : Instance(context, transform){
		constructor(transform: Transform) : this(null, transform)
		
		@Suppress("UNCHECKED_CAST")
		private val availableConnections = findAvailableConnections() as MutableList<TransformedStructurePieceConnection> // ugly implementation detail
		private val usedConnections = mutableMapOf<TransformedStructurePieceConnection, StructurePiece<*>.MutableInstance>()
		
		fun useConnection(connection: IStructurePieceConnection, neighbor: StructurePiece<*>.MutableInstance){
			val index = availableConnections.indexOfFirst { it.matches(connection) }
			
			if (index != -1){
				availableConnections.removeAt(index).let { usedConnections[it] = neighbor }
			}
		}
		
		fun restoreConnection(neighbor: StructurePiece<*>.MutableInstance): Boolean{
			val used = usedConnections.filterValues { it === neighbor }.keys
			
			if (used.isEmpty()){
				return false
			}
			
			for(connection in used){
				availableConnections.add(connection)
				usedConnections.remove(connection)
			}
			
			return true
		}
		
		fun restoreAllConnections(): List<StructurePiece<*>.MutableInstance>{
			return usedConnections.values.toList().also {
				availableConnections.addAll(usedConnections.keys)
				usedConnections.clear()
			}
		}
	}
}
