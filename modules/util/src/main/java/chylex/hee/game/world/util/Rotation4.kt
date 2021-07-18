package chylex.hee.game.world.util

import chylex.hee.system.random.nextItem
import com.google.common.collect.Collections2
import net.minecraft.util.Rotation
import java.util.Random

object Rotation4 : List<Rotation> by Rotation.values().toList() {
	private val allPermutations: Array<List<Rotation>> = Collections2.permutations(this).toTypedArray()
	fun randomPermutation(rand: Random) = rand.nextItem(allPermutations)
}
