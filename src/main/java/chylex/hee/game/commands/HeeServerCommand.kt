package chylex.hee.game.commands
import chylex.hee.game.commands.sub.ISubCommand.Companion.subCommandMapOf
import chylex.hee.game.commands.sub.server.CommandDebugTestWorld
import chylex.hee.game.commands.sub.server.CommandServerCausatum
import chylex.hee.game.commands.sub.server.CommandServerHelp
import chylex.hee.game.commands.sub.server.CommandServerInfusions
import chylex.hee.system.Debug

internal object HeeServerCommand : HeeBaseCommand(){
	val availableAdminCommands = arrayOf(
		CommandServerHelp,
		CommandServerInfusions,
		CommandServerCausatum
	)
	
	val availableDebugCommands = if (Debug.enabled) arrayOf(
		CommandDebugTestWorld
	) else emptyArray()
	
	public override val allSubCommands = subCommandMapOf(
		*availableAdminCommands,
		*availableDebugCommands
	)
	
	override val defaultSubCommand = CommandServerHelp
	
	override fun getRequiredPermissionLevel(): Int = 2
}
