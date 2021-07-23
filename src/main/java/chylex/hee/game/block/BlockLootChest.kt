package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.game.world.util.getTile
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class BlockLootChest(builder: BlockBuilder) : BlockAbstractChest<TileEntityLootChest>(builder) {
	override val model
		get() = BlockStateModels.Chest(this.location("_particle"))
	
	override val drop
		get() = BlockDrop.Nothing
	
	override fun createTileEntity() = TileEntityLootChest()
	
	override fun openChest(world: World, pos: BlockPos, player: PlayerEntity) {
		if (player.isCreative && pos.getTile<TileEntityLootChest>(world)?.hasLootTable == true) {
			player.sendMessage(TranslationTextComponent("block.hee.loot_chest.error_has_loot_table"), Util.DUMMY_UUID)
		}
		else {
			super.openChest(world, pos, player)
		}
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: IBlockReader?, lines: MutableList<ITextComponent>, flags: ITooltipFlag) {
		lines.add(TranslationTextComponent("block.hee.loot_chest.tooltip"))
	}
}
