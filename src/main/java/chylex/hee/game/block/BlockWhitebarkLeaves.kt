package chylex.hee.game.block

import chylex.hee.client.render.block.IBlockLayerCutout
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.system.facades.Stats
import chylex.hee.system.migration.BlockLeaves
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.block.BlockState
import net.minecraft.block.material.MaterialColor
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class BlockWhitebarkLeaves(builder: BlockBuilder, private val color: MaterialColor) : BlockLeaves(builder.p), IBlockLayerCutout {
	override fun harvestBlock(world: World, player: EntityPlayer, pos: BlockPos, state: BlockState, tile: TileEntity?, stack: ItemStack) {
		if (!world.isRemote && stack.item === Items.SHEARS) {
			player.addStat(Stats.harvestBlock(this))
		}
		else {
			super.harvestBlock(world, player, pos, state, tile, stack)
		}
	}
	
	override fun canEntitySpawn(state: BlockState, world: IBlockReader, pos: BlockPos, entity: EntityType<*>): Boolean {
		return false
	}
	
	override fun getMaterialColor(state: BlockState, world: IBlockReader, pos: BlockPos): MaterialColor {
		return color
	}
}
