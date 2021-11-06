package chylex.hee.system

import chylex.hee.game.block.components.IPlayerUseBlockComponent
import chylex.hee.game.command.IClientCommand
import chylex.hee.game.command.ICommand

interface IDebugModule {
	val clientCommands: List<IClientCommand>
	val serverCommands: List<ICommand>
	val scaffoldingBlockBehavior: IPlayerUseBlockComponent?
}
