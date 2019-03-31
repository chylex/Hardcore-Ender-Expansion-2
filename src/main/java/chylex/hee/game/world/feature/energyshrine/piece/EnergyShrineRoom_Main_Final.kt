package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.mechanics.energy.IClusterGenerator.Companion.ENERGY_SHRINE
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineTerminalConnection
import chylex.hee.game.world.structure.IStructureGeneratorFromFile
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.file.StructureFiles
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.EnergyClusterStructureTrigger
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

class EnergyShrineRoom_Main_Final(file: String) : EnergyShrineAbstractPiece(), IStructureGeneratorFromFile{
	override val path = "energyshrine/$file"
	private val generator = StructureFiles.loadWithCache(path).Generator(EnergyShrinePieces.PALETTE.mappingForGeneration)
	
	override val size = generator.size
	
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineTerminalConnection(Pos(2, 0, size.maxZ), SOUTH),
		EnergyShrineTerminalConnection(Pos(size.maxX - 1, 0, size.maxZ), SOUTH),
		EnergyShrineTerminalConnection(Pos(size.maxX, 0, size.maxZ - 2), EAST),
		EnergyShrineTerminalConnection(Pos(0, 0, size.maxZ - 1), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		generator.generate(world)
		
		world.addTrigger(Pos(size.centerX, 2, 3), EnergyClusterStructureTrigger(ENERGY_SHRINE.generate(world.rand)))
	}
}
