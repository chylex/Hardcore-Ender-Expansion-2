package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import chylex.hee.system.util.getTile
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class BlockLootChest(builder: BlockBuilder) : BlockAbstractChest<TileEntityLootChest>(builder){
	override fun createTileEntity() = TileEntityLootChest()
	
	override fun openChest(world: World, pos: BlockPos, player: EntityPlayer){
		if (player.isCreative && pos.getTile<TileEntityLootChest>(world)?.hasLootTable == true){
			player.sendMessage(TextComponentTranslation("block.hee.loot_chest.error_has_loot_table"))
		}
		else{
			super.openChest(world, pos, player)
		}
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: IBlockReader?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		lines.add(TextComponentTranslation("block.hee.loot_chest.tooltip"))
	}
}
