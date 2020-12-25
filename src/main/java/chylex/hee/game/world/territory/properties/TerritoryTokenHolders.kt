package chylex.hee.game.world.territory.properties

import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.world.territory.TerritoryInstance

abstract class TerritoryTokenHolders {
	object Default : TerritoryTokenHolders()
	
	open fun onTick(holder: EntityTokenHolder, instance: TerritoryInstance) {}
	
	open fun afterUse(holder: EntityTokenHolder, instance: TerritoryInstance) {
		holder.remove()
	}
}
