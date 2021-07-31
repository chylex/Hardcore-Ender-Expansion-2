package chylex.hee.game.potion

import chylex.hee.client.text.LocalizationStrategy

interface IHeeEffect {
	val localization: LocalizationStrategy
		get() = LocalizationStrategy.Default
}
