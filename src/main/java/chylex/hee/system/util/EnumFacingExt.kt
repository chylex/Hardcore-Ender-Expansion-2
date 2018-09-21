package chylex.hee.system.util
import com.google.common.collect.Collections2
import net.minecraft.util.EnumFacing
import java.util.Random

object Facing4 : Iterable<EnumFacing>{
	private val allFacings = EnumFacing.HORIZONTALS
	private val allPermutations = Collections2.permutations(allFacings.toList()).toTypedArray()
	
	override fun iterator(): Iterator<EnumFacing> = allFacings.iterator()
	
	@JvmStatic fun randomOne(rand: Random) = allFacings[rand.nextInt(allFacings.size)]
	@JvmStatic fun randomSeq(rand: Random) = allPermutations[rand.nextInt(allPermutations.size)]
}

object Facing6 : Iterable<EnumFacing>{
	private val allFacings = EnumFacing.VALUES
	
	override fun iterator(): Iterator<EnumFacing> = allFacings.iterator()
	
	@JvmStatic fun randomOne(rand: Random) = allFacings[rand.nextInt(allFacings.size)]
}
