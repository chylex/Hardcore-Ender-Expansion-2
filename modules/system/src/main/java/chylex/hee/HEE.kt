package chylex.hee

import chylex.hee.game.Resource
import net.minecraft.util.RegistryKey
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object HEE {
	const val ID = "hee"
	
	lateinit var version: String
	
	val log: Logger = LogManager.getLogger("HardcoreEnderExpansion")
	val dim: RegistryKey<World> = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, Resource.Custom("end"))
}
