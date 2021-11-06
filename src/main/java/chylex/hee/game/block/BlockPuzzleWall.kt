package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.properties.Materials
import net.minecraft.block.SoundType
import net.minecraft.block.material.MaterialColor

object BlockPuzzleWall : HeeBlockBuilder() {
	init {
		includeFrom(BlockIndestructible)
		
		localization = LocalizationStrategy.Parenthesized(wordCount = 1, fromStart = false)
		
		material = Materials.SOLID
		color = MaterialColor.ADOBE // RENAME ORANGE
		sound = SoundType.STONE
		light = 14
	}
}
