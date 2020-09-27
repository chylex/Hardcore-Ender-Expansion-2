package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.world.getTile
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class BlockLootChest(builder: BlockBuilder) : BlockAbstractChest<TileEntityLootChest>(builder){
	override fun createTileEntity() = TileEntityLootChest()
	
	override fun openChest(world: World, pos: BlockPos, player: EntityPlayer){
		if (player.isCreative && pos.getTile<TileEntityLootChest>(world)?.hasLootTable == true){
			player.sendMessage(TranslationTextComponent("block.hee.loot_chest.error_has_loot_table"))
		}
		else{
			super.openChest(world, pos, player)
		}
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: IBlockReader?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		lines.add(TranslationTextComponent("block.hee.loot_chest.tooltip"))
	}
}
