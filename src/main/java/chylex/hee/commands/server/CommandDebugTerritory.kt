package chylex.hee.commands.server
import chylex.hee.HEE
import chylex.hee.commands.ICommand
import chylex.hee.commands.executes
import chylex.hee.commands.getLong
import chylex.hee.game.world.FLAG_REPLACE_NO_DROPS
import chylex.hee.game.world.FLAG_SYNC_CLIENT
import chylex.hee.game.world.Pos
import chylex.hee.game.world.component1
import chylex.hee.game.world.component2
import chylex.hee.game.world.getState
import chylex.hee.game.world.math.Transform
import chylex.hee.game.world.setState
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.system.migration.EntityPlayer
import com.mojang.brigadier.arguments.LongArgumentType.longArg
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument
import net.minecraft.command.Commands.literal
import net.minecraft.util.text.StringTextComponent
import java.util.Random

object CommandDebugTerritory : ICommand{
	override val name = "territory"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>){
		val execRegenerate = this::executeRegenerate
		
		builder.then(
			literal("regenerate").executes(execRegenerate, false).then(
				argument("seed", longArg()).executes(execRegenerate, true)
			)
		)
	}
	
	private fun executeRegenerate(ctx: CommandContext<CommandSource>, hasSeedArg: Boolean): Int{
		with(ctx.source){
			val world = ctx.source.world
			val pos = Pos(ctx.source.pos)
			val instance = TerritoryInstance.fromPos(pos)
			val seed = if (hasSeedArg) ctx.getLong("seed") else null
			
			if (world.dimensionKey !== HEE.dim){
				sendFeedback(StringTextComponent("Invalid dimension."), false)
				return 0
			}
			
			if (instance == null){
				sendFeedback(StringTextComponent("Invalid territory position."), false)
				return 0
			}
			
			sendFeedback(StringTextComponent("Regenerating..."), false)
			
			val territory = instance.territory
			val rand = seed?.let(::Random) ?: instance.createRandom(world.seed)
			
			val timeStart = System.currentTimeMillis()
			val (constructed, _) = territory.generate(rand)
			val timeEnd = System.currentTimeMillis()
			
			sendFeedback(StringTextComponent("Generated in ${timeEnd - timeStart} ms."), false)
			
			val chunks = territory.chunks
			val height = constructed.worldSize.y
			val bottomOffset = territory.height.first
			val (startChunkX, startChunkZ) = instance.topLeftChunk
			val startChunkBlockX = startChunkX * 16
			val startChunkBlockZ = startChunkZ * 16
			
			for(chunkX in startChunkX until (startChunkX + chunks))
			for(chunkZ in startChunkZ until (startChunkZ + chunks)){
				val chunk = world.getChunk(chunkX, chunkZ)
				
				for(entity in chunk.entityLists.flatMap { it }.filter { it !is EntityPlayer }){
					entity.remove()
				}
				
				for(tilePos in chunk.tileEntitiesPos){
					world.removeTileEntity(tilePos)
				}
				
				val chunkBlockX = chunkX * 16
				val chunkBlockZ = chunkZ * 16
				val internalOffset = Pos(chunkBlockX - startChunkBlockX, 0, chunkBlockZ - startChunkBlockZ)
				
				for(blockY in 0 until height) for(blockX in 0..15) for(blockZ in 0..15){
					val state = constructed.getState(internalOffset.add(blockX, blockY, blockZ))
					
					Pos(chunkBlockX + blockX, bottomOffset + blockY, chunkBlockZ + blockZ).let {
						if (it.getState(world) != state){
							it.setState(world, state, FLAG_SYNC_CLIENT or FLAG_REPLACE_NO_DROPS)
						}
					}
				}
			}
			
			for((triggerPos, trigger) in constructed.getTriggers()){
				trigger.realize(world, triggerPos.add(startChunkBlockX, bottomOffset, startChunkBlockZ), Transform.NONE)
			}
			
			sendFeedback(StringTextComponent("Done."), false)
			return 1
		}
	}
}
