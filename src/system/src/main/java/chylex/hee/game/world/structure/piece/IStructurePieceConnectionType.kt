package chylex.hee.game.world.structure.piece
import chylex.hee.game.world.structure.IStructureWorld

interface IStructurePieceConnectionType{
	/**
	 * Returns true if the current connection, which is always on a new piece, can be connected to the [target] connection of an already generated piece.
	 * The relation defined by the method does not have to be symmetric, which allows for one-directional connections.
	 */
	fun canBeAttachedTo(target: IStructurePieceConnectionType): Boolean
	
	/**
	 * Generates the connection (usually air blocks) in the world.
	 */
	fun placeConnection(world: IStructureWorld, connection: IStructurePieceConnection)
}
