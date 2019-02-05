package chylex.hee.game.world.structure.piece
import chylex.hee.game.world.structure.IStructureGenerator
import java.util.Random

interface IStructureBuilder{
	fun build(rand: Random): IStructureGenerator?
}
