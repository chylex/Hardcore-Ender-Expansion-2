package chylex.hee.game.world.territory
import chylex.hee.game.item.ItemPortalToken.TokenType.RARE
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.game.world.territory.properties.TerritoryTokenHolders
import chylex.hee.game.world.territory.storage.TerritoryEntry
import chylex.hee.game.world.territory.storage.TerritoryStorageComponent
import chylex.hee.game.world.territory.tickers.VoidTicker

interface ITerritoryDescription{
	val colors: TerritoryColors
	val environment: TerritoryEnvironment
	
	@JvmDefault
	val tokenHolders: TerritoryTokenHolders
		get() = TerritoryTokenHolders.Default
	
	@JvmDefault
	fun initialize(instance: TerritoryInstance, entry: TerritoryEntry, tickers: MutableList<ITerritoryTicker>){
		if (entry.type == RARE){
			tickers.add(VoidTicker(entry.registerComponent(TerritoryStorageComponent.VOID_DATA)))
		}
	}
}
