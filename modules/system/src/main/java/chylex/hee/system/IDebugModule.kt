package chylex.hee.system

import chylex.hee.game.block.HeeBlock
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.command.IClientCommand
import chylex.hee.game.command.ICommand

interface IDebugModule {
	val clientCommands: List<IClientCommand>
	val serverCommands: List<ICommand>
	
	fun createScaffoldingBlock(builder: BlockBuilder): HeeBlock
}
