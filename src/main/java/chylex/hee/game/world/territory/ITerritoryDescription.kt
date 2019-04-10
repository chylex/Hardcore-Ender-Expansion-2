package chylex.hee.game.world.territory
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment

interface ITerritoryDescription{
	val colors: TerritoryColors
	val environment: TerritoryEnvironment
}
