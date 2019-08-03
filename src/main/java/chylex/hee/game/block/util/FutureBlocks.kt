package chylex.hee.game.block.util
import chylex.hee.system.util.with
import chylex.hee.system.util.withFacing
import net.minecraft.block.BlockFlower
import net.minecraft.block.BlockFlower.EnumFlowerType
import net.minecraft.block.BlockNewLog
import net.minecraft.block.BlockOldLog
import net.minecraft.block.BlockPlanks
import net.minecraft.block.BlockSilverfish
import net.minecraft.block.BlockStoneBrick
import net.minecraft.block.BlockStoneSlab
import net.minecraft.block.BlockTallGrass
import net.minecraft.block.BlockWoodSlab
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
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
	
	val POPPY_STACK        get() = ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.POPPY.meta)
	val BLUE_ORCHID_STACK  get() = ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.BLUE_ORCHID.meta)
	val ALLIUM_STACK       get() = ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.ALLIUM.meta)
	val AZURE_BLUET_STACK  get() = ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.HOUSTONIA.meta)
	val RED_TULIP_STACK    get() = ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.RED_TULIP.meta)
	val ORANGE_TULIP_STACK get() = ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.ORANGE_TULIP.meta)
	val WHITE_TULIP_STACK  get() = ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.WHITE_TULIP.meta)
	val PINK_TULIP_STACK   get() = ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.PINK_TULIP.meta)
	val OXEYE_DAISY_STACK  get() = ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.OXEYE_DAISY.meta)
	val DANDELION_STACK    get() = ItemStack(Blocks.YELLOW_FLOWER, 1, BlockFlower.EnumFlowerType.DANDELION.meta)
	val FERN_STACK         get() = ItemStack(Blocks.TALLGRASS, 1, BlockTallGrass.EnumType.FERN.meta)
}
