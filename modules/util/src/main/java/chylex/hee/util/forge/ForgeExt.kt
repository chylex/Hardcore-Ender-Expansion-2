package chylex.hee.util.forge

import net.minecraftforge.eventbus.api.Event.Result.ALLOW
import net.minecraftforge.eventbus.api.Event.Result.DEFAULT
import net.minecraftforge.eventbus.api.Event.Result.DENY

val EventResult.asBool: Boolean?
	get() = when (this) {
		ALLOW -> true
		DENY -> false
		DEFAULT -> null
	}

fun EventResult(allow: Boolean): EventResult {
	return if (allow) ALLOW else DENY
}
