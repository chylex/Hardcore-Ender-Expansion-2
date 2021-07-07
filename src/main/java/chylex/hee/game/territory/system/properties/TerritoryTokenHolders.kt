package chylex.hee.game.territory.system.properties

import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.territory.system.TerritoryInstance

abstract class TerritoryTokenHolders {
	object Default : TerritoryTokenHolders()
	
	open fun onTick(holder: EntityTokenHolder, instance: TerritoryInstance) {}
	
	open fun afterUse(holder: EntityTokenHolder, instance: TerritoryInstance) {
		holder.remove()
	}
}
