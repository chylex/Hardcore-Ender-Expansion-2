package chylex.hee.game.commands.server
import chylex.hee.game.commands.ICommand
import chylex.hee.game.commands.util.ValidatedStringArgument.Companion.validatedString
import chylex.hee.game.commands.util.executes
import chylex.hee.game.commands.util.getLong
import chylex.hee.game.commands.util.getString
import chylex.hee.game.commands.util.returning
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerPieces
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructurePieceFromFile
import chylex.hee.game.world.structure.file.StructureFile
import chylex.hee.game.world.structure.file.StructureFiles
import chylex.hee.game.world.structure.world.TransformedStructureWorld
import chylex.hee.game.world.structure.world.WorldToStructureWorldAdapter
import chylex.hee.game.world.util.PosXZ
import chylex.hee.game.world.util.Transform
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.Pos
import com.mojang.brigadier.arguments.LongArgumentType.longArg
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument
import net.minecraft.command.Commands.literal
import net.minecraft.util.Rotation
import net.minecraft.util.text.StringTextComponent
import java.util.Random

object CommandDebugStructure : ICommand{ // UPDATE
	val structureDescriptions = mapOf(
		"stronghold" to StrongholdPieces,
		"energyshrine" to EnergyShrinePieces,
		"tombdungeon" to TombDungeonPieces,
		"obsidiantower" to ObsidianTowerPieces
	)
	
	override val name = "structure"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>){
		val execPieces = this::executePieces
		val execPiecesDev = this::executePiecesDev
		val execBuild = this::executeBuild
		
		builder.then(
			literal("resetcache").executes(this::executeResetCache)
		)
		
		builder.then(
			argument("structure", validatedString(structureDescriptions.keys)).then(
				literal("pieces").executes(execPieces, Transform.ALL).then(
					literal("notfs").executes(execPieces, listOf(Transform.NONE))
				)
			).then(
				literal("piecesdev").executes(execPiecesDev, false).then(
					argument("transform", validatedString(setOf("0", "90", "180", "270", "M0", "M90", "M180", "M270"))).executes(execPiecesDev, true)
				)
			).then(
				literal("build").executes(execBuild, false).then(
					argument("seed", longArg()).executes(execBuild, true)
				)
			).then(
				literal("locate").executes(this::executeLocate)
			)
		)
	}
	
	private fun executeResetCache(ctx: CommandContext<CommandSource>) = returning(1){
		StructureFiles.resetCache()
		ctx.source.sendFeedback(StringTextComponent("Done."), false)
	}
	
	private fun executePieces(ctx: CommandContext<CommandSource>, transforms: List<Transform>) = returning(1){
		val world = ctx.source.world
		val pos = Pos(ctx.source.pos)
		
		val structure = structureDescriptions.getValue(ctx.getString("structure"))
		var x = 0
		
		for(piece in structure.ALL_PIECES){
			val size = piece.size
			
			for((index, transform) in transforms.withIndex()){
				val adaptedWorld = WorldToStructureWorldAdapter(world, world.rand, pos.add(x, index * (size.y + 2), -size.centerZ))
				val transformedWorld = TransformedStructureWorld(adaptedWorld, size, transform)
				
				transformedWorld.placeCube(size.minPos, size.maxPos, Single(Blocks.BEDROCK))
				transformedWorld.apply { piece.generateWithTransformHint(this, transform) }.finalize()
			}
			
			x += transforms.maxOf { it(size).x } + 2
		}
	}
	
	private fun executePiecesDev(ctx: CommandContext<CommandSource>, hasTransformArg: Boolean) = returning(1){
		val world = ctx.source.world
		val pos = Pos(ctx.source.pos)
		
		val structure = structureDescriptions.getValue(ctx.getString("structure"))
		var x = 0
		
		val transformArg = if (hasTransformArg) ctx.getString("transform") else "0"
		val mirror = transformArg[0] == 'M'
		
		val transform = when(transformArg.trimStart('M')){
			"0" -> Transform(Rotation.NONE, mirror)
			"90" -> Transform(Rotation.CLOCKWISE_90, mirror)
			"180" -> Transform(Rotation.CLOCKWISE_180, mirror)
			"270" -> Transform(Rotation.COUNTERCLOCKWISE_90, mirror)
			else -> return 0
		}
		
		for(piece in structure.ALL_PIECES){
			if (piece is IStructurePieceFromFile){
				val adaptedWorld = WorldToStructureWorldAdapter(world, world.rand, pos.add(x, 0, -piece.size.centerZ))
				val transformedWorld = TransformedStructureWorld(adaptedWorld, piece.size, transform)
				
				StructureFile.spawn(transformedWorld, piece, structure.PALETTE)
				x += transform(piece.size).x + 2
			}
		}
	}
	
	private fun executeBuild(ctx: CommandContext<CommandSource>, hasSeedArg: Boolean): Int{
		with(ctx.source){
			val structure = structureDescriptions.getValue(ctx.getString("structure"))
			
			val rand = Random(if (hasSeedArg) ctx.getLong("seed") else world.rand.nextLong())
			val world = WorldToStructureWorldAdapter(world, rand, Pos(pos).subtract(structure.STRUCTURE_SIZE.centerPos))
			
			for(attempt in 1..100){
				val builder = structure.STRUCTURE_BUILDER.build(rand)
				
				if (builder != null){
					sendFeedback(StringTextComponent("Successful attempt: $attempt"), false)
					world.apply(builder::generate).finalize()
					return 1
				}
			}
			
			sendFeedback(StringTextComponent("Failed all attempts..."), false)
			return 0
		}
	}
	
	private fun executeLocate(ctx: CommandContext<CommandSource>): Int{
		with(ctx.source){
			val closest = structureDescriptions.getValue(ctx.getString("structure")).STRUCTURE_LOCATOR(world, PosXZ(Pos(pos)))
			
			if (closest == null){
				sendFeedback(StringTextComponent("Structure not found."), false)
				return 0
			}
			
			sendFeedback(StringTextComponent("Structure found at $closest."), false)
			return 1
		}
	}
}
