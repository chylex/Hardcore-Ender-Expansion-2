package chylex.hee
import chylex.hee.proxy.ISidedProxy
import net.minecraft.world.dimension.DimensionType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object HEE{
	const val ID = "hee"
	
	lateinit var version: String
	lateinit var proxy: ISidedProxy
	
	val log: Logger = LogManager.getLogger("HardcoreEnderExpansion")
	val dim: DimensionType = DimensionType.THE_END
}
