package chylex.hee.game.world.territory
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.game.world.territory.properties.TerritoryTokenHolders

interface ITerritoryDescription{
	val colors: TerritoryColors
	val environment: TerritoryEnvironment
	
	@JvmDefault
	val tokenHolders: TerritoryTokenHolders
		get() = TerritoryTokenHolders.Default
}
