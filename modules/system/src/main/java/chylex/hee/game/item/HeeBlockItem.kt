package chylex.hee.game.item

import chylex.hee.game.item.interfaces.IItemWithInterfaces
import net.minecraft.block.Block
import net.minecraft.item.BlockItem

abstract class HeeBlockItem(block: Block, properties: Properties) : BlockItem(block, properties), IHeeItem, IItemWithInterfaces
