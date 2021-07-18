package chylex.hee.game.command.server

import chylex.hee.game.Environment
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.command.ICommand
import chylex.hee.game.command.exception
import chylex.hee.game.command.message
import chylex.hee.game.command.util.getPos
import chylex.hee.game.command.util.simpleCommand
import chylex.hee.game.world.util.getTile
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument
import net.minecraft.command.Commands.literal
import net.minecraft.command.arguments.BlockPosArgument.blockPos
import net.minecraft.command.arguments.ResourceLocationArgument.getResourceLocation
import net.minecraft.command.arguments.ResourceLocationArgument.resourceLocation
import net.minecraft.loot.LootTable

object CommandServerLootChest : ICommand {
	override val name = "lootchest"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>) {
		builder.then(
			argument("pos", blockPos()).then(
				literal("table").then(
					literal("set").then(
						argument("loot_table", resourceLocation()).executes(this::executeSetTable)
					)
				).then(
					literal("remove").executes(this::executeResetTable)
				)
			).then(
				literal("resetplayers").executes(this::executeResetPlayers)
			)
		)
	}
	
	private val TABLE_NOT_FOUND = exception("table_not_found")
	private val NOT_LOOT_CHEST = exception("not_loot_chest")
	
	private fun getLootChest(ctx: CommandContext<CommandSource>): TileEntityLootChest {
		return ctx.getPos("pos").getTile(ctx.source.world) ?: throw NOT_LOOT_CHEST.create()
	}
	
	private fun executeSetTable(ctx: CommandContext<CommandSource>) = simpleCommand {
		val tile = getLootChest(ctx)
		val lootTable = getResourceLocation(ctx, "loot_table")
		
		if (Environment.getLootTable(lootTable) === LootTable.EMPTY_LOOT_TABLE) {
			throw TABLE_NOT_FOUND.create()
		}
		
		tile.setLootTable(lootTable)
		ctx.source.sendFeedback(message("set_table_success"), true)
	}
	
	private fun executeResetTable(ctx: CommandContext<CommandSource>) = simpleCommand {
		getLootChest(ctx).setLootTable(null)
		ctx.source.sendFeedback(message("remove_table_success"), true)
	}
	
	private fun executeResetPlayers(ctx: CommandContext<CommandSource>): Int {
		val total = getLootChest(ctx).resetPlayerInventories()
		
		ctx.source.sendFeedback(message("reset_success", total), true)
		return total
	}
}
