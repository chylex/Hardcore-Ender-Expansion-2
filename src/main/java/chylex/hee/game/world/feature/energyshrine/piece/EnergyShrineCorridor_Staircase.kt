package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.structure.IStructureGeneratorFromFile
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.file.StructureFiles
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Facing6
import chylex.hee.system.util.Pos
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItemOrNull
import chylex.hee.system.util.withFacing
import net.minecraft.util.math.BlockPos
import java.util.Random

abstract class EnergyShrineCorridor_Staircase(file: String) : EnergyShrineAbstractPiece(), IStructureGeneratorFromFile{
	final override val path = "energyshrine/$file"
	private val generator = StructureFiles.loadWithCache(path).Generator(EnergyShrinePieces.PALETTE.mappingForGeneration)
	
	final override val size = generator.size
	
	override fun generate(world: IStructureWorld, instance: Instance){
		generator.generate(world)
		
		val rand = world.rand
		val maxY = size.maxY
		
		val count = (size.maxX * 0.6F).ceilToInt()
		val attempts = 25
		
		for(index in 0..count){
			val progress = index.toFloat() / count
			
			for(attempt in 1..attempts){
				val (torchX, torchZ) = nextRandomXZ(rand, progress)
				
				if (world.getBlock(Pos(torchX, maxY, torchZ)) !== ModBlocks.GLOOMROCK_SMOOTH){
					continue
				}
				
				val yOffset = ((attempt.toFloat() / attempts) * (maxY - 6)).ceilToInt()
				val torchPos = BlockPos(torchX, yOffset + rand.nextInt(2, 5), torchZ)
				
				if ((0..3).all { world.isAir(torchPos.down(it)) }){
					val attachmentCandidates = Facing6.filter { world.getState(torchPos.offset(it)).isFullBlock }
					val attachTo = rand.nextItemOrNull(attachmentCandidates)
					
					if (attachTo != null){
						world.setState(torchPos, ModBlocks.GLOOMTORCH.withFacing(attachTo.opposite))
						break
					}
				}
			}
		}
	}
	
	protected abstract fun nextRandomXZ(rand: Random, progress: Float): Pair<Int, Int>
}
