package chylex.hee.game.commands.sub.server
import chylex.hee.game.commands.sub.ISubCommand
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureDescription
import chylex.hee.game.world.structure.IStructureGeneratorFromFile
import chylex.hee.game.world.structure.file.StructureFile
import chylex.hee.game.world.structure.file.StructureFiles
import chylex.hee.game.world.structure.world.RotatedStructureWorld
import chylex.hee.game.world.structure.world.WorldToStructureWorldAdapter
import chylex.hee.game.world.util.PosXZ
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.Rotation4
import net.minecraft.command.ICommandSender
import net.minecraft.init.Blocks
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentString

internal object CommandDebugStructure : ISubCommand{
	private val structureDescriptions = mapOf<String, IStructureDescription>(
		"stronghold" to StrongholdPieces
	)
	
	override val name = "structure"
	override val usage = "commands.hee.structure.usage"
	override val info = "commands.hee.structure.info"
	
	override fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		val arg = args.getOrNull(0) ?: return
		
		if (arg == "resetcache"){
			StructureFiles.resetCache()
			sender.sendMessage(TextComponentString("reset"))
			return
		}
		val structure = args.getOrNull(1)?.let { structureDescriptions[it] } ?: return
		
		when(arg){
			"pieces" -> {
				var x = 0
				
				val rotations = when(args.getOrNull(2)){
					"norots" -> listOf(Rotation.NONE)
					null -> Rotation4
					else -> return
				}
				
				for(piece in structure.ALL_PIECES){
					val size = piece.size
					
					for((index, rotation) in rotations.withIndex()){
						val world = WorldToStructureWorldAdapter(sender.entityWorld, sender.entityWorld.rand, sender.position.add(x, index * (size.y + 2), -size.centerZ))
						val rotatedWorld = RotatedStructureWorld(world, size, rotation)
						
						rotatedWorld.placeCube(size.minPos, size.maxPos, Single(Blocks.BEDROCK))
						rotatedWorld.apply(piece::generate).finalize()
					}
					
					x += rotations.map(size::rotate).map(Size::x).max()!! + 2
				}
			}
			
			"piecesdev" -> {
				var x = 0
				
				for(piece in structure.ALL_PIECES){
					if (piece is IStructureGeneratorFromFile){
						StructureFile.spawn(sender.entityWorld, sender.position.add(x, 0, -piece.size.centerZ), piece, structure.PALETTE)
						x += piece.size.x + 2
					}
				}
			}
			
			"build" -> {
				val rand = sender.entityWorld.rand
				val world = WorldToStructureWorldAdapter(sender.entityWorld, rand, sender.position.subtract(structure.STRUCTURE_SIZE.centerPos))
				
				for(attempt in 1..100){
					val builder = structure.STRUCTURE_BUILDER.build(rand)
					
					if (builder != null){
						sender.sendMessage(TextComponentString("Successful attempt: $attempt"))
						world.apply(builder::generate).finalize()
						return
					}
				}
				
				sender.sendMessage(TextComponentString("Failed all attempts..."))
			}
			
			"locate" -> {
				val closest = structure.STRUCTURE_LOCATOR(sender.entityWorld, PosXZ(sender.position))
				sender.sendMessage(TextComponentString("Found at: $closest"))
			}
		}
	}
}
