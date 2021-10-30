package chylex.hee.game.item

import chylex.hee.game.item.interfaces.IItemWithInterfaces
import net.minecraft.item.Item

abstract class HeeItem(properties: Properties) : Item(properties), IHeeItem, IItemWithInterfaces
