package chylex.hee.game.block.util
import chylex.hee.system.util.with
import chylex.hee.system.util.withFacing
import net.minecraft.block.BlockNewLog
import net.minecraft.block.BlockOldLog
import net.minecraft.block.BlockPlanks
import net.minecraft.block.BlockSilverfish
import net.minecraft.block.BlockStoneBrick
import net.minecraft.block.BlockStoneSlab
import net.minecraft.block.BlockWoodSlab
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing

object FutureBlocks{
	val STONE_SLAB        = Blocks.STONE_SLAB.with(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.STONE)
	val STONE_BRICK_SLAB  = Blocks.STONE_SLAB.with(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK)
	val DOUBLE_STONE_SLAB = Blocks.DOUBLE_STONE_SLAB.with(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.STONE)
	val SPRUCE_SLAB       = Blocks.WOODEN_SLAB.with(BlockWoodSlab.VARIANT, BlockPlanks.EnumType.SPRUCE)
	
	val SPRUCE_LOG   = Blocks.LOG.with(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE)
	val DARK_OAK_LOG = Blocks.LOG2.with(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK)
	
	val OAK_PLANKS    = Blocks.PLANKS.with(BlockPlanks.VARIANT, BlockPlanks.EnumType.OAK)
	val SPRUCE_PLANKS = Blocks.PLANKS.with(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE)
	val DARK_OAK_PLANKS = Blocks.PLANKS.with(BlockPlanks.VARIANT, BlockPlanks.EnumType.DARK_OAK)
	
	val STONE_BRICKS          = Blocks.STONEBRICK.with(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.DEFAULT)
	val MOSSY_STONE_BRICKS    = Blocks.STONEBRICK.with(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY)
	val CRACKED_STONE_BRICKS  = Blocks.STONEBRICK.with(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED)
	val CHISELED_STONE_BRICKS = Blocks.STONEBRICK.with(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CHISELED)
	
	val INFESTED_STONE_BRICKS          = Blocks.MONSTER_EGG.with(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONEBRICK)
	val INFESTED_MOSSY_STONE_BRICKS    = Blocks.MONSTER_EGG.with(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.MOSSY_STONEBRICK)
	val INFESTED_CRACKED_STONE_BRICKS  = Blocks.MONSTER_EGG.with(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.CRACKED_STONEBRICK)
	
	val SKULL_FLOOR = Blocks.SKULL.withFacing(EnumFacing.UP)
}
