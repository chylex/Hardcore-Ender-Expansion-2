package chylex.hee.game.commands.sub.server

/*
internal object CommandDebugStructure : ISubCommand{
	val structureDescriptions = mapOf(
		"stronghold" to StrongholdPieces,
		"energyshrine" to EnergyShrinePieces,
		"tombdungeon" to TombDungeonPieces,
		"obsidiantower" to ObsidianTowerPieces
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
				
				val transforms = when(args.getOrNull(2)){
					"notfs" -> listOf(Transform.NONE)
					null -> Transform.ALL
					else -> return
				}
				
				for(piece in structure.ALL_PIECES){
					val size = piece.size
					
					for((index, transform) in transforms.withIndex()){
						val world = WorldToStructureWorldAdapter(sender.entityWorld, sender.entityWorld.rand, sender.position.add(x, index * (size.y + 2), -size.centerZ))
						val transformedWorld = TransformedStructureWorld(world, size, transform)
						
						transformedWorld.placeCube(size.minPos, size.maxPos, Single(Blocks.BEDROCK))
						transformedWorld.apply(piece::generate).finalize()
					}
					
					x += transforms.map { it(size).x }.max()!! + 2
				}
			}
			
			"piecesdev" -> {
				var x = 0
				
				val transformArg = args.getOrElse(2){ "0" }
				val mirror = transformArg[0] == 'M'
				
				val transform = when(transformArg.trimStart('M')){
					"0" -> Transform(Rotation.NONE, mirror)
					"90" -> Transform(Rotation.CLOCKWISE_90, mirror)
					"180" -> Transform(Rotation.CLOCKWISE_180, mirror)
					"270" -> Transform(Rotation.COUNTERCLOCKWISE_90, mirror)
					else -> return
				}
				
				for(piece in structure.ALL_PIECES){
					if (piece is IStructurePieceFromFile){
						val world = WorldToStructureWorldAdapter(sender.entityWorld, sender.entityWorld.rand, sender.position.add(x, 0, -piece.size.centerZ))
						val transformedWorld = TransformedStructureWorld(world, piece.size, transform)
						
						StructureFile.spawn(transformedWorld, piece, structure.PALETTE)
						x += transform(piece.size).x + 2
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
}*/
