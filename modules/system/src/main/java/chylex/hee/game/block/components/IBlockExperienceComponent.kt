package chylex.hee.game.block.components

import java.util.Random

fun interface IBlockExperienceComponent {
	fun getExperience(rand: Random): Int
}
